#!/bin/bash

#scriptdir=`dirname "$0"`

#export RODA_HOME=`readlink -f $scriptdir/..`

#echo "RODA_HOME=$RODA_HOME"

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
#echo
#echo "Copying bin scripts from templates"
#cp -f $RODA_HOME/bin/templates/roda-wui-create-phpMyAdmin-mysql-db.sh $RODA_HOME/bin/

### FIXME what to do with phpMyAdmin???
#echo
#echo "Configuring bin scripts"
#ant -q -f $RODA_HOME/bin/roda-ui-setup.xml roda-wui-create-phpMyAdmin-mysql-db.sh > /dev/null

echo
echo "Making bin scripts executable"
chmod +x $RODA_HOME/bin/*.sh

echo
echo "Copying config files from templates"
cp -f $RODA_HOME/config/templates/roda-wui.properties $RODA_HOME/config/
cp -f $RODA_HOME/config/templates/roda-in-installer.properties $RODA_HOME/config/
cp -f $RODA_HOME/config/templates/roda-in.properties $RODA_HOME/config/
cp -f $RODA_HOME/config/templates/ldap-filter.properties $RODA_HOME/config/

echo
echo "Configuring config files"
ant -q -f $RODA_HOME/bin/roda-ui-setup.xml ldap-filter.properties > /dev/null
ant -q -f $RODA_HOME/bin/roda-ui-setup.xml roda-wui.properties > /dev/null
ant -q -f $RODA_HOME/bin/roda-ui-setup.xml roda-in-installer.properties > /dev/null
ant -q -f $RODA_HOME/bin/roda-ui-setup.xml roda-in.properties > /dev/null

echo
echo "Copying config/mail files from templates"
cp -f $RODA_HOME/config/templates/mail/* $RODA_HOME/config/mail/
echo
echo "Configuring config/mail files"
ant -q -f $RODA_HOME/bin/roda-ui-setup.xml velocity.mail.properties > /dev/null

### FIXME what to do with phpMyAdmin???
#echo
#echo "Copying config/phpMyAdmin files from templates"
#cp -f $RODA_HOME/config/templates/phpMyAdmin/* $RODA_HOME/config/phpMyAdmin/
#echo
#echo "Configuring config/phpMyAdmin files"
#ant -q -f $RODA_HOME/bin/roda-ui-setup.xml config.inc.php > /dev/null

### FIXME what to do with phpMyAdmin???
#echo
#echo "Copying config/sql files from templates"
#cp -f $RODA_HOME/config/templates/sql/create-phpMyAdmin-database.sql $RODA_HOME/config/sql/
#cp -f $RODA_HOME/config/templates/sql/create-phpMyAdmin-default-users.sql $RODA_HOME/config/sql/

### FIXME what to do with phpMyAdmin???
#echo
#echo "Configuring config/sql files"
#ant -q -f $RODA_HOME/bin/roda-ui-setup.xml create-phpMyAdmin-default-users.sql > /dev/null

### FIXME what to do with phpMyAdmin???
#echo
#echo "Creating phpMyAdmin disseminator database (phpmyadmin) ..."
#$RODA_HOME/bin/roda-wui-create-phpMyAdmin-mysql-db.sh

#echo
#echo "Linking Launch4j platform dependent libraries"
#if $(uname -m | grep '64'); then
#	ln -s $RODA_HOME/in/launch4j/ld-64 $RODA_HOME/in/launch4j/ld
#	ln -s $RODA_HOME/in/launch4j/windres-64 $RODA_HOME/in/launch4j/windres
#else
#	ln -s $RODA_HOME/in/launch4j/ld-32 $RODA_HOME/in/launch4j/ld
#	ln -s $RODA_HOME/in/launch4j/windres-32 $RODA_HOME/in/launch4j/windres
#fi

echo
echo "Linking WARs into JBoss deploy directory"
mv $RODA_HOME/webapps/roda-wui.war $RODA_HOME/webapps/roda-wui.zip
unzip -q $RODA_HOME/webapps/roda-wui.zip -d $RODA_HOME/webapps/roda-wui.war
ln -s $RODA_HOME/webapps/roda-wui.war $RODA_HOME/jboss/jboss-4.2.3.GA/server/default/deploy/ROOT.war
ln -s $RODA_HOME/webapps/roda-in-installer.war $RODA_HOME/jboss/jboss-4.2.3.GA/server/default/deploy/roda-in-installer.war

if [ $? -eq 0 ]; then
	echo
	#echo "********************************************************************************"
	echo "* RODA WUI setup finished"
	#echo "********************************************************************************"
	echo
else
	echo
	#echo "********************************************************************************"
	echo "* ERROR setting up RODA WUI"
	#echo "********************************************************************************"
	echo
fi

