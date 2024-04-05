#!/usr/bin/env bash

set -e

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
VERSION=$(mvn -f "$PROJECT_DIR"/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)

mvn -f "$PROJECT_DIR"/pom.xml clean package -DskipTests

# Clean up target folder
rm -rf "$SCRIPT_DIR"/target/*

# Create target directory
mkdir -p "$SCRIPT_DIR"/target

# Copy target
cp -r "$PROJECT_DIR"/roda-ui/roda-wui/target/roda-wui-"$VERSION"/* "$SCRIPT_DIR"/target/

# Build docker image
docker build -t keeps/roda:latest -t keeps/roda:"$VERSION" .
