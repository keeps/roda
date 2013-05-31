#!/bin/bash

cd $RODA_HOME/jboss/bin/

export RODA_HOME=$RODA_HOME

cp -f ./templates/jboss.config .
ant -q -f jboss-setup.xml jboss-setup.sh > /dev/null

echo
echo "Downloading JBoss"
wget -q --output-document=/tmp/jboss-4.2.3.GA-jdk6.zip "http://sourceforge.net/projects/jboss/files/JBoss/JBoss-4.2.3.GA/jboss-4.2.3.GA-jdk6.zip/download"
#cp ../todelete/jboss-4.2.3.GA-jdk6.zip /tmp/jboss-4.2.3.GA-jdk6.zip
echo
echo "Unpacking JBoss"
unzip -o -q /tmp/jboss-4.2.3.GA-jdk6.zip -d /tmp
cp -r /tmp/jboss-4.2.3.GA/* $RODA_HOME/jboss/jboss-4.2.3.GA/
rm -rf /tmp/jboss-4.2.3.GA/
rm /tmp/jboss-4.2.3.GA-jdk6.zip

echo
echo "Linking JBoss start/stop script to RODA bin"
chmod +x $RODA_HOME/jboss/bin/jboss
ln -s $RODA_HOME/jboss/bin/jboss $RODA_HOME/bin/

echo
echo "* JBoss setup finished"
echo
