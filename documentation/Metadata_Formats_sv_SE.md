# Metadataformat

RODA stödjer alla beskrivande metadataformat (dvs. beskrivande information enligt OAIS) så länge som det representeras av en XML-fil. Om man har ett beskrivande metadataformat som inte är baserat på XML (t.ex. CSV, JSON, MARC21, etc.), måste man konvertera det till XML innan man kan använda det i RODA. Det finns flera verktyg på webben som kan konvertera de flesta dataformat till XML.

När metadatan finns i en XML är den redo att paketeras i ett Submission Information Package (SIP) och levereras in till arkivet. Alternativt kanske du vill skapa en metadatafil direkt i arkivet genom att använda funktionaliteten som tillhandahålls i katalogen. När metadataformatet är nytt för RODA kommer systemet att göra sitt bästa för att stödja utan att behöva göra någon omkonfigurering av systemet, dock gäller följande begränsningar:

#### Validering

Om inget schema tillhandahålls för ditt metadataformat kommer arkivet att kontrollera om metadata-XML-filen är välformaterad. Eftersom arkivet inte vet om det är rätt grammatik kommer ingen verifiering göras angående om filen är giltig.

#### Indexering

Systemet kommer att indexera alla textelement och attributvärden som finns i metadatafilen, men eftersom systemet inte känner till rätt mappning mellan XML-elementen och den inre standarden för data, kommer endast grundläggande sökning att vara möjlig på den tillhandahållna metadatan.

#### Visualisering

När inga visuella mappningar är konfigurerade kommer en generisk metadatavisare att användas av systemet för att visa den XML-baserade metadatan. Alla textelement och attribut kommer att visas utan någon speciell ordning och deras XPath kommer att användas som märkning.

#### Version

RODA behöver ha en konfigurationsfil för att veta hur metadatafiler ska visas för redigeringsändamål. Om det inte finns någon sådan konfiguration kommer arkivet att visa ett textområde där användaren kan redigera direkt i XML:en.

För att stödja nya metadataformat måste arkivet konfigureras därefter. Följande avsnitt beskriver i detalj de åtgärder som måste utföras för att ett nytt metadataschema ska stödjas fullt ut i RODA.

## Metadataförbättringsfiler

För att förbättra metadataupplevelsen i RODA finns det 4 filer som måste läggas till i systemkonfigurationsmapparna. Följande avsnitt beskriver och ger exempel på sådana filer.

### Validering

