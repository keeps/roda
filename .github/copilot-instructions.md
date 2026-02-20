# RODA - Repository of Authentic Digital Records

## Repository Overview

RODA is a **long-term digital preservation repository** built with Java that implements the OAIS reference model. It's a multi-module Maven project providing functionalities for ingesting, managing, and providing access to various types of digital content.

**Key Technologies:**
- Java 21 (Oracle JDK)
- Apache Maven 3.8.6+ (build system)
- GWT 2.12.2 (Web UI framework)
- Spring Boot 3.4.10 (backend framework)
- Apache Solr 9.10.0 (indexing/search)
- PostgreSQL 17 (database)
- Docker (deployment)

**Project Size:** Large-scale enterprise application with ~3 main modules (roda-common, roda-core, roda-ui)

**License:** Open source

## Build & Development Setup

### Prerequisites

1. **Java 21** (Oracle JDK) - Required for compilation
2. **Maven 3.8.6+** - Build tool
3. **GitHub account configured with Maven** - Required to access GitHub Packages dependencies
   - Follow: https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token
   - Configure `~/.m2/settings.xml` with GitHub personal access token
4. **Docker & Docker Compose** - For running development dependencies (Solr, PostgreSQL, etc.)

### Build Commands

**Standard build:**
```bash
mvn clean package
```

**Build without tests (faster):**
```bash
mvn clean package -Dmaven.test.skip=true
```

**Build only core modules:**
```bash
mvn clean package -Pcore
```

**Install to local Maven repository:**
```bash
mvn install -Pcore -DskipTests
```

### Running Tests

**Run all tests:**
```bash
mvn clean test
```

**Run CI tests (Travis group):**
```bash
mvn -Dtestng.groups="travis" -Denforcer.skip=true clean org.jacoco:jacoco-maven-plugin:prepare-agent test
```

**Important:** Tests require external services (Solr, PostgreSQL, ZooKeeper, OpenLDAP) running via Docker. The CI workflow in `.github/workflows/CI.yml` shows the complete test setup.

### Development Environment Setup

**Start development dependencies:**
```bash
# Create required directories
mkdir -p $HOME/.roda/data/{storage,staging-storage}

# Start Solr, Zookeeper, PostgreSQL, ClamAV, etc.
docker compose -f deploys/standalone/docker-compose-dev.yaml up -d
```

**Debug Web UI with GWT:**
```bash
# First-time GWT compile
mvn -pl roda-ui/roda-wui -am gwt:compile -Pdebug-main -Dscope.gwt-dev=compile

# Install core modules
mvn install -Pcore -DskipTests

# Start Spring Boot application
mvn -pl roda-ui/roda-wui -am spring-boot:run -Pdebug-main

# In another terminal, start GWT codeserver for hot reload
mvn -f dev/codeserver gwt:codeserver -DrodaPath=$(pwd)
```

The application will be available at http://localhost:8080

### Common Build Issues & Solutions

1. **GitHub Packages authentication failure:**
   - Ensure `~/.m2/settings.xml` is properly configured with GitHub token
   - Token must have `read:packages` permission

2. **Test failures:**
   - Ensure Docker services are running: `docker compose -f deploys/standalone/docker-compose-dev.yaml up -d`
   - Check that ports 2181 (ZooKeeper), 8983 (Solr), 5432 (PostgreSQL), 1389 (LDAP) are available

3. **GWT compilation issues:**
   - Use profile `-Pdebug-main` for development
   - Clean GWT cache if needed: `rm -rf ~/.gwt`

4. **Build timing:**
   - Full build with tests: ~10-15 minutes
   - Build without tests: ~5-8 minutes
   - Running tests only: ~8-12 minutes

## Project Structure

### Module Layout

```
/
├── pom.xml                    # Root Maven POM with dependency management
├── code-style/                # Checkstyle & Eclipse formatter configurations
│   ├── checkstyle.xml
│   └── eclipse_formatter.xml
├── roda-common/               # Common components used across modules
│   ├── roda-common-data/      # Model objects (POJOs)
│   └── roda-common-utils/     # Shared utilities
├── roda-core/                 # Core business logic
│   ├── roda-core/             # Storage, indexing, model services
│   └── roda-core-tests/       # Test helpers and core tests
├── roda-ui/                   # Web User Interface
│   └── roda-wui/              # GWT-based web application + REST API
├── scripts/                   # Build and release scripts
├── deploys/                   # Deployment configurations
│   └── standalone/            # Docker Compose files
├── docker/                    # Docker image build scripts
└── documentation/             # User and developer documentation
```

### Key Directories

- **Source code:** `*/src/main/java/org/roda/`
- **Tests:** `*/src/test/java/org/roda/`
- **Resources/Config:** `*/src/main/resources/`
- **Web UI assets:** `roda-ui/roda-wui/src/main/resources/config/`
- **i18n translations:** `roda-ui/roda-wui/src/main/resources/config/i18n/`

### Important Configuration Files

- `pom.xml` - Maven project configuration (dependencies, plugins, profiles)
- `code-style/checkstyle.xml` - Java code style rules
- `code-style/eclipse_formatter.xml` - Java formatter configuration
- `.github/workflows/CI.yml` - CI test configuration
- `deploys/standalone/docker-compose-dev.yaml` - Development environment setup

## Code Style & Linting

### Java Code Style

RODA uses **Checkstyle** for code quality enforcement and **Eclipse formatter** for consistent formatting.

**Checkstyle configuration:** `code-style/checkstyle.xml`
**Eclipse formatter:** `code-style/eclipse_formatter.xml`

### IDE Configuration (IntelliJ IDEA)

