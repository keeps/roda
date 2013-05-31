#!/bin/bash

cd $RODA_HOME/bin/ 

export RODA_HOME=$RODA_HOME

cp -f ./templates/set-roda-env.sh .
ant -q -f roda-setup.xml set-roda-env.sh > /dev/null

#echo
#echo "Downloading jboss"
#wget -q --output-document=/tmp/jboss-4.2.3.GA-jdk6.zip "http://sourceforge.net/projects/jboss/files/JBoss/JBoss-4.2.3.GA/jboss-4.2.3.GA-jdk6.zip/download"
##cp ../todelete/jboss-4.2.3.GA-jdk6.zip /tmp/jboss-4.2.3.GA-jdk6.zip
#echo
#echo "Unpacking jboss"
#unzip -o -q /tmp/jboss-4.2.3.GA-jdk6.zip -d /tmp
#cp -r /tmp/jboss-4.2.3.GA/* $RODA_HOME/jboss/jboss-4.2.3.GA/
#rm -rf /tmp/jboss-4.2.3.GA/
#rm /tmp/jboss-4.2.3.GA-jdk6.zip

echo
echo "* RODA Common setup finished"
echo
