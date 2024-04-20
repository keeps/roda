# Pre-Ingest

Der Pre-Ingest-Prozess beschreibt die Fähigkeit eines Produzenten, Submission Information Packages (SIP) zu erstellen, die sowohl Daten als auch Metadaten (in einer genau definierten Struktur) enthalten, um sie an das Repository zur Aufnahme zu übermitteln. Von den erstellten SIPs wird erwartet, dass sie den vom Repository festgelegten (oder mit ihm ausgehandelten) Richtlinien entsprechen.

Der Prozess der Voruntersuchung umfasst in der Regel einige oder alle der folgenden Aktivitäten:

## Vorlagevereinbarung

Diese Tätigkeit besteht in der Festlegung der Bedingungen, Voraussetzungen und Anforderungen für Inhalte und begleitende Informationen (z.B. Metadaten, Dokumentation, Verträge, usw.), die der Hersteller an das Repositorium zu übermitteln hat. Sie wird in einer schriftlichen Vereinbarung zwischen dem Hersteller und dem Repositorium festgehalten, in der die Art der Inhalte und alle rechtlichen und technischen Anforderungen, die von beiden Parteien zu erfüllen sind, festgelegt werden.

## Klassifizierungsplan

Bei der Unterzeichnung des Einreichungsvertrags muss der Hersteller einem Basis-Klassifizierungsschema (oder einer Liste von Sammlungen) zugestimmt haben, auf dem er die ausdrückliche Genehmigung zur Hinterlegung neuer Informationen erhält.

Das Basis-Klassifikationsschema wird in der Regel vom Repositorium erstellt und kann in diesem Abschnitt in maschinenlesbarem Format heruntergeladen werden. Die heruntergeladene Datei kann in RODA-in geladen werden, um die Einreichungsinformationspakete besser zu ordnen und vorzubereiten, bevor sie an das Repositorium übertragen und dort aufgenommen werden.

[Herunterladen des Klassifikationsschemas](/api/v2/classification-plans) (Hinweis: Das Herunterladen des Klassifikationsschemas erfordert eine RODA-Instanz)

## Einreichungsinformationspakete (SIP)

Diese Tätigkeit besteht in der Erstellung eines oder mehrerer Einreichungsinformationspakete (SIP) gemäß den im Einreichungsabkommen festgelegten technischen und nicht-technischen Anforderungen. Um die Erstellung der SIPs zu erleichtern, können die Produzenten das RODA-in-Tool nutzen.

Das System und die Dokumentation sind verfügbar unter [http://rodain.roda-community.org](http://rodain.roda-community.org).


## Materialtransfer

Diese Aktivität besteht aus der Übertragung von Einreichungsinformationspakete (SIP) vom Hersteller zum Repository. Die SIPs werden vorübergehend in einem Quarantänebereich gelagert und warten auf die Verarbeitung durch das Repository.

Es gibt mehrere Möglichkeiten, wie SIPs an das Repository übertragen werden können. Dazu gehören unter anderem die folgenden Optionen:

### HTTP-Übertragung

1. Stellen Sie eine Verbindung zur Repository-Website her und melden Sie sich mit den angegebenen Anmeldedaten an.
2. Rufen Sie das Menü "Ingest/Transfer" auf und geben Sie den Ordner mit Ihrem Benutzernamen ein (oder erstellen Sie den Ordner, falls erforderlich).
3. Laden Sie alle Ihre SIPs in den neuen Ordner hoch.
4. Informieren Sie das Repository, dass das Material zum Ingest bereit ist.

### FTP-Übertragung

1. Stellen Sie eine Verbindung zu [ftp://address] her und und melden Sie sich mit den angegebenen Anmeldedaten an.
2. Erstellen Sie einen Ordner, in dem die SIPs gespeichert werden, die Sie in einem einzigen Ingest aufnehmen möchten (optional).
3. Kopieren Sie alle Ihre SIPs in den neuen Ordner.
4. Informieren Sie das Repository, dass das Material zum Ingest bereit ist.

### Externe Medienübertragung

1. SIPs auf einem externen Medium speichern (z.B. CD, USB-Diskette, usw.)
2. Liefern Sie es an die folgende Adresse: [Adresse von Repository]

## Ingest Prozess

Nach der Übertragung werden die SIPs von den Mitarbeitern des Repository für den Ingest ausgewählt. Der Ingest-Prozess bietet Dienste und Funktionen zur Annahme von SIPs von Produzenten und zur Vorbereitung der Inhalte für die Archivierung und Verwaltung.

Zu den Ingest-Funktionen gehören der Empfang von SIPs, die Qualitätssicherung der SIPs, die Erstellung eines Archivinformationspakets (AIP), das den Datenformatierungs- und Dokumentationsstandards des Repository entspricht, die Extraktion von beschreibenden Informationen aus den AIPs zur Aufnahme in den Repository-Katalog und die Koordinierung von Aktualisierungen der Archivspeicher und des Datenmanagements.