1. Install "Adapter for Eclipse Code Formatter" plugin
2. Configure formatter to use `code-style/eclipse_formatter.xml`
3. Set import order: `java;javax;org;com;`
4. Set class count for wildcard imports: 9999
5. Set static import count for wildcard: 9999

### Code Formatting Rules

- Use Eclipse formatter configuration
- Imports: No wildcard imports unless >9999 classes
- Line length: Follow Eclipse formatter settings
- Indentation: Spaces (configured in formatter)

**To format code:**
In IntelliJ: `Code > Reformat File...` with scope "Only VCS changed text"

## CI/CD & GitHub Workflows

### Continuous Integration

**Main CI workflow:** `.github/workflows/CI.yml`
- Triggered on: All pushes to any branch
- Java version: 21
- Maven version: 3.9.9
- Services: ZooKeeper, Solr, PostgreSQL, MailHog, OpenLDAP
- Test command: `mvn -Dtestng.groups="travis" -Denforcer.skip=true clean org.jacoco:jacoco-maven-plugin:prepare-agent test`
- Package command: `mvn -Dmaven.test.skip=true package`

### Other Workflows

- `codeql-analysis.yml` - Security scanning
- `development.yml` - Development deployment
- `staging.yml` - Staging deployment  
- `release.yml` - Production release
- `latest.yml` - Latest version deployment

### Pre-commit Validation

Before committing changes:
1. Run tests: `mvn clean test`
2. Build successfully: `mvn clean package`
3. Check code style (automatically via Checkstyle during build)
4. Ensure Docker services are running for integration tests

## Testing Strategy

### Test Framework

- **TestNG** for Java unit/integration tests
- Test groups: Use `@Test(groups = "travis")` for CI tests

### Test Execution

**All tests:**
```bash
mvn clean test
```

**CI test group only:**
```bash
mvn -Dtestng.groups="travis" clean test
```

**Skip tests:**
```bash
mvn clean package -Dmaven.test.skip=true
```

### Test Environment Requirements

Tests require these services running (via Docker Compose):
- Apache Solr (Cloud mode on ZooKeeper)
- PostgreSQL database
- OpenLDAP server
- MailHog (email testing)

Start services: `docker compose -f deploys/standalone/docker-compose-dev.yaml up -d`

### Test Helper Classes

- `org.roda.core.TestsHelper` - Common test utilities
- `org.roda.core.CorporaConstants` - Test data constants
- `roda-core-tests` module - Provides test infrastructure for all modules

## Dependencies & Package Management

### Maven Dependency Resolution

Dependencies are resolved from:
1. **GitHub Packages** - `https://maven.pkg.github.com/keeps/*` (requires authentication)
2. **Maven Central** - Default repository
3. **Maven Restlet** - `https://maven.restlet.talend.com/`

### Adding Dependencies

When adding new dependencies:
1. Add to root `pom.xml` in `<dependencyManagement>` section (defines version)
2. Add to module `pom.xml` in `<dependencies>` section (without version)
3. Always check for security vulnerabilities before adding
4. Use properties for version numbers (defined in root POM `<properties>`)

### Common Dependencies

- Spring Framework: `${spring.version}` (6.2.11)
- Apache Solr: `${solr.version}` (9.10.0)
- Jackson: `${jackson.version}` (2.20.1)
- GWT: `${gwt.version}` (2.12.2)

## Common Tasks

### Building Docker Image

```bash
cd docker
./build.sh
```

### Updating Version

See `DEV_NOTES.md` for release process using scripts:
- `./scripts/release.sh VERSION`
- `./scripts/prepare_next_version.sh VERSION`

### Running RODA Locally

```bash
# Start dependencies
docker compose -f deploys/standalone/docker-compose-dev.yaml up -d

# Build and run
mvn -pl roda-ui/roda-wui -am spring-boot:run -Pdebug-main
```

Access at: http://localhost:8080

### Creating Solr Collections

```bash
./scripts/createSolrCollections.sh
```

## Important Notes for AI Coding Agents

1. **Always configure GitHub Packages authentication** before building. Without it, Maven dependency resolution will fail.

2. **Start Docker services before running tests.** Integration tests require Solr, PostgreSQL, and other services.

3. **Use Maven profiles appropriately:**
   - Default profile: Builds all modules
   - `-Pcore`: Only core modules (faster)
   - `-Pdebug-main`: For GWT development/debugging

4. **Test execution time:** Full test suite takes 8-12 minutes. Use `-Dtestng.groups="travis"` for faster CI subset.

5. **Code formatting:** Always use the Eclipse formatter configuration from `code-style/eclipse_formatter.xml`. Checkstyle validation runs automatically during build.

6. **Multi-module builds:** When making changes to `roda-core`, you must rebuild dependent modules (roda-ui) or use `mvn install` to update local repository.

7. **GWT compilation:** First-time GWT compile is slow (~5-10 minutes). For development, use GWT codeserver for hot reload.

8. **Environment variables for tests:** See `.github/workflows/CI.yml` for required environment variables when running tests (e.g., `RODA_CORE_SOLR_TYPE=CLOUD`, `SIEGFRIED_MODE=standalone`).

9. **Documentation is in Markdown:** All documentation files are in the `documentation/` directory and use Markdown format.

10. **Internationalization:** Translation files are in `roda-ui/roda-wui/src/main/resources/config/i18n/`. RODA supports multiple languages via properties files.

11. **When exploring the codebase:** Start with `README.md`, then `DEV_NOTES.md`, then `documentation/Developers_Guide.md` for comprehensive information.

12. **Trust these instructions:** The build and test commands documented here are validated and work correctly. Only search for additional information if these instructions are incomplete or encounter errors.
