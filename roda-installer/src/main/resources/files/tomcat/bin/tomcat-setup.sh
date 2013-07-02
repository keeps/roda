#!/bin/bash

cd $RODA_HOME/tomcat/bin/

export RODA_HOME=$RODA_HOME

cp -v -f ./templates/tomcat.config . &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_1)"
ant -f tomcat-setup.xml tomcat-setup.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_2)"

myEcho
myEcho "Downloading Tomcat"
if [ -f /tmp/$TOMCAT_ZIP ] && [ "$TOMCAT_ZIP_MD5" = "$(md5sum /tmp/$TOMCAT_ZIP | sed 's# .*$##')" ]; then
	myEcho
	myEcho "Tomcat already exists in /tmp and is valid (md5=\"$TOMCAT_ZIP_MD5\")"
else
	wget --output-document=/tmp/$TOMCAT_ZIP "$TOMCAT_ZIP_URL" &>> $INSTALL_LOG
	testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_3)"
fi
myEcho

myEcho "Unpacking Tomcat"
unzip -o -q /tmp/$TOMCAT_ZIP -d /tmp &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_4)"
rm -rf /tmp/$TOMCAT_DIRNAME/webapps/* &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_5)"
chmod +x /tmp/$TOMCAT_DIRNAME/bin/*.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_6)"
cp -v -r /tmp/$TOMCAT_DIRNAME $RODA_HOME/tomcat/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_7)"
rm -rf /tmp/$TOMCAT_DIRNAME/ &>> $INSTALL_LOG

myEcho
myEcho "Linking Tomcat start/stop script to RODA bin"
chmod +x $RODA_HOME/tomcat/bin/tomcat &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_8)"
ln -s $RODA_HOME/tomcat/bin/tomcat $RODA_HOME/bin/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_9)"

myEcho
myEcho "* Tomcat setup finished"
myEcho
