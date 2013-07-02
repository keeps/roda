RODA - Repository of Authentic Digital Objects
==============================================

## What is RODA?
RODA is a complete digital repository that delivers functionality for all the main units of the OAIS reference model. RODA is capable of ingesting, managing and providing access to the various types of digital objects produced by large corporations or public bodies. RODA is based on open-source technologies and is supported by existing standards such as the OAIS, METS, EAD and PREMIS.

## Some technical information about RODA

* Uses Fedora Commons for digital object storage
* Uses EAD as descriptive metadata schema
* Uses PREMIS as preservation metadata schema
* Has a multi-step ingestion workflow
* Supports LDAP for authentication & authorization
* Supports pluggable preservation actions


For more information, please fell free to visit RODA website:
**<http://www.roda-community.org>**

## Getting started

To try without installing checkout the demo at: <http://demo.roda-community.org>

To install RODA checkout the binary installation packages at: http://www.roda-community.org/download/

Also, you can checkout the [features](http://www.roda-community.org/what-is-roda/) and [screenshots](http://www.roda-community.org/screenshots/) at the [RODA Community website](http://www.roda-community.org).

## How to build and run

The prerequisites to build RODA are:
 * Git client
 * Apache Maven
 * Apache Ant
 * OpenJDK 6 JDK

To install all dependencies in Debian based systems execute:
```bash
$ sudo apt-get install git maven ant openjdk-6-jdk
```

To compile, go to the RODA sources and execute the command:
```bash
$ mvn clean install -Pcreate-installer
```

After a successful compile, the installer will be available at `roda-installer/roda-installer.zip`. To install, uncompress the archive and follow the instructions on the file INSTALL.txt that is inside.

For known issues running RODA and possible solutions please refer to the [Troubleshooting](https://github.com/keeps/roda/wiki/Troubleshooting) wiki page.

## Developers

To start developing for RODA check the [Developer guide](https://github.com/keeps/roda/wiki/Developer-guide) which has information on:

1. [How to get the source code](https://github.com/keeps/roda/wiki/Developer-guide#-how-to-get-the-source-code)
2. [How the code is laid out](https://github.com/keeps/roda/wiki/Developer-guide#-how-the-code-is-laid-out)
3. [How to set up the development environment](https://github.com/keeps/roda/wiki/Developer-guide#-how-to-set-up-the-development-environment)
4. [How to contribute](https://github.com/keeps/roda/wiki/Developer-guide#-how-to-contribute)

