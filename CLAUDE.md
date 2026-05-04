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
3. **Docker** — required for running the application locally and for tests (via Testcontainers — no manual `docker compose` needed for tests)
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

**For running tests:** No manual setup needed — tests use **Testcontainers** (`TestContainersManager`) which automatically starts ZooKeeper, Solr, PostgreSQL, Mailpit, ClamAV, and Siegfried as ephemeral Docker containers. The `RodaContainersLifecycleListener` TestNG suite listener wires this up before any test class loads. Docker must be running on the host, but no `docker compose` command is required.

**For running the application locally:**

```bash
# Create required data directories
mkdir -p $HOME/.roda/data/{storage,staging-storage}

# Start all services (Solr, ZooKeeper, PostgreSQL, ClamAV, Siegfried, OpenLDAP, MailPit)
docker compose -f deploys/standalone/docker-compose-dev.yaml up -d
```

Services and ports (for local app, not tests):
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

Tests use **Testcontainers** — no environment variables or `docker compose` setup required. Docker must be available on the host.

```bash
# All tests
mvn clean test -Pcore

# CI subset only (faster)
mvn -Dtestng.groups="travis" -Denforcer.skip=true clean org.jacoco:jacoco-maven-plugin:prepare-agent test -Pcore

# Specific test class
mvn -pl roda-core/roda-core-tests -am test -Dtest=NestedDocumentSearchTest -Dtestng.groups=dev -Denforcer.skip=true -DfailIfNoTests=false

# Skip tests
mvn clean package -Dmaven.test.skip=true
```

See `.github/workflows/CI.yml` for the full CI configuration.

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

2. **Always start Docker services before running tests.** Tests are integration tests — they need live Solr, PostgreSQL, ZooKeeper, and LDAP.

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
