#!/bin/bash

function error(){
   echo -e "[$(date --rfc-3339=seconds)][ERROR] $1" | tee -a $INSTALL_LOG
}

function warn(){
   echo -e "[$(date --rfc-3339=seconds)][WARN ] $1" | tee -a $INSTALL_LOG
}

function info(){
   echo -e "[$(date --rfc-3339=seconds)][INFO ] $1" | tee -a $INSTALL_LOG
}

function usage(){
   info "$1"
}

function myEcho(){
	echo "$@" | tee -a $INSTALL_LOG
}

function testExecutionAndExitOnFailure(){
	exitCode=$1
	codeToExitWith=$2
	if [ "$exitCode" -ne "0" ]; then
		exit $codeToExitWith
	fi
}

function testExecutionAndExitWithMsgOnFailure(){
	exitCode=$1
	codeToExitWith=$2
	msg=$3
	if [ "$exitCode" -ne "0" ]; then
		error "$msg"
		exit $codeToExitWith
	fi
}

function ask_roda_home() {
	if [ "$RODA_HOME" == "" ]; then
		read -p "RODA home directory [/usr/local/roda]: " RODA_HOME
		if [ "$RODA_HOME" == "" ]; then
			RODA_HOME="/usr/local/roda"
		fi
		export RODA_HOME=$RODA_HOME
	else
		if [ ! "$QUIET" ]; then
			echo "RODA_HOME already set to $RODA_HOME"
		fi
	fi
}

function ask_ldap_host() {
	if [ "$LDAPHOST" == "" ]; then
		read -p "LDAP host [localhost]: " LDAPHOST
		if [ "$LDAPHOST" == "" ]; then
			LDAPHOST="localhost"
		fi
		export LDAPHOST=$LDAPHOST
	else
		if [ ! "$QUIET" ]; then
			echo "LDAPHOST already set to $LDAPHOST"
		fi
	fi
}

function ask_ldap_port() {
	if [ "$LDAPPORT" == "" ]; then
		read -p "LDAP port [389]: " LDAPPORT
		if [ "$LDAPPORT" == "" ]; then
			LDAPPORT="389"
		fi
		export LDAPPORT=$LDAPPORT
	else
		if [ ! "$QUIET" ]; then
			echo "LDAPPORT already set to $LDAPPORT"
		fi
	fi
}

function ask_ldap_admin_dn() {
	if [ "$LDAP_ADMIN_DN" == "" ]; then
		while [ "$LDAP_ADMIN_DN" == "" ]; do
			read -p "LDAP admin DN (distinguished name): " LDAP_ADMIN_DN
			echo

			if [ "$LDAP_ADMIN_DN" == "" ]; then
				echo "LDAP admin DN cannot be empty"
				echo
				continue
			fi

			export LDAP_ADMIN_DN=$LDAP_ADMIN_DN
		done
	else
		if [ ! "$QUIET" ]; then
			echo "LDAP_ADMIN_DN already set to $LDAP_ADMIN_DN"
		fi
	fi
}

function ask_ldap_admin_pass() {
	if [ "$LDAP_ADMIN_PASSWD" == "" ]; then
		while [ "$LDAP_ADMIN_PASSWD" == "" ]; do
			read -s -p "LDAP admin password: " LDAP_ADMIN_PASSWD
			echo

			if [ "$LDAP_ADMIN_PASSWD" == "" ]; then
				echo "Password cannot be empty"
				echo
				continue
			fi

			export LDAP_ADMIN_PASSWD=$LDAP_ADMIN_PASSWD
		done
	else
		if [ ! "$QUIET" ]; then
			echo "LDAP_ADMIN_PASSWD already set to $LDAP_ADMIN_PASSWD"
		fi
	fi
}

function ask_fedora_db() {
	if [ "$FEDORA_DB" == "" ]; then
		read -p "Fedora database name [fedora22]: " FEDORA_DB
		if [ "$FEDORA_DB" == "" ]; then
			FEDORA_DB="fedora22"
		fi
		export FEDORA_DB=$FEDORA_DB
	else
		if [ ! "$QUIET" ]; then
			echo "FEDORA_DB already set to $FEDORA_DB"
		fi
	fi
}

