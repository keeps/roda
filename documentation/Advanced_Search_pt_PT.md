# Pesquisa avançada

Nesta página poderá pesquisar por Entidades Intelectuais, Representações ou Ficheiros (use a seta para baixo para selecionar o domínio de pesquisa). Para cada um destes domínios pode pesquisar em todas as suas propriedades ou em propriedades específicas (use a seta para baixo para expandir a pesquisa avançada). Por exemplo, se selecionar Entidades Intelectuais, poderá pesquisar num campo específico da metainformação descritiva, ou encontrar ficheiros de um determinado formato, caso selecione a pesquisa avançada por ficheiro.

O motor de pesquisa localiza apenas palavras inteiras. Se pretender pesquisar por termos parciais deverá utilizar o operador "\*".

## Operadores de pesquisa

Os seguintes operadores de pesquisa estão à sua disposição:

- Frase exata (e.g. "Miguel Ferreira")
- Termos começados por (e.g. Miguel F*)
- Ignorar carater (e.g. Miguel Ferreir?)
- Excluir termo (e.g. -Miguel Ferreira)
- Termos semelhantes (e.g. Ferreir~)
- Intervalo de números (e.g. 1900..2000)
- Reunião de termos (e.g. Miguel OR Ferreira)

## Search custom metadata fields

There are several steps to do it:

1. Generate SIPs with your new descriptive metadata type and version
2. Configure RODA to index your new descriptive metadata format
3. Configure RODA to show fields in the advanced search menu

Optional:
* Configure RODA to display your metadata
* Configure RODA to allow to edit your metadata with a form


### 1. Generate SIPs with your new desc. metadata type and version
On the SIP you must define the descriptive metadata type and version. As you are using your own, you should define metadata type OTHER, other metadata type e.g. "GolikSwe" and metadata type version e.g. "1". This can be done directly in the METS or using the [RODA-in application](http://rodain.roda-community.org/) or the [commons-ip library](https://github.com/keeps/commons-ip).

### 2. Configure RODA to index your new desc. metadata format
On RODA, you must configure how it can index this file. To do so, you must define the XSLT under `$RODA_HOME/config/crosswalks/ingest/` with a name that is calculated by your metadata type and version.

On the example with metadata type=OTHER, other metadata type="GolikSwe" and metadata version 1, you must create the file  `$RODA_HOME/config/crosswalks/ingest/golikswe_1.xslt`.

You can look at examples in the `$RODA_HOME/example-config/crosswalks/dissemination/ingest/` or the [online version](https://github.com/keeps/roda/tree/master/roda-core/roda-core/src/main/resources/config/crosswalks/ingest).

The resulting XML must be something like:
```xml
<doc>
  <field name="title">abcdefgh</field>
  <field name="origdesc_txt">abcdefgh</field>
  <field name="destructiondate_txt">2020-01-01</field>
  <field name="destructiondate_dd">2020-01-01T00:00:00Z</field>
</doc>
```
Rules:
- There are some reserved field names, specially `title`, `dateInitial` and `dateFinal`, that define what appear on the lists
- You can add new specific fields, but must always add a suffix for the data type. The most used suffixes are "\_txt" (any string tokenized), "\_ss" (non-tokenized strings for identifiers), "\_dd" for ISO1601 dates.
- The definition of the reserved fields names is made [here](https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/java/org/roda/core/index/schema/collections/AIPCollection.java#L61) but you may need to also access [here](https://github.com/keeps/roda/blob/master/roda-common/roda-common-data/src/main/java/org/roda/core/data/common/RodaConstants.java#L604) to find out the final name.
- A complete list of suffixes and fields types is available at the [SOLR base schema](https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/resources/config/index/common/conf/managed-schema).

To apply the changes on the stylesheet you must ingest new content or re-index existing content.

### 3. Configure RODA to show fields in the advanced search menu

Change your `roda-wui.properties` to [add an new advanced search field item](https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/roda-wui.properties#L165):

```javaproperties
ui.search.fields.IndexedAIP = destructiondate # add new field to the list of fields for items (i.e. AIPs), other options are representations or files
ui.search.fields.IndexedAIP.destructiondate.fields = destructiondate_txt # the id of the field in the index, equal to the one on the stylesheet you create
ui.search.fields.IndexedAIP.destructiondate.i18n = ui.search.fields.IndexedAIP.destructiondate # key for the translation in ServerMessages.properties
ui.search.fields.IndexedAIP.destructiondate.type = text # the type of the field which influences the search form input
ui.search.fields.IndexedAIP.destructiondate.fixed = true # if it appears on advanced search by default or if it needs to be added via the "ADD SEARCH FIELD" button.
```
You should also add the necessary translations to your `$RODA_HOME/config/i18n/ServerMessages.properties`, and in all languages you which to support.

Add [a translation for your new metadata type and version](https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/i18n/ServerMessages.properties#L121):

```javaproperties
ui.browse.metadata.descriptive.type.golikswe_1=Golik SWE (version 1)
```

Add [translations for your fields](https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/i18n/ServerMessages.properties#L2):

```javaproperties
ui.search.fields.IndexedAIP.destructiondate= Destruction Date
```
