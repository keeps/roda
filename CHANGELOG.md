# Changelog

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

## v6.0.1 (22/09/2025)
#### Enhancements 
- Change RODA packaging from WAR to JAR in order to use Spring [PropertiesLauncher](https://docs.spring.io/spring-boot/specification/executable-jar/property-launcher.html).


#### Bug fixes
- Add option for ChildFilter when using ChildDocTransform

#### Security
- Several dependency major upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v6.0.0 (29/07/2025)
### :warning: Breaking Changes

- Due to various dependency changes in this release, it is strongly recommended to back up all data and configurations before performing the upgrade. After upgrading, a complete reindexing of all data is required to maintain system integrity and performance.
- The embedded ApacheDS has been replaced by an external LDAP server running in an OpenLDAP container. Due to this change, starting RODA will cause previously stored user data to be lost. We recommend backing up all user information before upgrading. A migration process to transfer existing user data will be provided as soon as possible.
- The legacy REST API (v1) has been fully removed. All external integrations must now use the new REST API (v2)

#### New features:
- Major Web UI redesign: The RODA interface has been completely reimagined to deliver a cleaner, more intuitive, and user-friendly experience. This overhaul touches nearly every aspect of the UI, streamlining workflows, improving accessibility, and aligning with modern design standards. [3330](https://github.com/keeps/roda/issues/3330)
- Introduced a transactional storage mechanism that stages most write operations before committing them to the main storage, enabling rollback in case of errors and improving data integrity and reliability. [102](https://github.com/keeps/roda/issues/102)[1224](https://github.com/keeps/roda/issues/1224)
- The user database service has been upgraded from embedded ApacheDS to an external LDAP server with Spring LDAP integration, enhancing security, performance, and maintainability. [3115](https://github.com/keeps/roda/issues/3115)
- Added support for manual override of file format identification via the Web UI, allowing users to correct misidentified formats when automatic detection fails. [3256](https://github.com/keeps/roda/issues/3256)
- File format identification warnings now generate risk incidents, visible in the file information panel, allowing users to assess and accept potential issues like format mismatches or multiple matches.  [3259](https://github.com/keeps/roda/issues/3259)
- Improved audit log presentation by grouping related REST-API calls under single user actions and allowing inspection of detailed calls, enhancing clarity and reducing noise in the Web UI. [3383](https://github.com/keeps/roda/issues/3383)
- Added support for advanced search over nested items using Solr block join queries, enabling more precise queries across hierarchical metadata structures via new filter parameters: ParentWhichFilterParameter and ChildOfFilterParameter [3322](https://github.com/keeps/roda/issues/3322)
- Added support for external user group mapping by allowing administrators to define mappings between CAS attributes and RODA groups through configuration. User group membership is now resolved dynamically at login based on the external attribute (e.g. memberOf) and assigned to corresponding RODA groups [3499](https://github.com/keeps/roda/pull/3499)

#### Changes:
- Migrated all GWT-RPC interface methods to REST API, reducing dependency on GWT and aligning with modern web architecture practices. [2060](https://github.com/keeps/roda/issues/2060)
- Removed the sourceObjects field from the JobCollections index to prevent Solr overload caused by large identifier lists, improving system scalability and stability. Adjusted interface components to retrieve object data from the model instead of the index as needed.  [3307](https://github.com/keeps/roda/issues/3307)
- Added welcome pages for languages other than English and Portuguese, improving user onboarding for a wider audience. [7c506370f](https://github.com/keeps/roda/commit/7c506370f22fd598ecaa48f5b26714ca4e3dbb8e)
- Reviewed and updated pre-ingest text [3412](https://github.com/keeps/roda/pull/3412)
- Improve support for E-ARK SIP administrative metadata (amdSec) [3380](https://github.com/keeps/roda/issues/3380)
- Added detailed prompts and outcome tracking for lifting disposal holds, including preservation event generation via ModelService. Replaced liftDisposalHoldBySelectedItems API calls with dissociateDisposalHold for disposal hold removal.  [3235](https://github.com/keeps/roda/pull/3235)
- Added indexing support for technical metadata to improve searchability and metadata management. [0723959e](https://github.com/keeps/roda/commit/0723959e45f137fee982d67450058fc8e757426a)

#### Security:
- Several dependency major upgrades to fix security vulnerabilities

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
---

## v5.6.0 (04/04/2024)
#### New features 

- Auto refresh after the session expires

#### Enhancements 

- Update representation information links

#### Security
- Several dependency major upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.5.3 (13/03/2024)
#### Security
- Several dependency major upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.5.2 (11/03/2024)
#### Bug fixes
- Fixed other metadata download #3117


#### Security
- Several dependency major upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).

---

## v5.5.1 (08/03/2024)
#### Bug fixes
- Remove "opt-in" from roda-core.properties #3113
- Fix ns2 namespace in premis.xml when creating technical metadata  #3114 

#### Security
- Several dependency major upgrades to fix security vulnerabilities


---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).

---

## v5.5.0 (04/03/2024)
#### New features
-  Support for generic technical metadata creation and visualization #3097

#### Bug fixes
- Fixed unexpected behaviour when trying to create a new AIP #3110
- Fixed AIP permissions calculation using ModelService #3105 

#### Security
- Several dependency major upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.4.0 (08/02/2024)
#### New features
-  Technological platforms major upgrade, which largely improves overall security, maintanability and performance #3055
-  Adding support for the latest version of the [E-ARK SIP specification](https://dilcis.eu/specifications/sip) (version 2.1.0) #3046
-  Support [trusting the your own plugins](https://github.com/keeps/roda/blob/master/documentation/Plugin_signing.md) #3059

#### Enhancements
-  Added help text to Agents register page that was missing #2831 
-  Added close button to license popup #2975
-  Improved documentation about default permissions #3045
- Other small improvements #3063

#### Bug fixes
-  Fixed "Clear" button in search component that did not behave as expected #3062
-  Fixed the Event Register menu entry that did not match the title of page #2832
-  Fixed Date and time of last transfer resource refresh in RODA interface only updated when reloading the page #3038 
-  Fixed default permissions issue when reading admin user permissions from configuration #3066 

#### Security
- Several dependency major upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.3.1 (11/01/2024)
#### Bug fixes:
- Changed default permissions to old behaviour #3043

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.3.0 (14/12/2023)
#### Enhancement:
- Added tool tip to plugin license verification panel #2974
#### New features:
- Added permissions configuration for newly created AIPs #3032
#### Bug fixes:
- Unable to perform actions even having right permissions #2986
- Ingest jobs created in RODA 4 cannot be accessed on the interface of RODA 5 #3037
- Problem using index REST API without filter #2962
#### Security:
- Several dependency upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.2.5 (06/12/2023)
#### Bug fixes:

- Error sending ingestion failure notification via email #3023 

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).
---

## v5.2.4 (10/11/2023)
#### Enhancements:

- Update Swedish translation language

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).


---

## v5.2.3 (10/11/2023)
#### What's new:

- New German (Austrian) translation of the Web interface :austria: 

#### Bug fixes:

- Create folder access-keys when initializing RODA for the first time #2992
- Add default representation type when creating a preservation action job #2990
- Edit button for selecting parent does not work as expected #2988
- EAD 2002 dissemination crosswalk duplicates record group level #2987

#### Enhancements:

- Add title attribute to improve accessibility #2989

#### Security:

- Bump several dependencies

---

To try out this version, check the [install instructions](https://github.com/keeps/roda/blob/master/deploys/standalone/README.md).


---

## v5.2.2 (04/10/2023)
#### Bug fixes:
- Fixed FileID when it is encoded #2963
- Fixed API filter issue #2965

#### Security:
- Several dependency upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://www.roda-community.org/deploys/standalone/).


---

## v5.2.1 (08/09/2023)
#### Bug fixes:
- Listing RODA objects via REST-API is not showing any results #2935
- Preservation events page is not showing no events #2928
- REST API endpoint to retrieve the last transferred resource report does not show the reports #2929
- Problem with pre-filter not being reset when searching preservation events #2941

#### Security:
- Several dependency upgrades to fix security vulnerabilities

---

To try out this version, check the [install instructions](https://www.roda-community.org/deploys/standalone/).


---

## v5.2.0 (28/07/2023)
#### Enhancements:
- DIP must be deleted if it no longer contains any link with any entity. #2863
- Ingest job report could expose if SIP is update #2212

#### Bug fixes:
- Unexpected behaviour can cause index to be completely deleted #2921

#### Security:
- Several dependency upgrades to fix security vulnerabilities
- Remove python from Docker image

---

To try out this version, check the [install instructions](https://www.roda-community.org/deploys/standalone/).

