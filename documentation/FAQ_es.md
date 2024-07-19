# Frequently Asked Questions

Frequent questions that are asked by RODA users and their answers.

Do you have a burning question that is not here? Just [create an issue](https://github.com/keeps/roda/issues/new) on GitHub and mark it with a "question" label.

## Viewers

### Can we preview files directly on the Web interface of RODA?

The system comes with a few predefined viewers for certain standard formats (e.g. PDF, images, HTML 5 multimedia formats, etc.).

Special formats need special viewers or converters to adapt them to existing viewers, e.g. SIARD 2 viewer. These are developments that need to be undertaken case by case.

## Metadata

### What descriptive metadata formats are supported by RODA?

All descriptive metadata formats are supported as long as there is a grammar in XML Schema (XSD) to validate it. By default, RODA comes configured with Dublin Core and Encoded Archival Description 2002. More schemas can be added.

### Can RODA support multiple classification schemes?

The system enables the definition of multiple hierarchical structures where one can place records. To each of the nodes of these structures we can assign descriptive metadata. Picture this as a file/folder system where each folder can have custom metadata in EAD or DC format (or any other format for that matter). Each of these “folders” (or placeholders) can be a fonds, collection, series, or aggregation, etc.

### Does the system provide in possibilities to inherit metadata from higher levels in the structure?

Not currently. A plugin would have to be developed.

### Can the unit of description be linked to one or more files in an other archive or system?

Unit of descriptions are part of the AIP (Archival Information Package), which mean that representations and files are usually closely tied to the record’s metadata. However, it is possible to add HTTP links to other resources that sit outside the repository by placing them in in the descriptive metadata.

### Is it possible to link an archival description to a contextual entity (e.g.ISAAR authority)? 

The system does not support authority records internally, however, if you manage these records externally, you may link to them by editing the descriptive metadata. 

### How to support hybrid archives (paper and digital)?

It is possible to have records without digital representations, i.e. only with metadata. From a catalogue perspective, this is typically sufficient to support paper archives.

### Can the application record the level of transfer, e.g. who transferred what, when?

SIPs typically include information about who, what and when they have been created. The ingest process creates records of the entire ingest process. However, SIPs are expected to be placed on a network location that is accessible by the system. Determining who copied SIPs to these locations is outside of the scope of the system. 

### How can the system record the location of physical archives? 

It can be handled by filling a metadata field. Typically <ead:physloc>.

## Buscar

### What metadata attributes can we search on? 

The search page is completely configurable via config file. You may set the attributes, types, label names, etc.

### Is full text search supported?

Yes, natively supported by advanced search.

### Can a user request analogue documents from the archives from the search result?

No. It would have to be integrated with an external system that would handle these requests

### Does the search result list reflect the permissions applied to the records presented?

Yes. You can only see the records to which you have access to.

### Is the audit trail searchable and accessible in a user friendly way?

Yes. You can navigate on the actions log (entire set of actions performed on the repository) or on preservation metadata (list of preservation actions performed on the data) right from the Web user interface.

## Preservación

### Describe the functioning of the quarantaine environment.

When SIPs are being processed during ingest, if they fail to be accepted they are moved to a special folder on the filesystem. The ingest process generates a detailed report that describes the reasons for the rejection. Manual care must be taken from that point on.

### How does the system support preservation? 

This is a complex question that cannot be answered in just a few lines of text. That being said, we can say that the system handles preservation in multiple ways:

- Actions exist that perform regular fixity checks of the ingested files and warn the repository managers if any problem is detected
- The system comes with an embedded risk management GUI (i.e. risk registry)
- Actions exist that detect risks on files and add new threats to the risk registry that have to manually tackled (e.g. a record is not sufficiently described, a file does not follow the format policy of the repository, a file format is unknown or there is no representation information, etc.).
- Actions exist that allows the preservation managers to mitigate risks, e.g. perform file format conversions (tens of formats supported).

### How does the application support appraisal, selection the definition of retention periods?

RODA provides a complex workflow for disposal of records. Please refer to [Disposal](Disposal.md) for more information.

### Is the system logging search interactions?

Yes. Every action in the system is logged.

## Requirements

### Are there any system requirements on the client side for those who consult the archives?

Not really. A modern browser is sufficient.

## How to

### How to add a new language to the system?

Complete instructions on how to add a new language to the system are available at: [Translation guide](Translation_Guide.md).

### How to set up the development environment for RODA?

Complete instructions on how to set up the development environment are available at: [Developers guide](Developers_Guide.md).
