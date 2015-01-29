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
	
ask_roda_guest_passwd
ask_roda_admin_passwd
ask_roda_wui_passwd
ask_roda_ingest_task_passwd
ask_roda_preservation_task_passwd
ask_roda_handle_passwd

ask_roda_core_url
ask_roda_cas_url
ask_roda_cas_external_url

ask_fedora_db
ask_fedora_admin_passwd

ask_rodadata_mysql_db	
ask_rodadata_mysql_host
ask_rodadata_mysql_port
ask_rodadata_mysql_rodacore_passwd

ask_rodadata_host
ask_rodadata_port

ask_rodamigratorlinux_host
ask_rodamigratorlinux_port

FEDORA_VERSION="2.2.4"
FEDORA_DIRNAME="fedora-$FEDORA_VERSION"
FEDORA_JAR="$FEDORA_DIRNAME-installer.jar"
FEDORA_JAR_MD5="a7b85a546be4224a93252c7bc13dd048"
FEDORA_JAR_URL="http://sourceforge.net/projects/fedora-commons/files/fedora/$FEDORA_VERSION/$FEDORA_JAR/download"
myEcho
myEcho "Downloading fedora"
if [ -f "/tmp/$FEDORA_JAR" ] && [ "$FEDORA_JAR_MD5" = "$(md5sum /tmp/$FEDORA_JAR | sed 's# .*$##')" ]; then
	myEcho
	myEcho "Fedora already exists in /tmp and is valid (md5=\"$FEDORA_JAR_MD5\")"
else
	wget --output-document=/tmp/$FEDORA_JAR "$FEDORA_JAR_URL" &>> $INSTALL_LOG
	testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_1)"
fi
cp /tmp/$FEDORA_JAR $RODA_HOME/fedora/$FEDORA_JAR &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_2)"
myEcho
myEcho "Configuring fedora installation properties"
ant -f $RODA_HOME/bin/roda-core-setup.xml fedora-$FEDORA_VERSION-install.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_3)"
myEcho
myEcho "Installing fedora"
java -jar $RODA_HOME/fedora/$FEDORA_JAR $RODA_HOME/fedora/fedora-$FEDORA_VERSION-install.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_4)"
## hacks (to allow a successful deployment in jboss and others)
mv $RODA_HOME/fedora/$FEDORA_DIRNAME/install/fedorawar/WEB-INF/lib/jsf-api.jar $RODA_HOME/fedora/$FEDORA_DIRNAME/install/fedorawar/WEB-INF/lib/jsf-api.jar.bak &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_5)"
mv $RODA_HOME/fedora/$FEDORA_DIRNAME/install/fedorawar/WEB-INF/lib/jsf-impl.jar $RODA_HOME/fedora/$FEDORA_DIRNAME/install/fedorawar/WEB-INF/lib/jsf-impl.jar.bak &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_6)"
rm $RODA_HOME/fedora/$FEDORA_DIRNAME/install/fedora.war &>> $INSTALL_LOG
ln -s $RODA_HOME/fedora/$FEDORA_DIRNAME/install/fedorawar $RODA_HOME/fedora/$FEDORA_DIRNAME/install/fedora &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_7)"
cp -v $RODA_HOME/fedora/extra-files/beSecurity.xml $RODA_HOME/fedora/$FEDORA_DIRNAME/server/config/beSecurity.xml &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_8)"
mkdir -p $RODA_HOME/fedora/$FEDORA_DIRNAME/server/fedora-internal-use/fedora-internal-use-backend-service-policies &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_9)"
cp -v $RODA_HOME/fedora/extra-files/callback*.xml $RODA_HOME/fedora/$FEDORA_DIRNAME/server/fedora-internal-use/fedora-internal-use-backend-service-policies/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_10)"

