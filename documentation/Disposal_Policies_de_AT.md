# Entsorgungsrichtlinien

## Entsorgungsplan

Entsorgungspläne legen die Mindestanforderungen für die Pflege, Aufbewahrung oder Vernichtung der bestehenden oder künftigen intellektuelle Entitäten in diesem Repository fest. Eine intellektuelle Entität darf nur im Rahmen eines Vernichtungsprozesses entsorgt werden, der durch den dieser Entität zugewiesenen Entsorgungsplan geregelt wird. Der Entsorgungsplan bestimmt, wie lange ein Datensatz aufbewahrt wird und wie er am Ende seiner Aufbewahrungsfrist entsorgt wird.

### Was ist ein Entsorgungsplan?

In [MoReq2010®](https://moreq.info/) heißt es: "Entsorgungspläne sind entscheidend für die Verwaltung von Aufzeichnungen, da MoReq2010® festlegt, dass eine Aufzeichnung in einem MCRS nur im Rahmen eines Entsorgungsprozesses vernichtet werden darf, der durch den dieser Aufzeichnung zugewiesenen Entsorgungsplan geregelt wird. Es ist der Entsorgungsplan der Aufzeichnung, der bestimmt, wie lange eine Aufzeichnung aufbewahrt wird und wie sie am Ende der Aufbewahrungsfrist entsorgt wird.

RODA unterstützt drei Arten von Entsorgungsaktionen:

1. Dauerhaft aufbewahren;
2. Überprüfung am Ende der Aufbewahrungsfrist;
3. Entsorgung am Ende der Aufbewahrungsfrist.

Zur Berechnung der Aufbewahrungsfrist wird der Aufbewahrungsauslöser-Identifikator verwendet, dessen Wert zur Aufbewahrungsfrist addiert wird. Die möglichen Werte für die Aufbewahrungsfrist sind:

1. Keine Aufbewahrugsfrist;
2. Tage;
3. Wochen;
4. Monate;
5. Jahre.

### 2. Was kategorisiert einen Entsorgungsplan?

Folgende Eigenschaften/Merkmale kategorisieren einen Entsorgungsplan:

| *Feld* | *Beschreibung* | *Verpflichtend* |
| --------- |---------- | ------------- |
| Titel | Name oder Titel des Entsorgungsplans | wahr |
| Beschreibung | Beschreibung des Entsorgungsplans | falsch |
| Mandat | Textverweis auf ein rechtliches oder anderes Instrument, das die Ermächtigung für einen Entsorgungsplan liefert | falsch |
| Bereich Notizen | Leitlinien für autorisierte Benutzer geben an, wie eine bestimmte Entität anzuwenden ist und etwaige organisatorische Richtlinien oder Beschränkungen für ihre Verwendung anzugeben sind | falsch |
| Entsorgungsaktion | Kode zur Beschreibung der Maßnahme, die bei der Beseitigung des Datensatzes zu ergreifen ist (mögliche Werte: dauerhaft aufbewahren, überprüfen, vernichten) | wahr |
| Aufbewahrungsauslöser-Identifikator | Das beschreibende Metadatenfeld, das für die Berechnung der Aufbewahrungsfrist verwendet wird | wahr (wenn der Code für die Entsorgungsmaßnahme sich von "dauerhaft aufbewahren" unterscheidet) |
| Aufbewahrugsfrist | Anzahl der Tage, Wochen, Monate oder Jahre, die für die Aufbewahrung eines Datensatzes angegeben werden, nachdem die Aufbewahrungsfrist ausgelöst wurde | wahr (wenn der Code für die Entsorgungsmaßnahme sich von "dauerhaft aufbewahren" unterscheidet) |

### 3. Lebenszyklus der Aufzeichnung

#### Lebenszyklus der dauerhaften Aufbewahrung

Diese Art von Entsorgungsplan, bei dem es keinen Auslöser für die Aufbewahrung gibt, verhindert die Berechnung eines Anfangsdatums für die Aufbewahrung und einer anschließenden Aufbewahrungsfrist.

![Lebenszyklus der dauerhaften Aufbewahrung](images/permanent_retention_life_cycle.png "Lebenszyklus der dauerhaften Aufbewahrung")

#### Überprüfung des Lebenszyklus

Wenn die Beseitigungsmaßnahme für einen Datensatz auf Überprüfung gesetzt wird, wird er nicht sofort vernichtet. Stattdessen muss das Ergebnis der Überprüfung die Anwendung eines Entsorgungsplans für den Datensatz auf der Grundlage der Überprüfungsentscheidung beinhalten. Der neue Beseitigungsplan ersetzt den vorherigen Beseitigungsplan für den Datensatz und legt dann den endgültigen Verbleib des Datensatzes fest oder kann dazu verwendet werden, eine weitere Überprüfung zu planen oder den Datensatz dauerhaft aufzubewahren.

![Überprüfung des Lebenszyklus](images/review_life_cycle.png "Überprüfung des Lebenszyklus")

#### Lebenszyklus der Vernichtung

Die Vernichtung von Unterlagen unterliegt besonderen Beschränkungen. Wie die Datensätze vernichtet werden, hängt von der Art des Inhalts ihrer Komponenten ab. Mit RODA können beschreibende Metadaten mit Hilfe von [XSLT (eXtensible Stylesheet Language Transformations)] (http://www.w3.org/standards/xml/transformation.html) beschnitten werden. Alle mit dem Datensatz verknüpften Dateien werden vernichtet, so dass sich der Datensatz in einem zerstörten Zustand befindet.

![Lebenszyklus der Vernichtung](images/destruction_life_cycle.png "Lebenszyklus der Vernichtung")

## Entsorgungsregeln

### Was ist eine Entsorgungsregel?

Entsorgungsregeln sind eine Reihe von Anforderungen, die den Entsorgungsplan für jede intellektuelle Entität in diesem Repository festlegen. Die Entsorgungsregeln können jederzeit angewendet werden, um die Konsistenz des Repositorys zu wahren. Entsorgungsregeln können auch während des Ingest-Prozesses angewendet werden. Entsorgungsregeln haben eine Prioritätseigenschaft, in der sie ausgeführt werden. Fällt ein Datensatz unter keine der Regeln, wird er nicht mit einem Entsorgungsplan verknüpft.

### 2. Was kategorisiert eine Entsorgungsregel?

Folgende Eigenschaften/Merkmale kategorisieren eine Entsorgungsregel:

| *Feld* | *Beschreibung* | *Verpflichtend* |
| --------- |---------- | ------------- |
| Sonstige | Prioritätsreihenfolge, in der die Regeln im Ingest-Prozess oder im Anwendungsprozess angewendet werden | wahr |
| Titel | Name oder Titel der Entsorgungsregel | wahr |
| Beschreibung | Beschreibung der Entsorgungsregel | falsch |
| Terminplaner | Entsorgungsplan, der mit einem Datensatz verknüpft werden soll | wahr |
| Auswahlverfahren | Bedingung, die die Entsorgungsregel auslöst (mögliche Werte: Kindelement von, Metadatenfeld) | wahr |

### 3. Auswahlverfahren

Die Auswahlmethode ist der Mechanismus, der für den Abgleich der Regeln mit den Datensätzen im Repository und die Anwendung des Entsorgungsplans verantwortlich ist.

Bei RODA gibt es zwei Arten von Auswahlmethoden:

* Kindelement von: wenn der Datensatz direkt unter einem bestimmten AIP liegt.
* Metadatenfeld: wenn der Datensatz einen beschreibenden Metadatenwert hat.

### 4. Wie funktioniert es?

Entsorgungsregeln können während des Ingest-Prozesses über ein Plugin oder, falls gewünscht, jederzeit auf das Repository angewendet werden. AIP mit manuell zugeordneten Entsorgungsplänen können überschrieben oder unverändert beibehalten werden.

## Entsorgungssperren

### 1. Was ist eine Entsorgungssperre?

Entsorgungssperren sind rechtliche oder administrative Anordnungen, die den normalen Entsorgungsprozess unterbrechen und die Vernichtung einer intellektuellen Entität verhindern, solange die Entsorgungssperre besteht. Wenn die Entsorgungssperre mit einem einzelnen Datensatz verbunden ist, verhindert sie die Vernichtung dieses Datensatzes, solange die Sperre aktiv ist. Sobald die Entsorgungssperre aufgehoben ist, wird der Prozess der Aktenvernichtung fortgesetzt.

### 2. Was kategorisiert eine Entsorgungssperre?

Folgende Eigenschaften/Merkmale kategorisieren eine Entsorgungssperre:

| *Feld* | *Beschreibung* | *Verpflichtend* |
| --------- |---------- | ------------- |
| Titel | Name oder Titel der Entsorgungssperre | wahr |
| Beschreibung | Beschreibung der Entsorgungssperre | falsch |
| Mandat | Textverweis auf ein rechtliches oder anderes Instrument, das die Ermächtigung für eine Entsorgungssperre liefert | falsch |
| Bereich Notizen | Leitlinien für autorisierte Benutzer geben an, wie eine bestimmte Entität anzuwenden ist und etwaige organisatorische Richtlinien oder Beschränkungen für ihre Verwendung anzugeben sind | falsch |

### 3. Wie funktioniert es?

Wenn ein Entsorgungssperrvermerk mit einem Datensatz verknüpft ist, verhindert dieser, dass der Datensatz über den Entsorgungsworkflow vernichtet wird und blockiert das Löschen des Datensatzes. Um die Kontrolle über den Datensatz zu erlangen, muss die Entsorgungssperre aufgehoben oder getrennt werden.