function ask_fedora_admin_passwd() {
	if [ "$FEDORA_ADMIN_PASSWD" == "" ]; then
		while [ "$FEDORA_ADMIN_PASSWD" == "" ]; do
			read -s -p "Fedora administrator 'fedoraAdmin' user password: " FEDORA_ADMIN_PASSWD
			echo

			if [ "$FEDORA_ADMIN_PASSWD" == "" ]; then
				echo "Password cannot be empty"
				echo
				continue
			fi

			export FEDORA_ADMIN_PASSWD=$FEDORA_ADMIN_PASSWD
		done
	else
		if [ ! "$QUIET" ]; then
			echo "FEDORA_ADMIN_PASSWD already set to $FEDORA_ADMIN_PASSWD"
		fi
	fi
}

function ask_rodadata_mysql_host() {
	if [ "$RODADATA_MYSQL_HOST" == "" ]; then
		read -p "RODA Data MySQL database port [localhost]: " RODADATA_MYSQL_HOST
		if [ "$RODADATA_MYSQL_HOST" == "" ]; then
			RODADATA_MYSQL_HOST="localhost"
		fi
		export RODADATA_MYSQL_HOST=$RODADATA_MYSQL_HOST
	else
		if [ ! "$QUIET" ]; then
			echo "RODADATA_MYSQL_HOST already set to $RODADATA_MYSQL_HOST"
		fi
	fi
}

function ask_rodadata_mysql_port() {
	if [ "$RODADATA_MYSQL_PORT" == "" ]; then
		read -p "RODA Data MySQL database port [3306]: " RODADATA_MYSQL_PORT
		if [ "$RODADATA_MYSQL_PORT" == "" ]; then
			RODADATA_MYSQL_PORT="3306"
		fi
		export RODADATA_MYSQL_PORT=$RODADATA_MYSQL_PORT
	else
		if [ ! "$QUIET" ]; then
			echo "RODADATA_MYSQL_PORT already set to $RODADATA_MYSQL_PORT"
		fi
	fi
}

function ask_ldap_users_dn() {
	if [ "$LDAP_USERS_DN" == "" ]; then
		while [ "$LDAP_USERS_DN" == "" ]; do
			read -p "LDAP users DN (eg. ou=users,dc=keep,dc=pt): " LDAP_USERS_DN
			echo

			if [ "$LDAP_USERS_DN" == "" ]; then
				echo "LDAP users DN cannot be empty"
				echo
				continue
			fi

			export LDAP_USERS_DN=$LDAP_USERS_DN
		done
	else
		if [ ! "$QUIET" ]; then
			echo "LDAP_USERS_DN already set to $LDAP_USERS_DN"
		fi
	fi
}

function ask_ldap_groups_dn() {
	if [ "$LDAP_GROUPS_DN" == "" ]; then
		while [ "$LDAP_GROUPS_DN" == "" ]; do
			read -p "LDAP groups DN (eg. ou=groups,dc=keep,dc=pt): " LDAP_GROUPS_DN
			echo

			if [ "$LDAP_GROUPS_DN" == "" ]; then
				echo "LDAP groups DN cannot be empty"
				echo
				continue
			fi

			export LDAP_GROUPS_DN=$LDAP_GROUPS_DN
		done
	else
		if [ ! "$QUIET" ]; then
			echo "LDAP_GROUPS_DN already set to $LDAP_GROUPS_DN"
		fi
	fi
}

function ask_ldap_roles_dn() {
	if [ "$LDAP_ROLES_DN" == "" ]; then
		while [ "$LDAP_ROLES_DN" == "" ]; do
			read -p "LDAP roles DN (eg. ou=roles,dc=keep,dc=pt): " LDAP_ROLES_DN
			echo

			if [ "$LDAP_ROLES_DN" == "" ]; then
				echo "LDAP roles DN cannot be empty"
				echo
				continue
			fi

			export LDAP_ROLES_DN=$LDAP_ROLES_DN
		done
	else
		if [ ! "$QUIET" ]; then
			echo "LDAP_ROLES_DN already set to $LDAP_ROLES_DN"
		fi
	fi
}

