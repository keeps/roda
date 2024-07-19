
# Usage

RODA is a complete digital repository that delivers functionality for all the main units of the OAIS reference model. RODA is capable of ingesting, managing and providing access to the various types of digital objects produced by large corporations or public bodies. RODA is based on open-source technologies and is supported by existing standards such as the OAIS, METS, EAD and PREMIS.

RODA also implements a series of specifications and standards. To know more about the OAIS Information Packages that RODA implements, please check out the [Digital Information LifeCycle Interoperability Standards Board](http://www.dilcis.eu/) repositories on GitHub at https://github.com/dilcisboard.

## Features

* User-friendly graphical user interface based on HTML 5 and CSS 3
* Digital objects storage and management
* Catalogue based on rich metadata (supports any XML-based format as descriptive metadata)
* Off-the-shelf support for Dublin Core and Encoded Archival Description.
* Configurable multi-step ingestion workflow
* PREMIS 3 for preservation metadata
* Authentication and authorization via LDAP and CAS
* Reports and statistics
* REST API
* Supports pluggable preservation actions
* Integrated risk management
* Integrated format registry
* Uses native file system for data storage
* 100% compatible with E-ARK SIP, AIP, and DIP specifications
* Support for themess

For more information, please feel free to visit the RODA website:
**<https://www.roda-community.org>**


## Functions

RODA has UI support for the following functional entities.

### Catálogo

The catalogue is the inventory of all items or records found in the repository. A record can represent any information entity available in the repository (e.g. book, electronic document, image, database export, etc.). Records are typically aggregated in collections (or fonds) and subsequently organised in subcollections, sections, series, files, etc. This page lists all the top-level aggregations in the repository. You may drill-down to sub-aggregations by clicking on the table items.

### Buscar

In this page you can search for Intellectual Entities, Representations or Files (use the down arrow to select the search domain). For each one of these domains you can search in all its properties or in specific properties (use the down arrow to expand the advanced search). For example, if you select Intellectual Entities, you can search in a specific field of the descriptive metadata, or find files of a certain format if the Files advanced search is selected.

The search engine locates only whole words. If you want to search for partial terms you should use the '*' operator. For more information on the available search operators, take a look at the next section.

### Pre-ingest

In the search page you can search for Intellectual Entities, Representations or Files (use the down arrow to select the search domain). For each one of these domains you can search in all its properties or in specific properties (use the down arrow to expand the advanced search). For example, if you select Intellectual Entities, you can search in a specific field of the descriptive metadata, or find files of a certain format if the Files advanced search is selected.

### Instrucciones para la pre-ingesta

The pre-ingest process depicts the ability of a Producer to create Submission Information Packages (SIPs) containing both data and metadata (in a well-defined structure) in order to submit them to the repository for ingest. The SIPs created are expected to comply to the policies established by (or negotiated with) the repository.

### Transferir

The Transfer area provides the appropriate temporary storage to receive Submission Information Packages (SIPs) from Producers. SIPs may be delivered via electronic transfer (e.g. FTP) or loaded from media attached to the repository. This page also enables the user to search files in the temporary transfer area, create/delete folders and upload multiple SIPs to the repository at the same time for further processing and ingest. The ingest process may be initiated by selecting the SIPs you wish to include in the processing batch. Click the "Process" button to initiate the ingest process.

### Ingesta

The Ingest process contains services and functions to accept Submission Information Packages (SIPs) from Producers, prepare Archival Information Packages (AIPs) for storage, and ensure that Archival Information Packages and their supporting Descriptive Information become established within the repository. This page lists all the ingest jobs that are currently being executed, and all the jobs that have been run in the past. In the right side panel, it is possible to filter jobs based on their state, user that initiated the job, and start date. By clicking on an item from the table, it is possible to see the progress of the job as well as additional details.

### Evaluación

Assessment is the process of determining whether records and other materials have permanent (archival) value. Assessment may be done at the collection, creator, series, file, or item level. Assessment can take place prior to donation and prior to physical transfer, at or after accessioning. The basis of assessment decisions may include a number of factors, including the records' provenance and content, their authenticity and reliability, their order and completeness, their condition and costs to preserve them, and their intrinsic value.

### Acciones de preservación

Preservation actions are tasks performed on the contents of the repository that aim to enhance the accessibility of archived files or to mitigate digital preservation risks. Within RODA, preservation actions are handled by a job execution module. The job execution module allows the repository manager to run actions over a given set of data (AIPs, representations or files). Preservation actions include format conversions, checksum verifications, reporting (e.g. to automatically send SIP acceptance/rejection emails), virus checks, etc.

### Acciones internas

Internal actions are complex tasks performed by the repository as background jobs that enhance the user experience by not blocking the user interface during long lasting operations. Examples of such operations are: moving AIPs, reindexing parts of the repository, or deleting a large number of files.

### Usuarios y grupos

The user management service enables the repository manager to create or modify login credentials for each user in the system. This service also allows the manager to define groups and permissions for each of the registered users. Managers may also filter users and groups currently being displayed by clicking on the available options in the right side panel. To create a new user, click the button "Add user". To create a new user group, click the button "Add group". To edit an existing user or group, click on an item from the table.

### Log de actividad

Event logs are special files that record significant events that happen in the repository. For example, a record is kept every time a user logs in, when a download is made or when an modification is made to a descriptive metadata file. Whenever these events occur, the repository records the necessary information in the event log to enable future auditing of the system activity. For each event the following information is recorded: date, involved component, system method or function, target objects, user that executed the action, the duration of action, and the IP address of the user that executed the action. Users are able to filter events by type, date and other attributes by selecting the options available in the right side panel.

### Notificaciones

Notifications are a way to inform RODA users that certain events have occurred. This communication consists of sending an email describing the specific event, where the user may acknowledge it.

### Metadata formats

This page shows a dashboard of statistics concerning several aspects of the repository. Statistics are organised by sections, each of these focusing on a particular aspect of the repository, e.g. issues related to metadata and data, statistics about ingest and preservation processes, figures about users and authentication issues, preservation events, risk management and notifications.

### Registro de riesgos

The risk register lists all identified risks that may affect the repository. It should be as comprehensive as possible to include all identifiable threats, and generally contain an estimated probability of each risk event occurring, the severity or possible impact of the risk, and its probable timing or anticipated frequency. Risk mitigation is the process of defining actions to enhance opportunities and reduce threats to repository objectives.

### Información de representación de Registro

Representation information is any information required to understand and render both the digital material and the associated metadata. Digital objects are stored as bitstreams, which are not understandable to a human being without further data to interpret them. Representation information is the extra structural or semantic information, which converts raw data into something more meaningful.

### Format register (deprecated)

El registro es un registro Formato técnica para apoyar los servicios de preservación digital de los repositorios.
