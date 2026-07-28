# RODA MCP Server — Design & Implementation Plan

## Context

RODA is used to answer questions that go beyond simple keyword lookup — e.g. *"Search for
the MoM of the meeting with the Iraqi MoD in January 2007, which were drafted by the
then-HoD."* Answering this requires semantic recall (the query doesn't match indexed text
verbatim), structured filtering (date range, creator), and often a multi-step reasoning
chain (resolve "the then-HoD" to a name, then search for that name's documents). This is a
natural fit for an LLM client that can call search tools repeatedly and reason over the
results — which is what the Model Context Protocol (MCP) is for.

This plan covers exposing RODA's search and preservation-action capabilities to MCP clients
(Claude Code, Claude Desktop, other MCP-compatible web clients), with two supporting
building blocks:

1. **Solr vector/semantic search**, so free-text queries that don't share vocabulary with
   the indexed metadata can still find relevant records.
2. **An MCP server with real authentication and authorization**, so results are always
   scoped to what the calling user is actually permitted to see — RODA is a repository of
   authentic, often sensitive, government/organizational records, so this is not optional.

### Goals

- Vector/semantic search over existing descriptive metadata and extracted full text,
  addressable through the same `Filter`/`FindRequest` model RODA already uses.
- An MCP server exposing search (basic, advanced/structured, semantic) and a useful subset
  of preservation actions (jobs/plugins) as tools.
- Every MCP call authorization-scoped exactly as RODA's own UI/REST API is today — no new
  permission model, no bypass path.
- Minimal footprint inside `roda-core`/`roda-wui`: reuse existing mechanisms wherever one
  already exists (permission filtering, `Filter`/`FilterParameter`, CAS/LDAP auth, the
  AccessKey/JWT bearer scheme) instead of building parallel ones.

### Non-goals (v1)

- No new "authority record" / role-over-time data model (e.g. EAC-CPF-style "who held
  position X from date A to date B"). RODA has no such model today (confirmed — only
  PREMIS preservation agents, which represent who ran a *preservation event*, not who
  authored a *record*). Resolving something like "the then-Head of Department" is left to
  the calling LLM doing multi-step tool calls over existing full-text/semantic/structured
  search — it works when the archive itself contains the resolving fact (an appointment
  record, an org chart, a signature block), which is the common case for an archive whose
  job is to keep exactly those records.
- No embedding-generation service inside RODA. Turning text into vectors at index time is
  handled by Solr's own `language-models` module calling an external embedding API, on an
  asynchronous second pass managed by a separate service outside this repository. RODA's
  only obligation to that service is a stable contract (field name, vector dimension, a
  tracking flag) — see below.
- No hand-rolled OAuth 2.1 protocol implementation (PKCE, dynamic client registration,
  RFC 8707 resource indicators) — delegated to purpose-built libraries (see Framework
  choices).

## Architecture

```
Claude (Desktop / Code / web client)
   │  OAuth 2.1 (authorization_code + PKCE, dynamic client registration)
   ▼
roda-mcp-authserver          (new module — Spring Authorization Server)
   │  authenticates via existing UserUtility/LdapUtility (+ optional CAS SSO redirect)
   │  issues RS256 JWT, sub=username, JWKS published
   ▼  (MCP client presents this JWT as a Bearer token to the MCP server)
roda-mcp-server               (new module — Spring AI MCP Server, WebMVC/Streamable HTTP)
   │  OAuth2 Resource Server — validates JWT against roda-mcp-authserver's JWKS
   │  forwards the SAME JWT as Authorization: Bearer to RODA
   ▼
RODA REST API v2  (existing — small trust extension, see Phase 3)
   ▼
IndexService.find(user, ...) — buildQueryPermissions() already scopes every query by
READ permission (roda-core/roda-core/src/main/java/org/roda/core/index/utils/SolrUtils.java:393-404)
```

Both new modules depend only on `roda-core` (for auth reuse) and never on `roda-wui` —
they talk to RODA exclusively over its REST API, so they never trigger a GWT build and can
be released/deployed independently of the main application.

## Component 1 — Solr vector/semantic search

Solr 10 (already RODA's version, `pom.xml:123`) ships a `language-models` module
(added in 9.8, [SOLR-17632](https://issues.apache.org/jira/browse/SOLR-17632)) that calls
external embedding APIs via LangChain4j (OpenAI/Cohere/HuggingFace/MistralAI client
shapes — the OpenAI-shaped client accepts an arbitrary `baseUrl`, so a local
OpenAI-API-compatible embedding server works). It supports vectorization at both index
time (`TextToVectorUpdateProcessorFactory`) and query time (`knn_text_to_vector` query
parser), and its own documentation recommends exactly the pattern requested here: index
documents fast without vectors, then enrich them on an asynchronous second pass.

Given that, RODA's changes are limited to:

1. **Deployment config** (not application code):
   - Enable the module (`SOLR_MODULES=language-models` on the Solr container —
     `deploys/standalone/docker-compose-dev.yaml`, `docker-compose.yaml`).
   - Add a `DenseVectorField` fieldType to
     `roda-core/roda-core/src/main/resources/config/index/common/conf/managed-schema.xml`,
     dimension fixed to match the embedding model in use.
   - Add the `knn_text_to_vector` `queryParser` declaration to `solrconfig.xml`.
2. **One-time model registration**, `PUT /schema/text-to-vector-model-store` pointing at
   the local embedding server. To keep schema and model config from drifting apart across
   environments, extend `SolrBootstrapUtils.bootstrapSchemas()`
   (`roda-core/roda-core/src/main/java/org/roda/core/index/schema/SolrBootstrapUtils.java:71-123`)
   to perform this registration automatically from new `core.index.embedding.*` properties
   (base URL, model name, dimension).
3. **New Solr fields** on `AIPCollection`/`FileCollection`
   (`roda-core/roda-core/src/main/java/org/roda/core/index/schema/collections/`): the
   vector field itself, and a `vectorized_b` boolean flag the external enrichment service
   can query (`fq=vectorized_b:false`) to find unvectorized documents. This is the same
   low-risk mechanism every existing field uses — `SolrBootstrapUtils` diffs declared
   fields against the live schema and POSTs only what's missing.
4. **One new `FilterParameter` subtype** —
   `TextToVectorFilterParameter` (new class in
   `roda-common/roda-common-data/src/main/java/org/roda/core/data/v2/index/filter/`),
   holding `field`, `query`, `model`, `topK`. Registered like every existing subtype
   (`FilterParameter.java:27-42`, `@JsonSubTypes`) and handled in
   `SolrUtils.parseFilterParameter()` (`SolrUtils.java:992-1023`, a plain `instanceof`
   chain today — one more branch) to emit
   `{!knn_text_to_vector model=... f=... topK=...}<query text>`. Because it is just
   another `FilterParameter`, it composes with `DateRangeFilterParameter`,
   `SimpleFilterParameter`, etc. through the existing AND/OR filter model, and flows
   through the **existing** `/api/v2/aips/find` (and equivalent) endpoints —
   **no new REST endpoint is required**.

Ranking strategy (pure top-K filter vs. blending into relevance scoring for true hybrid
BM25+vector ranking) needs empirical tuning against a live Solr instance with real data.
Ship as a hard top-K filter first (composes cleanly with structured filters, which is what
the target use case needs); revisit hybrid ranking once there's a corpus to test against.

**Explicitly out of scope for this repository:** the external service that finds
unvectorized documents and writes vectors back via atomic Solr updates. RODA's only
obligation to it is the stable contract above (field name, dimension, `vectorized_b`
marker) — the service's internals, schedule, and choice of embedding call are managed
externally, per the request that RODA not own this second pass.

## Component 2 — RODA REST API trust extension

RODA's REST API already accepts Bearer JWTs for machine-to-machine access, self-issued via
the AccessKey mechanism (`MembersController.authenticate`, `JwtUtils`, validated in
`CasApiAuthFilter`/`InternalApiAuthFilter`/`BearerAuthRequestWrapper`). This is extended
(additively — the existing self-signed HS256 path keeps working) to also trust RS256 JWTs
issued by `roda-mcp-authserver`, verified against its published JWKS. This is the only
change required inside `roda-wui` to make the MCP server's forwarded tokens work — RODA's
existing user/permission resolution (`UserUtility.getLdapUtility().getUser(username)`)
and `buildQueryPermissions()` handle the rest unchanged.

## Component 3 — `roda-mcp-authserver`

New Spring Boot module. Framework: `org.springaicommunity:mcp-authorization-server-spring-boot`
— a Spring Authorization Server auto-configuration purpose-built for MCP's OAuth 2.1
requirements (dynamic client registration / RFC 7591, resource indicators / RFC 8707),
avoiding a hand-rolled protocol implementation. It is community-maintained (not an
official Spring AI module) — a light dependency risk worth tracking, but it targets this
exact use case.

- Authentication backend: RODA's existing `UserUtility`/`LdapUtility`
  (`roda-core/roda-core/src/main/java/org/roda/core/model/utils/`), wired in as a custom
  `AuthenticationProvider` — no new user store, no duplicated LDAP logic. Optional CAS SSO
  redirect can reuse the same Apereo CAS client filters already used in
  `roda-ui/roda-wui/src/main/resources/static/WEB-INF/web.xml`.
- Issues RS256 JWTs (`sub` = RODA username, RODA group claims), publishes JWKS.
- Known current limitation of the library: every registered client is granted all
  `resource` identifiers (no per-client resource restriction yet) — acceptable for v1
  given a single downstream resource (RODA itself).

## Component 4 — `roda-mcp-server`

New Spring Boot module. Framework: `org.springframework.ai:spring-ai-starter-mcp-server-webmvc`
(GA 1.0.x — servlet-based Streamable HTTP transport, matching RODA's existing
servlet-MVC-only stack; WebFlux is used nowhere in this codebase and isn't supported by
the security module below anyway). Tools are plain `@Service` methods annotated
`@Tool(description = ...)`, registered via a `MethodToolCallbackProvider` bean.

- OAuth2 Resource Server wiring: `org.springaicommunity:mcp-server-security-spring-boot`
  (`McpServerOAuth2Configurer.mcpServerOAuth2()`, `spring.security.oauth2.resourceserver.jwt.issuer-uri`
  pointed at `roda-mcp-authserver`).
- On every tool call, the validated JWT is forwarded unchanged as
  `Authorization: Bearer <jwt>` to RODA's REST API v2 — no impersonation, no service
  credential, the request executes as the actual calling user end-to-end.
- Thin typed HTTP client to RODA API v2 (hand-written, or generated from the repo's
  `openapi.json`).
- Tools (terminology note: parameterized search takes structured input, which in MCP terms
  is a **tool**, not a passive "resource" — a couple of read-only **resources** are used
  for discovery, e.g. listing configured advanced-search fields):
  - `search_archive` — free text + structured filters (date range, creator/origination,
    type/level, collection) + semantic mode via `TextToVectorFilterParameter`.
  - `get_item_details` — full metadata for an AIP/representation/file UUID.
  - `get_document_text` — extracted full text of a file, so the calling LLM can read a
    candidate document mid-reasoning (needed for the multi-hop "then-HoD" case).
  - `list_preservation_plugins`, `start_preservation_job`, `get_job_status`,
    `get_job_report` — thin wraps of the existing Jobs/Configuration endpoints
    (`JobsController`, `ConfigurationController.retrievePluginsInfo`).
- Advanced-search field exposure is config-driven (YAML in this module), so adding a new
  advanced field later is a configuration change, not a code change.

## Module layout

New top-level directory `roda-mcp/` (this plan lives at `roda-mcp/PLAN.md`), containing:

```
roda-mcp/
├── pom.xml                    (parent for the two modules below)
├── roda-mcp-authserver/
└── roda-mcp-server/
```

Both registered as modules in the root `pom.xml`, alongside `roda-common`, `roda-core`,
`roda-ui`.

## Phased delivery

1. **Solr schema + config + `TextToVectorFilterParameter`** — additive only, no new
   services; testable via existing TestNG index tests and manual Solr queries.
2. **`roda-mcp-authserver`** — OAuth 2.1 AS backed by LDAP/CAS via
   `mcp-authorization-server-spring-boot` and reused RODA auth code.
3. **RODA API trust extension** — `CasApiAuthFilter`/`BearerAuthRequestWrapper` accept
   JWTs from the AS's JWKS alongside today's self-signed tokens.
4. **`roda-mcp-server`** — tools listed above, forwarding the validated JWT to RODA's REST
   API.
5. **End-to-end validation** — two users with different permissions issue the same query;
   confirm result sets differ correctly. Run the actual reasoning-search example
   ("MoM with the Iraqi MoD in January 2007, drafted by the then-HoD") against real data.

## Verification plan

- Phase 1: TestNG additions alongside `IndexServiceTest`/`ModelServiceTest`
  (`roda-core/roda-core-tests/`) covering `TextToVectorFilterParameter` parsing and Solr
  schema bootstrap of the new fields; manual `curl` against `/schema/text-to-vector-model-store`
  and a `{!knn_text_to_vector}` query against a dev Solr instance with the `language-models`
  module enabled.
- Phases 2–4: integration test hitting `roda-mcp-server`'s tool endpoints with a
  `roda-mcp-authserver`-issued token for two differently-permissioned RODA users, asserting
  result sets are correctly scoped.
- Phase 5: manual end-to-end run from Claude Code/Desktop configured against the deployed
  MCP server, exercising the motivating reasoning-search example.
