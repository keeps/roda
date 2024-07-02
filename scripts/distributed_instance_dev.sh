#! /bin/bash

# Version
STATUS=${1:-'up'}
SCRIPT_DIR=$(dirname "$(readlink -f "$0")")
RODA_PROJECT_DIR=$(readlink -f "${SCRIPT_DIR}/..")

function up() {
  echo "Starting distributed instance"
  mkdir -p "$HOME/.roda_central/data/storage"
  mkdir -p "$HOME/.roda_local/data/storage"

  docker compose -f "$RODA_PROJECT_DIR/deploys/distributed/central/docker-compose-dev.yaml" up -d
  docker compose -f "$RODA_PROJECT_DIR/deploys/distributed/local/docker-compose-dev.yaml" up -d

  echo "Run 'mvn -pl roda-ui/roda-wui -am spring-boot:run -Pdebug-main-central' to start the central instance"
  echo "Open another terminal and run 'mvn -pl roda-ui/roda-wui -am spring-boot:run -Pdebug-main-local' to start the local instance"
}

function down() {
  echo "Stopping distributed instance"
  docker compose -f "$RODA_PROJECT_DIR/deploys/distributed/central/docker-compose-dev.yaml" down -v
  docker compose -f "$RODA_PROJECT_DIR/deploys/distributed/local/docker-compose-dev.yaml" down -v
}

function clean() {
  echo "Cleaning distributed instance environment"
  down
  rm -rf "$HOME/.roda_central/"
  rm -rf "$HOME/.roda_local/"
}

if [ "$STATUS" == 'up' ]; then
  up
elif [ "$STATUS" == 'down' ]; then
  down
elif [ "$STATUS" == 'clean' ]; then
  clean
else
  echo "Invalid status: $STATUS"
  echo "Syntax: $0 [up|down|clean]"
  exit 1
fi

exit 0
