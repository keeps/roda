# Changelog

## 2.0.0 (2017-06-02)

### New features

* **Performance greatly improved** with a more monolithic design
* **New design** is cleaner and easier to use
* **Customizable descriptive metadata**
* New storage system that keeps files **directly on storage using standards**
* Apache Solr for indexing all information and keep **access fast and scalable**
* Akka.io for orchestrating all **ingest, preservation and internal actions**
* Easy deployment using **docker** containers
* And much more



## 1.3.0 (2015-05-25)

### New features

  * Using CAS (Central Authentication Service) as the authentication mechanism
  * Added [extended version FITS](https://github.com/keeps/fits/releases/tag/v0.8) to the ingest with support for technical metadata extraction of several new metadata types
  * Added support for customized description level, defined in a properties file

### Improvements

  * EAD-C used by RODA is now compliant with the original EAD Component

###  Bug fixes

  * Installation scripts (e.g. specified Tomcat was no longer available for download)


## 1.2.0 (2013-10-25)

### New features:

  * Using Droid for file format identification
  * Added support for presentations: Microsoft Powerpoint (ppt, pptx), OpenOffice Presentation / LibreOffice Impress (odp)
  * Added suport for spreadsheets: Microsoft Excel (xls, xlsx), OpenOffice Spreadsheet / LibreOffice Calc (ods)
  * Added support for email (.eml) but conversion is not yet supported
  * Added web service to download AIP
  * Added web service to download DIP

### Bug fixes:

  * Validation of SIP XML was not being properly done
  * Fixed visualization of preservation metadata in roda-wui when system locale was not in English
  * Fixed roda-in representation type selector in some Java versions


## 1.1.0 (2013-05-31)

### New features

  * RODA was released to GitHub
    (https://github.com/keeps/roda/issues/2)
  * RODA was completly mavenized (installer creation included,
    using the maven profile "create-installer")
    (https://github.com/keeps/roda/issues/1)
  * RODA now running on Apache Tomcat
  * Added new instalation process
  * Added support for Ubuntu Linux 12.04 LTS (supported until April 2017)
  * Updated Google Web Toolkit to v2.4.0, adding support for Internet Explorer 8, 9 and 10.

### Bug fixes

  * Scheduler was storing dates incorrectly
    (https://github.com/keeps/roda/issues/3)
  * String formater was used in some parts of the code to build
    SQL statements and proved to be a problem with different
    locales (when different characters are used as separator
    for floating point values)
    (https://github.com/keeps/roda/issues/4)
  * Revised footer logos (roda-wui)


## 1.0.0 (2009-05-18)

  * First release of RODA
