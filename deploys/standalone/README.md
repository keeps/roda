# Demonstration standalone deployment

RODA standalone deployment with a single-node Solr and Zookeeper services. It also includes additional support services for file format identification (Siegfried) and virus check (ClamAV). RODA is configured with a default index configuration of four (4) shards and one (1) replica per shard. This deployment is set to be run on http://localhost:8080, to change some alterations to the configuration will be required.

This is an example deployment and SHOULD NOT BE USED FOR PRODUCTION, due to security, performance and stability reasons. Please check [RODA Enterprise](https://www.roda-enterprise.com) for advice on how to go into production.

Requirements:
- Linux or macOS (Windows is not supported)
- Docker: [https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/)
- Docker Compose: [https://docs.docker.com/compose/install/](https://docs.docker.com/compose/install/)

```sh
# Download the docker compose
wget https://github.com/keeps/roda/raw/master/deploys/standalone/docker-compose.yaml

# Start services:
docker compose up -d
## This may take a couple of minutes to initialize, specially the first startup
## If it takes too long, check the logs

# Check the logs (CTRL+C to escape)
docker compose logs -f --tail=100
```

When the services load, they should be available on the Web browser:
* RODA will be at [http://localhost:8080](http://localhost:8080) (user: admin, password: roda)
* REST-API documentation will be at [http://localhost:8081](http://localhost:8081) (same passwords as in RODA)
* Indexing backend (Solr) will be at [http://localhost:8983](http://localhost:8983) (not protected, must be protected in production)

When finished, stop the services:
```sh
# Stop services:
docker compose down
```

# Need help?

If you have any issue, check the community issues and discussion on [Github](https://github.com/keeps/roda). If you need professional services, check [RODA Enterprise](https://www.roda-enterprise.com).
