# CLAUDE.md — RODA Codebase Guide for AI Assistants

## Project Overview

RODA (Repository of Authentic Digital Records) is an enterprise-grade **digital preservation repository** implementing the OAIS reference model. It handles ingestion, storage, indexing, and access to digital archival content using internationally recognized preservation standards (METS, EAD, Dublin Core, PREMIS, E-ARK).

**Current Version:** 6.1.0-SNAPSHOT
**License:** Apache 2.0
**Package Namespace:** `org.roda.*`

### Core Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 21 (Oracle JDK) |
| Build | Apache Maven | 3.8.6+ |
| Backend | Spring Boot | 3.5.x |
| Web UI | GWT | 2.12.2 |
| Search/Index | Apache Solr | 9.10.0 |
| Database | PostgreSQL | 17 |
| Actors | Pekko (Akka replacement) | 1.4.0 |
| Serialization | Jackson | 2.20.1 |

---

## Repository Structure

```
roda/
├── pom.xml                        # Root Maven POM — dependency management, profiles
├── code-style/
│   ├── checkstyle.xml             # Enforced style rules
│   └── eclipse_formatter.xml     # Java formatter (required for IntelliJ)
├── roda-common/
│   ├── roda-common-data/          # Shared POJOs: AIP, DIP, Representation, etc.
│   └── roda-common-utils/         # Shared utility classes
├── roda-core/
│   ├── roda-core/                 # Core business logic
│   │   └── src/main/java/org/roda/core/
│   │       ├── common/            # PREMIS, email, Handlebars templates
│   │       ├── config/            # Spring Boot configuration
│   │       ├── entity/            # JPA/Hibernate entities
│   │       ├── events/            # Async event system
│   │       ├── index/             # Solr indexing services & filters
│   │       ├── migration/         # Schema/data migration logic
│   │       ├── model/             # CRUD for RODA objects (AIPs, DIPs)
│   │       ├── plugins/           # Preservation action plugin framework
│   │       ├── protocols/         # File transfer protocols
│   │       ├── repository/        # Repository service interfaces
│   │       ├── security/          # Auth, LDAP, permissions
│   │       ├── storage/           # Filesystem/cloud storage abstraction
│   │       └── transaction/       # Transaction logging
│   └── roda-core-tests/           # Test helpers, TestsHelper, CorporaConstants
├── roda-ui/
│   └── roda-wui/                  # GWT web app + Spring Boot REST API
│       └── src/main/java/org/roda/wui/
│           ├── api/v1/            # Legacy REST endpoints
│           ├── api/v2/            # Current REST endpoints (OpenAPI)
│           ├── client/            # GWT client-side components
│           ├── common/            # Shared UI utilities
│           ├── config/            # Spring configuration
│           ├── filter/            # Security filters
│           ├── security/          # Authentication handlers
│           └── servlets/          # Custom servlet implementations
├── deploys/
│   └── standalone/
│       ├── docker-compose.yaml         # Production-like setup
│       └── docker-compose-dev.yaml     # Development setup
├── docker/                        # Docker image build files
│   └── Dockerfile                 # eclipse-temurin:21-jre-jammy base
├── dev/
│   └── codeserver/                # GWT codeserver config for hot reload
├── scripts/                       # Release and utility scripts
├── documentation/                 # 100+ Markdown docs (multi-language)
├── openapi.json                   # REST API specification
└── .github/workflows/             # CI/CD pipelines
```

---

## Development Setup

### Prerequisites

1. **Java 21** (Oracle JDK) — strictly required for compilation
2. **Maven 3.8.6+** — build tool
3. **Docker & Docker Compose** — for running Solr, PostgreSQL, LDAP, etc.
4. **GitHub account with PAT** — required for GitHub Packages dependency resolution

**Configure Maven for GitHub Packages** (`~/.m2/settings.xml`):

The easiest way is to set the environment variables and run the provided script:
```bash
export GITHUB_MAVEN_USER=YOUR_GITHUB_USERNAME
export GITHUB_MAVEN_PASSWORD=YOUR_GITHUB_PAT
./scripts/setup_maven_settings.sh
```

Alternatively, create `~/.m2/settings.xml` manually:
```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_PAT</password>
    </server>
  </servers>
</settings>
```
The PAT must have `read:packages` permission. Without this, the build will fail to resolve dependencies.

### Starting Development Dependencies

```bash
# Create required data directories
mkdir -p $HOME/.roda/data/{storage,staging-storage}

# Start all services (Solr, ZooKeeper, PostgreSQL, ClamAV, Siegfried, OpenLDAP, MailPit)
docker compose -f deploys/standalone/docker-compose-dev.yaml up -d
```

