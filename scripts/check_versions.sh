#!/bin/bash

SCRIPT_DIR=$(dirname $(readlink -f $0))

TYPE=${1:-MINOR}

case "$TYPE" in
  "MINOR")
    echo "Checking plugin and dependecies available minor updates"
    echo
    mvn versions:display-plugin-updates versions:display-dependency-updates \
        -DallowAnyUpdates=false  \
        -DallowMajorUpdates=false \
        -Dmaven.version.rules=file://$SCRIPT_DIR/versions-maven-rules.xml
    ;;
  "MAJOR")
    echo "Checking plugin and dependecies available major updates"
    echo
    mvn versions:display-plugin-updates versions:display-dependency-updates \
        -Dmaven.version.rules=file://$SCRIPT_DIR/versions-maven-rules.xml
    ;;
  *)
    echo "Syntax: $0 [MAJOR | MINOR]"
    echo "Default is MINOR"
    exit 1
    ;;
esac
