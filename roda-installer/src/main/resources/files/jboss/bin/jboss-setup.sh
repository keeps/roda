#!/bin/bash

cd $RODA_HOME/jboss/bin/

export RODA_HOME=$RODA_HOME

cp -v -f ./templates/jboss.config . &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_JBOSS" "Error installting JBoss (errorCode=${EX_FAILED_TO_INSTALL_JBOSS}_1)"
ant -f jboss-setup.xml jboss-setup.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_JBOSS" "Error installting JBoss (errorCode=${EX_FAILED_TO_INSTALL_JBOSS}_2)"

JBOSS_ZIP="jboss-4.2.3.GA-jdk6.zip"
JBOSS_ZIP_MD5="e548d9e369589f8b9be0abc642c19842"
JBOSS_ZIP_URL="http://sourceforge.net/projects/jboss/files/JBoss/JBoss-4.2.3.GA/jboss-4.2.3.GA-jdk6.zip/download"
myEcho
myEcho "Downloading JBoss"
if [ -f /tmp/$JBOSS_ZIP ] && [ "$JBOSS_ZIP_MD5" = "$(md5sum /tmp/$JBOSS_ZIP | sed 's# .*$##')" ]; then
	myEcho "JBoss already exists in /tmp and is valid (md5=\"$JBOSS_ZIP_MD5\")"
else
	wget --output-document=/tmp/$JBOSS_ZIP "$JBOSS_ZIP_URL" &>> $INSTALL_LOG
	testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_JBOSS" "Error installting JBoss (errorCode=${EX_FAILED_TO_INSTALL_JBOSS}_3)"
fi
myEcho

myEcho "Unpacking JBoss"
unzip -o -q /tmp/$JBOSS_ZIP -d /tmp &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_JBOSS" "Error installting JBoss (errorCode=${EX_FAILED_TO_INSTALL_JBOSS}_4)"
cp -v -r /tmp/jboss-4.2.3.GA/* $RODA_HOME/jboss/jboss-4.2.3.GA/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_JBOSS" "Error installting JBoss (errorCode=${EX_FAILED_TO_INSTALL_JBOSS}_5)"
rm -rf /tmp/jboss-4.2.3.GA/ &>> $INSTALL_LOG

myEcho
myEcho "Linking JBoss start/stop script to RODA bin"
chmod +x $RODA_HOME/jboss/bin/jboss &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_JBOSS" "Error installting JBoss (errorCode=${EX_FAILED_TO_INSTALL_JBOSS}_6)"
ln -s $RODA_HOME/jboss/bin/jboss $RODA_HOME/bin/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_JBOSS" "Error installting JBoss (errorCode=${EX_FAILED_TO_INSTALL_JBOSS}_7)"

myEcho
myEcho "* JBoss setup finished"
myEcho
