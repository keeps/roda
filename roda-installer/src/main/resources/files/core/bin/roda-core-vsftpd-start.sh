#!/bin/sh

scriptdir=`dirname "$0"`

# Set enviroment variables for RODA
. $scriptdir/set-roda-env.sh

if [ ! -d /var/run/vsftpd ]; then
	mkdir /var/run/vsftpd
fi

echo
echo "Starting VSFTPD server"

vsftpd $RODA_HOME/core/config/roda-vsftpd.conf &

