RODA 2.0 - Repository of Authentic Digital Objects
==============================================
RODA is a digital repository solution that delivers functionality for all the main units of the OAIS reference model. RODA is capable of ingesting, managing and providing access to the various types of digital objects produced by large corporations or public bodies. RODA is based on open-source technologies and is supported by existing standards such as the OAIS, METS, EAD and PREMIS.

## Features

* User-friendly graphical user interface based on HTML 5 and CSS 3
* Digital objects storage and management
* Catalog based on rich metadata (supports any XML-based format as descriptive metadata)
* Off-the-shelf support for Dublin Core (DC) and Enconded Archival Description (EAD).
* Configurable multi-step ingestion workflow
* PREMIS 3 for preservation metadata
* Authentication & authorization via LDAP and CAS (support for additional authentication methods)
* Reports and statistics
* REST API
* Pluggable preservation actions
* Integrated Risk management
* Integrated Format Registry
* Uses native file system for data storage for greater performance and transparency
* 100% compatible with E-ARK SIP, AIP, and DIP specifications
* Support for design themes

For more information, please fell free to visit RODA website:
**<http://www.roda-community.org>**


## Documentation

RODA is provided with a series of documentation articles that are constantly being updated. You may find all the available documentation [here](https://github.com/keeps/roda/tree/master/documentation).

All documentation articles are written in markdown, which means that you can easily converted to various formats such as PDF, HTML, etc. Check this online tool that converts markdown to PDF [http://www.markdowntopdf.com](http://www.markdowntopdf.com).

## Installation

We provide installation methods for testing and production environments for several different operating systems. For more information on the installation process, please visit our [documentation page](https://github.com/keeps/roda/tree/master/documentation).


## Usage

After installing, direct your browser to the correct IP address (this depends on your installation mode and used settings) and log in with the following credentials:

* Username: admin
* Password: roda

With these credentials you will have access to all features.

Then you can start using RODA. Here's an example of what you can do:

1. Go to Catalogue and click the button **NEW**, select Dublin Core and fill the title of your new collection.
2. Go to **Ingest > Transfer** and upload files (e.g. PDF) or SIPs made by [RODA-in](http://rodain.roda-community.org/). SIPs will have metadata while PDFs wont. To know how to use RODA-in [watch the tutorials](http://rodain.roda-community.org/).
3. After upload, select the SIPs or files to ingest on the checkbox and click the button **PROCESS** on the sidebar under the section Ingest.
4. Now configure the ingest workflow, select the SIP format, if you upload a file select **Uploaded file/folder**, if you uploaded a SIP select the SIP format (E-ARK or Bagit).
5. Under the **Parent Object** you can select the new collection you created above.
6. After configuring ingest click the **CREATE** button.
7. Now ingest will start and you can see the status of it at **Ingest > Process**, you can also inspect the status by clicking the table row.
8. When finished you can go to **Catalogue** or **Search** to find your new ingested content.


## Developers

To start developing new components for RODA check the [Developer guide](https://github.com/keeps/roda/blob/master/documentation/Developers_Guide.md) which has information on:

- How to get the source code
- How to build and run
- How to set up the development environment
- Code structure
- How to contribute
- etc.

## Translators

Translations are maintained in [Transifex](https://www.transifex.com/roda-1/roda2) and updated using the [Transifex Client](http://docs.transifex.com/client/).

Check our [Translation guide](https://github.com/keeps/roda/blob/master/documentation/Translation_Guide.md) for more information.
## Specifications

RODA implements a series of specifications and standards. To know more about the OAIS Information Packages that RODA implements, please check out the [DLM Archival Standards Board](http://www.dasboard.eu) repositories at https://github.com/DLMArchivalStandardsBoard

[![Build Status](https://travis-ci.org/keeps/roda.png?branch=master)](https://travis-ci.org/keeps/roda)

# Professional Support

We’re committed to providing the highest standard of service that empowers you to succeed in preserving your digital assets, on premises, in the cloud or anywhere in between. 

Professional Support is available as a single “pay-per-incident” (PPI) or anual support service. Professional Support incidents focus on troubleshooting a specific problem, error message, or functionality that is not working as intended. An incident is defined as a single support issue and the reasonable effort to resolve it. Incidents should be submitted online. Response time will be between 2 and 8 days, depending on severity of incident.

For more information and commercial support, please contact [KEEP SOLUTIONS](http://www.keep.pt).
