# Häufig gestellte Fragen

Die von RODA-Benutzern häufig gestellten Fragen und deren Antworten.

Haben Sie eine Frage, die hier nicht gestellt wurde? Erstellen Sie [eine Anfrage] (https://github.com/keeps/roda/issues/new) auf GitHub und markieren Sie sie mit dem Vermerk "Frage".

## Viewers

### Kann man Dateien direkt auf der Web-Oberfläche von RODA ansehen?

Das System verfügt über vordefinierte Viewer für bestimmte Standardformate (z.B. PDF, Bilder, HTML 5 Multimediaformate, etc.).

Spezielle Formate benötigen spezielle Viewer oder Konverter, um sie an bestehende Viewer anzupassen, z.B. den SIARD 2 Viewer. Dies sind Entwicklungen, die von Fall zu Fall vorgenommen werden müssen.

## Metadaten

### Welche beschreibenden Metadatenformate werden von RODA unterstützt?

Alle beschreibenden Metadatenformate werden unterstützt, solange es Grammatik in XML Schema (XSD) gibt, um sie zu validieren. Standardmäßig wird RODA mit Dublin Core und Encoded Archival Description 2002 konfiguriert. Weitere Schemata können hinzugefügt werden.

### Kann RODA mehrere Klassifizierungssysteme unterstützen?

Das System ermöglicht eine Definition von mehreren hierarchischen Strukturen, in denen Datensätze abgelegt werden können. Die beschreibenden Metadaten können jedem Knoten dieser Strukturen zugewiesen werden. Sie können sich es als ein Datei-/Ordnersystem vorstellen, in dem jeder Ordner benutzerdefinierte Metadaten im EAD- oder DC-Format (oder in jedem anderen Format) enthalten kann. Jeder dieser "Ordner" (oder Platzhalter) kann ein Bestand, eine Sammlung, eine Serie, oder ein Aggregat, usw. sein.

### Bietet das System die Möglichkeit, Metadaten von höheren Ebenen in der Struktur zu übernehmen?

Aktuell nicht. Es müsste ein Plugin entwickelt werden.

### Kann die Beschreibungseinheit mit einer oder mehreren Dateien in einem anderen Archiv oder System verknüpft werden?

Die Beschreibungseinheiten sind Teil des AIP (Archival Information Package), was bedeutet, dass Darstellungen und Dateien in der Regel eng mit den Metadaten des Datensatzes verbunden sind. Es ist jedoch möglich, HTTP-Links zu anderen Ressourcen außerhalb des Repository hinzuzufügen, indem sie in die beschreibenden Metadaten eingefügt werden.

### Ist es möglich, eine Archivbeschreibung mit einer kontextuellen Einheit (z. B. ISAAR-Behörde) zu verknüpfen?

Das System unterstützt keine Behördendatensätze. Wenn Sie diese Datensätze jedoch extern verwalten, können Sie durch Bearbeiten der beschreibenden Metadaten einen Link hinterlegen.

### Wie können hybride Archive (Papier und digital) unterstützt werden?

Es ist möglich, Datensätze ohne digitale Repräsentationen zu haben, d.h. nur mit Metadaten. Aus Sicht des Katalogs ist dies in der Regel ausreichend, um Papierarchive zu unterstützen.

### Kann die Anwendung die Ebene der Übertragung aufzeichnen, z.B. wer, was und wann übertragen hat?

SIPs enthalten normalerweise Informationen darüber, wer, was und wann sie erstellt wurden. Der Ingest-Prozess erstellt Aufzeichnungen über den gesamten Prozess. Es wird jedoch erwartet, dass die SIPs an einem für das System zugänglichen Ort im Netzwerk abgelegt werden. Die Feststellung, wer die SIPs an diese Orte kopiert hat, liegt außerhalb des Anwendungsbereichs des Systems.

### Wie kann das System den Standort der physischen Archive erfassen?

Dies kann durch Ausfüllen eines Metadatenfeldes geschehen. Typischerweise <ead:physloc>.

## Suche

### Nach welchen Metadatenattributen können wir suchen?

Die Suchseite ist über die Konfigurationsdatei vollständig konfigurierbar. Sie können Attribute, Typen, Bezeichnungsnamen, usw. festlegen.

### Ist eine Volltextsuche unterstützt?

Ja, ist von der erweiterten Suche unterstützt.

### Kann ein Benutzer aus dem Suchergebnis heraus analoge Dokumente aus den Archiven anfordern?

Nein. Dies müsste in ein externes System integriert werden, das diese Anfragen bearbeitet.

### Spiegelt die Liste der Suchergebnisse die Berechtigungen wider, die für die angezeigten Datensätze gelten?

Ja. Sie können nur die Datensätze sehen, zu denen Sie Zugang haben.

### Ist der Prüfpfad durchsuchbar und benutzerfreundlich zugänglich?

Ja. Sie können direkt von der Web-Benutzeroberfläche aus durch das Aktionsprotokoll (alle am Repository durchgeführten Aktionen) oder durch die Erhaltungsmetadaten (Liste der an den Daten durchgeführten Erhaltungsaktionen) navigieren.

## Präservation

### Beschreiben Sie die Funktionsweise der Quarantäne-Umgebung.

Wenn SIPs während des Ingest-Prozesses verarbeitet werden, werden sie in einen speziellen Ordner im Dateisystem verschoben, wenn sie nicht akzeptiert werden. Der Ingest-Prozess erstellt einen detaillierten Bericht, der die Gründe für die Ablehnung beschreibt. Ab diesem Zeitpunkt müssen die Dateien manuell bearbeitet werden.

### Wie unterstützt das System die Bewahrung?

Dies ist eine komplexe Frage, die nicht mit ein paar Zeilen beantwortet werden kann. Wir können jedoch zusammenfassen, dass das System die Bewahrung auf verschiedene Weise handhabt:

- Es gibt Aktionen, die regelmäßige Korrekturprüfungen der aufgenommenen Dateien durchführen und die Repository-Manager warnen, wenn ein Problem entdeckt wird.
- Das System verfügt über eine eingebettete Risikomanagement-GUI (d.h. ein Risikoregister).
- Es gibt Aktionen, die Risiken für Dateien erkennen und dem Risikoregister neue Bedrohungen hinzufügen, die manuell angegangen werden müssen (z.B. ein Datensatz ist nicht ausreichend beschrieben, eine Datei entspricht nicht den Formatrichtlinien von Repository, ein Dateiformat ist unbekannt oder es gibt keine Darstellungsinformationen usw.).
- Es gibt Maßnahmen, die es den Verantwortlichen für die Bewahrung ermöglichen, Risiken zu mindern, z.B. die Konvertierung von Dateiformaten (Dutzende von Formaten werden unterstützt).

### Wie unterstützt die Anwendung die Beurteilung, die Auswahl und die Definition von Aufbewahrungsfristen?

RODA bietet einen komplexen Workflow für die Entsorgung von Datensätzen. Weitere Informationen finden Sie in [Entsorgung](Disposal.md).

### Protokolliert das System die Suchinteraktionen?

Ja. Jede Suchaktion im System wird protokolliert.

## Anforderungen

### Gibt es irgendwelche Systemanforderungen auf der Client-Seite für diejenigen, die die Archive beraten?

Nicht wirklich. Ein aktueller Browser ist ausreichend.

## Anleitung

### Wie wird eine neue Sprache in das System hinzugefügt?

Eine vollständige Anleitung zum Hinzufügen einer neuen Sprache in das System finden Sie unter: [Übersetzungsleitfaden](Translation_Guide.md).

### Wie richtet man die Entwicklungsumgebung für RODA ein?

Eine vollständige Anleitung zum Einrichten der Entwicklungsumgebung finden Sie unter: [Leitfaden für Entwickler](Developers_Guide.md).