Services and ports:
- ZooKeeper: `2181`
- Apache Solr: `8983`
- PostgreSQL: `5432`
- OpenLDAP: `1389`
- MailPit (SMTP): `1025`
- Swagger UI: `8088`

---

## Build Commands

```bash
# Full build with tests
mvn clean package

# Build without tests (faster)
mvn clean package -Dmaven.test.skip=true

# Build core modules only (skip UI)
mvn clean package -Pcore

# Install core to local Maven repo
mvn install -Pcore -DskipTests

# First-time GWT compile (slow, ~5-10 min)
mvn -pl roda-ui/roda-wui -am gwt:compile -Pdebug-main -Dscope.gwt-dev=compile
```

### Build Profiles

| Profile | Purpose |
|---------|---------|
| _(default)_ | All modules |
| `-Pcore` | Core modules only (faster, skips GWT) |
| `-Pdebug-main` | GWT development/debugging mode |
| `-Proda-core-jar` | Produces shaded JAR |
| `-Proda-core-jar-docker` | Docker-ready shaded JAR |

### Typical Build Times

- Full build with tests: ~10–15 minutes
- Build without tests: ~5–8 minutes
- Tests only: ~8–12 minutes
- First GWT compile: ~5–10 minutes

---

## Running the Application Locally

```bash
# 1. Install core to local repo
mvn install -Pcore -DskipTests

# 2. Start Spring Boot application
mvn -pl roda-ui/roda-wui -am spring-boot:run -Pdebug-main

# 3. (Optional) Start GWT codeserver for hot reload in a separate terminal
mvn -f dev/codeserver gwt:codeserver -DrodaPath=$(pwd)
```

- App available at: `http://localhost:8080`
- GWT codeserver UI: `http://127.0.0.1:9876/`
- Activate GWT dev mode: Open RODA in browser, click "Dev Mode On" bookmark

---

## Testing

### Test Framework

- **TestNG** (not JUnit) for all Java tests
- Tag CI tests with `@Test(groups = "travis")`

### Running Tests

```bash
# All tests (requires Docker services running)
mvn clean test

# CI subset only (faster)
mvn -Dtestng.groups="travis" -Denforcer.skip=true clean org.jacoco:jacoco-maven-plugin:prepare-agent test

# Skip tests
mvn clean package -Dmaven.test.skip=true
```

### Required Environment Variables (for tests matching CI)

```
RODA_CORE_SOLR_TYPE=CLOUD
SIEGFRIED_MODE=standalone
```

See `.github/workflows/CI.yml` for the full CI test environment configuration.

### Key Test Classes

- `org.roda.core.TestsHelper` — common test utilities
- `org.roda.core.CorporaConstants` — test data constants
- `ModelServiceTest`, `IndexServiceTest`, `StorageServiceTest`
- `IngestPluginTests`, `DisposalTests`, `PermissionsTest`

Tests live in:
- `roda-core/roda-core-tests/src/main/java/org/roda/core/`
- `roda-ui/roda-wui/src/test/`

---

## Code Style & Conventions

### Java Formatting

- Formatter: **Eclipse Code Formatter** (`code-style/eclipse_formatter.xml`)
- Linter: **Checkstyle** (`code-style/checkstyle.xml`) — enforced during Maven build
- Import order: `java;javax;org;com;`
- Wildcard imports: **disabled** (threshold set to 9999 classes)

### IntelliJ IDEA Setup

1. Install plugin: "Adapter for Eclipse Code Formatter"
2. Point formatter at `code-style/eclipse_formatter.xml`
3. Set import thresholds (class count: 9999, static: 9999)
4. Format with: `Code > Reformat File...` → "Only VCS changed text"

### Naming & Package Conventions

- Root package: `org.roda`
- Module packages: `org.roda.core.*`, `org.roda.wui.*`, `org.roda.common.*`
- Model objects (POJOs) live in `roda-common-data`
- REST endpoints follow versioned paths: `/api/v1/` (legacy) and `/api/v2/` (current)

---

## Architecture Overview

### Layered Architecture

```
GWT Web UI (client-side)
       ↕  REST API (Spring MVC, /api/v2/)
Spring Boot Application (roda-wui)
       ↕  Service interfaces
Core Services (roda-core)
   ├── ModelService    — CRUD for AIPs, DIPs, Representations
   ├── IndexService    — Solr-backed search & filtering
   ├── StorageService  — Filesystem/cloud storage abstraction
   └── PluginService  — Preservation action orchestration
       ↕  Pekko actors (async jobs)
Infrastructure
   ├── PostgreSQL      — Transactions, JPA entities
   ├── Apache Solr     — Indexing & search (Cloud mode)
   └── LDAP/CAS        — Authentication
```

