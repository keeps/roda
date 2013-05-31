#!/bin/bash

scriptdir=`dirname "$0"`

# Set enviroment variables for RODA MySQL
. $scriptdir/set-roda-env.sh

DATE_YYMMDD=`date +"%F"`

INGEST_BACKUP_DIR=$RODA_BACKUP_DIR/$DATE_YYMMDD/ingest

BACKUP_USER=roda
BACKUP_HOST=roda-data-bak
BACKUP_HOST_DEFAULT=roda-data

# Create the destination directory
sudo -u $BACKUP_USER rsh $BACKUP_USER@$BACKUP_HOST mkdir -p $INGEST_BACKUP_DIR
# Copy the ingest content files to destination
sudo -u $BACKUP_USER scp -rqv $RODA_HOME/core/data/ingest/* $BACKUP_USER@$BACKUP_HOST:$INGEST_BACKUP_DIR

