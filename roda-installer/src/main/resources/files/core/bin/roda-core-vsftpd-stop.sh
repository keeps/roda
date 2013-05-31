#!/bin/sh

scriptdir=`dirname "$0"`

# Set enviroment variables for Fedora
#. $scriptdir/set-roda-env.sh

echo
echo "Stoping VSFTPD server"

PID=`ps aux | grep vsftpd | grep -v grep | awk '{print $2}'`
kill -TERM $PID

