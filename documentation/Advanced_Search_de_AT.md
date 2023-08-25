# Erweiterte Suche

Auf der Suchseite können Sie nach intellektuellen Entitäten, Darstellungen oder Dateien suchen (verwenden Sie den Pfeil nach unten, um den Suchbereich auszuwählen). Für jeden dieser Bereiche können Sie nach allen oder bestimmten Kriterien suchen (verwenden Sie den Pfeil nach unten für erweiterte Suche). Wenn Sie beispielsweise "intellektuelle Entitäten" auswählen, können Sie in einem bestimmten Bereich der beschreibenden Metadaten suchen oder Dateien eines bestimmten Formats finden, wenn die erweiterte Dateien-Suche ausgewählt ist.

Die Suchmaschine findet nur ganze Wörter. Wenn Sie nach Teilbegriffen suchen möchten, verwenden Sie bitte "*".

## Suchoperatoren

Folgende Suchoperatoren stehen zur Verfügung:

- Genaue Angabe (z.B. "Max Mustermann")
- Angabe beginnt mit (z.B. "Max M*")
- Zeichen ignorieren (z.B. Max Mustermann?)
- Begriff ausschließen (z.B. -Max Mustermann)
- Ähnlicher Begriff (z.B. Mustermann~)
- Nummernkreis (z.B. 1900..2000)
- Auswahl (z.B. Max oder Mustermann)

## In benutzerdefinierten Metadatenfeldern suchen

Es gibt mehrere Ausführungsmöglichkeiten:

1. SIPs mit dem neuen beschreibenden Metadatentyp und der neuen beschreibenden Metadatenversion erstellen
2. RODA für Indexierung eines neuen beschreibenden Metadatenformats konfigurieren
3. RODA für Feldanzeige in erweiterter Suche konfigurieren

Optional:
* RODA für Anzeige der Metadaten konfigurieren
* RODA für Bearbeitung der Metadaten mittels Formular konfigurieren


### 1. SIPs mit dem neuen beschreibenden Metadatentyp und der neuen beschreibenden Metadatenversion erstellen
Auf dem SIP müssen der beschreibende Metadatentyp und die beschreibende Metadatenversion festgelegt werden. Da Sie Ihre eigenen Metadaten verwenden, sollten Sie den Metadatentyp ANDERE, anderen Metadatentyp (z.B. "GolikSwe") und Metadatenversion (z.B. "1") definieren. Dies kann direkt in METS oder mit Hilfe der [RODA-in-Anwendung] (http://rodain.roda-community.org/) oder der [commons-ip-Bibliothek] (https://github.com/keeps/commons-ip) geschehen.

### 2. RODA für Indexierung eines neuen beschreibenden Metadatenformats konfigurieren
Bei RODA muss konfiguriert werden, wie diese Datei indexiert werden kann. Dazu muss das XSLT unter `$RODA_HOME/config/crosswalks/ingest/` mit einem Namen hinterlegt werden, der sich aus dem Metadatentyp und der Metadatenversion ergibt.

Auf dem Beispiel von Metadatentyp=ANDERE, anderem Metadatentyp="GolikSwe" und Metadatenversion=1, muss eine Datei angelegt werden: `$RODA_HOME/config/crosswalks/ingest/golikswe_1.xslt`.

Sie können sich Beispiele unter `$RODA_HOME/example-config/crosswalks/dissemination/ingest/` oder [online version](https://github.com/keeps/roda/tree/master/roda-core/roda-core/src/main/resources/config/crosswalks/ingest) anschauen.

Die XML-Datei muss in etwa so lauten:
```xml
<doc>
    <field name="title">abcdefgh</field>
    <field name="origdesc_txt">abcdefgh</field>
    <field name="destructiondate_txt">2020-01-01</field>
    <field name="destructiondate_dd">2020-01-01T00:00:00Z</field>
</doc>
```
Regeln:
- Es gibt einige reservierte Feldnamen, insbesondere `title`, `dateInitial` und `dateFinal`, die festlegen, was in den Listen erscheint
- Sie können neue Felder hinzufügen, es muss aber immer ein Suffix für den Datentyp hinterlegt werden. Die am häufigsten verwendeten Suffixe sind "\_txt" (beliebige Zeichenketten mit Token), "\_ss" (nicht mit Token versehene Zeichenketten für Bezeichner) und "\_dd" für ISO1601-Daten.
- Die Definition der Namen der Felder erfolgt [hier](https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/java/org/roda/core/index/schema/collections/AIPCollection.java#L61), aber Sie müssen möglicherweise auch [hier](https://github.com/keeps/roda/blob/master/roda-common/roda-common-data/src/main/java/org/roda/core/data/common/RodaConstants.java#L604) aufrufen, um den endgültigen Namen zu erfahren.
- Eine komplette Liste der Suffixe und Feldtypen ist verfügbar auf [SOLR-Basisschema](https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/resources/config/index/common/conf/managed-schema).

Um die Änderungen auf der Formatvorlage anzuwenden, müssen Sie neue Inhalte einlesen oder vorhandene Inhalte neu indizieren.

### 3. RODA für Feldanzeige in erweiterter Suche konfigurieren

Ändern Sie die Datei `roda-wui.properties` in [ein neues Feld für die erweiterte Suche hinzufügen] (https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/roda-wui.properties#L165):

```javaproperties
ui.search.fields.IndexedAIP = destructiondate # neues Feld zur Liste der Felder für Elemente (d.h. AIPs) hinzufügen, andere Optionen sind Darstellungen oder Dateien
ui.search.fields.IndexedAIP.destructiondate.fields = destructiondate_txt # ID des Feldes im Index, gleich derjenigen im Formatvorlage, das Sie erstellen
ui.search.fields.IndexedAIP.destructiondate.i18n = ui.search.fields.IndexedAIP.destructiondate # Schlüssel für die Übersetzung in ServerMessages.properties
ui.search.fields.IndexedAIP.destructiondate.type = text # Typ des Feldes, der die Eingabe im Suchformular beeinflusst
ui.search.fields.IndexedAIP.destructiondate.fixed = true # ob es bei der erweiterten Suche standardmäßig erscheint oder ob es über die Schaltfläche "SUCHFELD HINZUFÜGEN" hinzugefügt werden muss.
```
Sie sollten die notwendigen Übersetzungen zur `$RODA_HOME/config/i18n/ServerMessages.properties` hinzufügen, und zwar in allen Sprachen, die unterstützt werden sollen.

Fügen Sie [eine Übersetzung für den neuen Metadatentyp und die Metadatenversion] hinzu (https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/i18n/ServerMessages.properties#L121):

```javaproperties
ui.browse.metadata.descriptive.type.golikswe_1=Golik SWE (version 1)
```

Hinzufügen [Übersetzung der Felder](https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/i18n/ServerMessages.properties#L2):

```javaproperties
ui.search.fields.IndexedAIP.destructiondate= Destruction Date
```
