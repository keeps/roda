#!/bin/bash

scriptdir=`dirname "$0"`

# Set enviroment variables for RODA
. $scriptdir/set-roda-env.sh

DATE_YYMMDD=`date +"%F"`

mkdir -p $RODA_BACKUP_DIR/$DATE_YYMMDD
LDAP_BACKUP_FILE=$RODA_BACKUP_DIR/$DATE_YYMMDD/`hostname`.ldif

echo "Stopping OpenLDAP"
/etc/init.d/slapd stop

echo "/usr/sbin/slapcat -l $LDAP_BACKUP_FILE"
/usr/sbin/slapcat -l $LDAP_BACKUP_FILE

echo "Starting OpenLDAP"
/etc/init.d/slapd start

