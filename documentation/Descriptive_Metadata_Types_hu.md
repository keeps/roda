# Leíró metaadattípusok

Új entitás létrehozásakor az egyik lépés a leíró metaadatok "típusának" kiválasztása.

Ez a használt leíró metaadatsémára utal, és alapértelmezés szerint a RODA a következő opciókat támogatja:

* **[EAD 2002](https://www.loc.gov/ead/)**: Az EAD (Encoded Archival Description) 2002-es verziója egy XML-szabvány az archiválási segédletek kódolására, amelyet az Amerikai Levéltárosok Társasága kódolt archiválási szabványok technikai albizottsága tart fenn a Kongresszusi Könyvtárral együttműködve. Elsősorban levéltárak használják mind a digitálisan keletkezett, mind az analóg dokumentumok leírására.
* **[Dublin Core](https://www.dublincore.org/schemas/xmls/)**: A Dublin Core (DC) metaadat-kezdeményezés támogatja a metaadat-tervezés és a legjobb gyakorlatok innovációját. A jelenleg ajánlott sémák közé tartozik az *Simple DC XML séma, 2002-12-12 verzió*, amely az egyszerű Dublin Core, azaz a http://purl.org/dc/elements/1.1/ névtér 15 elemének kifejezéseit határozza meg, kódolási sémák vagy elemfinomítások használata nélkül.
* **[Kulcs-érték](https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/resources/config/schemas/key-value.xsd)**: A RODA belső egyszerű leírási séma a kulcs-érték metaadatok meghatározásához, ahol a metaadat kulcs az elemet azonosítja (pl. "cím"), az érték pedig a metaadatelem tartalmát.
*  **Más**: Általános XML típus, ahol nincs séma definiálva.

Új metaadattípusok adhatók hozzá a RODA-hoz a dokumentáció [Metaadatformátumok] (Metadata_Formats.md) szerint.

| Leíró metaadattípus | Hitelesítés           | Indexelés         | Vizualizáció         | Kiadvány      |
|---------------------------|----------------------|------------------|-----------------------|--------------|
| EAD 2002                  | Séma validálás    | Indexelési szabályok   | Vizualizációs szabályok   | Kiadási forma |
| Dublin Core               | Séma validálás    | Indexelési szabályok   | Vizualizációs szabályok   | Kiadási forma |
| Kulcsérték                 | Séma validálás    | Indexelési szabályok   | Vizualizációs szabályok   | Kiadási forma |
| Egyéb                     | Jólformáltsági vizsgálat | Általános indexelés | Általános vizualizáció | XML szerkesztés     |

Legenda:
* **Séma érvényesítés**: Az adattár XML-sémát kínál a megadott metaadatfájl szerkezetének és adattípusainak érvényesítéséhez. A validációs séma a beolvasási folyamat során kerül felhasználásra annak ellenőrzésére, hogy a SIP-ben szereplő metaadatok érvényesek-e a meghatározott korlátozásoknak megfelelően, valamint a metaadatok katalóguson keresztül történő szerkesztésekor.
* **Jólformáltsági vizsgálat**: A tároló csak azt ellenőrzi, hogy a metaadat XML fájl jól formázott-e. Mivel nincs séma definiálva, a tároló nem ellenőrzi, hogy a fájl érvényes-e.
* **Indexelési szabályok**: Az adattár alapértelmezett XSLT-t biztosít, amely az XML-alapú metaadatokat olyanná alakítja, amelyet az indexelő motor képes megérteni. A leíró metaadatokon keresztüli fejlett keresés lehetővé tétele.
* **Általános indexelés**: Mivel azonban az adattár nem ismeri a megfelelő leképezést az XML-elemek és a belső adatmodell között, csak alapvető keresés lehetséges a megadott metaadatokon.
* **Vizualizációs szabályok**: Az adattár biztosít egy alapértelmezett XSLT-t, amely az XML-alapú metaadatokat egy HTML-fájllá alakítja át, amely a felhasználó számára megjelenik, amikor a katalógusban egy meglévő AIP-et böngészik.
* **Általános vizualizáció**: Az adattár egy általános metaadat-megjelenítő eszközt biztosít az XML-alapú metaadatok megjelenítéséhez. Az összes szöveges elem és attribútum különösebb sorrend nélkül jelenik meg, és az XPath-jukat használja címkeként.
* **Kiadási forma**: Az adattár egy konfigurációs fájlt biztosít, amely megadja, hogyan jelenítsünk meg egy űrlapot a meglévő metaadatok szerkesztéséhez.
* **XML szerkesztés**: Az adattár megjelenít egy szöveges területet, ahol a felhasználó közvetlenül szerkesztheti az XML-t.
