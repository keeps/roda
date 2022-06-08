# Standalone example deployment

RODA standalone deployment with a single-node Solr and Zookeeper services. Also includes support services for file format identification (Siegfried) and virus check (ClamAV). RODA is configured with remote debug enabled, and has a default index configuration of four (4) shards and one (1) replica per shard.

This is an example deployment and SHOULD NOT BE USED FOR PRODUCTION, due to security, performance and stability reasons. Please contact sales@keep.pt for advice on how to go into production.

Requirements:
- Linux or macOS (Windows not supported)
- Docker: https://docs.docker.com/get-docker/
- Docker Compose: https://docs.docker.com/compose/install/

Start services:
```sh
docker compose up -d
```

Stop services:
```sh
docker compose down
```


## Development

To support the development of RODA there is an additional docker compose that runs all support services so RODA can be run locally using dev-mode.

Start support services:
```sh
# Start support services
docker compose -f docker-compose-dev.yaml up
```

Start RODA on dev-mode (on RODA base folder):
```sh
# If never GWT compiled before, compile once and copy gwt.rpc files
mvn -pl roda-ui/roda-wui -am gwt:compile -Pdebug-main -Dscope.gwt-dev=compile
cd roda-ui/roda-wui
./copy_gwt_rpc.sh
cd -

# Open WUI in Spring boot (one terminal)
mvn -pl roda-ui/roda-wui -am spring-boot:run -Pdebug-main

# Open codeserver (another terminal)
mvn -pl roda-ui/roda-wui -am gwt:codeserver -Pdebug-main -Dscope.gwt-dev=compile

# Open codeserver http://127.0.0.1:9876/ and add bookmarks
# Open RODA http://localhost:8080 and click the "Dev Mode On" bookmark
```

Follow logs on:
```
tail -f $HOME/.roda/log/roda-core.log
```