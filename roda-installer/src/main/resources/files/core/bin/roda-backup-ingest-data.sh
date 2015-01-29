#!/bin/bash

scriptdir=`dirname "$0"`

# Set enviroment variables for RODA MySQL
. $scriptdir/set-roda-env.sh

DATE_YYMMDD=`date +"%F"`

INGEST_BACKUP_DIR=$RODA_BACKUP_DIR/$DATE_YYMMDD/ingest
mkdir -p $INGEST_BACKUP_DIR

cp -r $RODA_HOME/data/ingest/* $INGEST_BACKUP_DIR

