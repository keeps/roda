#!/bin/bash

MAXIMUM_HEAP_SIZE=256m
JAVA_EXEC="$JAVA_HOME/bin/java"

cd "$(dirname $0)"

"$JAVA_EXEC" -Djavax.net.ssl.trustStore=$INSTALL_PATH/config/rodaTruststore.ts -Xmx$MAXIMUM_HEAP_SIZE -jar $INSTALL_PATH/roda-in.jar 
