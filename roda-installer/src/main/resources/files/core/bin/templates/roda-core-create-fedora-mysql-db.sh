#!/bin/bash

scriptdir=`dirname "$0"`

RODA_HOME=$scriptdir/..

if [ "$RODADATA_MYSQL_RODACORE_USER" == "" ]; then
   read -p "RODA Data MySQL user (must have permissions to create users and databases) [root]: " RODADATA_MYSQL_RODACORE_USER
   if [ "$RODADATA_MYSQL_RODACORE_USER" == "" ]; then
      RODADATA_MYSQL_RODACORE_USER="root"
   fi
fi

if [ "$RODADATA_MYSQL_RODACORE_PASSWD" == "" ]; then
   read -s -p "RODA Data MySQL $RODADATA_MYSQL_RODACORE_USER password: " RODADATA_MYSQL_RODACORE_PASSWD
fi

echo "Creating Fedora DB"
mysql -h RODADATA_MYSQL_HOST -P RODADATA_MYSQL_PORT -u $RODADATA_MYSQL_RODACORE_USER -p$RODADATA_MYSQL_RODACORE_PASSWD < $RODA_HOME/config/sql/create-fedora-db.sql
