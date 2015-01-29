#!/bin/bash

cd $(dirname $0)

. ../bin/set-roda-env.sh
. $RODA_HOME/bin/roda-common-setup.sh
. install.config

export JAVA_HOME=$(readlink -f /usr/bin/javac | sed "s:/bin/javac::")
sudo keytool -delete -alias mykey -storepass changeit -keypass changeit -keystore $JAVA_HOME/jre/lib/security/cacerts

warn "You're about to uninstall RODA...\nDo you want to procede? [yN]"
read anwser
case $anwser in
   [yY])
   ;;
   *)
      exit 1
   ;;
esac

DB_NUMBER=$(echo "show databases;" | mysql -u $RODADATA_MYSQL_RODACORE_USER -p$RODADATA_MYSQL_RODACORE_PASSWD | egrep "^($RODADATA_MYSQL_DB|$FEDORA_DB)$" | wc -l)
if [ "$DB_NUMBER" -eq "2" ]; then
   warn "Do you want to delete RODA databases (i.e., \"$RODADATA_MYSQL_DB\" & \"$FEDORA_DB\")? [yN]"
   read anwser
   case $anwser in
      [yY])
         echo "drop database $RODADATA_MYSQL_DB;" | mysql -u $RODADATA_MYSQL_RODACORE_USER -p$RODADATA_MYSQL_RODACORE_PASSWD
         echo "drop database $FEDORA_DB;" | mysql -u $RODADATA_MYSQL_RODACORE_USER -p$RODADATA_MYSQL_RODACORE_PASSWD
      ;;
      *)
      ;;
   esac
fi

warn "Do you want to delete RODA_HOME (i.e., \"$RODA_HOME\")? [yN]"
read anwser
case $anwser in
   [yY])
      rm -rf $RODA_HOME
   ;;
   *)
   ;;
esac
info "Done uninstalling RODA!"
