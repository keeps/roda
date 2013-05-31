#!/bin/bash

#scriptdir=`dirname "$0"`

#export RODA_HOME=$(readlink -f $scriptdir/..)

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
	
ask_roda_guest_passwd
ask_roda_admin_passwd
ask_roda_wui_passwd
ask_roda_ingest_task_passwd
ask_roda_preservation_task_passwd
ask_roda_handle_passwd

ask_fedora_db
ask_fedora_admin_passwd
	
ask_rodadata_mysql_host
ask_rodadata_mysql_port
ask_rodadata_mysql_rodacore_passwd

ask_rodadata_host
ask_rodadata_port

ask_rodamigratorlinux_host
ask_rodamigratorlinux_port

echo
echo "Downloading fedora"
wget -q --output-document=$RODA_HOME/fedora/fedora-2.2.4-installer.jar "http://sourceforge.net/projects/fedora-commons/files/fedora/2.2.4/fedora-2.2.4-installer.jar/download"
#cp $RODA_HOME/todelete/fedora-2.2.4-installer.jar $RODA_HOME/fedora/fedora-2.2.4-installer.jar
echo
echo "Configuring fedora installation properties"
ant -q -f $RODA_HOME/bin/roda-core-setup.xml fedora-2.2.4-install.properties > /dev/null
echo
echo "Installing fedora"
java -jar $RODA_HOME/fedora/fedora-2.2.4-installer.jar $RODA_HOME/fedora/fedora-2.2.4-install.properties > /dev/null
## hacks (to allow a successful deployment in jboss and others)
mv $RODA_HOME/fedora/fedora-2.2.4/install/fedorawar/WEB-INF/lib/jsf-api.jar $RODA_HOME/fedora/fedora-2.2.4/install/fedorawar/WEB-INF/lib/jsf-api.jar.bak
mv $RODA_HOME/fedora/fedora-2.2.4/install/fedorawar/WEB-INF/lib/jsf-impl.jar $RODA_HOME/fedora/fedora-2.2.4/install/fedorawar/WEB-INF/lib/jsf-impl.jar.bak
rm $RODA_HOME/fedora/fedora-2.2.4/install/fedora.war
ln -s $RODA_HOME/fedora/fedora-2.2.4/install/fedorawar $RODA_HOME/fedora/fedora-2.2.4/install/fedora
cp $RODA_HOME/fedora/extra-files/beSecurity.xml $RODA_HOME/fedora/fedora-2.2.4/server/config/beSecurity.xml
mkdir -p $RODA_HOME/fedora/fedora-2.2.4/server/fedora-internal-use/fedora-internal-use-backend-service-policies
cp $RODA_HOME/fedora/extra-files/callback*.xml $RODA_HOME/fedora/fedora-2.2.4/server/fedora-internal-use/fedora-internal-use-backend-service-policies/

