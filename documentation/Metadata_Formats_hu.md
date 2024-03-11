# Metaadatformátumok

A RODA bármilyen leíró metaadatformátumot (azaz az OAIS-ban meghatározott leíró információt) támogat, amennyiben az XML-fájlban jelenik meg. Ha olyan leíró metaadatformátummal rendelkezik, amely nem XML alapú (pl. CSV, JSON, MARC21 stb.), akkor azt XML-be kell konvertálnia, mielőtt a RODA-ban használhatná. A weben számos olyan eszköz létezik, amely lehetővé teszi a legtöbb adatformátum XML-be történő konvertálását.

Ha a metaadatokat XML formátumban megkapta, akkor készen áll arra, hogy egy benyújtási információs csomagba (SIP) csomagolja, és a repozitóriumba bevigye. Alternatív megoldásként létrehozhat egy metaadatfájlt közvetlenül az adattárban a katalógus által biztosított funkciók segítségével. Ha a metaadat-formátum új a RODA számára, a repozitórium mindent megtesz, hogy támogassa, anélkül, hogy a rendszer újrakonfigurálására lenne szükség, azonban a következő korlátozások érvényesek:

#### Hitelesítés

Ha a metaadatformátumhoz nincs megadva séma, a repozitórium ellenőrzi, hogy a metaadat XML-fájl jól formázott-e. Mivel azonban a repozitóriumnak nincs fogalma a megfelelő grammatikáról, nem fogja ellenőrizni, hogy a fájl érvényes-e.

#### Indexelés

A repozitórium indexelni fogja a metaadatfájlban található összes szöveges elemet és attribútumértéket, mivel azonban a repozitórium nem ismeri a megfelelő megfeleltetést az XML-elemek és a belső adatmodell között, csak alapvető keresés lehetséges a megadott metaadatokon.

#### Vizualizáció

Ha nincs vizualizációs hozzárendelés konfigurálva, az XML-alapú metaadatok megjelenítésére a repozitórium egy általános metaadat-megjelenítő eszközt használ. Az összes szöveges elem és attribútum különösebb sorrend nélkül jelenik meg, és az XPath-jukat használja címkeként.

#### Kiadvány

A RODA-nak szüksége van egy konfigurációs fájlra, amely tájékoztatja a metaadatfájlok szerkesztési célú megjelenítésének módjáról. Ha nincs ilyen konfiguráció, akkor a repozitórium egy szöveges területet jelenít meg, ahol a felhasználó közvetlenül szerkesztheti az XML-t.

Az új metaadatformátumok támogatásához a repozitóriumot ennek megfelelően kell konfigurálni. A következő szakaszok részletesen ismertetik azokat a műveleteket, amelyeket el kell végezni ahhoz, hogy a RODA teljes mértékben támogassa az új metaadatsémákat.

## Metaadatjavító fájlok

A metaadatok RODA-ban való használatának javítása érdekében 4 fájlt kell hozzáadni a repozitórium konfigurációs mappáihoz. A következő szakaszok leírják és példákat mutatnak be az ilyen fájlokra.

### Hitelesítés

