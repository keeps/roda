#!/bin/bash

# run extension scripts
DIR=/docker-entrypoint.d

if [[ -d "$DIR" ]]
then
  /bin/run-parts --regex '^.*$' --verbose "$DIR"
fi

if [[ $# -eq 0 ]] ; then
    RODA_JAR=$(ls -t /KEEPS/bin/roda-wui-*.jar | head -1)
    echo "Starting RODA with JAR: $RODA_JAR (user: $(whoami))"
    exec java -jar "$RODA_JAR"
fi

exec "$@"
