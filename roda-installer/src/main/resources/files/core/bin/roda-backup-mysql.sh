#!/bin/bash

scriptdir=`dirname "$0"`

# Set enviroment variables for RODA MySQL
. $scriptdir/set-roda-data-mysql-env.sh

DATE_YYMMDD=`date +"%F"`

#echo "MySQL root password"
#read -s PASSWORD
PASSWORD=froda

MYSQL_BACKUP_DIR=$RODA_BACKUP_DIR/$DATE_YYMMDD
mkdir -p $MYSQL_BACKUP_DIR
MYSQL_BACKUP_FILE=$MYSQL_BACKUP_DIR/`hostname`-mysqldump.sql

#$MYSQL_BIN_DIR/mysqladmin --defaults-file=$MYSQL_DEFAULTS_FILE $*
$MYSQL_BIN_DIR/mysqldump --defaults-file=$MYSQL_DEFAULTS_FILE -u root -p$PASSWORD --all-databases > $MYSQL_BACKUP_FILE

