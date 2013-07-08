#!/bin/bash

################################################################################
# RODA installation script (http://www.roda-community.org)
#
# Last update : 2013-06-05
# Updated by  : hsilva@keep.pt
#
# Exit codes  :
EX_OK=0                         # RODA installer script run smoothly
EX_RODA_HOME_ALREADY_EXISTS=1   # RODA_HOME already exists
EX_FAILED_TO_INSTALL_COMMON=2   # Failed to install RODA Common files
EX_FAILED_TO_INSTALL_JBOSS=3    # Failed to install JBoss
EX_FAILED_TO_INSTALL_TOMCAT=4   # Failed to install Tomcat 
EX_FAILED_TO_INSTALL_CORE=5     # Failed to install RODA CORE
EX_FAILED_TO_INSTALL_MIGRATOR=6 # Failed to install RODA MIGRATOR
EX_FAILED_TO_INSTALL_UI=7       # Failed to install RODA UI
EX_FAILED_TO_INSTALL_HANDLE=8   # Failed to install RODA HANDLE
#
################################################################################

# Change dir to the script dir
SCRIPT_DIR=$(dirname $(readlink -m $0))
cd $SCRIPT_DIR
INSTALL_LOG="$SCRIPT_DIR/install.log"
if [ -f $INSTALL_LOG ]; then
	rm $INSTALL_LOG
fi


################################################################################
# Source needed files
################################################################################
# Source common script functions
. files/common/bin/roda-common-setup.sh
# Source the installation config file
. install.config


################################################################################
# Test whether RODA_HOME already exists so we don't override
# already existing files/dirs by accident
################################################################################
if [ -d $RODA_HOME ]; then
	warn "The installation dir already exists ($RODA_HOME)!\nDo you want to procede? [yN]"
	read anwser
	case $anwser in
   	[yY])
	      ;;
   	*)
	      exit $EX_RODA_HOME_ALREADY_EXISTS
   	   ;;
	esac
else
	mkdir $RODA_HOME
fi