function ask_roda_guest_passwd(){
	if [ "$RODA_GUEST_PASSWD" == "" ]; then
		while [ "$RODA_GUEST_PASSWD" == "" ]; do
			read -s -p "RODA 'guest' password: " RODA_GUEST_PASSWD
			echo

			if [ "$RODA_GUEST_PASSWD" == "" ]; then
				echo "Password cannot be empty"
				echo
				continue
			fi

			export RODA_GUEST_PASSWD=$RODA_GUEST_PASSWD
		done
	else
		if [ ! "$QUIET" ]; then
			echo "RODA_GUEST_PASSWD already set to $RODA_GUEST_PASSWD"
		fi
	fi
}

function ask_roda_admin_passwd(){
	if [ "$RODA_ADMIN_PASSWD" == "" ]; then
		while [ "$RODA_ADMIN_PASSWD" == "" ]; do
			read -s -p "RODA 'admin' password: " RODA_ADMIN_PASSWD
			echo

			if [ "$RODA_ADMIN_PASSWD" == "" ]; then
				echo "Password cannot be empty"
				echo
				continue
			fi

			export RODA_ADMIN_PASSWD=$RODA_ADMIN_PASSWD
		done
	else
		if [ ! "$QUIET" ]; then
			echo "RODA_ADMIN_PASSWD already set to $RODA_ADMIN_PASSWD"
		fi
	fi
}

function ask_roda_wui_passwd(){
	if [ "$RODA_WUI_PASSWD" == "" ]; then
		while [ "$RODA_WUI_PASSWD" == "" ]; do
			read -s -p "RODA 'roda-wui' user password: " RODA_WUI_PASSWD
			echo

			if [ "$RODA_WUI_PASSWD" == "" ]; then
				echo "Password cannot be empty"
				echo
				continue
			fi

			export RODA_WUI_PASSWD=$RODA_WUI_PASSWD
		done
	else
		if [ ! "$QUIET" ]; then
			echo "RODA_WUI_PASSWD already set to $RODA_WUI_PASSWD"
		fi
	fi
}

function ask_roda_ingest_task_passwd(){
	if [ "$RODA_INGEST_TASK_PASSWD" == "" ]; then
		while [ "$RODA_INGEST_TASK_PASSWD" == "" ]; do
			read -s -p "RODA 'roda-ingest-task' user password: " RODA_INGEST_TASK_PASSWD
			echo

			if [ "$RODA_INGEST_TASK_PASSWD" == "" ]; then
				echo "Password cannot be empty"
				echo
				continue
			fi

			export RODA_INGEST_TASK_PASSWD=$RODA_INGEST_TASK_PASSWD
		done
	else
		if [ ! "$QUIET" ]; then
			echo "RODA_INGEST_TASK_PASSWD already set to $RODA_INGEST_TASK_PASSWD"
		fi
	fi
}

function ask_roda_preservation_task_passwd(){
	if [ "$RODA_PRESERVATION_TASK_PASSWD" == "" ]; then
		while [ "$RODA_PRESERVATION_TASK_PASSWD" == "" ]; do
			read -s -p "RODA 'roda-preservation-task' user password: " RODA_PRESERVATION_TASK_PASSWD
			echo

			if [ "$RODA_PRESERVATION_TASK_PASSWD" == "" ]; then
				echo "Password cannot be empty"
				echo
				continue
			fi

			export RODA_PRESERVATION_TASK_PASSWD=$RODA_PRESERVATION_TASK_PASSWD
		done
	else
		if [ ! "$QUIET" ]; then
			echo "RODA_PRESERVATION_TASK_PASSWD already set to $RODA_PRESERVATION_TASK_PASSWD"
		fi
	fi
}