echo
echo "Downloading genericsearch"
wget -q --output-document=/tmp/genericsearch-2.1.1.zip "http://sourceforge.net/projects/fedora-commons/files/services/3.0/genericsearch-2.1.1.zip/download"
#cp $RODA_HOME/todelete/genericsearch-2.1.1.zip /tmp/genericsearch-2.1.1.zip
echo
echo "Unpacking genericsearch"
unzip -o -q /tmp/genericsearch-2.1.1.zip -d /tmp
unzip -o -q /tmp/genericsearch-2.1.1/fedoragsearch.war -d /tmp/genericsearch-2.1.1/fedoragsearchwar
cp -r /tmp/genericsearch-2.1.1/fedoragsearchwar/* $RODA_HOME/fedora/genericsearch-2.1.1/fedoragsearchwar/
rm -rf /tmp/genericsearch-2.1.1/
rm /tmp/genericsearch-2.1.1.zip

echo
echo "Copying bin scripts from templates"
cp -f $RODA_HOME/bin/templates/roda-ldapadd $RODA_HOME/bin/
cp -f $RODA_HOME/bin/templates/roda-ldapmodify $RODA_HOME/bin/
cp -f $RODA_HOME/bin/templates/roda-core-create-fedora-mysql-db.sh $RODA_HOME/bin/
cp -f $RODA_HOME/bin/templates/roda-core-create-roda-mysql-db.sh $RODA_HOME/bin/
cp -f $RODA_HOME/bin/templates/roda-data-create-gsearch-index.sh $RODA_HOME/bin/
cp -f $RODA_HOME/bin/templates/roda-data-update-gsearch-index.sh $RODA_HOME/bin/

echo
echo "Configuring bin scripts"
ant -q -f $RODA_HOME/bin/roda-core-setup.xml roda-ldapadd > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml roda-ldapmodify > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml roda-core-create-fedora-mysql-db.sh > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml roda-core-create-roda-mysql-db.sh > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml roda-data-create-gsearch-index.sh > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml roda-data-update-gsearch-index.sh > /dev/null

echo
echo "Making bin scripts executable"
chmod +x $RODA_HOME/bin/*.sh
chmod +x $RODA_HOME/bin/roda-ldapadd
chmod +x $RODA_HOME/bin/roda-ldapmodify

echo
echo "Copying config files from templates"
cp -f $RODA_HOME/config/templates/ldap-filter.properties $RODA_HOME/config/
cp -f $RODA_HOME/config/templates/roda-core.properties $RODA_HOME/config/
cp -f $RODA_HOME/config/templates/logger.properties $RODA_HOME/config/
cp -f $RODA_HOME/config/templates/ingest.properties $RODA_HOME/config/
cp -f $RODA_HOME/config/templates/reports.properties $RODA_HOME/config/
cp -f $RODA_HOME/config/templates/statistics.properties $RODA_HOME/config/
cp -f $RODA_HOME/config/templates/scheduler.properties $RODA_HOME/config/
cp -f $RODA_HOME/config/templates/quartz.properties $RODA_HOME/config/
cp -f $RODA_HOME/config/templates/plugins.properties $RODA_HOME/config/
cp -f $RODA_HOME/config/templates/roda-vsftpd.conf $RODA_HOME/config/

echo
echo "Configuring config files"
ant -q -f $RODA_HOME/bin/roda-core-setup.xml ldap-filter.properties > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml roda-core.properties > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml logger.properties > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml ingest.properties > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml reports.properties > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml statistics.properties > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml scheduler.properties > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml quartz.properties > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml plugins.properties > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml roda-vsftpd.conf > /dev/null

echo
echo "Copying config/fedora files from templates"
ln -s $RODA_HOME/fedora/fedora-2.2.4/server/config/ $RODA_HOME/config/fedora
cp -f $RODA_HOME/config/templates/fedora/* $RODA_HOME/config/fedora/
echo
echo "Configuring config/fedora files"
ant -q -f $RODA_HOME/bin/roda-core-setup.xml fedora.fcfg > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml fedora-users.xml > /dev/null

echo
echo "Copying config/ldap files from templates"
cp -f $RODA_HOME/config/templates/ldap/* $RODA_HOME/config/ldap/
echo
echo "Configuring config/ldap files"
ant -q -f $RODA_HOME/bin/roda-core-setup.xml create-parent-users-groups-roles.ldif > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml create-default-values.ldif > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml delete-default-values.ldif > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml create-demo-users.ldif > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml delete-demo-users.ldif > /dev/null

echo
echo "Copying config/sql files from templates"
cp -f $RODA_HOME/config/templates/sql/create-fedora-db.sql $RODA_HOME/config/sql/
cp -f $RODA_HOME/config/templates/sql/create-roda-db.sql $RODA_HOME/config/sql/
echo
echo "Configuring config/sql files"
ant -q -f $RODA_HOME/bin/roda-core-setup.xml create-fedora-db.sql > /dev/null
ant -q -f $RODA_HOME/bin/roda-core-setup.xml create-roda-db.sql > /dev/null

echo
echo "Copying config/plugins files from templates"
cp -f $RODA_HOME/config/templates/plugins/* $RODA_HOME/config/plugins/
echo
echo "Configuring config/plugins files"
ant -q -f $RODA_HOME/bin/roda-core-setup.xml core-plugins > /dev/null

echo
echo "Copying roda-filter to fedora application"
cp -rf $RODA_HOME/fedora/roda-filter/* $RODA_HOME/fedora/fedora-2.2.4/install/fedorawar/WEB-INF/lib
echo
echo "Copying fedora web.xml from templates"
cp -f $RODA_HOME/config/templates/fedora/web.xml $RODA_HOME/fedora/fedora-2.2.4/install/fedorawar/WEB-INF/
echo
echo "Configuring fedora web.xml"
ant -q -f $RODA_HOME/bin/roda-core-setup.xml fedora-web.xml > /dev/null

echo
echo "Copying fedoragsearch log4.xml from templates"
cp -f $RODA_HOME/config/templates/fedoragsearch/log4j.xml $RODA_HOME/fedora/genericsearch-2.1.1/fedoragsearchwar/WEB-INF/classes
FEDORA_HOME=$RODA_HOME/fedora/fedora-2.2.4 RODACORE_HOSTPORT="$RODADATA_HOST:$RODADATA_PORT" ant -q -f $RODA_HOME/bin/configure-gsearch.xml -Ddeployed.config.path=$RODA_HOME/fedora/genericsearch-2.1.1/fedoragsearchwar/WEB-INF/classes > /dev/null

echo
echo "Creating Fedora database ($FEDORA_DB) ..."
$RODA_HOME/bin/roda-core-create-fedora-mysql-db.sh > /dev/null
echo
echo "Creating RODA database (roda) ..."
$RODA_HOME/bin/roda-core-create-roda-mysql-db.sh > /dev/null
echo
echo -n "Creating LDAP parent entries users/groups/roles ..."
$RODA_HOME/bin/roda-ldapadd -c -f $RODA_HOME/config/ldap/create-parent-users-groups-roles.ldif > /dev/null && echo "OK" || echo "FAILED!!!"
echo
echo -n "Creating LDAP entries default users/groups/roles ..."
$RODA_HOME/bin/roda-ldapadd -c -f $RODA_HOME/config/ldap/create-default-values.ldif > /dev/null && echo "OK" || echo "FAILED!!!"

echo
echo "Linking WARs into JBoss deploy directory"
ln -s $RODA_HOME/fedora/genericsearch-2.1.1/fedoragsearchwar/ $RODA_HOME/jboss/jboss-4.2.3.GA/server/default/deploy/fedoragsearch.war
ln -s $RODA_HOME/fedora/fedora-2.2.4/install/fedorawar/ $RODA_HOME/jboss/jboss-4.2.3.GA/server/default/deploy/fedora.war
ln -s $RODA_HOME/webapps/roda-core.war $RODA_HOME/jboss/jboss-4.2.3.GA/server/default/deploy/

if [ $? -eq 0 ]; then
	echo
	#echo "********************************************************************************"
	echo "* RODA Core setup finished"
	#echo "********************************************************************************"
	echo
else
	echo
	#echo "********************************************************************************"
	echo "* ERROR setting up RODA Core"
	#echo "********************************************************************************"
	echo
fi

