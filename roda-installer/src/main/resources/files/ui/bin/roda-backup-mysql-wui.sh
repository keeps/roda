#!/bin/bash

scriptdir=`dirname "$0"`

# Set enviroment variables for RODA MySQL
. $scriptdir/set-roda-mysql-env.sh


DIA=`date +"%w"`

echo "MySQL root password"
read -s PASSWORD

#$MYSQL_BIN_DIR/mysqladmin --defaults-file=$MYSQL_DEFAULTS_FILE $*
$MYSQL_BIN_DIR/mysqldump --defaults-file=$MYSQL_DEFAULTS_FILE -u root -p$PASSWORD --all-databases > $MYSQL_BACKUP_FILE