function ask_roda_handle_passwd(){
	if [ "$RODA_HANDLE_PASSWD" == "" ]; then
		while [ "$RODA_HANDLE_PASSWD" == "" ]; do
			read -s -p "RODA 'roda-handle' user password: " RODA_HANDLE_PASSWD
			echo

			if [ "$RODA_HANDLE_PASSWD" == "" ]; then
				echo "Password cannot be empty"
				echo
				continue
			fi

			export RODA_HANDLE_PASSWD=$RODA_HANDLE_PASSWD
		done
	else
		if [ ! "$QUIET" ]; then
			echo "RODA_HANDLE_PASSWD already set to $RODA_HANDLE_PASSWD"
		fi
	fi
}

function ask_rodadata_mysql_rodacore_passwd() {
	if [ "$RODADATA_MYSQL_RODACORE_PASSWD" == "" ]; then
		while [ "$RODADATA_MYSQL_RODACORE_PASSWD" == "" ]; do
			read -s -p "RODA Data MySQL 'roda-core' user password: " RODADATA_MYSQL_RODACORE_PASSWD
			echo

			if [ "$RODADATA_MYSQL_RODACORE_PASSWD" == "" ]; then
				echo "Password cannot be empty"
				echo
				continue
			fi

			export RODADATA_MYSQL_RODACORE_PASSWD=$RODADATA_MYSQL_RODACORE_PASSWD
		done
	else
		if [ ! "$QUIET" ]; then
			echo "RODADATA_MYSQL_RODACORE_PASSWD already set to $RODADATA_MYSQL_RODACORE_PASSWD"
		fi
	fi
}

function ask_roda_core_host(){
	if [ "$RODACORE_HOST" == "" ]; then
		read -p "RODA Core host [localhost]: " RODACORE_HOST
		if [ "$RODACORE_HOST" == "" ]; then
			RODACORE_HOST="localhost"
		fi
		export RODACORE_HOST=$RODACORE_HOST
	else
		if [ ! "$QUIET" ]; then
			echo "RODACORE_HOST already set to $RODACORE_HOST"
		fi
	fi
}

function ask_roda_core_port(){
	if [ "$RODACORE_PORT" == "" ]; then
		read -p "RODA Core port [8080]: " RODACORE_PORT
		if [ "$RODACORE_PORT" == "" ]; then
			RODACORE_PORT="8080"
		fi
		export RODACORE_PORT=$RODACORE_PORT
	else
		if [ ! "$QUIET" ]; then
			echo "RODACORE_PORT already set to $RODACORE_PORT"
		fi
	fi
}

function ask_rodawui_mysql_host() {
	if [ "$RODAWUI_MYSQL_HOST" == "" ]; then
		read -p "RODA WUI MySQL database host [localhost]: " RODAWUI_MYSQL_HOST
		if [ "$RODAWUI_MYSQL_HOST" == "" ]; then
			RODAWUI_MYSQL_HOST="localhost"
		fi
		export RODAWUI_MYSQL_HOST=$RODAWUI_MYSQL_HOST
	else
		if [ ! "$QUIET" ]; then
			echo "RODAWUI_MYSQL_HOST already set to $RODAWUI_MYSQL_HOST"
		fi
	fi
}

function ask_rodawui_mysql_port() {
	if [ "$RODAWUI_MYSQL_PORT" == "" ]; then
		read -p "RODA WUI MySQL database port [3306]: " RODAWUI_MYSQL_PORT
		if [ "$RODAWUI_MYSQL_PORT" == "" ]; then
			RODAWUI_MYSQL_PORT="3306"
		fi
		export RODAWUI_MYSQL_PORT=$RODAWUI_MYSQL_PORT
	else
		if [ ! "$QUIET" ]; then
			echo "RODAWUI_MYSQL_PORT already set to $RODAWUI_MYSQL_PORT"
		fi
	fi
}

