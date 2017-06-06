# Metadata formats

RODA supports any descriptive metadata format (i.e. Descriptive Information as stated in the OAIS) as long as it represented by an XML file. If you have a descriptive metadata format that is not based on XML (e.g. CSV, JSON, MARC21, etc.), you will have to convert it to XML before you can use in RODA. Several tools exist on the Web that allow you to convert most data formats into XML.

Once you have your metadata in XML you are ready to package it into a Submission Information Package (SIP) and ingest it on the repository. Alternatively, you may want to create a metadata file directly on the repository by using the functionality provided by the Catalogue. When the metadata format is new to RODA, the repository will do its best to support without the need to do any reconfiguration of system, however, the following limitations apply:

#### Validation

If no schema is provided for your metadata format, the repository will check if the metadata XML file is well-formed, however because the repository has no notion of the right grammar, it will not verify if the file is valid.

#### Indexing

The repository will index all text elements and attribute values found on the metadata file, however because the repository does not know the right mapping between the XML elements and the inner data model, only basic search will possible on the provided metadata.

#### Visualization

When no visualization mappings are configured, a generic metadata viewer will be used by the repository to display the XML-based metadata. All text elements and attributes will shown in no particular order and their XPath will be used as the label.

#### Edition

RODA needs a configuration file to inform how metadata files should be displayed for editing purposes. If no such configuration exists, the repository will display a text area where the user is able to edit the XML directly.

In order to support new metadata formats, the repository must be configured accordingly. The following sections describe in detail the set of actions that need to be performed to fully support a new metadata schema in RODA.

## Metadata enhancement files

To enhance the metadata experience in RODA there are 4 files that need to be added to the repository configuration folders. The following sections describe and provide examples of such files.

### Validation

