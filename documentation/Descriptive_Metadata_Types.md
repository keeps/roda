# Descriptive metadata types

When creating a new intellectual entity, one of the steps is to select the "type" of descriptive metadata.

This refers to the descriptive metadata scheme that will be used, and by default RODA supports the following options:

* **[EAD 2002](https://www.loc.gov/ead/)**: Encoded Archival Description (EAD) version 2002 is an XML standard for encoding archiving finding aids, maintained by the Technical Subcommittee for Encoded Archival Standards of the Society of American Archivists, in partnership with the Library of Congress. It is mainly used by archives to describe both digitally-born and analog documents.
* **[Dublin Core](https://www.dublincore.org/schemas/xmls/)**: The Dublin Core (DC) Metadata Initiative supports innovation in metadata design and best practices. Currently recommended schemas include the *Simple DC XML schema, version 2002-12-12*, which defines terms for Simple Dublin Core, i.e. the 15 elements from the http://purl.org/dc/elements/1.1/ namespace, with no use of encoding schemes or element refinements.
* **[Key-value](https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/resources/config/schemas/key-value.xsd)**: An RODA internal simple description schema for key-value metadata definition, where the metadata key identifies the element (e.g. "title") and the value the content of the metadata element.
*  **Other**: Generic XML type where no schema is defined.

New metadata types can be added to RODA following the documentation [Metadata formats](Metadata_Formats.md).

| Descriptive metadata type | Validation           | Indexing         | Visualization         | Edition      |
|---------------------------|----------------------|------------------|-----------------------|--------------|
| EAD 2002                  | Schema validation    | Indexing rules   | Visualization rules   | Edition form |
| Dublin Core               | Schema validation    | Indexing rules   | Visualization rules   | Edition form |
| Key-value                 | Schema validation    | Indexing rules   | Visualization rules   | Edition form |
| Other                     | Wellformedness check | General indexing | Generic visualization | XML edit     |

Legend:
* **Schema validation**: The repository offers an XML schema to validate the structure and data types of the provided metadata file. The Validation schema will be used during ingest process to check if the metadata included in the SIP is valid according the established constraints, as well as when the metadata is edited via the catalogue.
* **Wellformedness check**: The repository will only check if the metadata XML file is well-formed and since no schema is defined the repository will not verify if the file is valid.
* **Indexing rules**: The repository provides a default XSLT that transforms the XML-based metadata into something that the indexing engine is able to understand. Enabling advanced search over the descriptive metadata.
* **General indexing**: The repository will index all text elements and attribute values found on the metadata file, however because the repository does not know the right mapping between the XML elements and the inner data model, only basic search will possible on the provided metadata.
* **Visualization rules**: The repository provides a default XSLT that transforms the XML-based metadata into an HTML file that will be shown to the user when browsing an existing AIP on the catalogue.
* **Generic visualization**: The repository provides a generic metadata viewer to display the XML-based metadata. All text elements and attributes will show in no particular order and their XPath will be used as the label. 
* **Edition form**: The repository provides a configuration file will instruct on how to display a form to edit existing metadata.
* **XML edit**: The repository will display a text area where the user is able to edit the XML directly.