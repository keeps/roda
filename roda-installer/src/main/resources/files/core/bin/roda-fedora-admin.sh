#!/bin/sh

scriptdir=`dirname "$0"`

# Set enviroment variables for RODA & Fedora
. $scriptdir/set-roda-env.sh

# Override default java 1.5
JAVA_HOME=/usr/lib/jvm/java-6-sun 

$FEDORA_HOME/client/bin/fedora-admin.sh $*

