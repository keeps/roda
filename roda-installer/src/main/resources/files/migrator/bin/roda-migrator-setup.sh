#!/bin/bash

. $RODA_HOME/bin/roda-common-setup.sh

ask_roda_home

ask_ldap_host
ask_ldap_port
ask_ldap_admin_dn
ask_ldap_admin_pass
ask_ldap_users_dn
ask_ldap_groups_dn
ask_ldap_roles_dn

ask_rodacore_host
ask_rodacore_port

ask_rodawui_mysql_host
ask_rodawui_mysql_port
ask_rodawui_mysql_user
ask_rodawui_mysql_user_passwd

myEcho
myEcho "Making bin scripts executable"
chmod +x $RODA_HOME/bin/*.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_MIGRATOR" "Error installing RODA MIGRATOR (errorCode=${EX_FAILED_TO_INSTALL_MIGRATOR}_1)"

myEcho
myEcho "Copying config files from templates"
cp -v -f $RODA_HOME/config/templates/ldap-filter.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_MIGRATOR" "Error installing RODA MIGRATOR (errorCode=${EX_FAILED_TO_INSTALL_MIGRATOR}_2)"
cp -v -f $RODA_HOME/config/templates/roda-migrator.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_MIGRATOR" "Error installing RODA MIGRATOR (errorCode=${EX_FAILED_TO_INSTALL_MIGRATOR}_3)"

myEcho
myEcho "Configuring config files"
ant -f $RODA_HOME/bin/roda-migrator-setup.xml ldap-filter.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_MIGRATOR" "Error installing RODA MIGRATOR (errorCode=${EX_FAILED_TO_INSTALL_MIGRATOR}_4)"
ant -f $RODA_HOME/bin/roda-migrator-setup.xml roda-migrator.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_MIGRATOR" "Error installing RODA MIGRATOR (errorCode=${EX_FAILED_TO_INSTALL_MIGRATOR}_5)"

myEcho
myEcho "Linking WARs into Servlet container deploy directory"
ln -s $RODA_HOME/webapps/roda-migrator.war $SERVLET_CONTAINER_DEPLOY_DIR &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_MIGRATOR" "Error installing RODA MIGRATOR (errorCode=${EX_FAILED_TO_INSTALL_MIGRATOR}_6)"

myEcho
myEcho "Creating cache directory"
mkdir -p $RODA_HOME/migrator/cache &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_MIGRATOR" "Error installing RODA MIGRATOR (errorCode=${EX_FAILED_TO_INSTALL_MIGRATOR}_7)"

myEcho
myEcho "* RODA Migrator setup finished"
myEcho
