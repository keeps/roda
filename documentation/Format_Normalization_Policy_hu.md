*** MEGJEGYZÉS: Az ezen az oldalon található információk elavultak. További információkért kérjük, forduljon a projekt adminisztrátoraihoz.***

# Formátumnormalizálási szabályzat

A RODA bármilyen fájlformátumot támogat, de csak a fájlformátumok és objektumosztályok korlátozott körére vonatkozóan rendelkezik a formátumok megőrzési formátumokká való automatikus átalakítására (normalizálás) szolgáló eszközökkel.

Ezen az oldalon megtalálja a formátumok és objektumosztályok közötti alapértelmezett leképezést, valamint a normalizálási házirendre vonatkozó ajánlásokat tartalmazó táblázatot.

## A bevitt formátumok objektumosztályokba történő alapértelmezett leképezése

| *Osztály* | *Formátum* | *Identifikáció (MIME, PRONOM, kiterjesztések)* |
| --------- |---------- | ------------- |
| Szöveg | PDF | application/pdf<br>fmt/14, fmt/15, fmt/16, fmt/17, fmt/18, fmt/19, fmt/20, fmt/276, fmt/95, fmt/354, fmt/476, fmt/477, fmt/478, fmt/479, fmt/480, fmt/481, fmt/493, fmt/144, fmt/145, fmt/146, fmt/147, fmt/148, fmt/157, fmt/488, fmt/489, fmt/490, fmt/491<br>.pdf |
| Szöveg | Microsoft Word | application/msword<br>fmt/40, fmt/609, fmt/39, x-fmt/2, x-fmt/129, x-fmt/273, x-fmt/274, x-fmt/275, x-fmt/276, fmt/37, fmt/38<br>.doc |
| Szöveg | Microsoft Word Open XML dokumentum | application/vnd.openxmlformats-officedocument.wordprocessingml.document<br>fmt/412<br>.docx |
| Szöveg | Open Office szöveg | application/vnd.oasis.opendocument.text<br>x-fmt/3, fmt/136, fmt/290, fmt/291 <br>.odt |
| Szöveg | Rich Text Format | application/rtf<br>fmt/355, fmt/45, fmt/50, fmt/52 ,fmt/53 <br>.rtf |
| Szöveg | Egyszerű szöveg | text/plain<br>x-fmt/111 <br>.txt |
| Prezentáció | Microsoft Powerpoint | application/vnd.ms-powerpoint<br>x-fmt/88, fmt/125, fmt/126, fmt/181 <br>.ppt |
| Prezentáció | Microsoft Powerpoint Open XML dokumentum | application/vnd.openxmlformats-officedocument.presentationml.presentation<br/>fmt/215 <br>.pptx |
| Prezentáció | Open Office prezentáció | application/vnd.oasis.opendocument.presentation<br> fmt/138, fmt/292, fmt/293 <br>.odp |
| Táblázatkezelő | Microsoft Excel | application/vnd.ms-excel<br>fmt/55, fmt/56, fmt/57, fmt/59, fmt/61, fmt/62 <br>.xls |
| Táblázatkezelő | Microsoft Excel Open XML dokumentum | application/vnd.openxmlformats-officedocument.spreadsheetml<br>fmt/214 <br>.xlsx |
| Táblázatkezelő | Open Office táblázatkezelő | application/ vnd.oasis.opendocument.spreadsheet<br>fmt/137, fmt/294, fmt/295 <br>.ods |
| Kép | TIFF | image/tiff<br>fmt/152, x-fmt/399, x-fmt/388, x-fmt/387, fmt/155, fmt/353, fmt/154, fmt/153, fmt/156 <br>.tiff .tif |
| Kép | JPEG | image/jpeg<br>fmt/41, fmt/42, x-fmt/398, x-fmt/390, x-fmt/391, fmt/43, fmt/44, fmt/112 <br>.jpeg .jpg |
| Kép | PNG | image/png<br>fmt/11,fmt/12,fmt/13 <br>.png |
| Kép | BMP | image/bmp<br>x-fmt/270,fmt/115,fmt/118,fmt/119,fmt/114,fmt/116,fmt/117 <br>.bmp |
| Kép | GIF | image/gif<br>fmt/3, fmt/4 <br>.gif |
| Kép | ICO | image/ico<br>x-fmt/418 <br>.ico |
| Kép | XPM | image/xpm<br>x-fmt/208 <br>.xpm |
| Kép | TGA | image/tga<br>fmt/402,x-fmt/367 <br>.tga |
| Adatbázis<sup>1</sup> | DBML | application/dbml <br>.xml |
| Adatbázis<sup>1</sup> | DBML+FILES | application/dbml+octet-stream <br>.xml .bin |
| Hang | Hullám | audio/wav <br>.wav |
| Hang | MP3 | audio/mpeg <br>.mp3 |
| Hang | MP4 | audio/mp4 <br>.mp4 |
| Hang | Flac | audio/flac <br>.flac |
| Hang | AIFF | audio/aiff <br>.aif .aiff |
| Hang | Ogg Vorbis | audio/ogg <br>.ogg |
| Hang | Windows Media Audio | audio/x-ms-wma <br>.wma |
| Videó | Mpeg 1 | video/mpeg <br>.mpg .mpeg |
| Videó | Mpeg 2 | video/mpeg2 <br>.vob .mpv2 mp2v |
| Videó | Mpeg 4 | video/mp4 <br>.mp4 |
| Videó | Audio Video Interlave | video/avi <br>.avi |
| Videó | Windows Media Video | video/x-ms-wmv <br>.wmv |
| Videó | Quicktime | video/quicktime <br>.mov .qt |
| Vector Graphics | Adobe Illustrator | application/illustrator<br>x-fmt/20, fmt/419, fmt/420, fmt/422, fmt/423, fmt/557, fmt/558, fmt/559, fmt/560, fmt/561, fmt/562, fmt/563, fmt/564, fmt/565 <br>.ai |
| Vector Graphics | CorelDraw | application/coreldraw<br>fmt/467, fmt/466, fmt/465, fmt/464, fmt/427, fmt/428, fmt/429, fmt/430, x-fmt/291, x-fmt/292, x-fmt/374, x-fmt/375, x-fmt/378, x-fmt/379, x-fmt/29 <br> .cdr |
| Vector Graphics | Autocad kép | image/vnd.dwg<br>fmt/30, fmt/31, fmt/32, fmt/33, fmt/34, fmt/35, fmt/36, fmt/434, x-fmt/455, fmt/21, fmt/22, fmt/23, fmt/24, fmt/25, fmt/26, fmt/27, fmt/28, fmt/29, fmt/531 <br>.dwg |  
| E-mail | EML | message/rfc822<br>fmt/278 <br>.eml |
| Email | Microsoft Outlook email | application/vnd.ms-outlook<br>x-fmt/430, x-fmt/431 <br>.msg |

## Ajánlott megőrzési formátumok az egyes objektumosztályokhoz

| *Osztály* | *Formátum* | *MIME típus* | *Kiterjesztések* | *Leírás* |
|---------|----------|-------------|--------------|---------------|
| Szöveg    | PDF/A | application/pdf or text/plain<sup>2</sup> | .pdf | PDF archiváláshoz|
| Bemutató | PDF/A | application/pdf | .pdf | PDF archiváláshoz|
| Táblázatkezelő | PDF/A | application/pdf | .pdf | PDF archiváláshoz|
| Kép | METS+TIFF | image/mets+tiff | .xml .tiff | METS XML fájl TIFF struktúrával és tömörítetlen képekkel |
| Hang | Hullám | audio/wav | .wav | Wave hangformátum |
| Videó | MPEG-2 | video/mpeg2 | .mpeg .mpg |MPEG 2 videoformátum, DVD belső struktúrával |
| Adatbázis | SIARD 2 |  | .siard | Nyílt formátum a relációs adatbázisok archiválásához |
