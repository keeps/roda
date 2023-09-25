# Beschreibende Metadaten bearbeiten

Sie können beschreibende Metadaten direkt auf der Browse-Seite bearbeiten, indem Sie auf die Schaltfläche ![Bearbeiten](images/md_edit.png "Metadaten bearbeiten") klicken.

Wenn das Schema für beschreibende Metadaten unterstützt wird (standardmäßig oder in der Konfiguration), können Sie ein Webformular zur Bearbeitung der Metadaten verwenden. Informationen wie der Titel werden in der Regel in das Feld "Titel" eingetragen.

Sie können das XML auch direkt bearbeiten, indem Sie auf den ![Kode bearbeiten](images/md_edit_code.png "Metadaten XML bearbeiten") klicken und das XML ändern.

Wenn Sie fertig sind, klicken Sie auf SPEICHERN.

## beschreibendes Metadatentyp

Sie müssen den beschreibenden Metadatentyp definieren, der die Regeln dafür festlegt, wie Metadaten validiert, indiziert, angezeigt und bearbeitet werden. Deskriptive Metadatentypen haben einen Namen und eine Version, zum Beispiel "Encoded Archival Description (EAD) Version 2002, Dublin Core Version 2002-12-12".

Sie können Ihre eigenen beschreibenden Metadatentypen und deren Konfiguration hinzufügen, um ein Formular zu validieren, zu indizieren, anzuzeigen oder zu bearbeiten. Weitere Informationen finden Sie unter [Metadatenformate](Metadata_Formats.md).

## Warnungen bearbeiten

**Die vom Formular generierte Metadatendatei entspricht nicht der Struktur der Originaldatei. Es kann zu Datenverlusten kommen.**

Wenn diese Warnung bei der Bearbeitung von Metadaten erscheint, bedeutet dies, dass beim Testen des konfigurierten Formulars durch Extrapolation der Feldwerte aus der ursprünglichen XML-Datei, Neugenerierung der XML-Datei mit der Formularvorlage und Vergleich mit dem Original keine perfekte Übereinstimmung erzielt wurde. Dies kann bedeuten, dass Informationen verloren gegangen sind, hinzugefügt wurden oder die Anordnung geändert wurde (z.B. Reihenfolge der Felder).

Wenn Sie sicherstellen möchten, dass die ursprüngliche XML-Datei nach Ihren Wünschen geändert wird, können Sie die XML-Datei direkt bearbeiten (siehe Anleitung oben).

## Überprüfung bearbeiten

Beim Speichern wird die erzeugte XML-Datei anhand des XML-Schemas (sofern konfiguriert) überprüft überprüft, ob zumindest die XML-Datei korrekt geformt ist. Syntaxfehler werden oben angezeigt.

## Versionierung

Metadatenausgaben sind versioniert, Sie können alle früheren Versionen auflisten, indem Sie auf das Symbol ![Frühere Versionen](images/md_versions.png "Frühere Versionen der beschreibenden Metadaten") klicken.

Sie können die früheren Versionen in einem Dropdown-Menü durchsuchen, das Informationen darüber enthält, wer die Änderungen vorgenommen hat und wann. Sie können auch eine frühere Version wiederherstellen, indem Sie auf ZURÜCK klicken, und eine frühere Version entfernen, indem Sie auf ENTFERNEN klicken.

## Herunterladen

Sie können die beschreibenden Metadaten im XML-Format herunterladen, indem Sie auf ![Herunterladen](images/md_download.png "Herunterladen der beschreibenden Metadaten")
