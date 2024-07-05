# Metadatenformate

RODA unterstützt jedes deskriptive Metadatenformat (d. h. beschreibende Informationen, wie im OAIS angegeben), solange es durch eine XML-Datei repräsentiert wird. Wenn Sie ein deskriptives Metadatenformat haben, das nicht auf XML basiert (z. B. CSV, JSON, MARC21 usw.), müssen Sie es vor der Verwendung in RODA in XML konvertieren. Es gibt mehrere Tools im Web, mit denen Sie die meisten Datenformate in XML konvertieren können.

Sobald Ihre Metadaten in XML vorliegen, können Sie diese in ein Submission Information Package (SIP) verpacken und in das Repository einbringen. Alternativ können Sie eine Metadatendatei direkt im Repository erstellen, indem Sie die Funktionen des Katalogs verwenden. Wenn das Metadatenformat neu für RODA ist, wird dieses im Repository angeboten, um es zu unterstützen, ohne dass eine Neukonfiguration des Systems erforderlich ist. Es gelten jedoch die folgenden Einschränkungen:

#### Überprüfung

Wenn kein Schema für Ihr Metadatenformat bereitgestellt wird, überprüft das Repository, ob die Metadaten-XML-Datei gut geformt ist. Da dem Repository die korrekte Grammatik nicht bekannt ist, kann es nicht überprüfen, ob die Datei gültig ist.

#### Indexierung

Das Repository wird alle Textelemente und Attributwerte in der Metadaten-Datei indizieren. Da das Repository jedoch nicht die richtige Zuordnung zwischen den XML-Elementen und dem inneren Datenmodell kennt, ist nur eine grundlegende Suche möglich.

#### Visualisierung

Wenn keine Visualisierungszuordnungen konfiguriert sind, wird das Repository einen generischen Metadaten-Viewer verwenden, um die XML-basierten Metadaten anzuzeigen. Alle Textelemente und Attribute werden ohne bestimmte Reihenfolge angezeigt, und ihr XPath wird als Bezeichnung verwendet.

#### Ausgabe

RODA benötigt eine Konfigurationsdatei, damit bekannt ist wie die Metadatendateien für Bearbeitungszwecke angezeigt werden sollen. Wenn eine solche Konfiguration nicht vorhanden ist, wird das Repository einen Textbereich anzeigen, in dem der Benutzer die XML direkt bearbeiten kann.

Um neue Metadatenformate zu unterstützen, muss das Repository entsprechend konfiguriert werden. Im folgenden Abschnitt werden die Schritte im Detail beschrieben, die durchgeführt werden müssen, um ein neues Metadatenschema vollständig in RODA zu unterstützen.

## Metadaten-Verbesserungsdateien

Um die Metadatenerfahrung in RODA zu verbessern, müssen 4 Dateien zu den Repository-Konfigurationsordnern hinzugefügt werden. Im folgenden Abschnitt werden diese Dateien beschrieben und Beispiele dafür bereitgestellt.

### Überprüfung