if [ "$SERVLET_CONTAINER" = "tomcat6" ]; then
	for i in $(ls $RODA_HOME/fedora/$FEDORA_DIRNAME/client/lib/*.jar | egrep -v "(xml|xerces)");
	do 
		cp $i "$SERVLET_CONTAINER_LIB_DIR/fedora_$(basename $i)"
	done
	testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_1x)"
fi

GSEARCH_VERSION="2.1.1"
GSEARCH_DIRNAME="genericsearch-$GSEARCH_VERSION"
GSEARCH_ZIP="$GSEARCH_DIRNAME.zip"
GSEARCH_ZIP_MD5="904fee63c01f52a745cd75c39ce94a62"
GSEARCH_ZIP_URL="http://sourceforge.net/projects/fedora-commons/files/services/3.0/$GSEARCH_ZIP/download"
myEcho
myEcho "Downloading genericsearch"
if [ -f "/tmp/$GSEARCH_ZIP" ] && [ "$GSEARCH_ZIP_MD5" = "$(md5sum /tmp/$GSEARCH_ZIP | sed 's# .*$##')" ]; then
	myEcho
	myEcho "Fedora GSearch already exists in /tmp and is valid (md5=\"$GSEARCH_ZIP_MD5\")"
else
	wget --output-document=/tmp/$GSEARCH_ZIP "$GSEARCH_ZIP_URL" &>> $INSTALL_LOG
	testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_11)"
fi
myEcho
myEcho "Unpacking genericsearch"
unzip -o -q /tmp/$GSEARCH_ZIP -d /tmp &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_12)"
unzip -o -q /tmp/$GSEARCH_DIRNAME/fedoragsearch.war -d /tmp/$GSEARCH_DIRNAME/fedoragsearchwar &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_13)"
cp -v -r /tmp/$GSEARCH_DIRNAME/fedoragsearchwar/* $RODA_HOME/fedora/$GSEARCH_DIRNAME/fedoragsearchwar/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_14)"
rm -rf /tmp/$GSEARCH_DIRNAME/ &>> $INSTALL_LOG

myEcho
myEcho "Copying bin scripts from templates"
cp -v -f $RODA_HOME/bin/templates/roda-ldapadd $RODA_HOME/bin/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_15)"
cp -v -f $RODA_HOME/bin/templates/roda-ldapmodify $RODA_HOME/bin/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_16)"
cp -v -f $RODA_HOME/bin/templates/roda-core-create-fedora-mysql-db.sh $RODA_HOME/bin/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_17)"
cp -v -f $RODA_HOME/bin/templates/roda-core-create-roda-mysql-db.sh $RODA_HOME/bin/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_18)"
cp -v -f $RODA_HOME/bin/templates/roda-data-create-gsearch-index.sh $RODA_HOME/bin/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_19)"
cp -v -f $RODA_HOME/bin/templates/roda-data-update-gsearch-index.sh $RODA_HOME/bin/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_20)"

myEcho
myEcho "Configuring bin scripts"
ant -f $RODA_HOME/bin/roda-core-setup.xml roda-ldapadd &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_21)"
ant -f $RODA_HOME/bin/roda-core-setup.xml roda-ldapmodify &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_22)"
ant -f $RODA_HOME/bin/roda-core-setup.xml roda-core-create-fedora-mysql-db.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_23)"
ant -f $RODA_HOME/bin/roda-core-setup.xml roda-core-create-roda-mysql-db.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_24)"
ant -f $RODA_HOME/bin/roda-core-setup.xml roda-data-create-gsearch-index.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_25)"
ant -f $RODA_HOME/bin/roda-core-setup.xml roda-data-update-gsearch-index.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_26)"

myEcho
myEcho "Making bin scripts executable"
chmod +x $RODA_HOME/bin/*.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_27)"
chmod +x $RODA_HOME/bin/roda-ldapadd &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_28)"
chmod +x $RODA_HOME/bin/roda-ldapmodify &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_29)"

myEcho
myEcho "Copying config files from templates"
cp -v -f $RODA_HOME/config/templates/cas-filter.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_30)"
cp -v -f $RODA_HOME/config/templates/roda-core.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_31)"
cp -v -f $RODA_HOME/config/templates/logger.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_32)"
cp -v -f $RODA_HOME/config/templates/ingest.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_33)"
cp -v -f $RODA_HOME/config/templates/reports.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_34)"
cp -v -f $RODA_HOME/config/templates/statistics.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_35)"
cp -v -f $RODA_HOME/config/templates/scheduler.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_36)"
cp -v -f $RODA_HOME/config/templates/quartz.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_37)"
cp -v -f $RODA_HOME/config/templates/plugins.properties $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_38)"
cp -v -f $RODA_HOME/config/templates/roda-vsftpd.conf $RODA_HOME/config/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_39)"

myEcho
myEcho "Configuring config files"
ant -f $RODA_HOME/bin/roda-core-setup.xml cas-filter.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_40)"
ant -f $RODA_HOME/bin/roda-core-setup.xml roda-core.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_41)"
ant -f $RODA_HOME/bin/roda-core-setup.xml logger.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_42)"
ant -f $RODA_HOME/bin/roda-core-setup.xml ingest.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_43)"
ant -f $RODA_HOME/bin/roda-core-setup.xml reports.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_44)"
ant -f $RODA_HOME/bin/roda-core-setup.xml statistics.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_45)"
ant -f $RODA_HOME/bin/roda-core-setup.xml scheduler.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_46)"
ant -f $RODA_HOME/bin/roda-core-setup.xml quartz.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_47)"
ant -f $RODA_HOME/bin/roda-core-setup.xml plugins.properties &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_48)"
ant -f $RODA_HOME/bin/roda-core-setup.xml roda-vsftpd.conf &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_49)"

myEcho
myEcho "Copying config/fedora files from templates"
ln -s $RODA_HOME/fedora/$FEDORA_DIRNAME/server/config/ $RODA_HOME/config/fedora &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_50)"
cp -v -f $RODA_HOME/config/templates/fedora/* $RODA_HOME/config/fedora/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_51)"
myEcho
myEcho "Configuring config/fedora files"
ant -f $RODA_HOME/bin/roda-core-setup.xml fedora.fcfg &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_52)"
ant -f $RODA_HOME/bin/roda-core-setup.xml fedora-users.xml &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_53)"

myEcho
myEcho "Copying config/ldap files from templates"
mkdir -p $RODA_HOME/config/ldap/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_54)"
cp -v -f $RODA_HOME/config/templates/ldap/* $RODA_HOME/config/ldap/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_55)"
myEcho
myEcho "Configuring config/ldap files"
ant -f $RODA_HOME/bin/roda-core-setup.xml create-parent-users-groups-roles.ldif &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_56)"
ant -f $RODA_HOME/bin/roda-core-setup.xml create-default-values.ldif &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_57)"
ant -f $RODA_HOME/bin/roda-core-setup.xml delete-default-values.ldif &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_58)"
ant -f $RODA_HOME/bin/roda-core-setup.xml create-demo-users.ldif &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_59)"
ant -f $RODA_HOME/bin/roda-core-setup.xml delete-demo-users.ldif &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_60)"

myEcho
myEcho "Copying config/sql files from templates"
cp -v -f $RODA_HOME/config/templates/sql/create-fedora-db.sql $RODA_HOME/config/sql/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_61)"
cp -v -f $RODA_HOME/config/templates/sql/create-roda-db.sql $RODA_HOME/config/sql/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_62)"
myEcho
myEcho "Configuring config/sql files"
ant -f $RODA_HOME/bin/roda-core-setup.xml create-fedora-db.sql &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_63)"
ant -f $RODA_HOME/bin/roda-core-setup.xml create-roda-db.sql &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_64)"

myEcho
myEcho "Copying config/plugins files from templates"
cp -v -f $RODA_HOME/config/templates/plugins/* $RODA_HOME/config/plugins/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_65)"
myEcho
myEcho "Configuring config/plugins files"
ant -f $RODA_HOME/bin/roda-core-setup.xml core-plugins &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_66)"

myEcho
myEcho "Copying roda-filter to fedora application"
cp -v -rf $RODA_HOME/fedora/roda-filter/* $RODA_HOME/fedora/$FEDORA_DIRNAME/install/fedorawar/WEB-INF/lib &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_67)"
myEcho
myEcho "Copying fedora web.xml from templates"
cp -v -f $RODA_HOME/config/templates/fedora/web.xml $RODA_HOME/fedora/$FEDORA_DIRNAME/install/fedorawar/WEB-INF/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_68)"
myEcho
myEcho "Configuring fedora web.xml"
ant -f $RODA_HOME/bin/roda-core-setup.xml fedora-web.xml &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_69)"

myEcho
myEcho "Copying fedoragsearch log4.xml from templates"
cp -v -f $RODA_HOME/config/templates/fedoragsearch/log4j.xml $RODA_HOME/fedora/$GSEARCH_DIRNAME/fedoragsearchwar/WEB-INF/classes &>> $INSTALL_LOG
FEDORA_HOME=$RODA_HOME/fedora/$FEDORA_DIRNAME RODACORE_HOSTPORT="$RODADATA_HOST:$RODADATA_PORT" ant -q -f $RODA_HOME/bin/configure-gsearch.xml -Ddeployed.config.path=$RODA_HOME/fedora/$GSEARCH_DIRNAME/fedoragsearchwar/WEB-INF/classes &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_70)"

myEcho
myEcho "Creating Fedora database ($FEDORA_DB)..."
$RODA_HOME/bin/roda-core-create-fedora-mysql-db.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_71)"
myEcho
myEcho "Creating RODA database (roda)..."
$RODA_HOME/bin/roda-core-create-roda-mysql-db.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_72)"
myEcho
myEcho "Creating LDAP parent entries users/groups/roles..."
$RODA_HOME/bin/roda-ldapadd -c -f $RODA_HOME/config/ldap/create-parent-users-groups-roles.ldif &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_73)"
myEcho
myEcho "Creating LDAP entries default users/groups/roles..."
$RODA_HOME/bin/roda-ldapadd -c -f $RODA_HOME/config/ldap/create-default-values.ldif &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_74)"

myEcho
myEcho "Linking WARs into Servlet container deploy directory"
if [ "$SERVLET_CONTAINER" = "tomcat6" ]; then
	ln -s $RODA_HOME/fedora/$GSEARCH_DIRNAME/fedoragsearchwar/ $SERVLET_CONTAINER_DEPLOY_DIR/fedoragsearch &>> $INSTALL_LOG
elif [ "$SERVLET_CONTAINER" = "jboss4" ]; then
	ln -s $RODA_HOME/fedora/$GSEARCH_DIRNAME/fedoragsearchwar/ $SERVLET_CONTAINER_DEPLOY_DIR/fedoragsearch.war &>> $INSTALL_LOG
fi
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_75)"
if [ "$SERVLET_CONTAINER" = "tomcat6" ]; then
	ln -s $RODA_HOME/fedora/$FEDORA_DIRNAME/install/fedorawar/ $SERVLET_CONTAINER_DEPLOY_DIR/fedora &>> $INSTALL_LOG
elif [ "$SERVLET_CONTAINER" = "jboss4" ]; then
	ln -s $RODA_HOME/fedora/$FEDORA_DIRNAME/install/fedorawar/ $SERVLET_CONTAINER_DEPLOY_DIR/fedora.war &>> $INSTALL_LOG
fi
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_76)"
ln -s $RODA_HOME/webapps/roda-core.war $SERVLET_CONTAINER_DEPLOY_DIR &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_77)"

myEcho
myEcho "Creating needed folders"
mkdir -p $RODA_HOME/data/ingest/FTP_DROP_DIR/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_CORE" "Error installing RODA CORE (errorCode=${EX_FAILED_TO_INSTALL_CORE}_78)"

myEcho
myEcho "* RODA Core setup finished"
myEcho
