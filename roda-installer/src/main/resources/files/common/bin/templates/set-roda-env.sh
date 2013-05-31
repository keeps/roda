#!/bin/bash

export RODA_HOME=RODAHOME
export RODA_BACKUP_DIR=$RODA_HOME/backup
export RODA_TEMP_DIR=$RODA_HOME/tmp
export FEDORA_HOME=$RODA_HOME/fedora/fedora-2.2.4

export PS1="\[\033[1;41m\]\[\033[1;37m\]RODA\[\033[0m\] $PS1"

if [ ! -z $DEBUG ]; then
	echo "*************** RODA ENVIROMENT ***************"
	echo "RODA_HOME       = $RODA_HOME"
	echo "RODA_BACKUP_DIR = $RODA_BACKUP_DIR"
	echo "RODA_TEMP_DIR   = $RODA_TEMP_DIR"
	echo "FEDORA_HOME     = $FEDORA_HOME"
	echo "***********************************************"
fi