################################################################################
# Install RODA Common files
################################################################################
info "Installing RODA Common"
# Copy common files to the installation dir
cp -v -r files/common/* $RODA_HOME &>> $INSTALL_LOG
# Also copy install.config to be able to correctly uninstall RODA
cp -v install.config $RODA_HOME/uninstall/ &>> $INSTALL_LOG
. $RODA_HOME/bin/roda-setup.sh


################################################################################
# Optional: reconfigure, non interactively, slapd in Debian machines
################################################################################
if [ "$LDAP_RECONFIGURE_SLAPD" = "yes" ]; then
	touch /tmp/slapd_conf
	chmod 600 /tmp/slapd_conf
	cat > /tmp/slapd_conf <<-EOF
	slapd	slapd/no_configuration	boolean	false
	slapd	slapd/domain	string	$LDAP_DOMAIN
	slapd	shared/organization	string	My LDAP
	slapd	slapd/backend	select	HDB
	slapd	slapd/purge_database	boolean	true
	slapd	slapd/move_old_database	boolean	true
	slapd	slapd/password1	password	$LDAP_ADMIN_PASSWD
	slapd	slapd/password2	password	$LDAP_ADMIN_PASSWD
	slapd	slapd/allow_ldap_v2	boolean	false
	EOF
	sudo debconf-set-selections /tmp/slapd_conf
	sudo dpkg-reconfigure --frontend=noninteractive slapd &> /dev/null
	rm /tmp/slapd_conf
fi


# Disable environment variables output 
# from roda-common-setup.sh script
QUIET=yes


################################################################################
# Install Servlet Container (Tomcat or JBoss)
################################################################################
cd $SCRIPT_DIR
if [ "$SERVLET_CONTAINER" = "tomcat6" ]; then
	TOMCAT_VERSION="6.0.37"
	TOMCAT_DIRNAME="apache-tomcat-$TOMCAT_VERSION"
	export TOMCAT_DIRNAME
	TOMCAT_ZIP="$TOMCAT_DIRNAME.zip"
	TOMCAT_ZIP_MD5="2cdccdb521196932fcf2bc26b60c388b"
	TOMCAT_ZIP_URL="http://www.eu.apache.org/dist/tomcat/tomcat-${TOMCAT_VERSION%%.*}/v$TOMCAT_VERSION/bin/$TOMCAT_ZIP"
	info "Installing Tomcat"
	cp -v -r files/tomcat $RODA_HOME &>> $INSTALL_LOG
	. $RODA_HOME/tomcat/bin/tomcat-setup.sh
	SERVLET_CONTAINER_DEPLOY_DIR=$RODA_HOME/tomcat/$TOMCAT_DIRNAME/webapps
	SERVLET_CONTAINER_LIB_DIR=$RODA_HOME/tomcat/$TOMCAT_DIRNAME/lib
elif [ "$SERVLET_CONTAINER" = "jboss4" ]; then
	JBOSS_VERSION="4.2.3"
	JBOSS_DIRNAME="jboss-$JBOSS_VERSION.GA-jdk6"
	JBOSS_ZIP="$JBOSS_DIRNAME.zip"
	JBOSS_ZIP_MD5="e548d9e369589f8b9be0abc642c19842"
	JBOSS_ZIP_URL="http://sourceforge.net/projects/jboss/files/JBoss/JBoss-${JBOSS_VERSION}.GA/${JBOSS_ZIP}/download"
	info "Installing JBoss"
	cp -v -r files/jboss $RODA_HOME &>> $INSTALL_LOG
	. $RODA_HOME/jboss/bin/jboss-setup.sh
	SERVLET_CONTAINER_DEPLOY_DIR=$RODA_HOME/jboss/$JBOSS_DIRNAME/server/default/deploy
fi


################################################################################
# Install RODA CORE?
################################################################################
cd $SCRIPT_DIR
if [ "$INSTALL_CORE" = "yes" ] && [ -f files/core/bin/roda-core-setup.sh ]; then
	info "Installing RODA Core"
	cp -v -r files/core/* $RODA_HOME &>> $INSTALL_LOG
	. $RODA_HOME/bin/roda-core-setup.sh
fi


################################################################################
# Install RODA MIGRATOR?
################################################################################
cd $SCRIPT_DIR
if [ "$INSTALL_MIGRATOR" = "yes" ] && [ -f files/migrator/bin/roda-migrator-setup.sh ]; then
	info "Installing RODA Migrator"
	cp -v -r files/migrator/* $RODA_HOME &>> $INSTALL_LOG
	. $RODA_HOME/bin/roda-migrator-setup.sh
fi


################################################################################
# Install RODA UI?
################################################################################
cd $SCRIPT_DIR
if [ "$INSTALL_UI" = "yes" ] && [ -f files/ui/bin/roda-ui-setup.sh ]; then
	info "Installing RODA WUI"
	cp -v -r files/ui/* $RODA_HOME &>> $INSTALL_LOG
	. $RODA_HOME/bin/roda-ui-setup.sh
fi


################################################################################
# Install RODA HANDLE?
################################################################################
cd $SCRIPT_DIR
if [ "$INSTALL_HANDLE" = "yes" ]; then
	info "Installing RODA Handle"
	cp -v -r files/handle/* $RODA_HOME &>> $INSTALL_LOG
	testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_HANDLE" \
		"Error installting RODA HANDLE (errorCode=${EX_FAILED_TO_INSTALL_HANDLE}_1)"
	myEcho
	myEcho "* RODA Handle setup finished"
	myEcho
fi

END_MSG="***************************************************************************\n"
END_MSG="$END_MSG*\n"
if [ "$SERVLET_CONTAINER" = "tomcat6" ]; then
	END_MSG="$END_MSG* Now, you just need to start Tomcat\n"
	END_MSG="$END_MSG*\n"
	END_MSG="$END_MSG*\t${RODA_HOME%/}/bin/tomcat start\n"
elif [ "$SERVLET_CONTAINER" = "jboss4" ]; then
	END_MSG="$END_MSG* Now, you just need to start JBoss\n"
	END_MSG="$END_MSG*\n"
	END_MSG="$END_MSG*\t${RODA_HOME%/}/bin/jboss start\n"
fi
END_MSG="$END_MSG*\n"
END_MSG="$END_MSG* and create Fedora Generic Search index by pointing out your browser to\n"
END_MSG="$END_MSG*\n"
END_MSG="$END_MSG*\thttp://$RODACORE_HOST:$RODACORE_PORT/fedoragsearch/rest?operation=updateIndex&action=createEmpty\n"
END_MSG="$END_MSG*\n"
END_MSG="$END_MSG* and you're done\n"
END_MSG="$END_MSG*\n"
END_MSG="$END_MSG*\n"
END_MSG="$END_MSG* Finally, point your browser to RODA Web Interface (RODA-WUI)\n"
END_MSG="$END_MSG*\n"
END_MSG="$END_MSG*\thttp://$RODAWUI_PUBLIC_HOSTNAME:$RODAWUI_PUBLIC_PORT/\n"
END_MSG="$END_MSG*\n"
END_MSG="$END_MSG*\n***************************************************************************\n"

info "Done installing RODA!\n\n$END_MSG"

exit $EX_OK