function ask_rodawui_mysql_user() {
	if [ "$RODAWUI_MYSQL_USER" == "" ]; then
		read -p "RODA WUI MySQL database user [roda-wui]: " RODAWUI_MYSQL_USER
		if [ "$RODAWUI_MYSQL_USER" == "" ]; then
			RODAWUI_MYSQL_USER="roda-wui"
		fi
		export RODAWUI_MYSQL_USER=$RODAWUI_MYSQL_USER
	else
		if [ ! "$QUIET" ]; then
			echo "RODAWUI_MYSQL_USER already set to $RODAWUI_MYSQL_USER"
		fi
	fi
}

function ask_rodawui_mysql_user_passwd() {
	if [ "$RODAWUI_MYSQL_USER_PASSWD" == "" ]; then
		while [ "$RODAWUI_MYSQL_USER_PASSWD" == "" ]; do
			read -s -p "RODA WUI MySQL database '$RODAWUI_MYSQL_USER' password: " RODAWUI_MYSQL_USER_PASSWD
			echo

			if [ "$RODAWUI_MYSQL_USER_PASSWD" == "" ]; then
				echo "Password cannot be empty"
				echo
				continue
			fi

			export RODAWUI_MYSQL_USER_PASSWD=$RODAWUI_MYSQL_USER_PASSWD
		done
	else
		if [ ! "$QUIET" ]; then
			echo "RODAWUI_MYSQL_USER_PASSWD already set to $RODAWUI_MYSQL_USER_PASSWD"
		fi
	fi
}

function ask_rodawui_host(){
	if [ "$RODAWUI_HOST" == "" ]; then
		read -p "RODA WUI host [localhost]: " RODAWUI_HOST
		if [ "$RODAWUI_HOST" == "" ]; then
			RODAWUI_HOST="localhost"
		fi
		export RODAWUI_HOST=$RODAWUI_HOST
	else
		if [ ! "$QUIET" ]; then
			echo "RODAWUI_HOST already set to $RODAWUI_HOST"
		fi
	fi
}

function ask_rodamigratorlinux_host(){
	if [ "$RODAMIGRATORLINUX_HOST" == "" ]; then
		read -p "RODA Migrator (linux) host [localhost]: " RODAMIGRATORLINUX_HOST
		if [ "$RODAMIGRATORLINUX_HOST" == "" ]; then
			RODAMIGRATORLINUX_HOST="localhost"
		fi
		export RODAMIGRATORLINUX_HOST=$RODAMIGRATORLINUX_HOST
	else
		if [ ! "$QUIET" ]; then
			echo "RODAMIGRATORLINUX_HOST already set to '$RODAMIGRATORLINUX_HOST'"
		fi
	fi
}

function ask_rodamigratorlinux_port(){
	if [ "$RODAMIGRATORLINUX_PORT" == "" ]; then
		read -p "RODA Migrator (linux) port [8080]: " RODAMIGRATORLINUX_PORT
		if [ "$RODAMIGRATORLINUX_PORT" == "" ]; then
			RODAMIGRATORLINUX_PORT="8080"
		fi
		export RODAMIGRATORLINUX_PORT=$RODAMIGRATORLINUX_PORT
	else
		if [ ! "$QUIET" ]; then
			echo "RODAMIGRATORLINUX_PORT already set to '$RODAMIGRATORLINUX_PORT'"
		fi
	fi
}

function ask_rodadata_host(){
	if [ "$RODADATA_HOST" == "" ]; then
		read -p "RODA Data host [localhost]: " RODADATA_HOST
		if [ "$RODADATA_HOST" == "" ]; then
			RODADATA_HOST="localhost"
		fi
		export RODADATA_HOST=$RODADATA_HOST
	else
		if [ ! "$QUIET" ]; then
			echo "RODADATA_HOST already set to $RODADATA_HOST"
		fi
	fi
}

