#! /bin/bash

scriptdir=`dirname "$0"`

. $scriptdir/set-roda-env.sh

rm -rf $RODA_HOME/ui/disseminators/cache/*

