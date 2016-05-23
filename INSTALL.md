# Install instructions

## Dependencies

### Java 8

* Ubuntu
```bash
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
sudo apt-get install oracle-java8-set-default
```

### Application Server (required)
Recommended application server is Apache Tomcat 8, but other Java EE application servers might work.
To install, [download Apache Tomcat](http://tomcat.apache.org/) and unzip it to your preferred folder.

### Apache Solr (optional)

By default Solr embedded is available and used, but it is not recommended for production.
Check the following site for instructions on how to install Apache Solr in production.

https://cwiki.apache.org/confluence/display/solr/Taking+Solr+to+Production


### Ingest tools (optional)
The following dependencies are only needed if the following ingest tasks are required.

Install ClamAV (anti-virus)
* Ubuntu
```bash
sudo apt-get install clamav clamav-daemon -y
```
* CentOS 7
```bash
sudo yum install clamav-server clamav-data clamav-update clamav-filesystem \
clamav clamav-scanner-systemd clamav-devel clamav-lib clamav-server-systemd
```
Check these instructions: http://linux-audit.com/install-clamav-on-centos-7-using-freshclam/

Note: The user `clamav` must have permissions to access the storage. For some folders you might need to add permissions in apparmor file  at `/etc/apparmor.d/local/usr.sbin.clamd`, see [instructions for Ubuntu](https://help.ubuntu.com/community/AppArmor#Profile_customization).

Install Siegfried (format identification)
* Ubuntu 14.04 LTS
```bash
curl https://bintray.com/user/downloadSubjectPublicKey?username=bintray | sudo apt-key add -
echo "deb http://dl.bintray.com/siegfried/debian wheezy main" | sudo tee -a /etc/apt/sources.list
sudo apt-get update -qq
sudo apt-get install siegfried -y
```

* Ubuntu 16.04 LTS
```bash
sudo apt-get install golang git
echo "export GOPATH=\$HOME/gocode" >> ~/.bash_profile
echo "export PATH=\$PATH:\$GOPATH/bin" >> ~/.bash_profile
source ~/.bash_profile
go get github.com/richardlehane/siegfried/cmd/sf
sf -update
```

* CentOS 7
```bash
sudo yum install golang git
echo "export GOPATH=\$HOME/gocode" >> ~/.bash_profile
echo "export PATH=\$PATH:\$GOPATH/bin" >> ~/.bash_profile
source ~/.bash_profile
go get github.com/richardlehane/siegfried/cmd/sf
sf -update
```

NOTE: Please note that the user must have a correct HOME defined, if using a special user (e.g. `roda`), use `sudo -iu roda` to login and test siegfried install.
NOTE: Please note that the install instruction above include new environment variables that need

### Format migration tools (optional)
The following dependencies are only needed if support for file formation migration for the following format families are required.

Install Avconv (for video)
- Ubuntu: `sudo apt-get install libav-tools -y`
- CentOS 7: `TBD`

Install ImageMagick (for images)
- Ubuntu: `sudo apt-get install imagemagick -y`
- CentOS 7: `TBD`

Install SoX (for audio)
- Ubuntu: `sudo apt-get install sox libsox-fmt-all -y`
- CentOS 7: `TBD`

Install GhostScript (for PDF and PS)
- Ubuntu: `sudo apt-get install ghostscript libgs-dev -y`
- CentOS 7:

Install Unoconv (for text documents: Microsoft Office, LibreOffice, and others)
* Ubuntu:
```bash
sudo add-apt-repository ppa:libreoffice/ppa -y
sudo apt-get update
sudo apt-get install libreoffice -y
sudo apt-get install unoconv -y
sudo apt-get --only-upgrade install cpio fonts-opensymbol initscripts libc6 \
libgcrypt11 libgraphite2-3 libnss3 libreoffice-avmedia-backend-gstreamer \
libreoffice-pdfimport multiarch-support sysv-rc sysvinit-utils -y
```
* CentOS: `TBD`

## Install

[Download a RODA release](https://github.com/keeps/roda/releases) and install on your application server. Alternatively, [download the source-code](https://github.com/keeps/roda) and compile with `mvn clean package`, the release will be at `roda-ui/roda-wui/target/roda-wui-*.war`.

To install in Apache Tomcat 8, find the webapps directory and copy the RODA WAR file there. Rename it to ROOT.war if you want it to be the default web application (also delete ROOT directory).
Then you can start the service by running `./bin/startup.sh`.

Check if it worked: http://localhost:8080

Apache Tomcat should be added as a service to the system so it can be initiated at startup, this is dependent on the selected operative system.

## Configure

RODA default configuration folder is available at `~/.roda/config`.
By default no configuration is needed.

To change the default configuration folder, add the following lines at the beginning of `catalina.sh` file in the Apache Tomcat bin folder.

```
RODA_HOME=/home/roda/roda
JAVA_OPTS="$JAVA_OPTS -Droda.home=$RODA_HOME"
```

You might also force here which Java is being used and increase the memory limit.
```
JAVA_HOME="/usr/lib/jvm/java-8-oracle"
JAVA_OPTS="$JAVA_OPTS -Xmx4g"
```

### Use Siegfried format identifier as a service (recommended)

Edit the config/roda-core.properties file and make sure these are the activated options. Comment the existing ones if necessary

```
core.tools.siegfried.mode = server
#core.tools.siegfried.mode = standalone
core.tools.siegfried.binary = sf
core.tools.siegfried.server = http://localhost:5138
```

### Use ClamV antivirus as a service (recommended)

Make sure you have the following settings activated. Comment the existing ones if necessary.

```
core.plugins.internal.virus_check.clamav.bin = /usr/bin/clamdscan
core.plugins.internal.virus_check.clamav.params = -m --fdpass
core.plugins.internal.virus_check.clamav.get_version = clamdscan --version
```

### Add risks based on Drambora (optional)


Under the folder "$HOME/.roda/data/storage" run the following commands:

```
wget https://github.com/keeps/roda/archive/risks.zip
unzip risks.zip; mv roda-risks Risk; rm risks.zip
curl -X POST --header "Content-Type: application/json" --header "Accept: application/json" "http://localhost:8080/api/v1/management_tasks/index/reindex?entity=risk" -u USERNAME:PASSWORD
```
NOTE: On the last command change the "USERNAME" and "PASSWORD" to your admin credentials on RODA. Also update the URL if necessary. This will load the risks into the repository indexes.

### Configure Apache Solr (optional)

TBD

### Configure tools (optional)

TBD

### Change design (optional)

TBD