RODA uses a [XML schema](http://www.w3.org/standards/xml/schema) to validate the structure and data types of the provided metadata file. The Validation schema will be used during ingest process to check if the metadata included in the SIP is valid according the the established constraints, as well as when the metadata is edited via the catalogue.

You may use a standard schema file for validation purposes or create a specific one that verifies all the particular conditions that you need to check in your repository installation, e.g. mandatory fields, closed-vocabularies for the values of certain elements, etc.

The validation schema files should be placed on the configuration folder under `[RODA_HOME]/config/schemas/`.

Most metadata formats are published together with documentation and a XML schema. The following example represents the schema for the Simple Dublin Core metadata format:

```
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema  xmlns:xs="http://www.w3.org/2001/XMLSchema"
            xmlns:dc="http://purl.org/dc/elements/1.1/"
            elementFormDefault="qualified"
            attributeFormDefault="unqualified">

  <xs:import namespace="http://www.w3.org/XML/1998/namespace"
    schemaLocation="xml.xsd">
  </xs:import>

  <xs:complexType name="SimpleLiteral">
    <xs:complexContent mixed="true">
      <xs:restriction base="xs:anyType">
        <xs:sequence>
          <xs:any processContents="lax" minOccurs="0" maxOccurs="0"/>
        </xs:sequence>
        <xs:attribute ref="xml:lang" use="optional"/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>

  <xs:element name="any" type="SimpleLiteral" abstract="true"/>

  <xs:element name="title" substitutionGroup="any"/>
  <xs:element name="creator" substitutionGroup="any"/>
  <xs:element name="subject" substitutionGroup="any"/>
  <xs:element name="description" substitutionGroup="any"/>
  <xs:element name="publisher" substitutionGroup="any"/>
  <xs:element name="contributor" substitutionGroup="any"/>
  <xs:element name="date" substitutionGroup="any"/>
  <xs:element name="type" substitutionGroup="any"/>
  <xs:element name="format" substitutionGroup="any"/>
  <xs:element name="identifier" substitutionGroup="any"/>
  <xs:element name="source" substitutionGroup="any"/>
  <xs:element name="language" substitutionGroup="any"/>
  <xs:element name="relation" substitutionGroup="any"/>
  <xs:element name="coverage" substitutionGroup="any"/>
  <xs:element name="rights" substitutionGroup="any"/>

  <xs:group name="elementsGroup">
    <xs:sequence>
      <xs:choice minOccurs="0" maxOccurs="unbounded">
        <xs:element ref="any"/>
      </xs:choice>
    </xs:sequence>
  </xs:group>

  <xs:complexType name="elementContainer">
    <xs:choice>
      <xs:group ref="elementsGroup"/>
    </xs:choice>
  </xs:complexType>

   <xs:element name="simpledc" type="elementContainer"/>
</xs:schema> 
```

### Indexing

The _Indexing_ activity is supported by a [XSLT (eXtensible Stylesheet Language Transformations)](http://www.w3.org/standards/xml/transformation.html) that transforms the XML-based metadata into something that the indexing engine is able to understand. This file is responsible for selecting the data that is expected to be indexed, map data into specific field names, and instruct the engine on how the data is expected to be indexed based on its data type (i.e. text, number, date, etc.).

The Index map file should be added to the configuration folder under `[RODA_HOME]/config/crosswalks/ingest/`.

The following is an example of an Index map for the Simple Dublin Core example.

```
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
  exclude-result-prefixes="dc">
  <xsl:output method="xml" indent="yes" encoding="UTF-8"
    omit-xml-declaration="yes" />

  <xsl:template match="/">
    <doc>
      <xsl:apply-templates />
    </doc>
  </xsl:template>
  <xsl:template match="simpledc">
    <xsl:if test="count(title)  &gt; 0">
      <xsl:if test="title[1]/text()">
        <field name="title">
          <xsl:value-of select="title[1]/text()" />
        </field>
      </xsl:if>
      <xsl:for-each select="title">
        <xsl:if test="normalize-space(text())!=''">
          <field name="title_txt">
            <xsl:value-of select="text()" />
          </field>
        </xsl:if>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="count(description)  &gt; 0">
      <xsl:if test="description[1]/text()">
        <field name="description">
          <xsl:value-of select="description[1]/text()" />
        </field>
      </xsl:if>
      <xsl:for-each select="description">
        <xsl:if test="normalize-space(text())!=''">
          <field name="description_txt">
            <xsl:value-of select="text()" />
          </field>
        </xsl:if>
      </xsl:for-each>
    </xsl:if>
    <xsl:for-each select="contributor">
      <xsl:if test="normalize-space(text())!=''">
        <field name="contributor_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="coverage">
      <xsl:if test="normalize-space(text())!=''">
        <field name="coverage_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="creator">
      <xsl:if test="normalize-space(text())!=''">
        <field name="creator_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:if test="count(date)  &gt; 0">
      <xsl:if test="count(date)  &lt; 2">
        <xsl:analyze-string regex="^\d{{4}}-\d{{2}}-\d{{2}}$"
        select="date[1]/text()">
        <xsl:matching-substring>
          <xsl:variable name="date">
            <xsl:value-of select="regex-group(0)" />
          </xsl:variable>
          <xsl:if test="not(normalize-space($date)='')">
            <field name="dateInitial">
              <xsl:value-of select="$date" />
              <xsl:text>T00:00:00Z</xsl:text>
            </field>
            <field name="dateFinal">
              <xsl:value-of select="$date" />
              <xsl:text>T00:00:00Z</xsl:text>
            </field>
          </xsl:if>
        </xsl:matching-substring>
      </xsl:analyze-string>
      </xsl:if>
      <xsl:if test="count(date)  &gt; 1">
        <xsl:analyze-string regex="^\d{{4}}-\d{{2}}-\d{{2}}$" select="date[1]/text()">
          <xsl:matching-substring>
            <xsl:variable name="date">
              <xsl:value-of select="regex-group(0)" />
            </xsl:variable>
            <xsl:if test="not(normalize-space($date)='')">
              <field name="dateInitial">
                <xsl:value-of select="$date" /><xsl:text>T00:00:00Z</xsl:text>
              </field>
            </xsl:if>
          </xsl:matching-substring>
        </xsl:analyze-string>
        <xsl:analyze-string regex="^\d{{4}}-\d{{2}}-\d{{2}}$" select="date[2]/text()">
          <xsl:matching-substring>
            <xsl:variable name="date">
              <xsl:value-of select="regex-group(0)" />
            </xsl:variable>
            <xsl:if test="not(normalize-space($date)='')">
              <field name="dateFinal">
                <xsl:value-of select="$date" />
                <xsl:text>T00:00:00Z</xsl:text>
              </field>
            </xsl:if>
          </xsl:matching-substring>
        </xsl:analyze-string>
      </xsl:if>
      <xsl:for-each select="date">
        <xsl:if test="normalize-space(text())!=''">
          <field name="date_txt">
            <xsl:value-of select="text()" />
          </field>
        </xsl:if>
      </xsl:for-each>
    </xsl:if>

    <xsl:for-each select="format">
      <xsl:if test="normalize-space(text())!=''">
        <field name="format_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="identifier">
      <xsl:if test="normalize-space(text())!=''">
        <field name="identifier_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="language">
      <xsl:if test="normalize-space(text())!=''">
        <field name="language_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="publisher">
      <xsl:if test="normalize-space(text())!=''">
        <field name="publisher_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="relation">
      <xsl:if test="normalize-space(text())!=''">
        <field name="relation_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="rights">
      <xsl:if test="normalize-space(text())!=''">
        <field name="rights_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="source">
      <xsl:if test="normalize-space(text())!=''">
        <field name="source_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="subject">
      <xsl:if test="normalize-space(text())!=''">
        <field name="subject_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="type">
      <xsl:if test="normalize-space(text())!=''">
        <field name="type_txt">
          <xsl:value-of select="text()" />
        </field>
      </xsl:if>
    </xsl:for-each>
    <field name="level">item</field>
  </xsl:template>
</xsl:stylesheet>
```

The output produced by this stylesheet is a [Solr document](https://wiki.apache.org/solr/UpdateXmlMessages) ready to be indexed by the RODA search engine. See example bellow:

```
<doc>
  <field name="title">{{title}}</field>
  <field name="title_txt">{{title}}</field>
  <field name="description">{{description}}</field>
  <field name="description_txt">{{description}}</field>
  <field name="creator_txt">{{creator}}</field>
</doc>
```

### Visualization

The _Visualization_ activity is supported by a [XSLT (eXtensible Stylesheet Language Transformations)](http://www.w3.org/standards/xml/transformation.html) that transforms the XML-based metadata file into HTML for presentation purposes. The output of this action is a HTML file that will be shown to the user when browsing an existing AIP on the catalogue.

The visualization mapping file should be added to the the configuration folder under `[RODA_HOME]/config/crosswalks/dissemination/html/`.

The following example shows how a Simple Dublin Core file can be transformed to HTML for visualization purposes:

```
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
    exclude-result-prefixes="dc">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"
        omit-xml-declaration="yes" />
    <xsl:param name="i18n.title" />
    <xsl:param name="i18n.description" />
    <xsl:param name="i18n.contributor" />
    <xsl:param name="i18n.coverage" />
    <xsl:param name="i18n.creator" />
    <xsl:param name="i18n.date" />
    <xsl:param name="i18n.format" />
    <xsl:param name="i18n.identifier" />
    <xsl:param name="i18n.language" />
    <xsl:param name="i18n.publisher" />
    <xsl:param name="i18n.relation" />
    <xsl:param name="i18n.rights" />
    <xsl:param name="i18n.source" />
    <xsl:param name="i18n.subject" />
    <xsl:param name="i18n.type" />

    <xsl:template match="/">
        <div class="descriptiveMetadata">
            <xsl:apply-templates />
        </div>
    </xsl:template>
    <xsl:template match="simpledc">
        <xsl:if test="normalize-space(string-join(title/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.title" />
                </div>
                <xsl:for-each select="title">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(description/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.description" />
                </div>
                <xsl:for-each select="description">
          <xsl:if test="normalize-space(text())!=''">
            <div class="value">
              <xsl:value-of select="text()" />
            </div>
          </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(contributor/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.contributor" />
                </div>
                <xsl:for-each select="contributor">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(coverage/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.coverage" />
                </div>
                <xsl:for-each select="coverage">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(creator/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.creator" />
                </div>
                <xsl:for-each select="creator">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(date/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.date" />
                </div>
                <xsl:for-each select="date">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(format/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.format" />
                </div>
                <xsl:for-each select="format">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(identifier/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.identifier" />
                </div>
                <xsl:for-each select="identifier">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(language/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.language" />
                </div>
                <xsl:for-each select="language">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(publisher/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.publisher" />
                </div>
                <xsl:for-each select="publisher">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(relation/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.relation" />
                </div>
                <xsl:for-each select="relation">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(rights/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.rights" />
                </div>
                <xsl:for-each select="rights">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(source/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.source" />
                </div>
                <xsl:for-each select="source">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(subject/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.subject" />
                </div>
                <xsl:for-each select="subject">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                  </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
        <xsl:if test="normalize-space(string-join(type/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.type" />
                </div>
                <xsl:for-each select="type">
                  <xsl:if test="normalize-space(text())!=''">
                      <div class="value">
                          <xsl:value-of select="text()" />
                      </div>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
```

### Editing

The _Editing_ activity is supported by a configuration file that will instruct the repository on how to display a form to edit existing metadata. The configuration file also serves the purpose of providing a template for creating a new metadata item with some predefined attributes already filled in.

Form templates should be added to the configuration under the folder `[RODA_HOME]/config/templates/`. The following example shows how a template file can be combined with annotations that will be used to render the metadata editor.

```
{{~field name="title"   order='2' auto-generate='title' label="{'en': 'Title'}" xpath="//*:title/string()"}}
{{~field name="id"      order='1' auto-generate='id' label="{'en': 'ID'}" xpath="//*:identifier/string()"}}
{{~field name="creator"   label="{'en': 'Creator'}" xpath="//*:creator/string()"}}
{{~field name="dateInitial" order='3' type='date' label="{'en': 'Initial date'}" xpath="//*:date[1]/string()"}}
{{~field name="dateFinal"   order='4' type='date' label="{'en': 'Final date'}" xpath="//*:date[2]/string()"}}
{{~field name="description" type='text-area' label="{'en': 'Description'}" xpath="//*:description/string()"}}
{{~field name="producer"  label="{'en': 'Producer'}" xpath="//*:publisher/string()"}}
{{~field name="rights"    label="{'en': 'Rights'}" xpath="//*:rights/string()"}}
{{~field name="language"  auto-generate='language' label="{'en': 'Language'}" xpath="//*:language/string()"~}}
<?xml version="1.0"?>

<simpledc xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation="../schemas/dc.xsd">
  <title>{{title}}</title>
  <identifier>{{id}}</identifier>
  <creator>{{creator}}</creator>
  <date>{{dateInitial}}</date>
  <date>{{dateFinal}}</date>
  <description>{{description}}</description>
  <publisher>{{producer}}</publisher>
  <rights>{{rights}}</rights>
  <language>{{language}}</language>
</simpledc>
```

The Form template files are based on the the powerful [Handlebars engine](http://handlebarsjs.com). Each field that is expected to be shown in the metadata editor should be identified in the beginning of the file by a _field_ handle (e.g. `{{~field name="title"~}}`). There are several options that can be used to modify the way each field is displayed. These options are a key-value pairs, e.g. `label="Title of work"`, where the key is the name of the option and the value is the value that will be given to that option.

The available options that alter the fields' behavior are:

*   **order** - the order by which the field should be displayed in the metadata editor
*   **label** - the label of the field to be shown at the left of the value in the editor. The format is a JSON map. The keys of the map are the language ID. Example: {"en": "Title", ""pt_PT": "Título"}"
*   **labeli18n** - the i18n key for the label of the field (see section "Internationalization of strings" below)
*   **description** - a text with some help related to the field
*   **value** - the predefined value of the field
*   **mandatory** - If set to true the label is styled in bold to draw attention.
*   **hidden** - if set to true the field is hidden
*   **xpath** - the xpath of the metadata XML document to which this field should bind to
*   **auto-generate** - Fills the value with one of the available auto-value generators. Overrides the value option:

*   **now** - the current date in the format year/month/day
*   **id** - generates an identifier
*   **title** - generates a title
*   **level** - adds the current description level
*   **parentid** - adds the parent's id, if it exists
*   **language** - adds the system language, based on the locale. Example: "português" or "English"

*   **type** - the type of the field. The possible values are:

*   **text** - text field
*   **text-area** - Text area. Larger than a field text.
*   **date** - text field with a date picker
*   **list** - list with the possible values (combo box)

*   **options** - List with the possible values that a field can have. This list is a JSonArray. Example: `options="['final','revised','draft']"`
*   **optionsLabels** - Map with the labels for each option. The key must match one of the options specified in the "options" list. The entry is another map, mapping a language (key) to a label (value). Example: `optionsLabels="{'final': {'en':'Final', 'pt_PT':'Final'},'revised': {'en':'Revised', 'pt_PT':'Verificado'},'draft': {'en':'Draft', 'pt_PT':'Rascunho'}}"`
*   **optionsLabelI18nKeyPrefix** - I18n prefix key. All the key starting with the prefix are used to build the list. Example:  

    `optionsLabelI18nKeyPrefix="crosswalks.dissemination.html.ead.level"`

    Properties file:
      
        crosswalks.dissemination.html.ead.level.fonds=Fonds
        crosswalks.dissemination.html.ead.level.class=Class
        crosswalks.dissemination.html.ead.level.collection=Collection
        crosswalks.dissemination.html.ead.level.recordgrp=Records group
        crosswalks.dissemination.html.ead.level.subgrp=Sub-group
        crosswalks.dissemination.html.ead.level.subfonds=Subfonds
        crosswalks.dissemination.html.ead.level.series=Series
        crosswalks.dissemination.html.ead.level.subseries=Sub-series
        crosswalks.dissemination.html.ead.level.file=File
        crosswalks.dissemination.html.ead.level.item=Item

    Output:  

        <select>
        <option value="fonds">Fonds</option>
        <option value="class">Class</option>
        <option value="collection">Collection</option>
        <option value="recordgrp">Records group</option>
        (...)
        </select>

#### Full example of a "list" field

    {{~field
      name="statusDescription"
      order="470"
      type="list"
      value="final"
      options="['final','revised','draft']"
      optionsLabels="{'final': {'en':'Final', 'pt_PT':'Final'},'revised': {'en':'Revised', 'pt_PT':'Verificado'},'draft': {'en':'Draft', 'pt_PT':'Rascunho'}}"
      optionsLabelI18nKeyPrefix="crosswalks.dissemination.html.ead.statusDescription"
      label="{'en': 'Status description', 'pt_PT': 'Estado da descrição'}"
      xpath="/*:ead/*:archdesc/*:odd[@type='statusDescription']/*:p/string()"
    ~}}

The following is an example of how the tags can be used:

    {{~file name="title" order="1" type="text" label="Template title" mandatory="true" auto-generate="title"~}}

## Activate the new format

After adding all the files described on the previous section, one needs to enable them on the repository. In order to acomplish that, the following activies need to be done.

### Enable the new metadata format

After you added the previously described files to your configuration folder, you must enable the new format in the RODA main configuration file.

Edit the `[RODA_HOME]/config/roda-wui.properties` file and add a new entry as the ones shown in the following example with the name of your recently added metadata format. This will make RODA aware of the new metadata format.

```
ui.browser.metadata.descriptive.types = dc
ui.browser.metadata.descriptive.types = ead_3
ui.browser.metadata.descriptive.types = ead_2002
```

### Internationalization of strings

In order to have your new metadata schema nicely integrated, your must provide internationalization information (i18n) so that RODA knows how to display the necessary information on the user interface in the best way possible.

Edit the `[RODA_HOME]/config/i18n/ServerMessages.properties` file and add the following entries as necessary making sure that the last part of the key matches the code provided on the `[RODA_HOME]/config/roda-wui.properties` file described on the previous section:

```
ui.browse.metadata.descriptive.type.dc=Dublin Core
ui.browse.metadata.descriptive.type.ead.3=Encoded Archival Description 3
ui.browse.metadata.descriptive.type.ead.2002=Encoded Archival Description 2002
```

Finally one should provide translations for the field names to be processed by RODA via the _vizualization_ config file. In order to to that, one must edit the `[RODA_HOME]/config/i18n/ServerMessages.properties` file and add the following entries as necessary, making sure that the last part of the key matches the `xsl:params`included in the visualization map.

The following example depicts how the field names in the Simple Dublin Core example should be displayed in the user interface.

```
crosswalks.dissemination.html.dc.title=Title
crosswalks.dissemination.html.dc.description=Description
crosswalks.dissemination.html.dc.contributor=Contributor
crosswalks.dissemination.html.dc.coverage=Coverage
crosswalks.dissemination.html.dc.creator=Creator
crosswalks.dissemination.html.dc.date=Date
crosswalks.dissemination.html.dc.format=Format
crosswalks.dissemination.html.dc.identifier=Identifier
crosswalks.dissemination.html.dc.language=Language
crosswalks.dissemination.html.dc.publisher=Publisher
crosswalks.dissemination.html.dc.relation=Relation
crosswalks.dissemination.html.dc.rights=Rights
crosswalks.dissemination.html.dc.source=Source
crosswalks.dissemination.html.dc.rights=Subject
crosswalks.dissemination.html.dc.type=Type
```

The previous key endings should match the ones on the xsl:param entries as follows:

```
<xsl:param name="i18n.title" />
<xsl:param name="i18n.description" />
<xsl:param name="i18n.contributor" />
<xsl:param name="i18n.coverage" />
<xsl:param name="i18n.creator" />
<xsl:param name="i18n.date" />
<xsl:param name="i18n.format" />
<xsl:param name="i18n.identifier" />
<xsl:param name="i18n.language" />
<xsl:param name="i18n.publisher" />
<xsl:param name="i18n.relation" />
<xsl:param name="i18n.rights" />
<xsl:param name="i18n.source" />
<xsl:param name="i18n.subject" />
<xsl:param name="i18n.type" />
```

### Reload configuration

After changing the configuration files you must restart RODA so that your changes become effective. Do that by restarting your container or your application server.
