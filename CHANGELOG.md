# Changelog

## v5.8.4 (23/11/2026)
#### Security
This is a security maintenance release for RODA 5. All RODA 5 installations should upgrade.

**Docker image rebuilt on an updated base image**
- Fixes CVE-2026-114938. RODA's code did not change for this; the fix comes from rebuilding the image on the current `tomcat:10-jre21` base image. Users of the official Docker image only need to pull `keeps/roda:v5.8.4` (or `v5.8` / `v5`).

**Dependency upgrades** (patch-level only; Spring Boot stays on 3.4.x)
- Netty 4.1.127 → 4.1.141. Fixes CVE-2026-75595 (critical): SNI routing bypass through a fragmented TLS ClientHello.
- Apache MINA 2.2.3 → 2.2.10, used by the embedded LDAP server (ApacheDS). Fixes CVE-2024-52046, CVE-2026-41409, CVE-2026-41635, CVE-2026-42778, CVE-2026-42779 and CVE-2026-47065 (unsafe deserialization that can lead to remote code execution).
- Spring Framework 6.2.11 → 6.2.19.
- Tomcat embed 10.1.46 → 10.1.62, for the WAR/embedded run. The Docker image's Tomcat comes from the base image.
- ZooKeeper client 3.9.3 → 3.9.5.

**Scanner findings that do not affect RODA**
- CVE-2026-47884 and CVE-2026-47890 (spring-webmvc) are only fixed upstream in Spring Framework 7. RODA does not use the affected features (`XsltView`, and Server-Sent Events rendered with views), so these findings do not apply.

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/roda-5/deploys/standalone/README.md).
---

## v5.8.3 (02/11/2026)
#### Bug fixes
- Fixed missing audit log entry when a user is automatically created on first login through an external authentication provider
---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.8.2 (12/10/2026)
#### Bug fixes

- Fixed move bug where descendants were not reindexed (https://github.com/keeps/roda/commit/6e9c6036cc75e7595651a332c8ca823d9568c57a)
- Fixed empty preservationLevelValue (https://github.com/keeps/roda/commit/02689f1765fcbc2cef59ddeb9b1603666b0808d7)

---

## v5.8.1 (14/07/2026)
#### Bug fixes

- Fix false positive results when running File Integrity Verifier plugin on historic AIPs c28987b2049ce35568361353503c8d9205dfdaea
- Fix historical date time zone; Replace DateBox with HTML5 date input for civil dates in create/edit descriptive metadata and advanced search 60532a0cdfbb186c26275e9a576bf69e9df4ae39

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.8.0 (29/06/2026)
#### New features 
- Added configuration for inventory report checksum algorithms (https://github.com/keeps/roda/commit/a51dddd2bd5b70731f3e06726d8570981692ad9a)


---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.16 (27/05/2026)
#### Security
- Bumped tomcat version

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.15 (08/04/2026)
#### Security
- Several dependency major upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.14 (09/03/2026)
#### New features 

#### Bug fixes

- Fix File Integrity Verifier plug-in

#### Security
- Several dependency major upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.13 (19/01/2026)
#### Enhancements 
- Added translations to access key management 

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.12 (05/01/2026)
#### Enhancements 
- Updating preservation agent only when its different from the existing one


---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.11 (09/12/2025)
#### Enhancements 
- Enable HTTP Notification Processor to have HTTP header 

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.10 (12/11/2025)
#### Bug fixes
- Fixed incorrect HTTP error code when using REST-API and Bearer authentication

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.9 (03/11/2025)
#### Enhancements 

- Enhance support for representation status other than Original when ingesting a E-ARK SIP package

#### Security
- Several dependency major upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.8 (03/11/2025)
#### Bug fixes

- Add UTF-8 encoding when downloading files with non-ASCII characteres

#### Security
- Several dependency major upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.7 (24/09/2025)
#### Enhancements 
- SecurityHeadersFilter is now fully configurable via properties. 7f7df9a3fd2a149df5b0dfee74264522cafb4a0f
- Added support for DIP lists in the portal. 91d5d0efccaca007271b90c0e61ba3076dffbc82
#### Bug fixes
- Storing the reason for created events aa4781eb89fec469ead45b67a9a9cfbdf762d205
- Resolved #3505: added support for both String and List values in conditionTypeMetadataValue

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.6 (27/06/2025)
#### Bug fixes
- Updated dependency of jaxb for glassfish (#3411)

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.5 (19/05/2025)
#### Security
- Several dependency major upgrades to fix security vulnerabilities

---

To try this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.4 (29/04/2025)
#### Enhancements 
- Improve support for E-ARK SIP administrative metadata (amdSec) #3380

#### Bug fixes
- NPE when editing a user via profile #3405


#### Security
- Several major dependency upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.3 (03/04/2025)
#### Security
-  Fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.2 (24/03/2025)
#### Bugs

- Disposal confirmation cancel button message #3303

#### Enhancements 

- Missing translations for disposal rules order panel #3312

#### Security
- Several major dependency upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.1 (08/01/2025)
#### Bug fixes

- Fix built-in plugin "AIP ancestor hierarchy fix"
- Deleting linked DIPs now longer increments objects processed (#3285)

#### Security
- Several dependency major upgrades to fix security vulnerabilities

---

To try this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.0 (05/09/2024)
#### Security
- Several dependency major upgrades to fix security vulnerabilities
- Improve HTTP headers security

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.7.0-beta1 (21/06/2024)
#### New features 

- Replace Akka with Apache Pekko

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.6.5 (07/06/2024)
#### Bug fixes

- Roda fails to resolve other metadata with folders #3219

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.6.4 (06/06/2024)
#### Bug fixes

- Roda fails to reindex due to problem with other metadata files #3218

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.6.3 (23/05/2024)
#### Bug fixes

- Revert webjars-locator functionality

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.6.2 (22/05/2024)
#### Bug fixes

- Base roda overwrites the configuration regarding user permissions in roda-config.properties #3189

#### Security
- Dependency major upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.6.1 (03/05/2024)
#### Bug fixes

- Custom E-ARK SIP representation type not being set when ingesting a E-ARK SIP #3139

#### Security
- Several dependency major upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
