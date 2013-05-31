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

ask_rodacore_host
ask_rodacore_port

ask_rodawui_mysql_host
ask_rodawui_mysql_port
ask_rodawui_mysql_user
ask_rodawui_mysql_user_passwd

echo
echo "Making bin scripts executable"
chmod +x $RODA_HOME/bin/*.sh

echo
echo "Copying config files from templates"
cp -f $RODA_HOME/config/templates/ldap-filter.properties $RODA_HOME/config/
cp -f $RODA_HOME/config/templates/roda-migrator.properties $RODA_HOME/config/

echo
echo "Configuring config files"
ant -q -f $RODA_HOME/bin/roda-migrator-setup.xml ldap-filter.properties > /dev/null
ant -q -f $RODA_HOME/bin/roda-migrator-setup.xml roda-migrator.properties > /dev/null

echo
echo "Linking WARs into JBoss deploy directory"
ln -s $RODA_HOME/webapps/roda-migrator.war $RODA_HOME/jboss/jboss-4.2.3.GA/server/default/deploy/

echo
echo "Creating cache directory"
mkdir -p $RODA_HOME/migrator/cache

if [ $? -eq 0 ]; then
	echo
	#echo "********************************************************************************"
	echo "* RODA Migrator setup finished"
	#echo "********************************************************************************"
	echo
else
	echo
	#echo "********************************************************************************"
	echo "* ERROR setting up RODA Migrator"
	#echo "********************************************************************************"
	echo
fi

