# Risikobewertung

RODA enthält ein Risikoregister mit mehr als 80 Präservationsrisiken [Digital Repository Audit Method Based on Risk Assessment (DRAMBORA)] (http://www.repositoryaudit.eu) Toolkit, das vom [Digital Curation Centre (DCC)] (http://www.dcc.ac.uk) und DigitalPreservationEurope (DPE) entwickelt wurde.

Es umfasst auch ein Risikoregister, das über die Benutzeroberfläche verwaltet werden kann, sowie mehrere Plugins zur Risikobewertung, die Informationen im Risikoregister aktualisieren.

## Wie können Präservationsrisiken in RODA bewertet und gemildert werden?

Sie möchten Risikobewertungsprozesse in Ihrem Repository durchführen. Beispielsweise möchten Sie einen Prozess zur Konvertierung von Dateien aus Formaten starten, die nicht mehr tragfähig sind (z.B. weil ein neues Risiko besteht, dass ein bestimmtes Dateiformat in Zukunft nicht mehr unterstützt wird).

Sie möchten einen Workflow für das folgende hypothetische Szenario haben:

1. Sie haben ein SIP mit einer Word 95.doc-Datei erstellt
1. Sie haben ein (hypothetisches) Risiko in Bezug auf die Word 95.doc Datei identifiziert (z.B. keine Software in unserem Institut kann dieses Format mehr lesen)
1. Da das Risiko erkannt wurde, möchte ich eine Konvertierung aller Word 95.doc-Dateien in DOCX und PDF/A vornehmen.

Es gibt verschiedene Möglichkeiten, neue Risiken zu bewältigen und eine Präservierungsmaßnahme zu ihrer Abschwächung einzuleiten, also konzentrieren wir uns darauf, wie wir das Beispiel lösen würden:

Stellen Sie sich vor, dass ich als Präservationsexperte weiß, dass Word 95 ein gefährdetes Format ist. Ich würde zum Risikoregister gehen und dieses Risiko registrieren, alle Informationen über dieses Risiko detailliert auflisten und mögliche Maßnahmen zur Abschwächung des Risikos benennen (z.B. die Migration in ein neues Format).

(Eine andere Möglichkeit wäre die Verwendung eines Plugins, das diese Art von Analyse automatisch durchführt. Derzeit gibt es kein solches Plugin und es müsste entwickelt werden.)

Sie können die Suchfunktion verwenden, um alle Word 95-Dateien im Repository zu finden. Alle Dateiformate wurden während des Ingest-Prozesses identifiziert. Ich würde dann das verfügbare Plugin zur Risikozuordnung verwenden, um diese Dateien als Instanzen des kürzlich erstellten Risikos festzulegen. Dies dient als Dokumentation der vom Präservationsexperten getroffenen Präservationsentscheidungen und als Begründung für das weitere Vorgehen - die eigentliche Präservationsplanung.

Der nächste Schritt wäre, die Dateien zu migrieren. Sie können dasselbe tun wie zuvor, d.h. alle Word-95-Dateien im Menü "Suchen" auswählen und eine Präservationsaktion für diese Dateien durchführen, um sie z.B. in PDF zu migrieren.

Sie könnten dann die Risikostufe herabsetzen, da sich keine Word-95-Dateien mehr im System befinden. Die Vorfälle können als "gemildert" markiert werden.

Dies ist der manuelle Arbeitsablauf, da wir derzeit kein Plugin für die Erkennung von Formatveralterungsrisiken haben. Ein solches Plugin könnte entwickelt werden. Die Schritte zur Risikominderung würden in diesem Fall direkt über die Risikomanagement-Schnittstelle eingeleitet.

Was die verfügbaren Konvertierungs-Plugins angeht, so unterstützt RODA derzeit die üblichen Formate (die wichtigsten Bild-, Video-, Text- und Audioformate). Nischenformate wird es immer geben. In diesem Fall müssen spezielle Plugins entwickelt werden.

## Haben Sie eine Idee für ein Plugin zur Risikobewertung?

Wenn Sie an der Entwicklung eines neuen Plugins zur Risikobewertung interessiert sind, wenden Sie sich bitte an das Produktteam, um weitere Informationen zu erhalten.
