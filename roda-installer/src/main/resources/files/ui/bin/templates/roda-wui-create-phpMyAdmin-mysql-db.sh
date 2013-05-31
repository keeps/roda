#!/bin/bash

scriptdir=`dirname "$0"`

RODA_HOME=$scriptdir/..

read -p "RODA WUI MySQL user (must have permissions to create users and databases) [root]: " DB_USER
if [ "$DB_USER" == "" ]; then
	DB_USER="root"
fi

read -s -p "RODA WUI MySQL $DB_USER password: " DB_PASSWORD

echo "Creating phpMyAdmin Database"
mysql -h RODAWUI_MYSQL_HOST -P RODAWUI_MYSQL_PORT -u $DB_USER -p$DB_PASSWORD < $RODA_HOME/config/sql/create-phpMyAdmin-database.sql
mysql -h RODAWUI_MYSQL_HOST -P RODAWUI_MYSQL_PORT -u $DB_USER -p$DB_PASSWORD < $RODA_HOME/config/sql/create-phpMyAdmin-default-users.sql