### Key Design Patterns

- **Factory pattern:** `RodaCoreFactory` bootstraps and provides all core services
- **Observer pattern:** Index services observe model changes for automatic re-indexing
- **Plugin architecture:** Extensible plugins for preservation workflows
- **Storage abstraction:** OpenStack Swift-inspired storage interface
- **Event-driven:** Pekko actors for async, parallel preservation tasks
- **Transactional wrapper:** `TransactionalModelService` adds transaction logging

### OAIS Information Packages

RODA implements the OAIS standard:
- **SIP** — Submission Information Package (ingest)
- **AIP** — Archival Information Package (stored)
- **DIP** — Dissemination Information Package (access)

---

## REST API

- **v1** (legacy): `/api/v1/` — maintained for backwards compatibility
- **v2** (current): `/api/v2/` — fully documented via OpenAPI
- OpenAPI spec: `openapi.json` and served at `/api/v2/openapi`
- Swagger UI (dev): `http://localhost:8088`

---

## Database

- **PostgreSQL 17** via Spring Data JPA + Hibernate
- DDL mode: `update` (Hibernate auto-updates schema)
- Default connection: `jdbc:postgresql://localhost:5432/roda_core_db`
- Credentials: `admin` / `roda` (development defaults)
- Schema init: `spring.sql.init.mode=always`

---

## CI/CD

### Workflows

| File | Trigger | Purpose |
|------|---------|---------|
| `CI.yml` | Every push | Tests + package build |
| `codeql-analysis.yml` | Schedule | Security scanning |
| `development.yml` | Push to dev branch | Dev deployment |
| `staging.yml` | Push to staging | Staging deployment |
| `release.yml` | Tag push | Production release |

### CI Test Command

```bash
mvn -Dtestng.groups="travis" -Denforcer.skip=true \
    clean org.jacoco:jacoco-maven-plugin:prepare-agent test
```

---

## Release Process

Before releasing:
```bash
# Security vulnerability check
mvn com.redhat.victims.maven:security-versions:check

# Check for dependency updates
./scripts/check_versions.sh MINOR
mvn versions:display-dependency-updates
```

Release workflow (example: releasing 2.2.0, next 2.3.0):
```bash
./scripts/release.sh 2.2.0
# Wait for GitHub Actions release.yml to succeed
# Review and publish the GitHub Release
./scripts/update_changelog.sh 2.2.0
./scripts/prepare_next_version.sh 2.3.0
```

---

## Docker

```bash
# Build local Docker image
cd docker && ./build.sh

# Production-like local stack
docker compose -f deploys/standalone/docker-compose.yaml up -d

# Development stack
docker compose -f deploys/standalone/docker-compose-dev.yaml up -d

# Access shell in running container
docker exec -it CONTAINER_ID /bin/bash
```

Base image: `eclipse-temurin:21-jre-jammy`

---

## Multi-Module Build Dependencies

When modifying a module, rebuild its dependents:

```
roda-common-data  →  roda-common-utils  →  roda-core  →  roda-wui
```

After changing `roda-core`, run `mvn install -Pcore -DskipTests` before building `roda-wui` to update the local Maven cache.

---

## Internationalization (i18n)

- Translation files: `roda-ui/roda-wui/src/main/resources/config/i18n/`
- Format: Java `.properties` files
- Managed via Transifex (cloud translation platform)
- Supported languages: English, Portuguese, Swedish, Hungarian, Spanish, Croatian, German (Austria), and more
- See `documentation/Translation_Guide.md` for translation workflow

---

## Key Files for Orientation

| File | Purpose |
|------|---------|
| `README.md` | Project overview, features, editions |
| `DEV_NOTES.md` | Quick-start for developers |
| `documentation/Developers_Guide.md` | Comprehensive development guide |
| `openapi.json` | REST API specification |
| `pom.xml` | Root Maven config with all dependency versions |
| `.github/workflows/CI.yml` | CI configuration and test environment |
| `deploys/standalone/docker-compose-dev.yaml` | Dev service stack |

---

## Critical Notes for AI Assistants

1. **GitHub Packages auth is required.** Maven build will fail without a valid `~/.m2/settings.xml` with a GitHub PAT having `read:packages`.

