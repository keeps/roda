***HINWEIS: Die Informationen auf dieser Seite sind nicht mehr aktuell. Für weitere Informationen wenden Sie sich bitte an die Projektadministratoren.***

# Formatnormalisierungsrichtlinie

RODA unterstützt jedes Dateiformat, verfügt aber nur über Werkzeuge zur automatischen Konvertierung von Formaten in Erhaltungsformate (Normalisierung) für eine begrenzte Anzahl von Dateiformaten und Objektklassen.

Auf dieser Seite finden Sie die Standardzuordnung zwischen Formaten und Objektklassen, sowie eine Tabelle mit Empfehlungen für eine Normalisierungsrichtlinie.

## Standardzuordnung von übernommenen Formaten zu Objektklassen

| *Klasse* | *Format* | *Identifizierung (MIME, PRONOM, Erweiterungen)* |
| --------- |---------- | ------------- |
| Text | PDF | application/pdf<br>fmt/14, fmt/15, fmt/16, fmt/17, fmt/18, fmt/19, fmt/20, fmt/276, fmt/95, fmt/354, fmt/476, fmt/477, fmt/478, fmt/479, fmt/480, fmt/481, fmt/493, fmt/144, fmt/145, fmt/146, fmt/147, fmt/148, fmt/157, fmt/488, fmt/489, fmt/490, fmt/491<br>.pdf |
| Text | Microsoft Word | application/msword<br>fmt/40, fmt/609, fmt/39, x-fmt/2, x-fmt/129, x-fmt/273, x-fmt/274, x-fmt/275, x-fmt/276, fmt/37, fmt/38<br>.doc |
| Text | Microsoft Word Open XML-Dokument | application/vnd.openxmlformats-officedocument.wordprocessingml.document<br>fmt/412<br>.docx |
| Text | Open Office Text | application/vnd.oasis.opendocument.text<br>x-fmt/3, fmt/136, fmt/290, fmt/291<br>.odt |
| Text | Rich-Text-Format | application/rtf<br>fmt/355, fmt/45, fmt/50, fmt/52 ,fmt/53<br>.rtf |
| Text | Klartext | text/plain<br>x-fmt/111<br>.txt |
| Gestaltung/Darstellung | Microsoft Powerpoint | application/vnd.ms-powerpoint<br>x-fmt/88, fmt/125, fmt/126, fmt/181<br>.ppt |
| Gestaltung/Darstellung | Microsoft Powerpoint Open XML-Dokument | application/vnd.openxmlformats-officedocument.presentationml.presentation<br/>fmt/215<br>.pptx |
| Gestaltung/Darstellung | Open Office Präsentation | application/vnd.oasis.opendocument.presentation<br>fmt/138, fmt/292, fmt/293<br>.odp |
| Arbeitsblatt | Microsoft Excel | application/vnd.ms-excel<br>fmt/55, fmt/56, fmt/57, fmt/59, fmt/61, fmt/62<br>.xls |
| Arbeitsblatt | Microsoft Excel Open XML-Dokument | application/vnd.openxmlformats-officedocument.spreadsheetml<br>fmt/214<br>.xlsx |
| Arbeitsblatt | Open Office Arbeitsblatt | application/ vnd.oasis.opendocument.spreadsheet<br>fmt/137, fmt/294, fmt/295<br>.ods |
| Abbildung | TIFF | image/tiff<br>fmt/152, x-fmt/399, x-fmt/388, x-fmt/387, fmt/155, fmt/353, fmt/154, fmt/153, fmt/156<br>.tiff .tif |
| Abbildung | JPEG | image/jpeg<br>fmt/41, fmt/42, x-fmt/398, x-fmt/390, x-fmt/391, fmt/43, fmt/44, fmt/112<br>.jpeg .jpg |
| Abbildung | PNG | image/png<br>fmt/11,fmt/12,fmt/13<br>.png |
| Abbildung | BMP | image/bmp<br>x-fmt/270,fmt/115,fmt/118,fmt/119,fmt/114,fmt/116,fmt/117<br>.bmp |
| Abbildung | GIF | image/gif<br>fmt/3, fmt/4<br>.gif |
| Abbildung | ICO | image/ico<br>x-fmt/418<br>.ico |
| Abbildung | XPM | image/xpm<br>x-fmt/208<br>.xpm |
| Abbildung | TGA | image/tga<br>fmt/402,x-fmt/367<br>.tga |
| Database<sup>1</sup> | DBML | application/dbml<br>.xml |
| Database<sup>1</sup> | DBML+FILES | application/dbml+octet-stream<br>.xml .bin |
| Audio | Wave | audio/wav<br>.wav |
| Audio | MP3 | audio/mpeg<br>.mp3 |
| Audio | MP4 | audio/mp4<br>.mp4 |
| Audio | Flac | audio/flac<br>.flac |
| Audio | AIFF | audio/aiff<br>.aif .aiff |
| Audio | Ogg Vorbis | audio/ogg<br>.ogg |
| Audio | Windows Media Audio | audio/x-ms-wma<br>.wma |
| Video | Mpeg 1 | video/mpeg<br>.mpg .mpeg |
| Video | Mpeg 2 | video/mpeg2<br>.vob .mpv2 mp2v |
| Video | Mpeg 4 | video/mp4<br>.mp4 |
| Video | Audio Video Interleave | video/avi<br>.avi |
| Video | Windows Media Video | video/x-ms-wmv<br>.wmv |
| Video | Quicktime | video/quicktime<br>.mov .qt |
| Vector Graphics | Adobe Illustrator | application/illustrator<br>x-fmt/20, fmt/419, fmt/420, fmt/422, fmt/423, fmt/557, fmt/558, fmt/559, fmt/560, fmt/561, fmt/562, fmt/563, fmt/564, fmt/565<br>.ai |
| Vector Graphics | CorelDraw | application/coreldraw<br>fmt/467, fmt/466, fmt/465, fmt/464, fmt/427, fmt/428, fmt/429, fmt/430, x-fmt/291, x-fmt/292, x-fmt/374, x-fmt/375, x-fmt/378, x-fmt/379, x-fmt/29<br>.cdr |
| Vector Graphics | Autocad image | image/vnd.dwg<br>fmt/30, fmt/31, fmt/32, fmt/33, fmt/34, fmt/35, fmt/36, fmt/434, x-fmt/455, fmt/21, fmt/22, fmt/23, fmt/24, fmt/25, fmt/26, fmt/27, fmt/28, fmt/29, fmt/531<br>.dwg |  
| Email | EML | message/rfc822<br>fmt/278<br>.eml |
| Email | Microsoft Outlook email | application/vnd.ms-outlook<br>x-fmt/430, x-fmt/431<br>.msg |

## Empfohlene Bewahrungsformate für jede Objektklasse

| *Klasse* | *Format* | *MIME Type* | *Erweiterung* | *Beschreibung* |
|---------|----------|-------------|--------------|---------------|
| Text    | PDF/A | application/pdf or text/plain<sup>2</sup> | .pdf | PDF für Archivierung|
| Gestaltung/Darstellung | PDF/A | application/pdf | .pdf | PDF für Archivierung|
| Arbeitsblatt | PDF/A | application/pdf | .pdf | PDF für Archivierung|
| Abbildung | METS+TIFF | image/mets+tiff | .xml .tiff | METS-XML-Datei mit der Struktur und den unkomprimierten TIFF-Bildern |
| Audio | Wave | audio/wav | .wav | WAVE-Dateiformat |
| Video | MPEG-2 | video/mpeg2 | .mpeg .mpg |MPEG 2-Videoformat, mit interner DVD-Struktur |
| Datenbank | SIARD 2 |  | .siard | Offenes Format für die Archivierung relationaler Datenbanken |
