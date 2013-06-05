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

ask_roda_wui_passwd

ask_rodawui_mysql_host
ask_rodawui_mysql_port
ask_rodawui_mysql_user
ask_rodawui_mysql_user_passwd

ask_rodacore_host
ask_rodacore_port

ask_rodacore_public_hostname
ask_rodacore_public_port

ask_rodamigratorlinux_host
ask_rodamigratorlinux_port

ask_rodawui_host
ask_rodawui_public_hostname
ask_rodawui_public_port

### FIXME what to do with phpMyAdmin???
#myEcho
#myEcho "Copying bin scripts from templates"
#cp -v -f $RODA_HOME/bin/templates/roda-wui-create-phpMyAdmin-mysql-db.sh $RODA_HOME/bin/

### FIXME what to do with phpMyAdmin???
#myEcho
#myEcho "Configuring bin scripts"
#ant -f $RODA_HOME/bin/roda-ui-setup.xml roda-wui-create-phpMyAdmin-mysql-db.sh &>> $INSTALL_LOG

myEcho
myEcho "Making bin scripts executable"
chmod +x $RODA_HOME/bin/*.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_1)"

myEcho
myEcho "Copying config files from templates"
cp -v -f $RODA_HOME/config/templates/roda-wui.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_2)"
cp -v -f $RODA_HOME/config/templates/roda-in-installer.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_3)"
cp -v -f $RODA_HOME/config/templates/roda-in.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_4)"
cp -v -f $RODA_HOME/config/templates/ldap-filter.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_5)"

myEcho
myEcho "Configuring config files"
ant -f $RODA_HOME/bin/roda-ui-setup.xml ldap-filter.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_6)"
ant -f $RODA_HOME/bin/roda-ui-setup.xml roda-wui.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_7)"
ant -f $RODA_HOME/bin/roda-ui-setup.xml roda-in-installer.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_8)"
ant -f $RODA_HOME/bin/roda-ui-setup.xml roda-in.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_9)"

myEcho
myEcho "Copying config/mail files from templates"
cp -v -f $RODA_HOME/config/templates/mail/* $RODA_HOME/config/mail/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_10)"
myEcho
myEcho "Configuring config/mail files"
ant -f $RODA_HOME/bin/roda-ui-setup.xml velocity.mail.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_11)"

### FIXME what to do with phpMyAdmin???
#myEcho
#myEcho "Copying config/phpMyAdmin files from templates"
#cp -v -f $RODA_HOME/config/templates/phpMyAdmin/* $RODA_HOME/config/phpMyAdmin/
#myEcho
#myEcho "Configuring config/phpMyAdmin files"
#ant -f $RODA_HOME/bin/roda-ui-setup.xml config.inc.php &>> $INSTALL_LOG

### FIXME what to do with phpMyAdmin???
#myEcho
#myEcho "Copying config/sql files from templates"
#cp -v -f $RODA_HOME/config/templates/sql/create-phpMyAdmin-database.sql $RODA_HOME/config/sql/
#cp -v -f $RODA_HOME/config/templates/sql/create-phpMyAdmin-default-users.sql $RODA_HOME/config/sql/

### FIXME what to do with phpMyAdmin???
#myEcho
#myEcho "Configuring config/sql files"
#ant -f $RODA_HOME/bin/roda-ui-setup.xml create-phpMyAdmin-default-users.sql &>> $INSTALL_LOG

### FIXME what to do with phpMyAdmin???
#myEcho
#myEcho "Creating phpMyAdmin disseminator database (phpmyadmin) ..."
#$RODA_HOME/bin/roda-wui-create-phpMyAdmin-mysql-db.sh

#myEcho
#myEcho "Linking Launch4j platform dependent libraries"
#if $(uname -m | grep '64'); then
#	ln -s $RODA_HOME/in/launch4j/ld-64 $RODA_HOME/in/launch4j/ld
#	ln -s $RODA_HOME/in/launch4j/windres-64 $RODA_HOME/in/launch4j/windres
#else
#	ln -s $RODA_HOME/in/launch4j/ld-32 $RODA_HOME/in/launch4j/ld
#	ln -s $RODA_HOME/in/launch4j/windres-32 $RODA_HOME/in/launch4j/windres
#fi

myEcho
myEcho "Linking WARs into Servlet container deploy directory"
mv $RODA_HOME/webapps/roda-wui.war $RODA_HOME/webapps/roda-wui.zip &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_12)"
unzip -q $RODA_HOME/webapps/roda-wui.zip -d $RODA_HOME/webapps/roda-wui.war &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_13)"
if [ "$SERVLET_CONTAINER" = "tomcat6" ]; then
	ln -s $RODA_HOME/webapps/roda-wui.war $SERVLET_CONTAINER_DEPLOY_DIR/ROOT &>> $INSTALL_LOG
elif [ "$SERVLET_CONTAINER" = "jboss4" ]; then
	ln -s $RODA_HOME/webapps/roda-wui.war $SERVLET_CONTAINER_DEPLOY_DIR/ROOT.war &>> $INSTALL_LOG
fi
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_14)"
ln -s $RODA_HOME/webapps/roda-in-installer.war $SERVLET_CONTAINER_DEPLOY_DIR &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_UI" "Error installting RODA UI (errorCode=${EX_FAILED_TO_INSTALL_UI}_15)"

myEcho
myEcho "* RODA WUI setup finished"
myEcho
