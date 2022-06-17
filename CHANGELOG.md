# Changelog

## v4.4.0 (17/06/2022)
### :warning: Breaking Changes
Solr 7.7 reached EOL meaning that is no longer supported and will not receive any security patches. As such RODA from version 4.4 onward will use Solr 8 as index system. If you have any implementation with Solr 7 you need to upgrade the Solr to version 8 and then rebuild all indexes on RODA.

#### New features:

- Upgrade Solr version from 7.7 to Solr 8.11.1
- Upgrade GWT version from 2.9.0 to 2.10.0

Install for demonstration:
```
docker pull keeps/roda:v4.4.0
```
---

## v4.3.1 (17/06/2022)
#### Bug Fixes:

- Edit descriptive metadata from an AIP with a disposal schedule gives an error #2190 
- Multiple plugin assumes last plugin state in final report #2067
- Preservation event and incident risk counters on representation panel #2064

Install for demonstration:
```
docker pull keeps/roda:v4.3.1
```


---

## v4.3.0 (26/04/2022)
#### New features:

-  Akka events with Zookeeper seed registration [#2001](https://github.com/keeps/roda/issues/2001)

#### Enhancements:

-  Add error message to ClientLogger for fatal method [#2002](https://github.com/keeps/roda/issues/2002)

#### Bug Fixes:

-  Ambiguous representation PREMIS relatedObjectIdentifierValue [#1993](https://github.com/keeps/roda/issues/1993)
-  Classification scheme won't load because of unrecognized field "type" [#1986](https://github.com/keeps/roda/issues/1986)
-  Reject assessment is creating premis events on AIP, change this event for Repository level [#1984](https://github.com/keeps/roda/issues/1984)

Install for demonstration:
```
docker pull keeps/roda:v4.3.0
```

---

## v3.7.0 (21/04/2022)

#### New features:

-  Akka events with Zookeeper seed registration [#2001](https://github.com/keeps/roda/issues/2001)

#### Enhancements:

-  Add error message to ClientLogger for fatal method [#2002](https://github.com/keeps/roda/issues/2002)

Install for demonstration:
```
docker pull keeps/roda:v3.7.0
```

---

## v3.6.4 (11/03/2022)
#### Bug Fixes:

-  Fix job orchestration displayed badges

Install for demonstration:
```
docker pull keeps/roda:v3.6.4
```

---

## v3.6.3 (11/03/2022)

#### Enhancements:

-  Remove trace logs from logback configuration [#1994](https://github.com/keeps/roda/issues/1994)

#### Bug Fixes:

-  Generalized noneselect option when building search filter [#1995](https://github.com/keeps/roda/issues/1995)

Install for demonstration:
```
docker pull keeps/roda:v3.6.3
```

---

## v3.6.2 (17/02/2022)
### Bug fixes:

- CAS Login issue when user with the same email already exists #1988

Install for demonstration:
```
docker pull keeps/roda:v3.6.2
```
---

## v4.2.1 (15/02/2022)
### Bug fixes:

- CAS Login issue when user with the same email already exists #1988

### Enhancements:

- Add under appraisel status to transferred resources deletion #1989
- Replace embed marked.js by webjar #1983

Install for demonstration:
```
docker pull keeps/roda:v4.2.1
```

---

## v3.6.1 (26/01/2022)
### Bug fixes:

- Update Dockerfile base image

Install for demonstration:
```
docker pull keeps/roda:v3.6.1
```

---

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

## v3.5.7 (17/01/2022)
#### Enhancements:
- Expand Portal to allow customization via AIP level #1969


Install for demonstration:
```
docker pull keeps/roda:v3.5.7
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
