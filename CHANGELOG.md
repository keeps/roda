# Changelog

## v4.2.0 (17/01/2022)
### New features:

- Job orchestration #1981
- Add prometheus metrics for HTTP notification system #1982

### Bug fixes:

- LinkingObjectIdentifierValue links to unknown URN (in preservation metadata PREMIS) #1946
- Object PREMIS does not register contentLocation in storage element #1947
- Fixity information computation to report SKIPPED #1970
- Allow run mutiple plugins #1977

Install for demonstration:
```
docker pull keeps/roda:v4.2.0
```

---

## v3.6.0 (17/01/2022)
New features:

- Job orchestration #1981
- Add prometheus metrics for HTTP notification system #1982

Install for demonstration:
```
docker pull keeps/roda:v3.6
```
---

## v3.5.7 (17/01/2022)
#### Enhancements:
- Expand Portal to allow customization via AIP level #1969


Install for demonstration:
```
docker pull keeps/roda:v3.5.7
```
---

## v4.1.1 (09/12/2021)

#### Enhancements:

-  Add an option to always display the last descriptive metadata [#1965](https://github.com/keeps/roda/issues/1965)

#### Bug Fixes:

-  Ingest assessment not working with filter [#1964](https://github.com/keeps/roda/issues/1964)

Install for demonstration:
```
docker pull keeps/roda:v4.1.1
```
---

## v4.1.0 (23/11/2021)

#### New features:

-  Compliance with E-ARK SIP and AIP version 2.0.4  [#1960](https://github.com/keeps/roda/issues/1960)

#### Enhancements:

-  Embedded RODA plugins that act on files to report SKIPPED when AIPs have no files [#1961](https://github.com/keeps/roda/issues/1961)

Install for demonstration:
```
docker pull keeps/roda:v4.1.0
```
---

## v4.0.3 (04/10/2021)

#### Bug Fixes:

-  Missing dependencies from RODA 4 Dockerfile [#1949](https://github.com/keeps/roda/issues/1949)

Install for demonstration:
```
docker pull keeps/roda:v4.0.3
```
---

## v4.0.2 (12/08/2021)
#### Bug Fixes:

-  Missing Cron in Tomcat base image. [#1940](https://github.com/keeps/roda/issues/1940)

Install for demonstration:
```
docker pull keeps/roda:v4.0.2
```
---

## v3.5.6 (12/08/2021)

#### Bug Fixes:

-  Missing Cron in Tomcat base image. [#1940](https://github.com/keeps/roda/issues/1940)

Install for demonstration:
```
docker pull keeps/roda:v3.5.6
```

---

## v4.0.1 (29/07/2021)
#### Enhancements:
-  Improve pruning of descriptive metadata when destroying a record #1920
-  Modify Tomcat on Dockerfile [#1923](https://github.com/keeps/roda/issues/1923)
-  Update Dockerfile siegfried repository [#1938](https://github.com/keeps/roda/issues/1938)

#### Bug Fixes:

-  master/slave action logs management [#1928](https://github.com/keeps/roda/issues/1928)
-  CAS login repeatedly register an action even though the user is already logged in [#1926](https://github.com/keeps/roda/issues/1926)
-  PluginHelper is faulty reporting a failure when transforming lite in object [#1925](https://github.com/keeps/roda/issues/1925)
-  Failed to open job-report via job page [#1922](https://github.com/keeps/roda/issues/1922)
-  Improve ErrorHandler to filter false positive errors [#1921](https://github.com/keeps/roda/issues/1921)
-  ImageMagick plugin does not create a preservation event  [#1936](https://github.com/keeps/roda/issues/1936)

Install for demonstration:
```
docker pull keeps/roda:v4.0.1
```

---

## v3.5.5 (28/07/2021)

#### Enhancements:

-  Update Dockerfile siegfried repository [#1938](https://github.com/keeps/roda/issues/1938)

#### Bug Fixes:

-  ImageMagick plugin does not create a preservation event  [#1936](https://github.com/keeps/roda/issues/1936)

Install for demonstration:
```
docker pull keeps/roda:v3.5.5
```
---

## v3.5.4 (22/04/2021)

#### Enhancements:

-  Modify Tomcat on Dockerfile [#1923](https://github.com/keeps/roda/issues/1923)

#### Bug Fixes:

-  master/slave action logs management [#1928](https://github.com/keeps/roda/issues/1928)
-  CAS login repeatedly register an action even though the user is already logged in [#1926](https://github.com/keeps/roda/issues/1926)
-  PluginHelper is faulty reporting a failure when transforming lite in object [#1925](https://github.com/keeps/roda/issues/1925)
-  Failed to open job-report via job page [#1922](https://github.com/keeps/roda/issues/1922)
-  Improve ErrorHandler to filter false positive errors [#1921](https://github.com/keeps/roda/issues/1921)

Install for demonstration:
```
docker pull keeps/roda:v3.5.4
```

---

## v3.5.3 (18/03/2021)

#### Enhancements:

-  Skipped reports are showing as failure [#1918](https://github.com/keeps/roda/issues/1918)

Install for demonstration:
```
docker pull keeps/roda:v3.5.3
```

---

## v4.0.0 (11/03/2021)

#### New features:

-  Adding Croatian language [#1711](https://github.com/keeps/roda/issues/1711)
-  Retention and disposal features [#1708](https://github.com/keeps/roda/issues/1708)

Install for demonstration:
```
docker pull keeps/roda:v4.0.0
```
---

## v3.5.2 (10/03/2021)

#### New features:

-  Add option to remove SIP from transfer resource folder after a successfully ingest workflow [#1917](https://github.com/keeps/roda/issues/1917)

#### Bug Fixes:

-  Siegfried and fixity skipped during ingest workflow [#1916](https://github.com/keeps/roda/issues/1916)

Install for demonstration:
```
docker pull keeps/roda:v3.5.2
```
---

## v3.5.1 (02/12/2020)
#### Security fixes
-  Bump xstream from 1.4.10-java7 to 1.4.14-java7 [#1710](https://github.com/keeps/roda/issues/1710) to fix CVE-2019-10173

#### Dependency upgrades
-  Bump Solr from 7.7.2 to 7.7.3 [#1706](https://github.com/keeps/roda/issues/1706)
-  Bump several dependencies [#1707](https://github.com/keeps/roda/issues/1707)
   - CAS from 3.5.0 to 3.6.1
   - Jersey from 2.27 to 2.31
   - Swagger from 1.5.24 to 1.6.2
   - Jackson from 2.10.1 to 2.11.2
   - Springboot from 2.1.9 to 2.3.3
   - Commons-code from 1.12 to 1.15
   - Commons-io from 2.6 to 2.8.0
   - Commons-lang from 3.9 to 3.11
   - Commons-csv from 1.6 to 1.8
   - Commons-text from 1.6 to 1.9
   - handlebars from 4.1.0 to 4.2.0
   - prometheus from 0.8.0 to 0.9.0
 
#### Enhancements
-  Skip Siegfried and Premis plugin when SIP update has no representations [#1709](https://github.com/keeps/roda/issues/1709)

Install for demonstration:
```
docker pull keeps/roda:v3.5.1
```
---

## v3.5.0 (16/09/2020)
#### New features:

-  Hide actions which user does not have permissions to execute [#1022](https://github.com/keeps/roda/issues/1022)

#### Enhancements:

-  Bump GWT from 2.8.3 to 2.9.0 [#1549](https://github.com/keeps/roda/issues/1549)
-  REST API: Tranferred resource reindex better error mapping [#1547](https://github.com/keeps/roda/issues/1547)
-  Default ingest plugin generalization [#1484](https://github.com/keeps/roda/issues/1484)
-  Job report list consistency [#1328](https://github.com/keeps/roda/issues/1328)
-  Improve default ingest plugin final states [#1300](https://github.com/keeps/roda/issues/1300)

#### Security Fixes:

- Blocking URL redirection from remote source ([CWE-601](https://cwe.mitre.org/data/definitions/601.html))
- Guarding against ["Zip Slip"](https://snyk.io/research/zip-slip-vulnerability)
- Fixed [information exposure through stack trace](https://wiki.sei.cmu.edu/confluence/display/java/ERR01-J.+Do+not+allow+exceptions+to+expose+sensitive+information)
- Guarding against cross-site scripting ([CWE-79](https://cwe.mitre.org/data/definitions/79.html))

#### Bug Fixes:

-  AntivirusPlugin version command polluted by warning [#1548](https://github.com/keeps/roda/issues/1548)
-  Siegfried task on SIP update optimization not working [#1536](https://github.com/keeps/roda/issues/1536)
-  API create/update descriptive metadata is failing [#1516](https://github.com/keeps/roda/issues/1516)
-  Greater than 100% progress on deletion of a list of AIPs in some cases [#1506](https://github.com/keeps/roda/issues/1506)

---

## v3.4.0 (09/07/2020)
### Security fix:
-  HTTP GET Request to reindex transferred resources folder can access data outside the folder [#1540](https://github.com/keeps/roda/issues/1540)

#### New features:

-  Option to force timezone on all presented dates to UTC [#1539](https://github.com/keeps/roda/issues/1539)

#### Enhancements:

-  Support show embedded / open new page option in DIPs on AIP or Representation levels [#1541](https://github.com/keeps/roda/issues/1541)

#### Bug Fixes:

-  Fixing plugin readme generator [#1542](https://github.com/keeps/roda/issues/1542)

Install for demonstration:
```
docker pull keeps/roda:v3.4.0
```

---

## v3.3.1 (24/02/2020)
Install for demonstration:
```
docker pull keeps/roda:v3.3.1
```


### Enhancements
* Improve file notification processor #1519

### Bug fixes
* Opening job reports directly via URL (new page) creates a javascript error

### Security
* Setting all Maven reports to HTTPS to secure against man-in-the-middle attacks to the compilation process
---

## v3.3.0 (15/01/2020)
Install for demonstration:
```
docker pull keeps/roda:v3.3.0
```

#### Features:
- Monitoring: Adding support for Prometheus metrics export
- Development: Adding Super devmode via mvn using springboot and codeserver

#### Enhancements:

-  Support very large queries to Solr [#1500](https://github.com/keeps/roda/issues/1500)
- Upgrading PDFJS to 2.3.200

#### Bug Fixes:

-  Authorization denied when accessing repository-level preservation event [#1503](https://github.com/keeps/roda/issues/1503)
-  Fixity PREMIS event not being created in case of a SIP update with new representations [#1502](https://github.com/keeps/roda/issues/1502)
-  Search advanced list multiplied everytime is selected [#1498](https://github.com/keeps/roda/issues/1498)
-  API index does not allow inverse search (regression) [#1497](https://github.com/keeps/roda/issues/1497)
-  Error showing Job with LongRangeFilterParameter without lower or upper limit  [#1496](https://github.com/keeps/roda/issues/1496)
-  Recovering login with e-mail makes user loose groups and roles [#1489](https://github.com/keeps/roda/issues/1489)
- Fixing descriptive metadata history panel when descriptive metadata is edited by the system

#### Security Fixes:
- Updating Jackson from 2.9.10 to 2.10.1


---

## v3.2.0 (31/10/2019)
Install for demonstration:
```
docker pull keeps/roda:v3.2.0
```

#### New features:

- E-ARK SIP version 2 support (Common Specification version 2.0.1)

#### Enhancements:

-  Add parentId as a default field when generating inventory report [#1488](https://github.com/keeps/roda/issues/1488)
-  Default ingest plugin generalization [#1484](https://github.com/keeps/roda/issues/1484)
-  Add a submission download button [#1479](https://github.com/keeps/roda/issues/1479)
-  Make post-job (ingest or not) notifications generic [#1477](https://github.com/keeps/roda/issues/1477)
-  Removing a user doesn't have a confirm dialog [#1458](https://github.com/keeps/roda/issues/1458)

#### Bug Fixes:

-  "Has failures" facet of job list not considering running jobs [#1493](https://github.com/keeps/roda/issues/1493)
-  Title sort not working with default configuration [#1492](https://github.com/keeps/roda/issues/1492)
-  Ingest reports issue after moving SIPs [#1491](https://github.com/keeps/roda/issues/1491)

#### Security:

- Upgrading jackson to fix CVE-2019-14540 and CVE-2019-16335 fixes #1495

---

## v3.1.1 (30/05/2019)
Install for demonstration:
```
docker pull keeps/roda:v3.1.1
```

#### Enhancements:

-  Create a way to define orchestrator block size per plugin [#1476](https://github.com/keeps/roda/issues/1476)
-  Introduce cache strategies to improve ingest performance [#1475](https://github.com/keeps/roda/issues/1475)
-  Add HTTP notification support on MinimalIngestPlugin [#1473](https://github.com/keeps/roda/issues/1473)
-  Reindex plugins must deal with org.apache.solr.client.solrj.impl.CloudSolrClient$RouteException [#1469](https://github.com/keeps/roda/issues/1469)
-  Add descriptive metadata config to open specific tab by default [#1464](https://github.com/keeps/roda/issues/1464)

#### Bug Fixes:

-  Fix translation disparities between english and portuguese languages [#1474](https://github.com/keeps/roda/issues/1474)
-  Stop job button missing [#1470](https://github.com/keeps/roda/issues/1470)
-  Cannot create an AIP using web user interface [#1468](https://github.com/keeps/roda/issues/1468)
-  Page information on navigation bar is not generic to files or other possible RODA objects [#1463](https://github.com/keeps/roda/issues/1463)

---

## v3.1.0 (30/04/2019)
Install for demonstration:
```
docker pull keeps/roda:v3.1.0
```

#### New features:
-  Configurable columns in all search results [#1459](https://github.com/keeps/roda/issues/1459)
-  Create Portal UI endpoint [#1452](https://github.com/keeps/roda/issues/1452)

#### Enhancements:
-  Upgrading **Solr version to 7.7**
-  Upgrading PDFjs to 2.0.943 [#1461](https://github.com/keeps/roda/issues/1461)
-  Possibility to orderly show descriptive metadata on UI [#1451](https://github.com/keeps/roda/issues/1451)
-  Configuring a ui.list should not need to override all lists [#1445](https://github.com/keeps/roda/issues/1445)

#### Bug Fixes:

-  Stemming for single-valued fields not ative [#1460](https://github.com/keeps/roda/issues/1460)
-  Repository preservation events are not being re-indexed [#1447](https://github.com/keeps/roda/issues/1447)
-  Report verification on ingest does not properly support transformation of resources to multiple AIPs [#1444](https://github.com/keeps/roda/issues/1444)
-  Being processed counter is not being correctly calculated [#1443](https://github.com/keeps/roda/issues/1443)
-  Bug while searching for filename [#1432](https://github.com/keeps/roda/issues/1432)

---

## v3.0.2 (31/01/2019)
### Install for demonstration
```
docker pull keeps/roda:v3.0.2
```
#### Security fixes
- Fixing CVE-2018-19360, CVE-2018-19362, CVE-2018-19361 by updating jackson library

#### Enhancements:

-  Chart.js and FileSaver.js as webjars [#1440](https://github.com/keeps/roda/issues/1440)
-  After login the browser back shows login panel although user is already logged in [#860](https://github.com/keeps/roda/issues/860)
- Adding menu text color configurations

#### Bug Fixes:

-  Ingest events not being indexed correctly [#1438](https://github.com/keeps/roda/issues/1438)
-  When executing an action over all objects of a specific entity, the humanized filter on UI is not properly showed [#1437](https://github.com/keeps/roda/issues/1437)
-  When ingesting with multiple SIPs, in the end of the job each AIP has multiple ingest ended events [#1436](https://github.com/keeps/roda/issues/1436)
-  Node selection window grows vertically forever [#1433](https://github.com/keeps/roda/issues/1433)
-  Button text overflow [#1431](https://github.com/keeps/roda/issues/1431)
-  Fixed problem related with updating old to new transferred resource identifier when moving SIP after ingest

---

## v3.0.1 (14/12/2018)
### Install for demonstration
```
docker pull keeps/roda:v3.0.1
```

#### New features:

-  Plugin parameter TextBox in read-only mode [#1234](https://github.com/keeps/roda/issues/1234)

#### Enhancements:

-  Hiding the advanced search and searching uses advanced search fields [#1426](https://github.com/keeps/roda/issues/1426)

#### Bug Fixes:

-  Users/groups REST API endpoint error when using XML as output format [#1429](https://github.com/keeps/roda/issues/1429)
-  AntiVirus does not show the version correctly [#1428](https://github.com/keeps/roda/issues/1428)
-  Wrong input box's title in advanced search for representations/files [#1424](https://github.com/keeps/roda/issues/1424)
-  Links between representation information and files is not working [#1420](https://github.com/keeps/roda/issues/1420)
-  Dynamic _txt field should be multivalued [#1419](https://github.com/keeps/roda/issues/1419)
-  Risk incidences table UI for a specific risk is messed up [#1417](https://github.com/keeps/roda/issues/1417)
-  Partial duplicate of preservation event [#1416](https://github.com/keeps/roda/issues/1416)
-  MP4 video is not playing in Safari in the HTML5 video [#907](https://github.com/keeps/roda/issues/907)

---

## v3.0.0 (28/11/2018)
### Install for demonstration
```
docker pull keeps/roda:v3.0.0
```

### Main highlights
* Native support for representation information management
* Improvements in product usability
* Ability to search all repository pages (e.g., intellectual entities, ingestion processes, preservation processes, preservation events, users/groups, etc.)
* New visual metaphors for the state of processes including the ability to pause them
* Delayed execution of actions in the background
* New PDF viewer
* Support for operation in Read Only mode and for operation in multi-server environments for high performance and high availability, i.e. cluster
* New indexing engine, faster in contexts in the hundreds of millions of records
* Bug fixes and improvements in terms of security and system stability

### Detailed changelog

#### New features:

-  Add JSONP support to all methods that need to be called via AJAX [#1374](https://github.com/keeps/roda/issues/1374)
-  Add visual cue  for tables being auto-updated [#1289](https://github.com/keeps/roda/issues/1289)
-  Implement actions layout as a contextual menu instead of sidebar [#1259](https://github.com/keeps/roda/issues/1259)
-  Configurable web interface date format [#1251](https://github.com/keeps/roda/issues/1251)
-  Job process source objects list must be changed/deleted [#1238](https://github.com/keeps/roda/issues/1238)
-  Show file name extention in file info sidebar panel [#1218](https://github.com/keeps/roda/issues/1218)
-  Storage level Read Only Mode [#1196](https://github.com/keeps/roda/issues/1196)
-  On SIP update add new SIP ids to the AIP [#1183](https://github.com/keeps/roda/issues/1183)
-  Add a way of downloading a RI record (XML) [#1177](https://github.com/keeps/roda/issues/1177)
-  Change the "nobranding" implementation to support multiple additional CSS instead of just one [#1157](https://github.com/keeps/roda/issues/1157)
-  Card filters [#1116](https://github.com/keeps/roda/issues/1116)
-  Obtain last report using transferred resource filename [#1076](https://github.com/keeps/roda/issues/1076)
-  Add a new option to the ingest workflow that specifies that a notification/email should only be sent if the ingest procedure is not 100% successful.  [#1031](https://github.com/keeps/roda/issues/1031)
-  Log sign-off events [#1028](https://github.com/keeps/roda/issues/1028)
-  Configuration option to only send ingest email notification when there are failures [#1026](https://github.com/keeps/roda/issues/1026)
-  Hide actions which user does not have permissions to execute [#1022](https://github.com/keeps/roda/issues/1022)
-  Override roda configuration via environment variables [#979](https://github.com/keeps/roda/issues/979)
-  Develop CSS for being "printer friendly" [#848](https://github.com/keeps/roda/issues/848)
-  Allow to filter jobs that have failures [#831](https://github.com/keeps/roda/issues/831)
-  "Representation type" should be a controlled vocabulary [#798](https://github.com/keeps/roda/issues/798)
-  Representation date created and updated [#712](https://github.com/keeps/roda/issues/712)
-  Support file streaming in REST API [#709](https://github.com/keeps/roda/issues/709)
-  Index representation metadata [#621](https://github.com/keeps/roda/issues/621)
-  FileStorageService support for folder scattering [#527](https://github.com/keeps/roda/issues/527)
-  Allow to define menu items and color in configuration [#521](https://github.com/keeps/roda/issues/521)
-  Handle RPC timeouts on long requests [#490](https://github.com/keeps/roda/issues/490)
-  Dynamic relationships between AIPs/representation/files and Representation information [#343](https://github.com/keeps/roda/issues/343)
-  Representation Information AIP [#331](https://github.com/keeps/roda/issues/331)

#### Enhancements:

-  Enter should automatically search on the list if still on the textbox [#1412](https://github.com/keeps/roda/issues/1412)
-  Representation information MUST have advanced search [#1410](https://github.com/keeps/roda/issues/1410)
-  When creating new ingest job and obtaining cURL, modal button text makes no sense [#1409](https://github.com/keeps/roda/issues/1409)
-  Eliminate some right side bars [#1407](https://github.com/keeps/roda/issues/1407)
-  When cancelling an AIP creation, a popup to view the remove job is unnecessary [#1403](https://github.com/keeps/roda/issues/1403)
-  Permissions "apply to all" [#1395](https://github.com/keeps/roda/issues/1395)
-  Stemming is not working [#1391](https://github.com/keeps/roda/issues/1391)
-  Allow setting fieldsToReturn in index/find API [#1387](https://github.com/keeps/roda/issues/1387)
-  Hide action-related elements for users without roles [#1384](https://github.com/keeps/roda/issues/1384)
-  Show instance id in log entry if exists [#1383](https://github.com/keeps/roda/issues/1383)
-  Add support for CORS [#1382](https://github.com/keeps/roda/issues/1382)
-  Backing to search page should maintain the result list visible [#1381](https://github.com/keeps/roda/issues/1381)
-  Improve page titles [#1380](https://github.com/keeps/roda/issues/1380)
-  Fix and improve filtered search [#1379](https://github.com/keeps/roda/issues/1379)
-  In the BrowseAIP details sidebar show a SIP identifier per line [#1376](https://github.com/keeps/roda/issues/1376)
-  Add support for jsonp requests in IndexResource#list [#1375](https://github.com/keeps/roda/issues/1375)
-  ERROR log about dead letter due to locking [#1369](https://github.com/keeps/roda/issues/1369)
-  Cache siegfried version [#1368](https://github.com/keeps/roda/issues/1368)
-  Representation information problems [#1361](https://github.com/keeps/roda/issues/1361)
-  A list's first auto-update should happen sooner [#1360](https://github.com/keeps/roda/issues/1360)
-  Metadata validation agent description should be more generic [#1359](https://github.com/keeps/roda/issues/1359)
-  Align header checkbox in lists [#1355](https://github.com/keeps/roda/issues/1355)
-  Improve empty list mesage [#1354](https://github.com/keeps/roda/issues/1354)
-  Viewing a pres. event from an AIP has no way to go back [#1346](https://github.com/keeps/roda/issues/1346)
-  roda-core.properties has wui related properties and shouldn't [#1341](https://github.com/keeps/roda/issues/1341)
-  BrowseAIP permissions and roles visibility [#1335](https://github.com/keeps/roda/issues/1335)
-  On search only show options in dropdown if user has roles [#1334](https://github.com/keeps/roda/issues/1334)
-  Search does superfluous requests to server [#1333](https://github.com/keeps/roda/issues/1333)
-  Group checkbox title should be group name [#1331](https://github.com/keeps/roda/issues/1331)
-  Implement new BrowseAIP design [#1320](https://github.com/keeps/roda/issues/1320)
-  Refactor search and list components [#1319](https://github.com/keeps/roda/issues/1319)
-  Confusing message about checkboxes that are not visible [#1318](https://github.com/keeps/roda/issues/1318)
-  List files under a representation using the REST API [#1312](https://github.com/keeps/roda/issues/1312)
-  Fix Job(report) links in AIP details sidebar [#1304](https://github.com/keeps/roda/issues/1304)
-  Create class hierarchy for wui advanced search [#1303](https://github.com/keeps/roda/issues/1303)
-  Re-design BrowseAIP cards to include actionable buttons [#1302](https://github.com/keeps/roda/issues/1302)
-  Make advanced search toggle easier to notice [#1301](https://github.com/keeps/roda/issues/1301)
-  Report list improvements [#1299](https://github.com/keeps/roda/issues/1299)
-  Change table select all button [#1295](https://github.com/keeps/roda/issues/1295)
-  Change permissions in AIP details slider [#1294](https://github.com/keeps/roda/issues/1294)
-  Not all tables should be auto-updated [#1288](https://github.com/keeps/roda/issues/1288)
-  I don't love the way KEEPS logo appears on the footer [#1271](https://github.com/keeps/roda/issues/1271)
-  Add breadcrumbs to replace "back" buttons [#1270](https://github.com/keeps/roda/issues/1270)
-  Relations with no title sometimes appear empty when editing representation information [#1269](https://github.com/keeps/roda/issues/1269)
-  Add KEEPS logo to RODA [#1266](https://github.com/keeps/roda/issues/1266)
-  Add icons to all page titles [#1264](https://github.com/keeps/roda/issues/1264)
-  Add description to Preservation Events page [#1263](https://github.com/keeps/roda/issues/1263)
-  Remove "beta" label from Representation Information [#1262](https://github.com/keeps/roda/issues/1262)
-  Remove sidebar [#1261](https://github.com/keeps/roda/issues/1261)
-  Remove date filter from sidebar and add it as advanced search [#1260](https://github.com/keeps/roda/issues/1260)
-  Useless tooltip on user permission checkboxes [#1255](https://github.com/keeps/roda/issues/1255)
-  Button to select all permissions should have the same size as the clear one [#1254](https://github.com/keeps/roda/issues/1254)
-  Support CAS single sign out [#1250](https://github.com/keeps/roda/issues/1250)
-  Improve folder creation method [#1248](https://github.com/keeps/roda/issues/1248)
-  Implement retries and metrics when using iterable index results [#1242](https://github.com/keeps/roda/issues/1242)
-  Missing CSS classes in several sidebar components necessary for customization [#1228](https://github.com/keeps/roda/issues/1228)
-  When creating new AIP if the type has no related form it should open XML text area automatically [#1227](https://github.com/keeps/roda/issues/1227)
-  Improve logging when error pages are shown [#1226](https://github.com/keeps/roda/issues/1226)
-  Support URI as Linking Object Identifier on Preservation Events [#1225](https://github.com/keeps/roda/issues/1225)
-  Some fields in preservation events are being indexed twice [#1223](https://github.com/keeps/roda/issues/1223)
-  Review indexes to include docValues [#1222](https://github.com/keeps/roda/issues/1222)
-  BrowserService.retrieveRepresentationInformationExtraBundle() should only receive Id [#1217](https://github.com/keeps/roda/issues/1217)
-  ingest.daily.file_endpoint doesn't need to start with 'file:///' [#1214](https://github.com/keeps/roda/issues/1214)
-  Add Content-Length header to file download REST API [#1212](https://github.com/keeps/roda/issues/1212)
-  Parsing transferred resource date warning [#1211](https://github.com/keeps/roda/issues/1211)
-  Move all local JS dependencies to webjars [#1206](https://github.com/keeps/roda/issues/1206)
-  RI search by tags show no feedback [#1202](https://github.com/keeps/roda/issues/1202)
-  An external link in RI should be opened in another window [#1201](https://github.com/keeps/roda/issues/1201)
-  Welcome page review (Portuguese version) [#1200](https://github.com/keeps/roda/issues/1200)
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
-  Statistics export url should explicitly request CSV via query parameter [#1049](https://github.com/keeps/roda/issues/1049)
-  Review preservation events based on the new PREMIS vocabulary [#1046](https://github.com/keeps/roda/issues/1046)
-  Make dbviewer iframe fullheight [#1044](https://github.com/keeps/roda/issues/1044)
-  Revise the README.md at the root of the project. Some of its sections are more updated on the Documentation folder [#1041](https://github.com/keeps/roda/issues/1041)
-  Indexing partial date of date final fixes it to first day of the year [#1038](https://github.com/keeps/roda/issues/1038)
-  "AIP ancestor hierarchy fix" plugin should be of category "management" [#1036](https://github.com/keeps/roda/issues/1036)
-  Limit result size of CSV export on WUI [#1030](https://github.com/keeps/roda/issues/1030)
-  Disable showing all results when entering search [#1029](https://github.com/keeps/roda/issues/1029)
-  Move documentation from the wiki to readthedocs.org [#1025](https://github.com/keeps/roda/issues/1025)
-  Create a new field on AIP that contains all create and update job ids [#1001](https://github.com/keeps/roda/issues/1001)
-  Add OrFilterParameters pre filter draw [#999](https://github.com/keeps/roda/issues/999)
-  Properties loading could be improved [#998](https://github.com/keeps/roda/issues/998)
-  Re-implement actions in all entities using the Actionable interface [#885](https://github.com/keeps/roda/issues/885)
-  Schedule transferred resource list update after upload [#858](https://github.com/keeps/roda/issues/858)
-  Docker build running user and volume permissions [#857](https://github.com/keeps/roda/issues/857)
-  The agent version of siegfried plugin should include also the version of the PRONOM signature file [#846](https://github.com/keeps/roda/issues/846)
-  External plugins should have their own configs & i18n [#785](https://github.com/keeps/roda/issues/785)
-  Review default corpora  [#781](https://github.com/keeps/roda/issues/781)
-  File upload encoding problem [#727](https://github.com/keeps/roda/issues/727)
-  Allow to define transferred resources absolute path [#651](https://github.com/keeps/roda/issues/651)
-  Transferred resource upload creates temp. files and does not remove them in the end [#625](https://github.com/keeps/roda/issues/625)
-  NPE when updating ApacheDS to newer versions [#475](https://github.com/keeps/roda/issues/475)

#### Bug Fixes:

-  AIP preservation events count is wrong sometimes [#1415](https://github.com/keeps/roda/issues/1415)
-  When doing manual ingest appraisal, the toast text is wrong [#1411](https://github.com/keeps/roda/issues/1411)
-  When the language selected is the one with the greatest char length, the check is positioned on top of the text [#1406](https://github.com/keeps/roda/issues/1406)
-  Agents should be created/updated alongside with user management [#1405](https://github.com/keeps/roda/issues/1405)
-  When creating sublevel, viewport is not at the top of the page [#1404](https://github.com/keeps/roda/issues/1404)
-  Missing/wrong i18n pt_PT translations [#1402](https://github.com/keeps/roda/issues/1402)
-  Client side error in item lists (select, navigate, go back, error) [#1401](https://github.com/keeps/roda/issues/1401)
-  File leak in theme resource when browser caches resources [#1400](https://github.com/keeps/roda/issues/1400)
-  Page range selection popup in search does not disappear on mobile [#1399](https://github.com/keeps/roda/issues/1399)
-  Hiding too much information on preservation actions list [#1398](https://github.com/keeps/roda/issues/1398)
-  Remove Format object [#1397](https://github.com/keeps/roda/issues/1397)
-  Missing translation in advanced search [#1396](https://github.com/keeps/roda/issues/1396)
-  Inconsistent reports when using manual appraisal [#1394](https://github.com/keeps/roda/issues/1394)
-  AIP "In this package" search option should only search representations and files [#1393](https://github.com/keeps/roda/issues/1393)
-  In search, sometimes the dropdown items break into 2 lines [#1392](https://github.com/keeps/roda/issues/1392)
-  Remove RegexFilterParameter [#1390](https://github.com/keeps/roda/issues/1390)
-  Inconsistent state in browse aip [#1385](https://github.com/keeps/roda/issues/1385)
-  Ingest: Do not release lock until whole ingest workflow ends [#1378](https://github.com/keeps/roda/issues/1378)
-  'Source objects' is not displaying correctly [#1373](https://github.com/keeps/roda/issues/1373)
-  'Clear facets' button appears when it should not [#1371](https://github.com/keeps/roda/issues/1371)
-  Auto-resize dialogs with lists when the list contents change [#1370](https://github.com/keeps/roda/issues/1370)
-  job report list shows failed column 1 when it is still running [#1367](https://github.com/keeps/roda/issues/1367)
-  Non admin users cannot see repository level events [#1366](https://github.com/keeps/roda/issues/1366)
-  SIP update failures not being moved to failure folder [#1365](https://github.com/keeps/roda/issues/1365)
-  Error when SIP update fails in default ingest due to NO_OUTCOME_ID [#1364](https://github.com/keeps/roda/issues/1364)
-  SIP update failures not updating job [#1363](https://github.com/keeps/roda/issues/1363)
-  Moving files to root throws an 500 error [#1358](https://github.com/keeps/roda/issues/1358)
-  Actions are overriding redirects [#1357](https://github.com/keeps/roda/issues/1357)
-  Viewing job after action does not work [#1356](https://github.com/keeps/roda/issues/1356)
-  Ingest transfer upload button not working [#1352](https://github.com/keeps/roda/issues/1352)
-  CAS API auth allows incoherent state in basic auth fallback [#1349](https://github.com/keeps/roda/issues/1349)
-  The selected items information is not coherent [#1348](https://github.com/keeps/roda/issues/1348)
-  New GSS integration makes theme.css with less priority [#1347](https://github.com/keeps/roda/issues/1347)
-  Changing representation type does not update page [#1345](https://github.com/keeps/roda/issues/1345)
-  Identifying formats action in representation shows remove job message [#1344](https://github.com/keeps/roda/issues/1344)
-  Move of a file to root gives NPE while checking permissions [#1343](https://github.com/keeps/roda/issues/1343)
-  NPE when opening selectdialog [#1342](https://github.com/keeps/roda/issues/1342)
-  Opening Advanced search the search button is disabled [#1340](https://github.com/keeps/roda/issues/1340)
-  Date range search is not accurate [#1339](https://github.com/keeps/roda/issues/1339)
-  Ingest ended event is not being created if move SIPs when auto accept is active [#1338](https://github.com/keeps/roda/issues/1338)
-  Cannot change user permission using user management UI [#1332](https://github.com/keeps/roda/issues/1332)
-  User without permissions cannot access its own profile page [#1330](https://github.com/keeps/roda/issues/1330)
-  When uploading a file, a JS error is thrown [#1329](https://github.com/keeps/roda/issues/1329)
-  Resolve of class not supported [#1327](https://github.com/keeps/roda/issues/1327)
-  Extraneous filters being submitted to solr [#1326](https://github.com/keeps/roda/issues/1326)
-  Search page is searching on load [#1325](https://github.com/keeps/roda/issues/1325)
-  Searching searches twice [#1324](https://github.com/keeps/roda/issues/1324)
-  Descendants search does not work [#1323](https://github.com/keeps/roda/issues/1323)
-  Reindexing does not work [#1322](https://github.com/keeps/roda/issues/1322)
-  Unable to remove users that are not on the visible range of the list [#1321](https://github.com/keeps/roda/issues/1321)
-  RODA does not start [#1317](https://github.com/keeps/roda/issues/1317)
-  Job with list of Ids should not use index.count() to count the list of items [#1314](https://github.com/keeps/roda/issues/1314)
-  Buggy UI when starting a preservation action on an inactive AIP [#1311](https://github.com/keeps/roda/issues/1311)
-  Selected items count misbehaves when items are removed from the list [#1308](https://github.com/keeps/roda/issues/1308)
-  'Done' button in upload is not working correctly [#1307](https://github.com/keeps/roda/issues/1307)
-  Going back does not clear table selection [#1306](https://github.com/keeps/roda/issues/1306)
-  Selecting checkboxes in lists causes exception in javascript [#1305](https://github.com/keeps/roda/issues/1305)
-  Search ignore character '?' not working [#1296](https://github.com/keeps/roda/issues/1296)
-  Ingest process list keeps selected item [#1293](https://github.com/keeps/roda/issues/1293)
-  Resolve of class not supported: org.roda.core.data.v2.log.LogEntry [#1292](https://github.com/keeps/roda/issues/1292)
-  AIP event and log count do not match the number of items in lists [#1291](https://github.com/keeps/roda/issues/1291)
-  Group permissions should be displayed in the AIP details slider [#1290](https://github.com/keeps/roda/issues/1290)
-  On table auto-update, checkboxes are de-selected [#1287](https://github.com/keeps/roda/issues/1287)
-  Popup menu may appear out of screen [#1284](https://github.com/keeps/roda/issues/1284)
-  Log entry parameters can exceed Solr string size limit [#1283](https://github.com/keeps/roda/issues/1283)
-  AIP corruption risk assessment (1.0) does not mitigate incidences after the risk has been solved [#1281](https://github.com/keeps/roda/issues/1281)
-  Counters on job execution don't seem right [#1280](https://github.com/keeps/roda/issues/1280)
-  Plugins without icon [#1279](https://github.com/keeps/roda/issues/1279)
-  Error changing permissions [#1277](https://github.com/keeps/roda/issues/1277)
-  On the staging server it fails when we upload files to RODA [#1276](https://github.com/keeps/roda/issues/1276)
-  Search page filtering fields that are not in that collection [#1274](https://github.com/keeps/roda/issues/1274)
-  List created packages of ingest job is not working [#1273](https://github.com/keeps/roda/issues/1273)
-  Move action popup does not show entity id [#1268](https://github.com/keeps/roda/issues/1268)
-  Translations are not working correctly [#1267](https://github.com/keeps/roda/issues/1267)
-  Unauthenticated API requests are not working [#1265](https://github.com/keeps/roda/issues/1265)
-  Back from AIP -> Event -> Agent doesn't work [#1258](https://github.com/keeps/roda/issues/1258)
-  Wrong icon when viewing a file [#1253](https://github.com/keeps/roda/issues/1253)
-  Moving folder does nothing and job reports success [#1245](https://github.com/keeps/roda/issues/1245)
-  Uploading a large number of files makes the "Done" button glitch [#1244](https://github.com/keeps/roda/issues/1244)
-  When editing AIP descriptive metadata, when changing from type to type the form never appears again [#1243](https://github.com/keeps/roda/issues/1243)
-  PDF viewer does not use the full window height [#1241](https://github.com/keeps/roda/issues/1241)
-  Error when registering error transforming lites into objects [#1236](https://github.com/keeps/roda/issues/1236)
-  Avoid duplicates when running plugin via filter in large scale [#1235](https://github.com/keeps/roda/issues/1235)
-  Error executing tasks defined via index filter when the task alters the result of the search [#1232](https://github.com/keeps/roda/issues/1232)
-  Simultaneous SIP updates [#1231](https://github.com/keeps/roda/issues/1231)
-  Wrong defaults for LDAP properties [#1230](https://github.com/keeps/roda/issues/1230)
-  Advanced search by date interval without latest that gives NPE [#1229](https://github.com/keeps/roda/issues/1229)
-  Change RI extra bundle permission to read instead of manage [#1221](https://github.com/keeps/roda/issues/1221)
-  RI relation count translation is not considering Representation and Files [#1220](https://github.com/keeps/roda/issues/1220)
-  RI relation count (on bottom) is counting non-active [#1219](https://github.com/keeps/roda/issues/1219)
-  Descriptive metadata form with separators gives NPE on clicking raw XML button [#1215](https://github.com/keeps/roda/issues/1215)
-  Ingest > Process > List Created Packages stopped working [#1213](https://github.com/keeps/roda/issues/1213)
-  File notification fails to index because recipientUsers is a required field [#1210](https://github.com/keeps/roda/issues/1210)
-  CAS API Authentication Filter does not generate user [#1209](https://github.com/keeps/roda/issues/1209)
-  Wrong date on Representation Information default corpora [#1204](https://github.com/keeps/roda/issues/1204)
-  When using CAS, with register active=false, a successful auth shows page with error 500 [#1199](https://github.com/keeps/roda/issues/1199)
-  When viewing a Representation Information & clicking in one tag, the sidebar does not go to the top [#1197](https://github.com/keeps/roda/issues/1197)
-  Double URL encoding on login redirection [#1194](https://github.com/keeps/roda/issues/1194)
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
-  Incomplete tooltip [#1051](https://github.com/keeps/roda/issues/1051)
-  Add translation on CSV when downloading it via statistics page [#1048](https://github.com/keeps/roda/issues/1048)
-  In browse, when looking an item with without siblings, it does not show Search options on sidebar [#1043](https://github.com/keeps/roda/issues/1043)
-  Sub-levels are not shown when browsing through catalogue with a facet selected [#1042](https://github.com/keeps/roda/issues/1042)
-  "Move orphan(s) to a parent node (1.0)" plugin reporting is wrong [#1040](https://github.com/keeps/roda/issues/1040)
-  Encoding problem [#1039](https://github.com/keeps/roda/issues/1039)
-  "AIP ancestor hierarchy fix (1.0)" fails with no logs [#1037](https://github.com/keeps/roda/issues/1037)
-  Moving a transferred resource with a long path makes an error [#1035](https://github.com/keeps/roda/issues/1035)
-  SIP update should try to find only active AIPs [#1034](https://github.com/keeps/roda/issues/1034)
-  The EAD visualisation XSLT has problems in groups, mappings and translations [#1027](https://github.com/keeps/roda/issues/1027)
-  Moving too many transferred resources gives an error [#993](https://github.com/keeps/roda/issues/993)
-  EARKSIPPluginsTest.testIngestAncestors() is throwing an unexpected error [#853](https://github.com/keeps/roda/issues/853)
-  A specific plugin execution thrown an exception yet job have completed with success [#837](https://github.com/keeps/roda/issues/837)
-  Error downloading PREMIS on demo server [#833](https://github.com/keeps/roda/issues/833)
-  PDF to PDFA conversion plugin reporting is not well [#619](https://github.com/keeps/roda/issues/619)

---

## v2.2.13 (25/10/2018)
Install for demonstration:
```
docker pull keeps/roda:v2.2.13
```


#### Enhancements:

- Add support for CORS [#1382](https://github.com/keeps/roda/issues/1382)
---

## v2.2.12 (11/10/2018)
Install for demonstration:
```
docker pull keeps/roda:v2.2.12
```


#### Enhancements:
- Add support for jsonp requests in IndexResource#list [#1375](https://github.com/keeps/roda/issues/1375)
---

## v2.2.11 (27/09/2018)
Install for demonstration:
```
docker pull keeps/roda:v2.2.11
```


#### Bug Fixes:
- CAS API auth allows incoherent state in basic auth fallback [#1349](https://github.com/keeps/roda/issues/1349)
---

## v2.2.10 (03/07/2018)
Install for demonstration:
```
docker pull keeps/roda:v2.2.10
```

Run locally for demonstration:
```
docker run -p 8080:8080 keeps/roda:v2.2.10
```
Open browser on "http://localhost:8080"

- username: admin
- password: roda

#### Enhancements:

-  Support CAS single sign out [#1250](https://github.com/keeps/roda/issues/1250)

#### Bug Fixes:

-  Unauthenticated API requests are not working [#1265](https://github.com/keeps/roda/issues/1265)
-  Wrong icon when viewing a file [#1253](https://github.com/keeps/roda/issues/1253)

---

## v2.2.9 (13/06/2018)
Install for demonstration:
```
docker pull keeps/roda:v2.2.9
```

#### Bug Fixes:

-  Unauthenticated users should have the same permissions as the user 'guest'  [#1249](https://github.com/keeps/roda/issues/1249)