RODA använder ett [XML-schema](http://www.w3.org/standards/xml/schema) för att validera strukturen och datatyperna för den tillhandahållna metadatafilen. Valideringsschemat kommer att användas under inleveransprocessen för att kontrollera om metadata som ingår i SIP är giltiga enligt de fastställda begränsningarna samt när metadata redigeras via katalogen.

Du kan använda en standardschemafil för valideringsändamål alternativt skapa en specifik fil som verifierar alla särskilda villkor som du behöver för att kontrollera i din systeminstallation, t.ex. obligatoriska fält, slutna vokabulärer för vissa elements värden osv.

Valideringsschemafilerna bör placeras i konfigurationsmappen under `[RODA_HOME]/config/schemas/`.

De flesta metadataformaten publiceras tillsammans med dokumentation och ett XML-schema. Följande exempel representerar schemat för metadataformatet Simple Dublin Core:

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

### Indexering

Aktiviteten _Indexering_ stöds av en [XSLT (eXtensible Stylesheet Language Transformations)](http://www.w3.org/standards/xml/transformation.html) som omvandlar den XML-baserade metadatan till något som indexeringsmotorn kan förstå. Den här filen ansvarar för att välja ut data som förväntas indexeras, mappa data till specifika fältnamn och instruerar motorn om hur data förväntas indexeras baserat på dess datatyp (t.ex. text, nummer, datum, etc. ).

Indexkartfilen bör läggas till i konfigurationsmappen under `[RODA_HOME]/config/crosswalks/ingest/`.

Nedan är ett exempel på en indexkarta för Simple Dublin Core-exemplet.

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

Utdatan som produceras av denna visningsmall är ett [Solr dokument](https://wiki.apache.org/solr/UpdateXmlMessages) som är redo att indexeras av RODA-sökmotorn. Se exempel nedan:

```
<doc>
  <field name="title">{{title}}</field>
  <field name="title_txt">{{title}}</field>
  <field name="description">{{description}}</field>
  <field name="description_txt">{{description}}</field>
  <field name="creator_txt">{{creator}}</field>
</doc>
```

### Visualisering

Aktiviteten _visualisering_ stöds av en [XSLT (eXtensible Stylesheet Language Transformations)](http://www.w3.org/standards/xml/transformation.html) som omvandlar den XML-baserade metadatafilen till HTML för presentationsändamål. Denna åtgärd ger en HTML-fil som kommer att visas för användaren när ett befintlig AIP i katalogen visas.

Visualiseringsmappningsfilen bör läggas till i konfigurationsmappen under `[RODA_HOME]/config/crosswalks/dissemination/html/`.

Följande exempel visar hur en Simple Dublin Core-fil kan omvandlas till HTML för visualiersingsändamål:

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

### Redigering

Aktiviteten _redigering_ stöds av en konfigurationsfil som kommer att instruera systemet om hur man visar ett formulär för att redigera befintlig metadata. Konfigurationsfilen syftar också till att tillhandahålla en mall för att skapa ett nytt metadataobjekt innehållande några redan ifyllda fördefinierade attribut.

Formulärmallar bör läggas till i konfigurationen under mappen `[RODA_HOME]/config/templates/`. Följande exempel visar hur en mallfil kan kombineras med kommentarer som kommer att användas för att för att rendera metadataredigeraren.
<!--- {% raw %} --->
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

Formulärmallsfilerna är baserade på den kraftfulla [Handlebars-motorn](http://handlebarsjs.com). Varje fält som förväntas visas i metadataredigeraren bör identifieras i början av filen med ett _field_-handle (t.ex. `{{~field name="title"~}}`). Det finns flera alternativ som kan användas för att ändra hur varje fält visas. Dessa alternativ är ett nyckelvärdepar, t.ex. `label="Title of work"`, där nyckeln är namnet på alternativet och värdet är det värde som kommer att ges till det alternativet.
<!--- {% endraw %} --->
De tillgängliga alternativen som ändrar fältens beteende är:

*   **order** - I vilken ordning fältet ska visas i metadataredigeraren
*   **label** - Etiketten för fältet som ska visas till vänster om värdet i redigeraren. Formatet är en JSON-karta. Kartans nycklar är språk-ID. Exempel: {"en": "Title", ""pt_PT": "Título"}"
*   **labeli18n** - i18n-tangenten för fältets etikett (se avsnittet "Internationalisering av strängar" nedan)
*   **description** - En hjälptext relaterad till fältet
*   **value** - Fältets fördefinierade värde
*   **mandatory** - Om satt till sant är etiketten skriven med fet stil för att dra till sig uppmärksamhet.
*   **hidden** - Om satt till sant så är fältet dolt
*   **xpath** - Xpath för XML-metadatadokumentet som detta fält ska bindas till
*   **auto-generate** - Fyller värdet med en av de tillgängliga automatiska värdegeneratorerna. Skriver över värdealternativet:

*   **now** - det aktuella datumet i formatet år/månad/dag
*   **id** - genererar en identifierare
*   **title** - generar en titel
*   **level** - lägger till den aktuella beskrivningsnivån
*   **parentid** - Lägger till förälderns id om det finns
*   **language** - lägger till systemspråket, baserat på det lokala språket. Exempel: "svenska" eller "engelska"

*   **type** - typen av fält. De möjliga värdena är:

*   **text** - textfält
*   **text-area** - textområde. Större än ett textfält.
*   **date** - textfält med en datumväljare
*   **list** - lista med möjliga värden (combo box)

*   **options** - lista med möjliga värden som ett fält kan ha. Den här listan är en JSonArray. Exempel: `options="['final','revised','draft']"`
*   **optionsLabels** - Karta med etiketterna för varje alternativ. Nyckeln måste matcha ett av alternativen som anges i alternativ-listan. Ingången är en annan karta som mappar ett språk (nyckel) till en etikett (värde). Exempel: `optionsLabels="{'final': {'en':'Final', 'pt_PT':'Final'},'revised': {'en':'Revised', 'pt_PT':'Verificado'} ,'draft': {'en':'Draft', 'pt_PT':'Rascunho'}}"`
*   **optionsLabelI18nKeyPrefix** - I18n-prefixnyckel. Alla nycklar som börjar med prefixet används för att bygga listan. Exempel:

    `optionsLabelI18nKeyPrefix="crosswalks.dissemination.html.ead.level"`

    Egenskapsfil:

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

    Utdata:

        <select>
        <option value="fonds">Arkivbestånd</option>
        <option value="class">Klass</option>
        <option value="collection">Samling</option>
        <option value="recordgrp">Volym</option>
        (...)
        </select>

#### Fullständigt exempel på ett "list"-fält
<!--- {% raw %} --->
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

Följande är ett exempel på hur taggarna kan användas:

    {{~file name="title" order="1" type="text" label="Template title" mandatory="true" auto-generate="title"~}}
<!--- {% endraw %} --->
## Aktivera det nya formatet

Efter att ha lagt till alla filer som beskrivs i föregående avsnitt måste man aktivera dem i systemet. För att göra det måste följande aktiviteter att göras.

### Aktivera det nya metadataformatet

När du har lagt till de tidigare beskrivna filerna till din konfigurationsmapp måste du aktivera det nya formatet i RODA-huvudkonfigurationsfilen.

Redigera filen `[RODA_HOME]/config/roda-wui.properties` och lägg till en ny post som de som visas i följande exempel, med namnet på ditt nyligen tillagda metadataformat. Detta kommer att göra RODA medveten om det nya metadataformatet.

```
ui.browser.metadata.descriptive.types = dc
ui.browser.metadata.descriptive.types = ead_3
ui.browser.metadata.descriptive.types = ead_2002
```

### Internationalisering av strängar

För att få ditt nya metadataschema integrerat på ett bra sätt måste du tillhandahålla internationaliseringsinformation (i18n) så att RODA vet hur man visar nödvändig information i användargränssnittet på bästa möjliga sätt.

Redigera filen `[RODA_HOME]/config/i18n/ServerMessages.properties` och lägg till följande poster vid behov och se till att den sista delen av nyckeln matchar koden som tillhandahålls på `[RODA_HOME]/config/roda-wui.properties`fil som beskrevs i föregående avsnitt:

```
ui.browse.metadata.descriptive.type.dc=Dublin Core
ui.browse.metadata.descriptive.type.ead.3=Encoded Archival Description 3
ui.browse.metadata.descriptive.type.ead.2002=Encoded Archival Description 2002
```

Slutligen bör man tillhandahålla översättningar för fältnamnen som ska behandlas av RODA via _visualiseirng_ config-filen. För att göra det måste man redigera filen `[RODA_HOME]/config/i18n/ServerMessages.properties` och lägga till följande poster vid behov och se till att den sista delen av nyckeln matchar `xsl:params` som ingår i visualiseringskartan.

Följande exempel visar hur fältnamnen i Simple Dublin Core-exemplet ska visas i användargränssnittet.

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

De föregående nyckeländelserna bör matcha dem på xsl:param-posterna enligt följande:

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

### Ladda om konfigurationen

Efter att ha ändrat konfigurationsfilerna måste du starta om RODA så att dina ändringar träder i kraft. Gör det genom att starta om din behållare (container) eller din applikationsserver.
