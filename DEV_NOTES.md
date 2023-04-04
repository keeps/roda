# Dev notes

[![CI](https://github.com/keeps/roda/actions/workflows/CI.yml/badge.svg)](https://github.com/keeps/roda/actions/workflows/CI.yml)
[![CodeQL](https://github.com/keeps/roda/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/keeps/roda/actions/workflows/codeql-analysis.yml)
[![Development](https://github.com/keeps/roda/actions/workflows/development.yml/badge.svg)](https://github.com/keeps/roda/actions/workflows/development.yml)
[![Staging](https://github.com/keeps/roda/actions/workflows/staging.yml/badge.svg)](https://github.com/keeps/roda/actions/workflows/staging.yml)
[![Release](https://github.com/keeps/roda/actions/workflows/release.yml/badge.svg)](https://github.com/keeps/roda/actions/workflows/release.yml)

## Setup

Install pre-requisites:
- Java 17
- Maven 3.8.6 or greater
- [Configure Maven to use your GitHub account for GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token)

Additional support for internal plugins:
 -  ClamAV using docker image

 ```sh
 sudo apt install clamdscan
 echo "TCPSocket 3310
TCPAddr localhost" | sudo tee /etc/clamav/clamd.conf
 ```

## Debug WUI

```bash
# If never GWT compiled before, compile once and copy gwt.rpc files
mvn -pl roda-ui/roda-wui -am gwt:compile -Pdebug-main -Dscope.gwt-dev=compile
./roda-ui/roda-wui/copy_gwt_rpc.sh

# Start up dependencies (Solr, Zookeeper, Siegfried, ClamAV)
mkdir -p $HOME/.roda/data/storage
docker compose -f deploys/standalone/docker-compose-dev.yaml up -d


# Open WUI in Spring boot
mvn -pl roda-ui/roda-wui -am spring-boot:run -Pdebug-main

# Open codeserver
mvn -pl roda-ui/roda-wui -am gwt:codeserver -Pdebug-main -Dscope.gwt-dev=compile

# Open codeserver http://127.0.0.1:9876/ and add bookmarks
# Open RODA http://localhost:8080 and click the "Dev Mode On" bookmark

```

Optional: Check Google Chrome "RemoteLiveReload" extension for automatic reloading with spring boot.

## Release new version

Before releasing:

1. Security check: `mvn com.redhat.victims.maven:security-versions:check`
2. Update check: `./scripts/check_versions.sh MINOR`

Example release 2.2.0 and prepare for next version 2.3.0.

1. Run `./scripts/release.sh 2.2.0`
2. Wait for [GitHub action build](https://github.com/keeps/roda/actions/workflows/release.yml) to be finished and successful
3. Review release and accept release:
    1. Review issues
    2. Add docker run instructions
    3. Publish release
4. Run `./scripts/update_changelog.sh 2.2.0`
5. Run `./scripts/prepare_next_version.sh 2.3.0`

Snippet for docker run instructions:

````text
Install for demonstration:
```
docker pull keeps/roda:v2.2.11
```
````

## Redeploy on docker

* Delete all stopped containers

```bash
docker ps -q -f status=exited | xargs --no-run-if-empty docker rm
```

* Delete all dangling (unused) images

```bash
docker images -q -f dangling=true | xargs --no-run-if-empty docker rmi
```

* Use bash in running container

```bash
docker exec -i -t CONTAINER_ID /bin/bash
```

## Browser compatibility

Testing RODA on different browsers is done in an easy way thanks to BrowserStack!

[![BrowserStack website](https://user-images.githubusercontent.com/98429/40908885-f1559ca4-67df-11e8-8a98-8b0b57d3febb.png)](http://browserstack.com/)

## Common problems

* Lack of permissions to download dependencies when building RODA: to compile you need to set your GitHub access token in your settings.xml as described on https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token
* Problems may arise when using GWT Dev Mode and having in the classpath a different jetty version

## Misc

* Executing worker (compiled with -Proda-core-jar):
  * `java -Droda.node.type=worker -jar roda-core/roda-core/target/roda-core-2.0.0-SNAPSHOT-jar-with-dependencies.jar`
  * `java -cp roda-core/roda-core/target/roda-core-2.0.0-SNAPSHOT-jar-with-dependencies.jar -Droda.node.type=worker org.roda.core.RodaCoreFactory`

* Analyze RODA dependencies for version update

```bash
mvn versions:display-dependency-updates
```

## Run CLI on docker or using tomcat libs

```bash
java -cp "webapps/ROOT/WEB-INF/lib/*:lib/*" org.roda.core.RodaCoreFactory migrate model
```
