# Changelog

## v2.2.0 (13/04/2018)
Install for demonstration:
```
docker pull keeps/roda:v2.2.0
```

#### New features:

-  Add a way of downloading a RI record (XML) [#1177](https://github.com/keeps/roda/issues/1177)
-  Obtain last report using transferred resource filename [#1076](https://github.com/keeps/roda/issues/1076)
-  Log sign-off events [#1028](https://github.com/keeps/roda/issues/1028)
-  Dynamic relationships between AIPs/representation/files and Representation information [#343](https://github.com/keeps/roda/issues/343)
-  Representation Information AIP [#331](https://github.com/keeps/roda/issues/331)

#### Enhancements:

-  Date (with or without time) should be presented always in the same format [#1198](https://github.com/keeps/roda/issues/1198)
-  Remove export button from edit association when seeing one representation information [#1188](https://github.com/keeps/roda/issues/1188)
-  Representation Information list tag design layout [#1178](https://github.com/keeps/roda/issues/1178)
-  Metadata field DC type should be mapped into RODA Level during indexing [#1174](https://github.com/keeps/roda/issues/1174)
-  Add pre-wrap to representation information long text values when viewing and increase height when editing. [#1168](https://github.com/keeps/roda/issues/1168)
-  Changing administrators permissions blocking should be properly handled [#1165](https://github.com/keeps/roda/issues/1165)
-  In the user group facet, show full name instead of identifier [#1164](https://github.com/keeps/roda/issues/1164)
-  When we move an AIP using the sidebar button on the right, it should say which AIP we are moving for safe checking. [#1163](https://github.com/keeps/roda/issues/1163)
-  It should be possible to search by content in the event register [#1161](https://github.com/keeps/roda/issues/1161)
-  Reconfigure logback to use RollingFileAppender instead of FileAppender [#1160](https://github.com/keeps/roda/issues/1160)
-  Representation list file count does not distinguish folders from files [#1158](https://github.com/keeps/roda/issues/1158)
-  Use ReturnWithExceptions logic on SolrUtils functions  [#1154](https://github.com/keeps/roda/issues/1154)
-  RODA core could generated plugin information in markdown format [#1151](https://github.com/keeps/roda/issues/1151)
-  Change rep. information (i) color to be fixed blue color [#1148](https://github.com/keeps/roda/issues/1148)
-  IncrementalList enhancements [#1147](https://github.com/keeps/roda/issues/1147)
-  Improvements on representation information association  [#1146](https://github.com/keeps/roda/issues/1146)
-  When changing only Rich Text Area content, save does not work [#1145](https://github.com/keeps/roda/issues/1145)
-  Rep. Information - Associate to existing [#1143](https://github.com/keeps/roda/issues/1143)
-  When selecting the AIP to create a relation with (from RI) the AIP title is not shown if the AIP has no title. [#1128](https://github.com/keeps/roda/issues/1128)
-  Adding new categories and then editing them makes the component show the first field empty [#1125](https://github.com/keeps/roda/issues/1125)
-  Support HTML rich text area on RI editor [#1124](https://github.com/keeps/roda/issues/1124)
-  Review format.json conversion script [#1123](https://github.com/keeps/roda/issues/1123)
-  Add columns to RI list [#1122](https://github.com/keeps/roda/issues/1122)
-  Review PT translations of RI labels [#1120](https://github.com/keeps/roda/issues/1120)
-  Job async cleanup [#1117](https://github.com/keeps/roda/issues/1117)
-  Add a section about Representation Information to the overview document (at the end) [#1114](https://github.com/keeps/roda/issues/1114)
-  Change RI Association page subtitle [#1113](https://github.com/keeps/roda/issues/1113)
-  Preview results when associating RI [#1112](https://github.com/keeps/roda/issues/1112)
-  Change link behaviour in show/edit representation information [#1111](https://github.com/keeps/roda/issues/1111)
-  Change IncrementalList [#1110](https://github.com/keeps/roda/issues/1110)
-  Change edit associations dialog [#1109](https://github.com/keeps/roda/issues/1109)
-  Intellectual Entity remove dialog text is not clear enough [#1106](https://github.com/keeps/roda/issues/1106)
-  Default title when creating representation information [#1099](https://github.com/keeps/roda/issues/1099)
-  Change menu item "Representation information registry" to "Representation network" [#1090](https://github.com/keeps/roda/issues/1090)
-  Create help texts for the RI relations popup [#1086](https://github.com/keeps/roda/issues/1086)
-  Text boxes should be white on RI panel and combos blue [#1084](https://github.com/keeps/roda/issues/1084)
-  Representation information families [#1079](https://github.com/keeps/roda/issues/1079)
-  Define relation types for Representation Information and Intellectual entities [#1078](https://github.com/keeps/roda/issues/1078)
-  Define risk to create when AIP only have non supported representation information [#1077](https://github.com/keeps/roda/issues/1077)
-  Define Representation Information Pop-ups descriptions [#1074](https://github.com/keeps/roda/issues/1074)
-  The key optionsLabelI18nKeyPrefix='otherlevel' is not translated into Portuguese on the default RODA config [#1071](https://github.com/keeps/roda/issues/1071)
-  Make sure these keys exist in all languages [#1068](https://github.com/keeps/roda/issues/1068)
-  There are templates and XSLTs called "EAD". Is this obsolete? [#1065](https://github.com/keeps/roda/issues/1065)
-  When we hit save on the descriptive metadata editor, the cursor should be placed on the top so that we can read the error messages [#1064](https://github.com/keeps/roda/issues/1064)
-  Update table to display the representation "Status" instead of "Original" which is obsolete [#1059](https://github.com/keeps/roda/issues/1059)
-  Update translation Change States > Change Status [#1058](https://github.com/keeps/roda/issues/1058)
-  Changing the TYPE of an AIP should have the same behaviour as changing the TYPE of a representation [#1056](https://github.com/keeps/roda/issues/1056)
-  Break lines on the visualisation XSLT are not being preserved [#1055](https://github.com/keeps/roda/issues/1055)
-  Define risk when representation information is missing [#1054](https://github.com/keeps/roda/issues/1054)
-  Review links on the footer of the demo. Some are wrong, others are broken [#1053](https://github.com/keeps/roda/issues/1053)
-  Review preservation events based on the new PREMIS vocabulary [#1046](https://github.com/keeps/roda/issues/1046)
-  Docker build running user and volume permissions [#857](https://github.com/keeps/roda/issues/857)
-  Review default corpora  [#781](https://github.com/keeps/roda/issues/781)

#### Bug Fixes:

-  When viewing a Representation Information & clicking in one tag, the sidebar does not go to the top [#1197](https://github.com/keeps/roda/issues/1197)
-  Error transforming preservation event details into HTML [#1192](https://github.com/keeps/roda/issues/1192)
-  Links at the bottom of Representation Information pages don't scroll the page to the top, leaving the user with the impression that the link did not work [#1191](https://github.com/keeps/roda/issues/1191)
-  Problems on Internet Explorer 11 on Windows [#1189](https://github.com/keeps/roda/issues/1189)
-  Representation information edit association dialog gets off center after doing a search [#1187](https://github.com/keeps/roda/issues/1187)
-  When seeing one representation information & clicking in one tag, the search does nothing [#1186](https://github.com/keeps/roda/issues/1186)
-  Representation information network has a cancel button that makes no sense [#1185](https://github.com/keeps/roda/issues/1185)
-  When exporting AIPs (using AIP batch export plugin) trash & history folders are created [#1181](https://github.com/keeps/roda/issues/1181)
-  Back button on AIP permissions is not working [#1179](https://github.com/keeps/roda/issues/1179)
-  When we try erase all the RI records, the process fails but then the entire thing becomes unstable [#1176](https://github.com/keeps/roda/issues/1176)
-  Associations to RI from file technical atributes is not working. The association doesn't stick [#1175](https://github.com/keeps/roda/issues/1175)
-  RI: wrong title on association popup and list of records now showing everything I have. [#1172](https://github.com/keeps/roda/issues/1172)
-  When we click the links on the theme, and the supporting text is in markdown the page is loaded but not positioned on top [#1167](https://github.com/keeps/roda/issues/1167)
-  The color of the standard login help popup should be white [#1166](https://github.com/keeps/roda/issues/1166)
-  Date range search filters on the interface don't cope with different time zones [#1159](https://github.com/keeps/roda/issues/1159)
-  Error while saving representation information [#1156](https://github.com/keeps/roda/issues/1156)
-  When in an AIP, and trying to start a new job, the cancel button does nothing [#1155](https://github.com/keeps/roda/issues/1155)
-  RI edit associations search preview is making AND search instead of OR [#1152](https://github.com/keeps/roda/issues/1152)
-  Sometimes zookeeper fails and ingest job (& generally speaking RODA) does not handle properly the failure [#1132](https://github.com/keeps/roda/issues/1132)
-  When integrated with CAS there is no sign-in log [#1130](https://github.com/keeps/roda/issues/1130)
-  Format register search is not working [#1129](https://github.com/keeps/roda/issues/1129)
-  There is a slight misalignment in the add button [#1127](https://github.com/keeps/roda/issues/1127)
-  The X button is not working on RI [#1119](https://github.com/keeps/roda/issues/1119)
-  Error on job cleanup in RODA initialization after forced crash [#1102](https://github.com/keeps/roda/issues/1102)
-  GWT error when closing edit association pop-up [#1101](https://github.com/keeps/roda/issues/1101)
-  Link to developers guide on RODA demo footer is broken [#1100](https://github.com/keeps/roda/issues/1100)
-  Advanced search: combo appears on the wrong place [#1096](https://github.com/keeps/roda/issues/1096)
-  RI additional info window is too big and jumps around when we try to move it [#1085](https://github.com/keeps/roda/issues/1085)
-  The associations to AIP panel is highly incomplete [#1083](https://github.com/keeps/roda/issues/1083)
-  Broken layout on RI editor [#1081](https://github.com/keeps/roda/issues/1081)
-  Relations from RI to RI still has the MOVE TO ROOT button enabled [#1080](https://github.com/keeps/roda/issues/1080)
-  The date popup that appears on the EAD 2002 editor for the field  "Date of creation or revision"/processDates appears way up on the UI (out of the screen).  [#1072](https://github.com/keeps/roda/issues/1072)
-  When using the UI to create representations and upload files the clip icon is not displayed [#1057](https://github.com/keeps/roda/issues/1057)
-  The EAD visualisation XSLT has problems in groups, mappings and translations [#1027](https://github.com/keeps/roda/issues/1027)
-  EARKSIPPluginsTest.testIngestAncestors() is throwing an unexpected error [#853](https://github.com/keeps/roda/issues/853)
-  Error downloading PREMIS on demo server [#833](https://github.com/keeps/roda/issues/833)
-  PDF to PDFA conversion plugin reporting is not well [#619](https://github.com/keeps/roda/issues/619)

---

## 2.2.0-beta1 (22/11/2017)
New features:
* Support for Representation Information

Install for demonstration:
```
docker pull keeps/roda:2.2.0-beta1
```
---

## 2.1.0 (20/09/2017)
New features:
* Representation created and updated date
* Representation type editor has controlled vocabulary with open other
* Add a new option to the ingest workflow that specifies that a notification/email should only be sent if the ingest procedure is not 100% successful
* Performance enhancements to SIP update ingestion
* Disable showing all results when entering search #1029 

Bug fixes:
* Showing search descendants and in this package buttons when item has no siblings #1043
* Sub-levels are not shown when browsing through catalogue with a facet selected #1042
* Moving a transferred resource with a long path makes an error #1035

Enhancements:
* Add translation on CSV when downloading it via statistics page #1048

Install for demonstration:
```
docker pull keeps/roda:2.1.0
```

---

## 2.0.1 (14/07/2017)
Bug fixes:
* Disable showing all results when entering search #1029
* Limit result size of CSV export on WUI  #1030

New features:
* Added button to ingest plugin to only send notification when errors occurs on ingest

Install for demonstration:
```
docker pull keeps/roda:2.0.1
```

Check instructions for production-level install.
---

## Version 2.0.0 (final) (02/06/2017)
Final version of RODA 2.0.0 that marks a complete overhaul of the system:
* **Performance greatly improved** with a more monolithic design
* **New design** is cleaner and easier to use
* **Customizable descriptive metadata**
* New storage system that keeps files **directly on storage using standards**
* Apache Solr for indexing all information and keep **access fast and scalable**
* Akka.io for orchestrating all **ingest, preservation and internal actions**
* Easy deployment using **docker** containers
* And much more

Install for demonstration:
```
docker pull keeps/roda:2.0.0
```

Check instructions for production-level install.

---

## RODA v1.3.0 (26/05/2015)
RODA version 1.3.0, codename Huron

New features:
- Using CAS (Central Authentication Service) as the authentication mechanism
- Added [extended version FITS](https://github.com/keeps/fits/releases/tag/v0.8) to the ingest with support for technical metadata extraction of several new metadata types
- Added support for customized description level, defined in a properties file

Improvements:
- EAD-C used by RODA is now compliant with the original EAD Component

Bug fixes:
- Installation scripts (e.g. specified Tomcat was no longer available for download)

---

## RODA v1.1.0 (01/11/2013)
RODA version 1.1.0, codename Superior

New features:
- RODA was released to GitHub
- RODA was completly mavenized (installer creation included, using the maven profile "create-installer")
- RODA now running on Apache Tomcat
- Added new instalation process
- Added support for Ubuntu Linux 12.04 LTS (supported until April 2017)

Bug fixes
- Scheduler was storing dates incorrectly
- String formater was used in some parts of the code to build SQL statements and proved to be a problem with different
  locales (when different characters are used as separator for floating point values)
- Revised footer logos (roda-wui)

---

## RODA v1.2.0 (01/11/2013)
RODA version 1.2.0, codename Victoria.

New features:
- Using Droid for file format identification
- Added support for presentations: Microsoft Powerpoint (ppt, pptx), OpenOffice Presentation / LibreOffice Impress (odp)
- Added suport for spreadsheets: Microsoft Excel (xls, xlsx), OpenOffice Spreadsheet / LibreOffice Calc (ods)
- Added support for email (.eml) but conversion is not yet supported
- Added web service to download AIP
- Added web service to download DIP

Bug fixes:
- Validation of SIP XML was not being properly done
- Fixed visualization of preservation metadata in roda-wui when system locale was not in English
- Fixed roda-in representation type selector in some Java versions
