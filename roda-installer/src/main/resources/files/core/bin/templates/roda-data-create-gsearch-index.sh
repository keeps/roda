#!/bin/bash

scriptdir=`dirname "$0"`

# Set enviroment variables for RODA
. $scriptdir/set-roda-env.sh

echo "*********************************************************************"
echo "* This script will delete the current Fedora GSearch (Lucene) index *"
echo "* and create an empty one.                                          *"
echo "*********************************************************************"
echo
echo "Are you sure you want to continue?"
echo "(yes) to continue; (no) or Ctrl+C to abort"
read sure

if [ $sure = "yes" ] ; then
	echo "Creating empty Fedora GSearch (Lucene) index"
	cd $RODA_HOME/webapps/fedoragsearch.war/client
	chmod +x runSOAPClient.sh
	./runSOAPClient.sh RODADATA_HOST:RODADATA_PORT updateIndex createEmpty
else
	echo "Operation aborted! No changed made to Lucene index"
fi

