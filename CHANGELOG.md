# Changelog

## v4.5.3 (25/01/2023)
#### Bug fixes:

- Support very large queries to Solr (fix regression) #2311

#### Enhancements:

- Add icon to experimental plugin categories #2306

Install for demonstration:
```
docker pull keeps/roda:v4.5.3
```
---

## v4.5.2 (19/01/2023)
#### Bug fixes:

- Failsafe fallback policy misconfigured #2303

Install for demonstration:
```
docker pull keeps/roda:v4.5.2
```
---

## v4.5.1 (16/01/2023)
#### Enhancements:

- Refactor RetryPolicyBuilder #2296
- Improve log information during initialization process #2297
- Add metrics about retries (related to RetryPolicyBuilder) #2298

Install for demonstration:
```
docker pull keeps/roda:v4.5.1
```

---

## v4.5.0 (06/01/2023)
### :warning: Breaking Changes
Due to a dependency upgrade from Apache DS a migration procedure should be executed:

1. Perform a LDAP backup
2. Remove the ldap folder inside config directory
3. Start RODA
4. Restore the LDAP backup.

#### New features:

- Solr retry #1216
- Add saved search functionality #2283

#### Enhancements:

- Dialogs.prompt() lack of feedback when input is invalid #908

#### Bug Fixes:

- Fixity information computation does not create an event when skipped #2291
- File leak when listing disposal resources #2250
- Ingest Assessment search filter does not clear up #2263

#### Security:

- Several dependency upgrades to fix security vulnerabilities

Install for demonstration:
```
docker pull keeps/roda:v4.5.0
```

---

## v4.5.0-RC5 (28/12/2022)

---

## v4.5.0-RC4 (27/12/2022)

---

## v4.5.0-RC3 (13/12/2022)

---

## v4.5.0-RC2 (09/12/2022)

---

## v4.5.0-RC (19/09/2022)

---

## v3.7.1 (01/08/2022)
#### Bug Fixes:

- Processes with different types of parallelism are sharing the same pool of workers #2211
- Deleting a Representation is also deleting PREMIS file #2033

Install for demonstration:
```
docker pull keeps/roda:v3.7.1
```
---

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
