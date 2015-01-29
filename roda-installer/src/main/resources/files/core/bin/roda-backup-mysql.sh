#!/bin/bash

scriptdir=`dirname "$0"`

# Set enviroment variables for RODA MySQL
. $scriptdir/set-roda-env.sh
. $RODA_HOME/uninstall/install.config

DATE_YYMMDD=$(date +"%F")

MYSQL_BACKUP_DIR=$RODA_BACKUP_DIR/$DATE_YYMMDD/mysql
mkdir -p $MYSQL_BACKUP_DIR
MYSQL_BACKUP_FILE=$MYSQL_BACKUP_DIR/mysqldump.sql

mysqldump --defaults-file=$MYSQL_DEFAULTS_FILE -u $RODADATA_MYSQL_RODACORE_USER -p$RODADATA_MYSQL_RODACORE_PASSWD --databases $RODADATA_MYSQL_DB $FEDORA_DB > $MYSQL_BACKUP_FILE
