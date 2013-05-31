#!/bin/bash

####################################################################
#
# Exit codes:
# 0 - Script run without problems
#
####################################################################

# Change dir to the script dir
SCRIPT_DIR=$(dirname $(readlink -m $0))
cd $SCRIPT_DIR

# Source common script functions
. files/common/bin/roda-common-setup.sh
# Source the installation config file
. install.config

if [ -d $RODA_HOME ]; then
	warn "The installation dir already exists ($RODA_HOME)!\nDo you want to procede? [yN]"
	read anwser
	case $anwser in
   	[yY])
	      ;;
   	*)
	      exit 1
   	   ;;
	esac
else
	mkdir $RODA_HOME
fi

info "Installing RODA Common"
# Copy common files to the installation dir
#cp -r files/todelete $RODA_HOME #for debug purpose (to avoid deleting the files)
cp -r files/common/* $RODA_HOME
. $RODA_HOME/bin/roda-setup.sh

if [ "$LDAP_RECONFIGURE_SLAPD" = "yes" ]; then
	# Reconfigure, non interactively, slapd in Debian machines
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

# Disables outputing the values of the environment variables on the roda-common-setup.sh script
QUIET=yes

cd $SCRIPT_DIR
info "Installing JBoss"
cp -r files/jboss $RODA_HOME
. $RODA_HOME/jboss/bin/jboss-setup.sh

# Test if it's to install RODA CORE
cd $SCRIPT_DIR
if [ "$INSTALL_CORE" = "yes" ] && [ -f files/core/bin/roda-core-setup.sh ]; then
	info "Installing RODA Core"
	cp -r files/core/* $RODA_HOME
	. $RODA_HOME/bin/roda-core-setup.sh
fi

# Test if it's to install RODA MIGRATOR
cd $SCRIPT_DIR
if [ "$INSTALL_MIGRATOR" = "yes" ] && [ -f files/migrator/bin/roda-migrator-setup.sh ]; then
	info "Installing RODA Migrator"
	cp -r files/migrator/* $RODA_HOME
	. $RODA_HOME/bin/roda-migrator-setup.sh
fi

# Test if it's to install RODA UI
cd $SCRIPT_DIR
if [ "$INSTALL_UI" = "yes" ] && [ -f files/ui/bin/roda-ui-setup.sh ]; then
	info "Installing RODA WUI"
	cp -r files/ui/* $RODA_HOME
	. $RODA_HOME/bin/roda-ui-setup.sh
fi

# Test if it's to install RODA HANDLE
cd $SCRIPT_DIR
if [ "$INSTALL_HANDLE" = "yes" ]; then
	info "Installing RODA Handle"
	cp -r files/handle/* $RODA_HOME
	echo
	echo "* RODA Handle setup finished"
	echo
fi

info "Done installing RODA!"

exit 0
