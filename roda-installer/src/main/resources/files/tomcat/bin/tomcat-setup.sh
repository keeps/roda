#!/bin/bash

. $RODA_HOME/bin/roda-common-setup.sh

ask_external_domain

cd $RODA_HOME/tomcat/bin/

export RODA_HOME=$RODA_HOME
export JAVA_HOME=$(readlink -f /usr/bin/javac | sed "s:/bin/javac::")

cp -v -f ./templates/tomcat.config . &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_1)"
ant -f tomcat-setup.xml tomcat-setup.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_2)"

myEcho
myEcho "Downloading Tomcat"
if [ -f /tmp/$TOMCAT_ZIP ] && [ "$TOMCAT_ZIP_MD5" = "$(md5sum /tmp/$TOMCAT_ZIP | sed 's# .*$##')" ]; then
	myEcho
	myEcho "Tomcat already exists in /tmp and is valid (md5=\"$TOMCAT_ZIP_MD5\")"
else
	wget --output-document=/tmp/$TOMCAT_ZIP "$TOMCAT_ZIP_URL" &>> $INSTALL_LOG
	testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_3)"
fi
myEcho

myEcho "Unpacking Tomcat"
unzip -o -q /tmp/$TOMCAT_ZIP -d /tmp &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_4)"
rm -rf /tmp/$TOMCAT_DIRNAME/webapps/* &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_5)"
chmod +x /tmp/$TOMCAT_DIRNAME/bin/*.sh &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_6)"
cp -v -r /tmp/$TOMCAT_DIRNAME $RODA_HOME/tomcat/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_7)"
rm -rf /tmp/$TOMCAT_DIRNAME/ &>> $INSTALL_LOG


myEcho
myEcho "Generating KEYSTORE"
keytool -genkey -alias roda -keypass changeit -storepass changeit -keyalg RSA -dname "CN=$RODA_EXTERNAL_DOMAIN, OU='', O='', L='', ST='', C='PT'" -noprompt -keystore rodaKeystore.jks
keytool -export -alias roda -keypass changeit -file server.crt -storepass changeit -keystore rodaKeystore.jks
keytool -import -trustcacerts -alias roda -file server.crt -keystore rodaTruststore.ts -storepass changeit -noprompt
sudo keytool -import -file server.crt -storepass changeit -keypass changeit -noprompt -keystore $JAVA_HOME/jre/lib/security/cacerts
cp rodaKeystore.jks $RODA_HOME/config/ &>> $INSTALL_LOG
cp rodaTruststore.ts $RODA_HOME/config/ &>> $INSTALL_LOG
export KEYSTOREPATH=$RODA_HOME/config/rodaKeystore.jks
export TRUSTSTOREPATH=$RODA_HOME/config/rodaTruststore.ts
ant -f tomcat-setup.xml ssl-setup &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_8)"

if [ "$(egrep "^127\.0\.0\.1" /etc/hosts | egrep -v "$RODA_EXTERNAL_DOMAIN" | wc -l)" -ge "1" ];then
        sudo sed -i "s#^127.0.0.1.*#& $RODA_EXTERNAL_DOMAIN#" /etc/hosts
fi


myEcho
myEcho "Enabling SSL connector"
sed -i "s#</Server>##g" $RODA_HOME/tomcat/$TOMCAT_DIRNAME/conf/server.xml
sed -i "s#  </Service>##g" $RODA_HOME/tomcat/$TOMCAT_DIRNAME/conf/server.xml
echo '    <Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true" maxThreads="150" scheme="https" secure="true" clientAuth="false" sslProtocol="TLS" keystoreFile="'$RODA_HOME'/config/rodaKeystore.jks" keystorePass="changeit" truststoreFile="'$RODA_HOME'/config/rodaTruststore.ts" truststorePass="changeit" />' >> $RODA_HOME/tomcat/$TOMCAT_DIRNAME/conf/server.xml
echo '  </Service>' >> $RODA_HOME/tomcat/$TOMCAT_DIRNAME/conf/server.xml
echo '</Server>' >> $RODA_HOME/tomcat/$TOMCAT_DIRNAME/conf/server.xml

myEcho
myEcho "Linking Tomcat start/stop script to RODA bin"
chmod +x $RODA_HOME/tomcat/bin/tomcat &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_9)"
ln -s $RODA_HOME/tomcat/bin/tomcat $RODA_HOME/bin/ &>> $INSTALL_LOG
testExecutionAndExitWithMsgOnFailure "$?" "$EX_FAILED_TO_INSTALL_TOMCAT" "Error installing Tomcat (errorCode=${EX_FAILED_TO_INSTALL_TOMCAT}_10)"

myEcho
myEcho "* Tomcat setup finished"
myEcho
