#!/bin/bash

scriptdir=`dirname "$0"`

# Set enviroment variables for RODA MySQL
. $scriptdir/set-roda-env.sh

DATE_YYMMDD=$(date +"%F")

LOGS_BACKUP_DIR=$RODA_BACKUP_DIR/$DATE_YYMMDD/logs
mkdir -p $LOGS_BACKUP_DIR

rm -rf $LOGS_BACKUP_DIR/*

cp -rL $RODA_HOME/log $LOGS_BACKUP_DIR/roda

if [ -d $RODA_HOME/jboss/server/default/log ]; then
   mkdir -p $LOGS_BACKUP_DIR/jboss
   cp -rL $RODA_HOME/jboss/server/default/log $LOGS_BACKUP_DIR/jboss
fi

if [ -d $RODA_HOME/tomcat/apache-tomcat-*/logs ]; then
   mkdir -p $LOGS_BACKUP_DIR/tomcat
   cp -rL $RODA_HOME/tomcat/apache-tomcat-*/logs/* $LOGS_BACKUP_DIR/tomcat
fi

if [ -d $FEDORA_HOME/server/logs ]; then
   mkdir -p $LOGS_BACKUP_DIR/fedora
   cp $FEDORA_HOME/server/logs/* $LOGS_BACKUP_DIR/fedora
fi
