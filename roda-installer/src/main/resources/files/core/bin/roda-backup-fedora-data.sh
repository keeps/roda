#!/bin/bash

scriptdir=`dirname "$0"`

# Set enviroment variables for RODA MySQL
. $scriptdir/set-roda-env.sh

DATE_YYMMDD=`date +"%F"`

FEDORA_BACKUP_DIR=$RODA_BACKUP_DIR/$DATE_YYMMDD/fedora
mkdir -p $FEDORA_BACKUP_DIR

cp -r $RODA_HOME/core/data/fedora/objects $FEDORA_BACKUP_DIR
cp -r $RODA_HOME/core/data/fedora/gsearch $FEDORA_BACKUP_DIR
cp -r $RODA_HOME/core/data/fedora/resourceIndex $FEDORA_BACKUP_DIR
cp -r $RODA_HOME/core/data/fedora/riIndex $FEDORA_BACKUP_DIR

