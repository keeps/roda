#!/bin/bash

. $RODA_HOME/bin/roda-common-setup.sh

ask_roda_home

ask_ldap_host
ask_ldap_users_dn
ask_ldap_groups_dn
ask_ldap_roles_dn

ask_roda_core_url
ask_roda_cas_url
ask_roda_cas_external_url

ask_cas_server_name
ask_cas_server_prefix


mv $RODA_HOME/webapps/roda-cas.war $RODA_HOME/webapps/cas.zip &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CAS" "Error installating RODA CAS (errorCode=${EX_FAILED_TO_INSTALL_CAS}_1)"
unzip -q $RODA_HOME/webapps/cas.zip -d $RODA_HOME/webapps/cas.war &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CAS" "Error installating RODA CAS (errorCode=${EX_FAILED_TO_INSTALL_CAS}_2)"

myEcho
myEcho "Copying config files from templates"
cp -v -f $RODA_HOME/config/templates/deployerConfigContext.xml $RODA_HOME/webapps/cas.war/WEB-INF/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CAS" "Error installing RODA CAS (errorCode=${EX_FAILED_TO_INSTALL_CAS}_3)"
cp -v -f $RODA_HOME/config/templates/cas.properties $RODA_HOME/webapps/cas.war/WEB-INF/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CAS" "Error installing RODA CAS (errorCode=${EX_FAILED_TO_INSTALL_CAS}_4)"


ant -f $RODA_HOME/bin/roda-cas-setup.xml deployerConfigContext.xml &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CAS" "Error installing RODA CAS (errorCode=${EX_FAILED_TO_INSTALL_CAS}_5)"
ant -f $RODA_HOME/bin/roda-cas-setup.xml cas.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CAS" "Error installing RODA CAS (errorCode=${EX_FAILED_TO_INSTALL_CAS}_6)"

if [ "$SERVLET_CONTAINER" = "tomcat6" ]; then
	ln -s $RODA_HOME/webapps/cas.war $SERVLET_CONTAINER_DEPLOY_DIR/cas &>> $INSTALL_LOG
elif [ "$SERVLET_CONTAINER" = "jboss4" ]; then
	ln -s $RODA_HOME/webapps/cas.war $SERVLET_CONTAINER_DEPLOY_DIR/cas.war &>> $INSTALL_LOG
fi
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CAS" "Error installating RODA CAS (errorCode=${EX_FAILED_TO_INSTALL_CAS}_7)"


myEcho
myEcho "* RODA CAS setup finished"
myEcho
