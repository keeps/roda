
# Übersicht

RODA ist ein umfassendes digitales Repository, das Funktionalitäten für alle Hauptbereiche des OAIS-Referenzmodells bietet. RODA kann verschiedene Arten von digitalen Objekten, die von großen Unternehmen oder öffentlichen Einrichtungen erstellt wurden, aufnehmen, verwalten und den Zugriff darauf ermöglichen. RODA basiert auf Open-Source-Technologien und unterstützt bestehende Standards wie OAIS, METS, EAD und PREMIS.

RODA implementiert auch eine Reihe von Spezifikationen und Standards. Um mehr über die OAIS Information Packages zu erfahren, die RODA implementiert, besuchen Sie bitte die Repositories des [Digital Information LifeCycle Interoperability Standards Board](http://www.dilcis.eu/) auf GitHub unter https://github.com/dilcisboard.

## Funktionen

* Benutzerfreundliche grafische Benutzeroberfläche basierend auf HTML 5 und CSS 3
* Speicherung und Verwaltung digitaler Objekte
* Katalog basierend auf umfangreichen Metadaten (unterstützt jedes XML-basierte Format als beschreibende Metadaten)
* Unterstützung für Dublin Core und Encoded Archival Description out-of-the-box
* Konfigurierbarer mehrstufiger Ingest-Workflow
* PREMIS 3 für Metadaten zur Langzeitarchivierung
* Authentifizierung und Autorisierung über LDAP und CAS
* Berichte und Statistiken
* REST-API
* Unterstützt erweiterbare Erhaltungsaktionen
* Integriertes Risikomanagement
* Integriertes Formatregister
* Verwendet ein natives Dateisystem zur Datenspeicherung
* 100 % kompatibel mit E-ARK SIP-, AIP- und DIP-Spezifikationen
* Unterstützung für Themen

Für weitere Informationen besuchen Sie bitte die RODA-Website:
**<https://www.roda-community.org>**


## Funktionen

RODA bietet eine Benutzeroberfläche für die folgenden Funktionseinheiten.

### Katalog

Der Katalog ist das Verzeichnis aller Elemente oder Aufzeichnungen, die im Repository zu finden sind. Eine Aufzeichnung kann jede im Repository verfügbare Informationsentität repräsentieren (z. B. Buch, elektronisches Dokument, Bild, Datenbankexport usw.). Aufzeichnungen werden normalerweise in Sammlungen (oder Fonds) zusammengefasst und anschließend in Unter-Sammlungen, Abschnitte, Serien, Dateien usw. organisiert. Diese Seite listet alle Top-Level-Aggregationen im Repository auf. Sie können durch Klicken auf die Einträge in der Tabelle zu Unteraggregationen navigieren.

### Suche

Auf dieser Seite können Sie nach geistigen Entitäten, Darstellungen oder Dateien suchen (verwenden Sie den Pfeil nach unten, um den Suchbereich auszuwählen). Für jeden dieser Bereiche können Sie in allen Eigenschaften oder in bestimmten Eigenschaften suchen (verwenden Sie den Pfeil nach unten, um die erweiterte Suche zu erweitern). Wenn Sie beispielsweise "Geistige Entitäten" auswählen, können Sie in einem bestimmten Feld der beschreibenden Metadaten suchen oder Dateien eines bestimmten Formats finden, wenn die erweiterte Suche "Dateien" ausgewählt ist.

Die Suchmaschine findet nur ganze Wörter. Wenn Sie nach Teilbegriffen suchen möchten, verwenden Sie den '*' Operator. Weitere Informationen zu den verfügbaren Suchoperatoren finden Sie im nächsten Abschnitt.

### Erweiterte Suche

Auf der Suchseite können Sie nach intellektuellen Entitäten, Repräsentationen oder Dateien suchen (verwenden Sie den Pfeil nach unten, um den Suchbereich auszuwählen). Für jeden dieser Bereiche können Sie nach allen oder bestimmten Kriterien suchen (verwenden Sie den Pfeil nach unten für erweiterte Suche). Wenn Sie beispielsweise "intellektuelle Entitäten" auswählen, können Sie in einem bestimmten Bereich der beschreibenden Metadaten suchen oder Dateien eines bestimmten Formats finden, wenn die erweiterte Dateien-Suche ausgewählt ist.

### Pre-Ingest

Der Pre-Ingest-Prozess beschreibt die Fähigkeit eines Produzenten, Submission Information Packages (SIPs) zu erstellen, die sowohl Daten als auch Metadaten (in einer genau definierten Struktur) enthalten, um sie dem Repositorium zum Ingest vorzulegen. Von den erstellten SIPs wird erwartet, dass sie den vom Repository festgelegten (oder mit ihm ausgehandelten) Richtlinien entsprechen.

### Übertragung

Der Transferbereich bietet den geeigneten temporären Speicherplatz für die Aufnahme von Submission Information Packages (SIPs) von Herstellern. SIPs können per elektronischer Übertragung (z. B. FTP) geliefert oder von an das Repository angeschlossenen Medien geladen werden. Diese Seite ermöglicht es dem Benutzer auch, Dateien im temporären Übertragungsbereich zu suchen, Ordner zu erstellen/löschen und mehrere SIPs gleichzeitig zur weiteren Verarbeitung und Aufnahme in das Repository hochzuladen. Der Ingest-Prozess kann durch Auswahl der SIPs, die Sie in den Verarbeitungsstapel aufnehmen möchten, eingeleitet werden. Klicken Sie auf die Schaltfläche "Verarbeiten", um den Ingest-Prozess zu starten.

### Übernahme

Der Ingest-Prozess umfasst Dienste und Funktionen zur Annahme von Einreichungs-Informationspaketen (Submission Information Packages, SIPs) von Produzenten, zur Vorbereitung von Archiv-Informationspaketen (Archival Information Packages, AIPs) für die Speicherung und zur Sicherstellung, dass Archiv-Informationspakete und die zugehörigen beschreibenden Informationen im Repository eingerichtet werden. Auf dieser Seite werden alle Ingest-Jobs aufgeführt, die derzeit ausgeführt werden, sowie alle Jobs, die in der Vergangenheit ausgeführt wurden. Auf der rechten Seite können Sie die Aufträge nach ihrem Status, dem Benutzer, der den Auftrag initiiert hat, und dem Startdatum filtern. Wenn Sie auf ein Element in der Tabelle klicken, können Sie den Fortschritt des Auftrags sowie weitere Details anzeigen.

### Bewertung

Bei der Bewertung wird festgestellt, ob Unterlagen und andere Materialien einen dauerhaften (archivarischen) Wert haben. Die Bewertung kann auf der Ebene der Sammlung, des Verfassers, der Serie, der Akte oder des Objekts erfolgen. Die Bewertung kann vor der Schenkung und vor der physischen Übergabe, bei oder nach dem Beitritt erfolgen. Die Grundlage für Bewertungsentscheidungen kann eine Reihe von Faktoren umfassen, einschließlich der Herkunft und des Inhalts der Unterlagen, ihrer Authentizität und Zuverlässigkeit, ihrer Ordnung und Vollständigkeit, ihres Zustands und der Kosten für ihre Erhaltung sowie ihres intrinsischen Wertes.

### Maßnahmen zur Erhaltung

Erhaltungsmaßnahmen sind Aufgaben, die am Inhalt des Repositorys durchgeführt werden, um die Zugänglichkeit der archivierten Dateien zu verbessern oder die Risiken der digitalen Bewahrung zu mindern. Innerhalb von RODA werden Erhaltungsmaßnahmen von einem Auftragsausführungsmodul durchgeführt. Mit dem Modul zur Auftragsausführung kann der Repository-Manager Aktionen für einen bestimmten Datensatz (AIPs, Repräsentationen oder Dateien) ausführen. Zu den Erhaltungsmaßnahmen gehören Formatkonvertierungen, Prüfsummenüberprüfungen, Berichterstellung (z. B. automatischer Versand von SIP-Annahme-/Ablehnungs-E-Mails), Virenprüfungen usw.

### Interne Aktionen

Interne Aktionen sind komplexe Aufgaben, die vom Repository als Hintergrundjobs ausgeführt werden und das Benutzererlebnis verbessern, indem sie die Benutzeroberfläche während langandauernder Operationen nicht blockieren. Beispiele für solche Operationen sind: Verschieben von AIPs, Neuindizierung von Teilen des Repositorys oder Löschen einer großen Anzahl von Dateien.

### Nutzer und Gruppen

Der Benutzerverwaltungsdienst ermöglicht es dem Repository-Manager, Anmeldeinformationen für jeden Benutzer im System zu erstellen oder zu ändern. Dieser Dienst ermöglicht es dem Manager auch, Gruppen und Berechtigungen für jeden der registrierten Benutzer festzulegen. Manager können Benutzer und Gruppen, die derzeit angezeigt werden, filtern, indem sie die verfügbaren Optionen in der rechten Seitenleiste auswählen. Um einen neuen Benutzer zu erstellen, klicken Sie auf die Schaltfläche "Benutzer hinzufügen". Um eine neue Benutzergruppe zu erstellen, klicken Sie auf die Schaltfläche "Gruppe hinzufügen". Um einen vorhandenen Benutzer oder eine vorhandene Gruppe zu bearbeiten, klicken Sie auf einen Eintrag in der Tabelle.

### Aktivitätsprotokoll

Ereignisprotokolle sind spezielle Dateien, die signifikante Ereignisse im Repository aufzeichnen. Zum Beispiel wird jedes Mal, wenn sich ein Benutzer anmeldet, ein Download durchgeführt wird oder eine Änderung an einer beschreibenden Metadatendatei vorgenommen wird, ein Eintrag protokolliert. Bei Auftreten dieser Ereignisse zeichnet das Repository die erforderlichen Informationen im Ereignisprotokoll auf, um eine zukünftige Überprüfung der Systemaktivität zu ermöglichen. Für jedes Ereignis werden folgende Informationen aufgezeichnet: Datum, beteiligte Komponente, Systemmethode oder -funktion, Zielobjekte, Benutzer, der die Aktion ausgeführt hat, die Dauer der Aktion und die IP-Adresse des Benutzers, der die Aktion ausgeführt hat. Benutzer können Ereignisse nach Typ, Datum und anderen Attributen filtern, indem sie die verfügbaren Optionen in der rechten Seitenleiste auswählen.

### Benachrichtigungen

Benachrichtigungen informieren RODA-Benutzer über bestimmte Ereignisse, die aufgetreten sind. Diese Kommunikation erfolgt durch das Versenden einer E-Mail, in der das spezifische Ereignis beschrieben wird und der Benutzer es bestätigen kann.

### Statistiken

Diese Seite zeigt ein Dashboard mit Statistiken zu verschiedenen Aspekten des Repositorys. Die Statistiken sind in Abschnitte unterteilt, von denen sich jeder auf einen bestimmten Aspekt des Repositorys konzentriert, z. B. Fragen im Zusammenhang mit Metadaten und Daten, Statistiken zu Ingestions- und Erhaltungsprozessen, Zahlen zu Benutzern und Authentifizierungsproblemen, Erhaltungsereignissen, Risikomanagement und Benachrichtigungen.

### Risikoverzeichnis

Das Risikoregister listet alle identifizierten Risiken auf, die das Repository beeinflussen können. Es sollte so umfassend wie möglich sein, um alle erkennbaren Bedrohungen zu erfassen, und in der Regel eine geschätzte Wahrscheinlichkeit für das Auftreten jedes Risikos, die Schwere oder mögliche Auswirkungen des Risikos und dessen wahrscheinliche zeitliche Abfolge oder erwartete Häufigkeit enthalten. Risikominderung ist der Prozess, Maßnahmen zu definieren, um Chancen zu verbessern und Bedrohungen für die Ziele des Repositorys zu reduzieren.

### Repräsentationsnetzwerk

Repräsentationsinformationen sind alle Informationen, die benötigt werden, um das digitale Material und die zugehörigen Metadaten zu verstehen und darzustellen. Digitale Objekte werden als Bitstreams gespeichert, die ohne weitere Daten zur Interpretation für den Menschen nicht verständlich sind. Repräsentationsinformationen sind die zusätzlichen strukturellen oder semantischen Informationen, die Rohdaten in etwas Bedeutungsvolleres umwandeln.

### Formatregister (veraltet)

Das Formatregister ist ein technisches Register zur Unterstützung von Digital Preservation Services der Repositorys.