function ask_rodadata_port(){
	if [ "$RODADATA_PORT" == "" ]; then
		read -p "RODA Data port [8080]: " RODADATA_PORT
		if [ "$RODADATA_PORT" == "" ]; then
			RODADATA_PORT="8080"
		fi
		export RODADATA_PORT=$RODADATA_PORT
	else
		if [ ! "$QUIET" ]; then
			echo "RODADATA_PORT already set to $RODADATA_PORT"
		fi
	fi
}

function ask_rodacore_host(){
	if [ "$RODACORE_HOST" == "" ]; then
		read -p "RODA Core host [localhost]: " RODACORE_HOST
		if [ "$RODACORE_HOST" == "" ]; then
			RODACORE_HOST="localhost"
		fi
		export RODACORE_HOST=$RODACORE_HOST
	else
		if [ ! "$QUIET" ]; then
			echo "RODACORE_HOST already set to $RODACORE_HOST"
		fi
	fi
}

function ask_rodacore_port(){
	if [ "$RODACORE_PORT" == "" ]; then
		read -p "RODA Core port [8080]: " RODACORE_PORT
		if [ "$RODACORE_PORT" == "" ]; then
			RODACORE_PORT="8080"
		fi
		export RODACORE_PORT=$RODACORE_PORT
	else
		if [ ! "$QUIET" ]; then
			echo "RODACORE_PORT already set to $RODACORE_PORT"
		fi
	fi
}

function ask_rodawui_public_hostname(){
	if [ "$RODAWUI_PUBLIC_HOSTNAME" == "" ]; then
		read -p "RODA WUI public address [localhost]: " RODAWUI_PUBLIC_HOSTNAME
		if [ "$RODAWUI_PUBLIC_HOSTNAME" == "" ]; then
			RODAWUI_PUBLIC_HOSTNAME="localhost"
		fi
		export RODAWUI_PUBLIC_HOSTNAME=$RODAWUI_PUBLIC_HOSTNAME
	else
		if [ ! "$QUIET" ]; then
			echo "RODAWUI_PUBLIC_HOSTNAME already set to $RODAWUI_PUBLIC_HOSTNAME"
		fi
	fi
}

function ask_rodawui_public_port(){
	if [ "$RODAWUI_PUBLIC_PORT" == "" ]; then
		read -p "RODA WUI public port [8080]: " RODAWUI_PUBLIC_PORT
		if [ "$RODAWUI_PUBLIC_PORT" == "" ]; then
			RODAWUI_PUBLIC_PORT="8080"
		fi
		export RODAWUI_PUBLIC_PORT=$RODAWUI_PUBLIC_PORT
	else
		if [ ! "$QUIET" ]; then
			echo "RODAWUI_PUBLIC_PORT already set to $RODAWUI_PUBLIC_PORT"
		fi
	fi
}

function ask_rodacore_public_hostname(){
	if [ "$RODACORE_PUBLIC_HOSTNAME" == "" ]; then
		read -p "RODA Core public address [localhost]: " RODACORE_PUBLIC_HOSTNAME
		if [ "$RODACORE_PUBLIC_HOSTNAME" == "" ]; then
			RODACORE_PUBLIC_HOSTNAME="localhost"
		fi
		export RODACORE_PUBLIC_HOSTNAME=$RODACORE_PUBLIC_HOSTNAME
	else
		if [ ! "$QUIET" ]; then
			echo "RODACORE_PUBLIC_HOSTNAME already set to $RODACORE_PUBLIC_HOSTNAME"
		fi
	fi
}

function ask_rodacore_public_port(){
	if [ "$RODACORE_PUBLIC_PORT" == "" ]; then
		read -p "RODA Core public port [8080]: " RODACORE_PUBLIC_PORT
		if [ "$RODACORE_PUBLIC_PORT" == "" ]; then
			RODACORE_PUBLIC_PORT="8080"
		fi
		export RODACORE_PUBLIC_PORT=$RODACORE_PUBLIC_PORT
	else
		if [ ! "$QUIET" ]; then
			echo "RODACORE_PUBLIC_PORT already set to $RODACORE_PUBLIC_PORT"
		fi
	fi
}

