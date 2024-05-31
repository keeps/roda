# Repräsentationsinformation



*Der Inhalt dieses Artikels ist eine wortwörtliche Kopie des Artikels "OAIS 7: Representation Information", veröffentlicht im [Blog Alan's Notes on Digital Preservation] (https://alanake.wordpress.com/2008/01/24/oais-7-representation-information/ ).*


Die Darstellungsinformation ist ein entscheidendes Konzept, da ein Datenobjekt nur durch unser Verständnis der Darstellungsinformation geöffnet und betrachtet werden kann. Die Repräsentationsinformationen selbst können nur im Zusammenhang mit einer geeigneten Wissensbasis interpretiert werden.

Das Konzept der Repräsentationsinformationen ist auch untrennbar mit dem Konzept der Gemeinschaftsbehörde verbunden, da die Art und Weise, wie wir die Gemeinschaftsbehörde (und die damit verbundene Wissensbasis) definieren, bestimmt, wie viele Repräsentationsinformationen wir benötigen. "Das OAIS muss die Wissensbasis seiner Gemeinschaftsbehörde kennen, um zu verstehen, welche Repräsentationsinformationen mindestens gepflegt werden müssen... Im Laufe der Zeit kann die Entwicklung der Wissensbasis der Gemeinschaftsbehörde Aktualisierungen der Repräsentationsinformationen erfordern, um ein kontinuierliches Verständnis zu gewährleisten" (2.2.1).


Das Datenobjekt ist im digitalen Repository eine Bitfolge. Die Darstellungsinformationen dienen dazu, diese Bits in etwas Sinnvolleres umzuwandeln (oder uns zu sagen, wie wir sie umwandeln sollen). Sie beschreiben die Format- oder Datenstrukturkonzepte, die auf die Bitfolgen angewandt werden sollten, was wiederum zu aussagekräftigeren Werten wie Zeichen, Pixeln, Tabellen, usw. führt.

Dies wird als **Strukturinformation** bezeichnet. Idealerweise sollten die Darstellungsinformationen auch **semantische Informationen** enthalten, z.B. in welcher menschlichen Sprache der Text verfasst ist, was eine wissenschaftliche Terminologie bedeutet, usw. (4.2.1.3.1). Indem wir sowohl Struktur- als auch semantische Informationen einbeziehen, sind wir zukunftssicher.

Die Präservation von RI ist am einfachsten, wenn die Darstellungsinformationen in einer leicht verständlichen Form ausgedrückt werden, "wie z.B. ASCII" (4.2.1.3.2). Das Modell besagt, dass es nicht sinnvoll wäre, die Darstellungsinformationen beispielsweise in einem proprietären oder schwach unterstützten Dateiformat zu speichern, selbst wenn das Datenobjekt in einem solchen Format vorliegt. Die Darstellungsinformationen können bei Bedarf auf Papier ausgedruckt werden.

## Was ist das Minimum, das eine Darstellungsinformation leisten muss?

Die Darstellungsinformationen müssen die Wiederherstellung der wesentlichen Eigenschaften des ursprünglichen Datenobjekts ermöglichen oder erlauben. Das bedeutet, dass die Darstellungsinformationen in der Lage sein sollten, eine Kopie des Originals zu erstellen.

## Repräsentationsnetzwerke

Darstellungsinformationen können Verweise auf andere Darstellungsinformationen enthalten. Da die Repräsentationsinformation selbst ein Informationsobjekt mit einem eigenen Datenobjekt und zugehörigen Repräsentationsinformationen ist, kann sich ein ganzes Netz von Repräsentationsinformationen aufbauen. Dies wird als Repräsentationsnetzwerk bezeichnet (4.3.1.3.2). Zum Beispiel könnte die Repräsentationsinformation für ein Objekt einfach besagen, dass ein Datensatz in ASCII vorliegt. Je nachdem, wie wir unsere benannte Gemeinschaft definieren, müssen wir möglicherweise zusätzliche Repräsentationsinformationen beisteuern, z.B. was der ASCII-Standard tatsächlich ist.

## Repräsentationsinformation bei Ingest

Ein SIP kann mit sehr dürftigen Darstellungsinformationen auftauchen - vielleicht nur ein oder zwei gedruckte Handbücher oder einige PDFs im Dokumentationsordner (siehe E-ARK SIP-Spezifikation).

Das OAIS braucht viel mehr, was es aber nicht davon abhalten sollte, Material zu akzeptieren. Es ist möglich, sich zu sehr auf die Repräsentationsnetzwerke zu versteifen. Nur weil ein SIP mit nur 4 von 700 obligatorischen Metadatenfeldern angekommen ist, ist das noch lange kein Grund, es abzulehnen, wenn es sich um einen Datensatz von bleibendem Wert handelt.

## Darstellungssoftware

Bei den Darstellungsinformationen kann es sich um **ausführbare Software** handeln. In dem im Modell (4.2.1.3.2) genannten Beispiel liegt die Darstellungsinformation als PDF-Datei vor. Anstatt weitere Darstellungsinformationen zur Definition von PDF zu haben, ist es hilfreich, stattdessen einen PDF-Viewer zu verwenden.

Das OAIS muss dies jedoch sorgfältig verfolgen, denn eines Tages wird es keine PDF-Viewer mehr geben und die ursprünglichen Darstellungsinformationen müssten dann in eine neue Form migriert werden.

## Zugangssoftware und Emulation

Kurzfristig werden die meisten Datensätze vermutlich mit genau der Software geöffnet, mit der sie erstellt wurden. Jemand, der ein archiviertes, aber kürzlich erstelltes Word-Dokument öffnet, wird wahrscheinlich seine eigene MS-Word-Anwendung verwenden. Daraus ergibt sich die Möglichkeit, dass erweiterte Vertretungsnetze aufgegeben werden können und wir die Originalsoftware verwenden.

OAIS nennt dies Zugangssoftware und warnt davor, weil es bedeutet, dass wir funktionierende Software erhalten müssen. Dies scheint jedoch genau der Sinn der HW-Emulation zu sein. Wenn Sie den Satz "MS-Word-Dokument" als Repräsentationsinformation haben und eine funktionierende Kopie der Word-Anwendung auf der emulierten Hardware und Betriebssystem vorhalten, brauchen Sie kein Repräsentationsnetzwerk. Zumindest nicht, bis alles schief geht!

## Repräsentationsinformation in der Praxis

Nehmen wir eine JPEG-Bilddatei als Beispiel. Ich denke, wir können davon ausgehen, dass unsere derzeitige Gemeinschaftsbehörde weiß, was ein JPEG ist: In der OAIS-Terminologie enthält die Wissensbasis der Gemeinschaftsbehörde das Konzept und den Begriff "JPEG". Unsere Repräsentationsinformation für diese Datei könnte theoretisch einfach eine Aussage sein, dass es sich um ein JPEG-Bild handelt. Das ist ausreichend. Für das JPEG-Format und für viele andere Formate ist die nützlichste Repräsentationsinformation eine Anwendung, die es öffnen und anzeigen kann. (Jeder aktuelle PC ist ohnehin in der Lage, das JPEG zu öffnen.)

Längerfristig müssen wir uns jedoch auf eine Welt vorbereiten, in der sich die benannte Gemeinschaftsbehörde von JPEGs entfernt hat. Deshalb sollten wir auch einen Link zur Website des JPEG-Standards hinzufügen, um JPEG zu erklären. Wir können Informationen einfügen, welche Software-Anwendungen ein JPEG-Bild öffnen können. Noch nützlicher wäre ein Link im Repräsentationsnetzwerk zur Webseite, wo man einen JPEG-Viewer herunterladen kann.

Das bedeutet, dass sich das Repräsentationsnetzwerk im Laufe der Zeit verändert. Mit der technologischen Entwicklung müssen wir in der Lage sein, es zu aktualisieren.
