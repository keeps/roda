# Install instructions

## Dependencies

### Application Server (required)
Recommended application server is Apache Tomcat 8, but other Java EE application servers might work.

* Ubuntu: `sudo apt-get install tomcat8 -y`
* CentOS: `TBD`

### Apache Solr (optional)

By default Solr embedded is available and used, but it is not recommended for production.
Check the following site for instructions on how to install Apache Solr in production.

https://cwiki.apache.org/confluence/display/solr/Taking+Solr+to+Production


### Ingest tools (optional)
The following dependencies are only needed if the following ingest tasks are required.

Install ClamAV (anti-virus)
* Ubuntu
```bash
sudo apt-get install clamav -y
```
* CentOS 7
```bash
sudo yum install clamav-server clamav-data clamav-update clamav-filesystem \
clamav clamav-scanner-systemd clamav-devel clamav-lib clamav-server-systemd
```

Install Siegfried (format identification)
* Ubuntu
```bash
curl https://bintray.com/user/downloadSubjectPublicKey?username=bintray | sudo apt-key add -
echo "deb http://dl.bintray.com/siegfried/debian wheezy main" | sudo tee -a /etc/apt/sources.list
sudo apt-get update -qq
sudo apt-get install siegfried -y
```
* CentOS 7
```bash
TBD
```

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

To install in Apache Tomcat 8, find the webapps directory (see below) and copy the RODA WAR file there. Rename it to ROOT.war if you want it to be the default web application (also delete ROOT directory).

Apache Tomcat 8 directory is at:
* Ubuntu: `/var/lib/tomcat8/webapps`
* CentOS: `TBD`

Start-up Tomcat:
* Ubuntu: `sudo service tomcat8 start`
* CentOS: `TBD`

Check if it worked: http://localhost:8080

## Configure

RODA default configuration folder is available at `~/.roda/config`.
By default no configuration is needed.

### Configure Apache Solr (optional)

TBD

### Configure tools (optional)

TBD

### Change design (optional)

TBD
