#!/bin/bash

cd $RODA_HOME

export RODA_HOME=$RODA_HOME

myEcho
myEcho "Downloading FITS"
if [ -f /tmp/$FITS_ZIP ] && [ "$FITS_ZIP_MD5" = "$(md5sum /tmp/$FITS_ZIP | sed 's# .*$##')" ]; then
        myEcho
        myEcho "FITS already exists in /tmp and is valid (md5=\"$FITS_ZIP_MD5\")"
else
        wget --output-document=/tmp/$FITS_ZIP "$FITS_ZIP_URL" &>> $INSTALL_LOG
        testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing FITS (errorCode=${EX_FAILED_TO_INSTALL_FITS}_1)"
fi
unzip -q /tmp/$FITS_ZIP 
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_FITS" "Error installing FITS (errorCode=${EX_FAILED_TO_INSTALL_FITS}_2)"
mv ${FITS_ZIP%.zip} fits
cd fits
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_FITS" "Error installing FITS (errorCode=${EX_FAILED_TO_INSTALL_FITS}_3)"
myEcho
myEcho "FITS download finished"
myEcho
myEcho "Compiling FITS"
ant clean
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_FITS" "Error installing FITS (errorCode=${EX_FAILED_TO_INSTALL_FITS}_4)"
ant
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_FITS" "Error installing FITS (errorCode=${EX_FAILED_TO_INSTALL_FITS}_5)"
myEcho "FITS compiled"
myEcho