2. **Always start Docker services before running tests.** Tests use Testcontainers (auto-starts ZooKeeper, Solr, PostgreSQL, LDAP, Mailpit, ClamAV, Siegfried). The Docker daemon must be running. In the Claude Code cloud environment, start it with: `service docker start` (may print an ulimit warning — that is harmless).

3. **Use the correct Maven profile.**
   - Skip UI/GWT: use `-Pcore`
   - GWT dev mode: use `-Pdebug-main`
   - Never mix profiles incorrectly.

4. **TestNG, not JUnit.** All test annotations are `org.testng.*`. Group CI tests with `@Test(groups = "travis")`.

5. **Multi-module dependency order.** Changing `roda-common-data` or `roda-core` requires `mvn install` before building downstream modules.

6. **GWT compilation is slow.** The first compile takes 5–10 minutes. For active UI development, use the codeserver (`mvn -f dev/codeserver gwt:codeserver`) for hot reload.

7. **Code formatting is enforced.** Checkstyle runs during every Maven build. Always apply the Eclipse formatter from `code-style/eclipse_formatter.xml` before committing.

8. **REST API has two versions.** New endpoints go in `/api/v2/`. Do not add business logic to `/api/v1/` (legacy).

9. **PREMIS metadata is mandatory.** Every preservation action must record a PREMIS event in the AIP's metadata. Follow existing plugin implementations as examples.

10. **Commit signing.** Commits should be GPG-signed per the project's contribution guidelines. See: https://docs.github.com/en/authentication/managing-commit-signature-verification/signing-commits

---

## Claude Code Cloud Environment — Quick Reference

This section captures environment-specific quirks for running in the Claude Code remote container.

### Docker Setup

Docker daemon is installed but may not be running at session start:

```bash
# Check if Docker is running
docker ps

# If not running, start it (the ulimit warning is harmless):
service docker start

# Verify
docker ps  # should show empty table, not an error
```

### Build Commands (Cloud Environment)

Maven Central access may be blocked by a proxy. Always use **offline mode** (`-o`) or the local repo when possible. The proxy is pre-configured via `JAVA_TOOL_OPTIONS` env var.

```bash
# Step 1: Build and install core modules (no tests, no GWT)
mvn install -Pcore -DskipTests -Denforcer.skip=true

# Step 2: Run a single test class to verify (fast validation)
mvn -pl roda-core/roda-core-tests test -Pcore \
  -Dtestng.groups="travis" \
  -Denforcer.skip=true \
  -Dsurefire.suiteXmlFiles=testng-single.xml \
  -o

# Step 3: Run full CI test suite
mvn -Pcore -Dtestng.groups="travis" -Denforcer.skip=true \
  clean org.jacoco:jacoco-maven-plugin:prepare-agent test
```

### Single-Test Shortcut

`roda-core/roda-core-tests/testng-single.xml` targets only `IndexServiceTest`. Edit the `<class name="...">` element to point at any test class you want to run in isolation.

### Test Infrastructure (Testcontainers)

Tests use `TestContainersManager` (singleton) to start containers once per JVM. The `RodaContainersLifecycleListener` triggers it via `testng.xml`. Containers started:

| Service     | Image                     |
|-------------|---------------------------|
| ZooKeeper   | zookeeper:3.9.1-jre-17    |
| Solr        | solr:9                    |
| PostgreSQL  | postgres:17               |
| Mailpit     | axllent/mailpit:latest    |
| ClamAV      | clamav/clamav:1.5.2       |
| Siegfried   | keeps/siegfried:v1.11.0   |

**Important**: On Linux, Solr registers its container IP in ZooKeeper. Bridge network IPs (`172.x.x.x`) are directly routable from the host — no port mapping is needed for the CloudSolrClient to reach Solr live nodes.

### ZooKeeper / Solr Connection Notes

- `zkConnectTimeout` defaults to 15 s in SolrJ. If the ZK session is not established within that window, `SolrZkClient` calls `ZooKeeper.close()`, which **hangs indefinitely** (sends CLOSESESSION but has no threads left to receive the response).
- The fix is in `RodaCoreFactory.instantiateSolr()`: `withZkConnectTimeout(300000, MILLISECONDS)` is set on the builder.
- `TestContainersManager` also sets `System.setProperty("zkConnectTimeout", "300000")` as belt-and-suspenders.

### Pre-PR Checklist

Before pushing/creating a PR:
1. `service docker start` (if not already running)
2. `mvn install -Pcore -DskipTests -Denforcer.skip=true` — compile all modules
3. Run a targeted single test to validate the change area
4. Run full CI test suite if the change is broad
5. Verify no Checkstyle violations (they are enforced in CI)
