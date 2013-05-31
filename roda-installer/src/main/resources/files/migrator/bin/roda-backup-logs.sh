#!/bin/bash

scriptdir=`dirname "$0"`

# Set enviroment variables for RODA MySQL
. $scriptdir/set-roda-env.sh

# Secure copy local backup folder to remote backup
BACKUP_USER=roda
BACKUP_HOST=roda-data-bak
BACKUP_HOST_DEFAULT=roda-data

DATE_YYMMDD=`date +"%F"`

LOGS_BACKUP_DIR=$RODA_BACKUP_DIR/$DATE_YYMMDD/logs/`hostname`
mkdir -p $LOGS_BACKUP_DIR

rm -rf $LOGS_BACKUP_DIR/*

cp -rL $RODA_HOME/logs $LOGS_BACKUP_DIR/roda
cp -rL $RODA_HOME/jboss/server/default/log $LOGS_BACKUP_DIR/jboss

if [ -d $RODA_HOME/core/data/mysql ]; then
	mkdir -p $LOGS_BACKUP_DIR/mysql
	cp $RODA_HOME/core/data/mysql/*.err $LOGS_BACKUP_DIR/mysql
fi

if [ -d $FEDORA_HOME/server/logs ]; then
	mkdir -p $LOGS_BACKUP_DIR/fedora
	cp $FEDORA_HOME/server/logs/* $LOGS_BACKUP_DIR/fedora
fi

chown $BACKUP_USER $RODA_BACKUP_DIR/* -R
chmod u+rwX $RODA_BACKUP_DIR/* -R

if [ $HOSTNAME != $BACKUP_HOST_DEFAULT ]
then
	sudo -u $BACKUP_USER scp -rqv $RODA_BACKUP_DIR/* $BACKUP_USER@$BACKUP_HOST:$RODA_BACKUP_DIR
	rm -rf $RODA_BACKUP_DIR/*
fi