A RODA egy [XML-séma](http://www.w3.org/standards/xml/schema) segítségével validálja a megadott metaadatfájl szerkezetét és adattípusait. A validációs sémát a beolvasási folyamat során arra használják, hogy ellenőrizzék, hogy a SIP-ben szereplő metaadatok érvényesek-e a megállapított korlátozásoknak megfelelően, valamint a metaadatok katalóguson keresztül történő szerkesztésekor.

Használhat egy szabványos sémafájlt érvényesítési célokra, vagy létrehozhat egy speciális sémafájlt, amely ellenőrzi az összes olyan különleges feltételt, amelyet a repozitórium telepítésénél ellenőrizni kell, pl. kötelező mezők, egyes elemek értékeire vonatkozó zárt szószedet stb.

Az érvényesítési sémafájlokat a konfigurációs mappában kell elhelyezni a `[RODA_HOME]/config/schemas/` alatt.

A legtöbb metaadatformátumot dokumentációval és XML-sémával együtt teszik közzé. Az alábbi példa a Simple Dublin Core metaadatformátum sémáját mutatja be:

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

### Indexelés

Az _Indexelés_ tevékenységet egy [XSLT (eXtensible Stylesheet Language Transformations)](http://www.w3.org/standards/xml/transformation.html) támogatja, amely az XML-alapú metaadatokat olyanná alakítja, amelyet az indexelő motor képes megérteni. Ez a fájl felelős az indexálandó adatok kiválasztásáért, az adatok meghatározott mezőnevekhez való hozzárendeléséért, és az adattípus alapján (pl. szöveg, szám, dátum stb.) utasítja a motort arra vonatkozóan, hogy az adatokat hogyan kell indexelni.

Az Index térképfájlt hozzá kell adni a konfigurációs mappához a `[RODA_HOME]/config/crosswalks/ingest/` alatt.

A következő egy példa az Indextérképre az egyszerű Dublin Core példához.

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

A stíluslap által előállított kimenet egy [Solr dokumentum](https://wiki.apache.org/solr/UpdateXmlMessages), amelyet a RODA keresőmotor indexelhet. Lásd az alábbi példát:

```
<doc>
  <field name="title">{{title}}</field>
  <field name="title_txt">{{title}}</field>
  <field name="description">{{description}}</field>
  <field name="description_txt">{{description}}</field>
  <field name="creator_txt">{{creator}}</field>
</doc>
```

### Vizualizáció

A _Vizualizáció_ tevékenységet egy [XSLT (eXtensible Stylesheet Language Transformations)](http://www.w3.org/standards/xml/transformation.html) támogatja, amely az XML-alapú metaadatfájlt HTML-be alakítja át megjelenítési célokra. A művelet kimenete egy HTML-fájl, amely a felhasználó számára megjelenik, amikor a katalógusban egy meglévő AIP-et böngészik.

A vizualizációs leképezési fájlt hozzá kell adni a konfigurációs mappához a `[RODA_HOME]/config/crosswalks/dissemination/html/` alatt.

A következő példa azt mutatja be, hogyan lehet egy Simple Dublin Core fájlt vizualizációs célokra HTML-be átalakítani:

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

### Szerkesztés

A _Editing_ tevékenységet egy konfigurációs fájl támogatja, amely utasítja a repozóriumot, hogyan jelenítsen meg egy űrlapot a meglévő metaadatok szerkesztéséhez. A konfigurációs fájl azt a célt is szolgálja, hogy egy sablont adjon egy új metaadatelem létrehozásához, néhány előre definiált attribútumot már kitöltve.

Az űrlap sablonokat a konfigurációhoz a `[RODA_HOME]/config/templates/` mappában kell hozzáadni. A következő példa azt mutatja, hogyan lehet egy sablonfájlt olyan megjegyzésekkel kombinálni, amelyek a metaadat-szerkesztő megjelenítésére szolgálnak.
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

A Form sablon fájlok a nagy teljesítményű [Handlebars engine](http://handlebarsjs.com) alapján készültek. Minden egyes mezőt, amelyet a metaadat-szerkesztőben várhatóan megjelenítünk, a fájl elején egy _mező_ handle-lel kell azonosítani (pl. `{{~field name="title"~}}`). Az egyes mezők megjelenítésének módját több opcióval is módosíthatjuk. Ezek az opciók kulcs-érték párok, pl. `címke="A munka címe"`, ahol a kulcs az opció neve, az érték pedig az opciónak adott érték.
<!--- {% endraw %} --->
A rendelkezésre álló, a mezők viselkedését megváltoztató beállítások a következők:

*   **sorrend** - a sorrend, amely szerint a mezőt a metaadat-szerkesztőben meg kell jeleníteni.
*   **címke** - a mező címkéje, amely a szerkesztőben az érték bal oldalán jelenik meg. A formátum egy JSON-térkép. A térkép kulcsai a nyelv azonosítója. Példa: {"hu": "Cím", ""pt_PT": "Título"}"
*   **labeli18n** - a mező címkéjének i18n kulcsa (lásd az alábbi "A karakterláncok nemzetközivé tétele" részt)
*   **leírás** - egy szöveg, amely a mezőhöz kapcsolódó segítséget tartalmaz.
*   **érték** - a mező előre meghatározott értéke
*   **kötelező** - Ha igazra van állítva, a címke félkövérrel van színezve, hogy felhívja a figyelmet.
*   **rejtett** - ha igazra van állítva, a mező el van rejtve.
*   **xpath** - annak a metaadat XML dokumentumnak az xpath-ja, amelyhez a mezőnek kötődnie kell.
*   **automatikus generálás** - Az értéket a rendelkezésre álló automatikus értékgenerátorok egyikével tölti ki. Felülírja az érték opciót:

*   **most** - az aktuális dátum az év/hónap/nap formátumban
*   **id** - azonosítót generál
*   **cím** - címet generál
*   **szint** - hozzáadja az aktuális leírási szintet
*   **szülőid** - hozzáadja a szülő azonosítóját, ha létezik.
*   **nyelv** - hozzáadja a rendszer nyelvét, a területi beállítások alapján. Példa: "português" vagy "English"

*   **típus** - a mező típusa. A lehetséges értékek a következők:

*   **szöveg** - szövegmező
*   **szövegterület** - Szövegterület. Nagyobb, mint egy mező szövege.
*   **dátum** - szöveges mező dátumválasztóval
*   **lista** - lista a lehetséges értékekkel (kombinált mező)

*   **opciók** - A mező lehetséges értékeit tartalmazó lista. Ez a lista egy JSonArray. Példa: `options="['final','revised','draft']"`
*   **opciókCímkék** - Térkép az egyes opciók címkéivel. A kulcsnak meg kell egyeznie az "opciók" listában megadott opciók valamelyikével. A bejegyzés egy másik térkép, amely egy nyelvet (kulcs) egy címkéhez (érték) rendel. Példa: `optionsLabels="{'final': {'en':'Final', 'pt_PT':'Final'},'revised': {'en':'Revised', 'pt_PT':'Verificado'},'draft': {'en':'Draft', 'pt_PT':'Rascunho'}}"`
*   **optionsLabelI18nKeyPrefix** - I18n prefix kulcs. Az összes, az előtaggal kezdődő kulcsot felhasználja a lista összeállításához. Példa:

    `optionsLabelI18nKeyPrefix="crosswalks.dissemination.html.ead.level"`

    Tulajdonságok fájl:

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

    Kimenet:

        <select>
        <option value="fonds">Alapok</option>
        <option value="class">Osztály</option>
        <option value="collection">Gyűjtemény</option>
        <option value="recordgrp">Rekordok csoport</option>
        (...)
        </select>

#### Teljes példa egy "lista" mezőre
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

Az alábbiakban egy példát mutatunk be a címkék használatára:

    {{~file name="title" order="1" type="text" label="Template title" mandatory="true" auto-generate="title"~}}
<!--- {% endraw %} --->
## Az új formátum aktiválása

Miután az előző szakaszban leírt összes fájlt hozzáadtuk, engedélyeznünk kell őket a repozitóriumban. Ehhez a következő műveleteket kell elvégezni.

### Az új metaadat-formátum engedélyezése

Miután a korábban leírt fájlokat hozzáadta a konfigurációs mappához, engedélyeznie kell az új formátumot a RODA fő konfigurációs fájljában.

Szerkessze a `[RODA_HOME]/config/roda-wui.properties` fájlt, és adjon hozzá egy új bejegyzést, mint a következő példában látható, a nemrég hozzáadott metaadatformátum nevével. Ezáltal a RODA tudomást szerez az új metaadatformátumról.

```
ui.browser.metadata.descriptive.types = dc
ui.browser.metadata.descriptive.types = ead_3
ui.browser.metadata.descriptive.types = ead_2002
```

### A karakterláncok nemzetközivé tétele

Ahhoz, hogy az új metaadatséma szépen integrálódjon, nemzetközivé kell tennie az információkat (i18n), hogy a RODA tudja, hogyan jelenítse meg a szükséges információkat a felhasználói felületen a lehető legjobb módon.

Szerkessze a `[RODA_HOME]/config/i18n/ServerMessages.properties` fájlt, és szükség szerint adja hozzá a következő bejegyzéseket, ügyelve arra, hogy a kulcs utolsó része megegyezzen az előző szakaszban leírt `[RODA_HOME]/config/roda-wui.properties` fájlban megadott kóddal:

```
ui.browse.metadata.descriptive.type.dc=Dublin Core
ui.browse.metadata.descriptive.type.ead.3=Encoded Archival Description 3
ui.browse.metadata.descriptive.type.ead.2002=Encoded Archival Description 2002
```

Végül a _vizualization_ config fájlban meg kell adni a RODA által feldolgozandó mezőnevek fordítását. Ehhez szerkeszteni kell a `[RODA_HOME]/config/i18n/ServerMessages.properties` fájlt, és szükség szerint hozzáadni a következő bejegyzéseket, ügyelve arra, hogy a kulcs utolsó része megegyezzen a vizualizációs térképben szereplő `xsl:params`-szal.

A következő példa azt mutatja, hogy az egyszerű Dublin Core példában szereplő mezőneveket hogyan kell megjeleníteni a felhasználói felületen.

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

Az előző kulcsvégződéseknek meg kell egyezniük az xsl:param bejegyzésekben találhatóakkal az alábbiak szerint:

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

### Konfiguráció újratöltése

A konfigurációs fájlok módosítása után újra kell indítania a RODA-t, hogy a módosítások érvénybe lépjenek. Ezt a tároló vagy az alkalmazáskiszolgáló újraindításával teheti meg.
