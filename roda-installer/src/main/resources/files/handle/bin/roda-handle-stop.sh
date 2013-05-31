#!/bin/sh

scriptdir=`dirname "$0"`

# Set enviroment variables for Fedora
#. $scriptdir/set-roda-env.sh

echo
echo "Stoping Handle Server"

PID=`ps aux | grep net.handle.server.Main | grep -v grep | awk '{print $2}'`
kill -TERM $PID

