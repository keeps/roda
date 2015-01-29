#!/bin/bash

cd $RODA_HOME/bin/ 

export RODA_HOME=$RODA_HOME

cp -v -f ./templates/set-roda-env.sh . &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_COMMON" "Error installing RODA Common files (errorCode=${EX_FAILED_TO_INSTALL_COMMON}_1)"
ant -f roda-setup.xml set-roda-env.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_COMMON" "Error installing RODA Common files (errorCode=${EX_FAILED_TO_INSTALL_COMMON}_2)"

myEcho
myEcho "* RODA Common setup finished"
myEcho