RODA verwendet ein [XML-Schema](http://www.w3.org/standards/xml/schema), um die Struktur und Datentypen der bereitgestellten Metadaten-Datei zu validieren. Das Validierungsschema wird während des Eingangsprozesses verwendet, um zu überprüfen, ob die Metadaten in SIP gemäß den festgelegten Einschränkungen gültig sind, sowie wann die Metadaten über den Katalog bearbeitet werden.

Sie können ein Standard-Schemafile für Validierungszwecke verwenden oder ein spezifisches erstellen, das alle speziellen Bedingungen überprüft, die in Ihrer Repository-Installation überprüft werden müssen, z. B. obligatorische Felder, geschlossene Vokabulare für die Werte bestimmter Elemente usw.

Die Validierungsschema-Dateien muss im Konfigurationsordner unter `[RODA_HOME]/config/schemas/` hinzugefügt werden.

Die meisten Metadatenformate werden zusammen mit einer Dokumentation und einem XML-Schema veröffentlicht. Die folgende Beispiel stellt das Schema für das einfache Dublin Core-Metadatenformat dar:

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

### Indexierung

Die Aktivität _Indexierung_ wird durch eine [XSLT (eXtensible Stylesheet Language Transformations)](http://www.w3.org/standards/xml/transformation.html)-Datei unterstützt, die die XML-basierten Metadaten so umwandeln, das sie von der Indexierungsmaschine verstanden werden kann. Diese Datei ist verantwortlich für die Auswahl der Daten, die indiziert werden sollen, für die Zuordnung der Daten zu bestimmten Feldnamen und für die Anweisung an die Maschine, wie die Daten auf der Grundlage ihres Datentyps (z. B. Text, Zahl, Datum usw.) indiziert werden sollen.

Die Indexmap-Datei muss im Konfigurationsordner unter `[RODA_HOME]/config/crosswalks/ingest/` hinzugefügt werden.

Das folgende Beispiel zeigt ein Index-Mapping für das einfache Dublin Core-Beispiel.

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

Die von diesem Stylesheet erzeugte Ausgabe ist ein [Solr-Dokument](https://wiki.apache.org/solr/UpdateXmlMessages), das von der RODA-Suchmaschine indiziert werden kann. Siehe Beispiel unten:

```
<doc>
<field name="title">{{title}}</field>
<field name="title_txt">{{title}}</field>
<field name="description">{{description}}</field>
<field name="description_txt">{{description}}</field>
<field name="creator_txt">{{creator}}</field>
</doc>
```

### Visualisierung

Die Aktivität _Visualisierung_ wird durch eine [XSLT (eXtensible Stylesheet Language Transformations)](http://www.w3.org/standards/xml/transformation.html)-Datei unterstützt, die die XML-basierte Metadatendatei in HTML für Präsentationszwecke umwandelt. Die Ausgabe dieser Aktion ist eine HTML-Datei, die dem Benutzer beim Durchsuchen eines bestehenden AIP im Katalog angezeigt wird.

Die Visualisierungs-Mapping-Datei muss im Konfigurationsordner unter `[RODA_HOME]/config/crosswalks/dissemination/html/` hinzugefügt werden.

Das folgende Beispiel zeigt, wie eine einfache Dublin-Core-Datei für Visualisierungszwecke in HTML umgewandelt werden kann:

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

### Bearbeitung

Die Aktivität _Bearbeitung_ wird durch eine Konfigurationsdatei unterstützt, die das Repository anweist, ein Formular zur Bearbeitung vorhandener Metadaten anzuzeigen. Die Konfigurationsdatei dient auch dazu, eine Vorlage für die Erstellung eines neuen Metadatenelements mit einigen vordefinierten, bereits ausgefüllten Attributen bereitzustellen.

Formulartemplates muss im Konfigurationsordner unter `[RODA_HOME]/config/templates/` hinzugefügt werden. Das folgende Beispiel zeigt, wie eine Vorlagendatei mit Anmerkungen kombiniert werden kann, die zur Darstellung des Metadaten-Editors verwendet werden.

```
{{~field name="title" order='2' auto-generate='title' label="{'en': 'Title'}" xpath="//*:title/string()"}}
{{~field name="id" order='1' auto-generate='id' label="{'en': 'ID'}" xpath="//*:identifier/string()"}}
{{~field name="creator" label="{'en': 'Creator'}" xpath="//*:creator/string()"}}
{{~field name="dateInitial" order='3' type='date' label="{'en': 'Initial date'}" xpath="//*:date[1]/string()"}}
{{~field name="dateFinal" order='4' type='date' label="{'en': 'Final date'}" xpath="//*:date[2]/string()"}}
{{~field name="description" type='text-area' label="{'en': 'Description'}" xpath="//*:description/string()"}}
{{~field name="producer" label="{'en': 'Producer'}" xpath="//*:publisher/string()"}}
{{~field name="rights" label="{'en': 'Rights'}" xpath="//*:rights/string()"}}
{{~field name="language" auto-generate='language' label="{'en': 'Language'}" xpath="//*:language/string()"~}}
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

Die Formularvorlagendateien basieren auf der leistungsfähigen [Handlebars-Engine] (http://handlebarsjs.com). Jedes Feld, das im Metadaten-Editor angezeigt werden soll, sollte am Anfang der Datei durch ein _field_-Handle identifiziert werden (z.B. `{{~field name="title"~}}`). Es gibt mehrere Optionen, mit denen die Art und Weise, wie die einzelnen Felder angezeigt werden, geändert werden kann. Diese Optionen sind Schlüssel-Wert-Paare, z. B. `label="Titel der Arbeit"`, wobei der Schlüssel der Name der Option und der Wert der Wert ist, der dieser Option gegeben wird.

Die verfügbaren Optionen, die das Verhalten der Felder ändern, sind:

*   **order** - die Reihenfolge, in der das Feld im Metadaten-Editor angezeigt werden soll.
*   **label** - das Label des Feldes, das links vom Wert im Editor angezeigt werden soll. Das Format ist eine JSON-Karte. Die Schlüssel der Karte sind die Sprachkennungen. Beispiel: {"en": "Title", "pt_PT": "Título"}
*   **labeli18n** - der i18n-Schlüssel für das Label des Feldes (siehe Abschnitt "Internationalisierung von Zeichenketten" weiter unten).
*   **description** - ein Text mit einer Hilfestellung zu dem Feld
*   **value** - der vordefinierte Wert des Feldes.
*   **mandatory** - Bei der Einstellung "true" wird das Etikett fett gedruckt, um die Aufmerksamkeit zu erhöhen.
*   **hidden** - Wenn auf true gesetzt, wird das Feld verborgen.
*   **xpath** - der XPath des Metadaten-XML-Dokuments, an das dieses Feld gebunden werden soll.
*   **auto-generate** - Füllt den Wert mit einem der verfügbaren automatischen Wertgeneratoren. Überschreibt die Wertoption:

*   **now** - das aktuelle Datum im Format Jahr/Monat/Tag
*   **id** - generiert eine Kennung
*   **title** - generiert einen Titel
*   **level** - fügt die aktuelle Beschreibungsebene hinzu
*   **parentid** - fügt die ID des Elternelements hinzu, sofern vorhanden
*   **language** - fügt die Systemsprache hinzu, basierend auf der Lokale. Beispiel: "português" oder "English"

*   **type** - der Typ des Feldes. Die möglichen Werte sind:

*   **text** - Textfeld
*   **text-area** - Textbereich. Größer als ein Textfeld.
*   **date** - Textfeld mit einem Datumsauswahl-Menü
*   **list** - Liste mit den möglichen Werten (Auswahlliste)

*   **options** - Liste mit den möglichen Werten, die ein Feld haben kann. Diese Liste ist ein JSON-Array. Beispiel: `options="['final','revised','draft']"`
*   **optionsLabels** - Karte mit den Bezeichnungen für jede Option. Der Schlüssel muss mit einer der in der Liste "options" angegebenen Optionen übereinstimmen. Der Eintrag ist eine weitere Map, die eine Sprache (Schlüssel) auf eine Bezeichnung (Wert) abbildet. Beispiel: `optionsLabels="{'final': {'en':'Final', 'pt_PT':'Final'},'revised': {'en':'Revised', 'pt_PT':'Verificado'},'draft': {'en':'Draft', 'pt_PT':'Rascunho'}}"`
*   **optionsLabelI18nKeyPrefix** - i18n-Präfixschlüssel. Alle Schlüssel, die mit dem Präfix beginnen, werden verwendet, um die Liste aufzubauen. Beispiel:

    `optionsLabelI18nKeyPrefix="crosswalks.dissemination.html.ead.level"`

    Eigenschaften-Datei.

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

    Ausgabe:

<select>
<option value="fonds">Fonds</option>
<option value="class">Class</option>
<option value="collection">Collection</option>
<option value="recordgrp">Records group</option>
(...)
</select> 

#### Beispiel für ein "Listen"-Feld

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

Das folgende Beispiel zeigt, wie die Tags verwendet werden können:

 {{~file name="title" order="1" type="text" label="Template title" mandatory="true" auto-generate="title"~}}

## Aktivieren des neuen Formats

Nachdem man alle im vorigen Abschnitt beschriebenen Dateien hinzugefügt hat, muss man sie im Repository aktivieren. Dazu müssen die folgenden Aktivitäten durchgeführt werden.

### Aktivieren des neuen Metadatenformats

Nachdem Sie die zuvor beschriebenen Dateien zu Ihrem Konfigurationsordner hinzugefügt haben, müssen Sie das neue Format in der Hauptkonfigurationsdatei von RODA aktivieren.

Bearbeiten Sie die Datei `[RODA_HOME]/config/roda-wui.properties` und fügen Sie einen neuen Eintrag wie in folgendem Beispiel mit dem Namen Ihres kürzlich hinzugefügten Metadatenformats hinzu. Dadurch wird RODA auf das neue Metadatenformat aufmerksam gemacht.

```
ui.browser.metadata.descriptive.types = dc
ui.browser.metadata.descriptive.types = ead_3
ui.browser.metadata.descriptive.types = ead_2002
```

### Internationalisierung von Zeichenketten

Damit Ihr neues Metadatenschema gut integriert werden kann, müssen Sie Internationalisierungsinformationen (i18n) bereitstellen, damit RODA weiß, wie die erforderlichen Informationen auf der Benutzeroberfläche bestmöglich angezeigt werden können.

Bearbeiten Sie die Datei `[RODA_HOME]/config/i18n/ServerMessages.properties` und fügen Sie die folgenden Einträge bei Bedarf hinzu, wobei darauf zu achten ist, dass der letzte Teil des Schlüssels mit dem im vorherigen Abschnitt `[RODA_HOME]/config/roda-wui.properties` angegebenen Code übereinstimmt:

```
ui.browse.metadata.descriptive.type.dc=Dublin Core
ui.browse.metadata.descriptive.type.ead.3=Encoded Archival Description 3
ui.browse.metadata.descriptive.type.ead.2002=Encoded Archival Description 2002
```

Schließlich sollte man noch Übersetzungen für die von RODA zu verarbeitenden Feldnamen über die Konfigurationsdatei _vizualization_ bereitstellen. Dazu muss man die Datei `[RODA_HOME]/config/i18n/ServerMessages.properties` bearbeiten und bei Bedarf die folgenden Einträge hinzufügen, wobei darauf zu achten ist, dass der letzte Teil des Schlüssels mit dem in der Visualisierungskarte enthaltenen `xsl:params` übereinstimmt.

Das folgende Beispiel zeigt, wie die Feldnamen im Beispiel für Simple Dublin Core in der Benutzeroberfläche angezeigt werden sollen.

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

Die Endungen der vorherigen Schlüssel sollten mit denen der `xsl:param`-Einträge wie folgt übereinstimmen:

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

### Konfiguration neu laden

Nachdem Sie die Konfigurationsdateien geändert haben, müssen Sie RODA neu starten, damit Ihre Änderungen wirksam werden. Starten Sie dazu Ihren Container oder Ihren Anwendungsserver neu.
