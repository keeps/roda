#!/bin/sh

scriptdir=`dirname "$0"`

# Set enviroment variables for Fedora
. $scriptdir/set-roda-env.sh

handle_dir=$RODA_HOME/handle.net

CLASSPATH=$handle_dir/bin/handle.jar

for jar in $handle_dir/bin/roda-storage/*
do
	CLASSPATH=$CLASSPATH:$jar
done

echo
echo "Starting Handle Server"
java -cp $CLASSPATH net.handle.server.Main $handle_dir/data &

