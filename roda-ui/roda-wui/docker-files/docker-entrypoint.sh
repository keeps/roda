#!/bin/bash

# Either use the LOCAL_USER_ID and LOCAL_GROUP_ID
# if passed in at runtime or fallback

USER_ID=${LOCAL_USER_ID:-9001}
GROUP_ID=${LOCAL_GROUP_ID:-9001}

# Adding roda user and group
echo "Starting with UID : $USER_ID and GUID : $GROUP_ID"
groupadd -g $GROUP_ID roda
useradd  --shell /bin/bash --no-log-init -u $USER_ID -o -c "RODA" -g roda roda

# Fixing permissions
chown -R roda:roda /usr/local/tomcat
mkdir -p /roda && chown roda:roda /roda

# run extension scripts
DIR=/docker-entrypoint.d

if [[ -d "$DIR" ]]
then
  /bin/run-parts --regex '^.*$' --verbose "$DIR"
fi

if [[ $# -eq 0 ]] ; then
    echo 'Starting Apache Tomcat (user: roda)'
    exec gosu roda catalina.sh run
fi

exec gosu roda $@
