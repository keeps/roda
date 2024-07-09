#!/usr/bin/env bash

set -e

# Skip compilation
SKIP_COMPILATION=${1:-false}

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
VERSION=$(mvn -f "$PROJECT_DIR"/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)

if [ "$SKIP_COMPILATION" = false ] || { [ "$SKIP_COMPILATION" = true ] && [ ! -d "$SCRIPT_DIR"/target ]; }; then
  mvn -f "$PROJECT_DIR"/pom.xml clean package -DskipTests
  # Clean up target folder
  rm -rf "$SCRIPT_DIR"/target/*
  mkdir -p "$SCRIPT_DIR"/target
fi

# Copy target
cp -r "$PROJECT_DIR"/roda-ui/roda-wui/target/roda-wui-"$VERSION".war "$SCRIPT_DIR"/target/

docker build -t keeps/roda:latest -t keeps/roda:"$VERSION" "$SCRIPT_DIR"
