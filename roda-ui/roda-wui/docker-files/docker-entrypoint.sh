#!/bin/bash

# run extension scripts
DIR=/docker-entrypoint.d

if [[ -d "$DIR" ]]
then
  /bin/run-parts --regex '^.*$' --verbose "$DIR"
fi

if [[ $# -eq 0 ]] ; then
    echo 'Starting Apache Tomcat (user: roda)'
    exec catalina.sh run
fi

exec "$@"
