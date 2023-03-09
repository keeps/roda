# Avancerad sökning

På söksidan kan du söka efter Intellektuella enheter, Representationer eller Filer (använd nedåtpilen för att välja sökdomän). För var och en av dessa domäner kan du söka i alla dess egenskaper eller i specifika egenskaper (använd nedåtpilen för att utöka den avancerade sökningen). Om du till exempel väljer Intellectual Entities kan du söka i ett specifikt fält av beskrivande metadata, eller hitta filer av ett visst format om Filer avancerad sökning är vald.

Sökmotorn hittar bara hela ord. Om du vill söka efter deltermer bör du använda asterisk '*'.

## Sökparametrar

Följande sökparametrar är möjliga:

- Exakt mening (t.ex. "Miguel Ferreira")
- Termer börjar med (t.ex. Miguel F*)
- Ignorera tecken (t.ex. Miguel Ferreir?)
- Exkludera term (t.ex. -Miguel Ferreira)
- Liknande termer (t.ex. Ferreir~)
- Nummerintervall (t.ex. 1900...2000)
- Antingen eller (t.ex. Miguel OR Ferreira)

## Sök efter anpassade metadatafält

Det finns flera steg för att göra det:

1. Generera SIP:ar med din nya beskrivande metadatatyp och version
2. Konfigurera RODA för att indexera ditt nya beskrivande metadataformat
3. Konfigurera RODA för att visa fält i den avancerade sökmenyn

Valfritt:
* Konfigurera RODA för att visa dina metadata
* Konfigurera RODA för att tillåta att redigera din metadata med ett formulär


### 1. Generera SIP:ar med din nya beskrivande metadatatyp och version
På SIP:en måste du definiera den beskrivande metadatatypen och versionen. Eftersom du använder din egen bör du definiera metadatatyp OTHER, annan metadatatyp t.ex. "GolikSwe" och metadatatypversion t.ex. "1". Detta kan göras direkt i METS eller med hjälp av [RODA-in application](http://rodain.roda-community.org/) eller [commons-ip library](https://github.com/keeps/commons-ip).

### 2. Konfigurera RODA för att indexera ditt nya beskrivande metadataformat
I RODA måste du konfigurera hur den kan indexera den här filen. För att göra det måste du definiera XSLT under `$RODA_HOME/config/crosswalks/ingest/` med ett namn baserat på din metadatatyp och version.

I exemplet med metadata type=OTHER, other metadata type="GolikSwe" och metadata version 1, måste du skapa filen `$RODA_HOME/config/crosswalks/ingest/golikswe_1.xslt`.

Se exempel i `$RODA_HOME/example-config/crosswalks/dissemination/ingest/` eller [online version](https://github.com/keeps/roda/tree/master/roda-core/roda-core/src/main/resources/config/crosswalks/ingest).

Den resulterande XML-filen måste vara något i stil med:
```xml
<doc>
  <field name="title">abcdefgh</field>
  <field name="origdesc_txt">abcdefgh</field>
  <field name="destructiondate_txt">2020-01-01</field>
  <field name="destructiondate_dd">2020-01-01T00:00:00Z</field>
</doc>
```
Regler:
- Det finns några reserverade fältnamn; `titel`, `dateInitial` och `dateFinal`, som definierar vad som visas på listorna
- Du kan lägga till nya specifika fält, men du måste alltid lägga till ett suffix för datatypen. De mest använda suffixen är "\_txt" (valfri sträng tokeniserad), "\_ss" (icke-tokeniserade strängar för identifierare), "\_dd" för ISO1601-datum.
- Definitionen av de reserverade fältnamnen görs [here](https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/java/org/roda/core/index/schema/collections/AIPCollection.java#L61) men du kan behöva komma åt [here](https://github.com/keeps/roda/blob/master/roda-common/roda-common-data/src/main/java/org/roda/core/data/common/RodaConstants.java#L604) för att ta reda på det slutliga namnet.
- En komplett lista över suffix och fälttyper finns på [SOLR base schema](https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/resources/config/index/common/conf/managed-schema).

För att tillämpa ändringarna på visningsmallen måste du leverera in nytt innehåll eller indexera om befintligt innehåll.

### 3. Konfigurera RODA för att visa fält i den avancerade sökmenyn

Ändra dina `roda-wui.properties` till [add an new advanced search field item](https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/roda-wui.properties#L165):

``` javaegenskaper
ui.search.fields.IndexedAIP = destructiondate # lägg till nytt fält i listan över fält för objekt (dvs. AIP), andra alternativ är representationer eller filer
ui.search.fields.IndexedAIP.destructiondate.fields = destructiondate_txt # ID:t för fältet i indexet, lika med det på visningsmallen du skapar
ui.search.fields.IndexedAIP.destructiondate.i18n = ui.search.fields.IndexedAIP.destructiondate # nyckel för översättningen i ServerMessages.properties
ui.search.fields.IndexedAIP.destructiondate.type = text # typen av fält som påverkar sökformulärets inmatning
ui.search.fields.IndexedAIP.destructiondate.fixed = true # om det visas vid avancerad sökning som standard eller om det behöver läggas till via knappen "ADD SEARCH FIELD".
```
Du bör också lägga till de nödvändiga översättningarna till dina `$RODA_HOME/config/i18n/ServerMessages.properties`, och på alla språk du vill stödja.

Lägg till [a translation for your new metadata type and version](https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/i18n/ServerMessages.properties#L121):

```javaproperties
ui.browse.metadata.descriptive.type.golikswe_1=Golik SWE (version 1)
```

Lägg till [translations for your fields](https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/i18n/ServerMessages.properties#L2):

```javaproperties
ui.search.fields.IndexedAIP.destructiondate= Destruction Date
```
