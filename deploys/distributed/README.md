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

## Central environment
RODA central instance that will control all the local RODA instances in a synchronized way.

### Debug WUI

```bash
# If never GWT compiled before, compile once and copy gwt.rpc files
mvn -pl roda-ui/roda-wui -am gwt:compile -Pdebug-main -Dscope.gwt-dev=compile
./roda-ui/roda-wui/copy_gwt_rpc.sh

# Start up dependencies (Solr, Zookeeper, Siegfried, ClamAV)
mkdir -p $HOME/.roda_central/data/storage
docker compose -f deploys/distributed/central/docker-compose-dev.yaml up -d


# Open WUI in Spring boot
mvn -pl roda-ui/roda-wui -am spring-boot:run -Pdebug-main-central

# Open codeserver
mvn -pl roda-ui/roda-wui -am gwt:codeserver -Pdebug-main -Dscope.gwt-dev=compile

# Open codeserver http://127.0.0.1:9876/ and add bookmarks
# Open RODA http://localhost:8080 and click the "Dev Mode On" bookmark

```

## Local environment
RODA local instance whose processes will be synchronized with a RODA central instance.

### Debug WUI

```bash
# If never GWT compiled before, compile once and copy gwt.rpc files
mvn -pl roda-ui/roda-wui -am gwt:compile -Pdebug-main -Dscope.gwt-dev=compile
./roda-ui/roda-wui/copy_gwt_rpc.sh

# Start up dependencies (Solr, Zookeeper, Siegfried, ClamAV)
mkdir -p $HOME/.roda_local/data/storage
docker compose -f deploys/distributed/local/docker-compose-dev.yaml up -d

# Open WUI in Spring boot
mvn -pl roda-ui/roda-wui -am spring-boot:run -Pdebug-main-local

# Open codeserver
mvn -pl roda-ui/roda-wui -am gwt:codeserver -Pdebug-main -Dscope.gwt-dev=compile

# Open codeserver http://127.0.0.1:9876/ and add bookmarks
# Open RODA http://localhost:8080 and click the "Dev Mode On" bookmark

```