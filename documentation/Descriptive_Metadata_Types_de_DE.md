# beschreibende Metadatentypen

Bei der Erstellung einer neuen intellektuellen Entität ist einer der Schritte die Auswahl des beschreibenden Metadatentyps.

Dies bezieht sich auf das zu verwendende Schema für beschreibende Metadaten, wobei RODA standardmäßig die folgenden Optionen unterstützt:

* **[EAD 2002](https://www.loc.gov/ead/)**: Encoded Archival Description (EAD) Version 2002 ist ein XML-Standard für die Kodierung von archivischen Findmitteln, der vom Technical Subcommittee for Encoded Archival Standards der Society of American Archivists in Zusammenarbeit mit der Library of Congress gepflegt wird. Er wird hauptsächlich von Archiven verwendet, um sowohl digital entstandene als auch analoge Dokumente zu beschreiben.
* **[Dublin Core](https://www.dublincore.org/schemas/xmls/)**: Die Dublin Core (DC) Metadaten-Initiative unterstützt Innovationen bei der Gestaltung von Metadaten und bewährte Verfahren. Zu den derzeit empfohlenen Schemata gehört das *Simple DC XML schema, Version 2002-12-12*, das Begriffe für Simple Dublin Core definiert, d.h. die 15 Elemente aus dem Namensraum http://purl.org/dc/elements/1.1/, ohne Verwendung von Kodierungsschemata oder Elementverfeinerungen.
* **[Schlüsselwert](https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/resources/config/schemas/key-value.xsd)**: Ein RODA-internes einfaches Beschreibungsschema für die Definition von Schlüsselwert-Metadaten, wobei der Metadaten-Schlüssel das Element identifiziert (z.B. "Titel") und der Wert den Inhalt des Metadatenelements.
*  **Andere**: Generischer XML-Typ, für den kein Schema definiert ist.

Neue Metadatentypen können zu RODA hinzugefügt werden, indem die Dokumentation [Metadata Format](Metadata_Formats.md) befolgt wird.

| beschreibendes Metadatentyp | Überprüfung           | Indexierung         | Visualisierung         | Ausgabe      |
|---------------------------|----------------------|------------------|-----------------------|--------------|
| EAD 2002                  | Schema-Überprüfung    | Indexierungsregeln   | Visualisierungsregeln   | Ausgabeform |
| Dublin Core               | Schema-Überprüfung    | Indexierungsregeln   | Visualisierungsregeln   | Ausgabeform |
| Schlüsselwert                 | Schema-Überprüfung    | Indexierungsregeln   | Visualisierungsregeln   | Ausgabeform |
| Sonstige                     | Überprüfung der Wohlgeformtheit | Allgemeine Indexierung | Generische Visualisierung | XML bearbeiten     |

Legende:
* **Schema-Validierung**: Das Repository bietet ein XML-Schema zur Validierung der Struktur und der Datentypen der bereitgestellten Metadaten-Datei. Das Validierungsschema wird während des Ingest-Prozesses verwendet, um zu prüfen, ob die im SIP enthaltenen Metadaten gemäß den festgelegten Einschränkungen gültig sind, und auch, wenn die Metadaten über den Katalog bearbeitet werden.
* **Überprüfung der Wohlgeformtheit**: Das Repository prüft nur, ob die Metadaten-XML-Datei wohlgeformt ist. Da kein Schema definiert ist, prüft das Repository nicht, ob die Datei gültig ist.
* **Indexierungsregeln**: Das Repository bietet ein Standard-XSLT, das die XML-basierten Metadaten umwandelt, damit die Indizierungsmaschine die Metadaten verstehen kann. Es ermöglicht erweiterte Suche über die beschreibenden Metadaten.
* **Allgemeine Indexierung**: Das Repository indexiert alle Textelemente und Attributwerte, die in der Metadatendatei gefunden werden. Da das Repository jedoch nicht das richtige Mapping zwischen den XML-Elementen und dem inneren Datenmodell kennt, ist nur eine einfache Suche in den bereitgestellten Metadaten möglich.
* **Visualisierungsregeln**: Das Repository bietet ein Standard-XSLT, das die XML-basierten Metadaten in eine HTML-Datei umwandelt, die dem Benutzer beim Durchsuchen eines bestehenden AIP im Katalog angezeigt wird.
* **Generische Visualisierung**: Das Repository bietet einen generischen Metadaten-Viewer zur Anzeige der XML-basierten Metadaten. Alle Textelemente und Attribute werden in keiner bestimmten Reihenfolge angezeigt und ihre XML-Pfadsprache wird als Bezeichnung verwendet.
* **Bearbeitungsformular**: Das Repository stellt eine Konfigurationsdatei zur Verfügung, die angibt, wie ein Formular zur Bearbeitung vorhandener Metadaten angezeigt werden soll.
* **XML bearbeiten**: Das Repository zeigt einen Textbereich an, in dem der Benutzer die XML-Datei direkt bearbeiten kann.
