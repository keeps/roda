# Gyakran ismételt kérdések

A RODA felhasználók által gyakran feltett kérdések és az azokra adott válaszok.

Van olyan égető kérdése, ami nincs itt? Csak [hozz létre egy problémát](https://github.com/keeps/roda/issues/new) a GitHubon, és jelöld meg a "kérdés" címkével.

## Nézők

### Megnézhetünk fájlokat közvetlenül a RODA webes felületén?

A rendszer néhány előre definiált megjelenítővel rendelkezik bizonyos szabványos formátumokhoz (pl. PDF, képek, HTML 5 multimédiaformátumok, stb.).

A speciális formátumoknak speciális megjelenítőkre vagy konvertálóprogramokra van szükségük, hogy a meglévő megjelenítőkhöz, pl. a SIARD 2 megjelenítőhöz igazítsák őket. Ezeket a fejlesztéseket esetről esetre kell végrehajtani. 

## Metaadat

### Milyen leíró metaadat-formátumokat támogat a RODA?

Minden leíró metaadat-formátum támogatott, amennyiben van egy XML-séma (XSD) nyelvtan az érvényesítéshez. A RODA alapértelmezés szerint a Dublin Core és a 2002-es kódolt archívumleírással van konfigurálva. További sémák is hozzáadhatók.

### Támogathat-e a RODA több osztályozási sémát?

A rendszer lehetővé teszi több hierarchikus struktúra meghatározását, ahol a rekordok elhelyezhetők. E struktúrák minden egyes csomópontjához leíró metaadatokat rendelhetünk. Képzeljük el ezt egy fájl/mappa rendszerként, ahol minden mappához egyéni metaadatok tartozhatnak EAD vagy DC formátumban (vagy bármilyen más formátumban). Minden ilyen "mappa" (vagy helyőrző) lehet állomány, gyűjtemény, sorozat, sorozat, gyűjteményegyüttes stb.

### Lehetővé teszi-e a rendszer a metaadatok átvételét a struktúra magasabb szintjeiről?

Jelenleg nem. Szükséges volna egy bővítmény kifejlesztése. 

### Összekapcsolható-e a leírási egység egy vagy több, más archívumban vagy rendszerben lévő fájllal?

A leírások egységei az AIP (Archival Information Package) részét képezik, ami azt jelenti, hogy az ábrázolások és a fájlok általában szorosan kapcsolódnak a rekord metaadataihoz. Lehetőség van azonban arra, hogy a leíró metaadatokban elhelyezett HTTP-linkeket adjunk hozzá más, az adattáron kívül található forrásokhoz.

### Lehetséges-e összekapcsolni egy levéltári leírást egy kontextuális entitással (pl. ISAAR authority)?

A rendszer belsőleg nem támogatja a hatósági nyilvántartásokat, azonban ha külsőleg kezeli ezeket a rekordokat, akkor a leíró metaadatok szerkesztésével linkelheti őket.

### Hogyan lehet támogatni a hibrid (papíralapú és digitális) archívumokat?

Lehetséges, hogy a rekordok csak metaadatokkal rendelkezzenek, digitális ábrázolás nélkül. A katalógusok szempontjából ez általában elegendő a papíralapú archívumok támogatásához.

### Tudja-e az alkalmazás rögzíteni az átutalás szintjét, pl. ki, mit és mikor utalt át?

A SIP-ek általában tartalmaznak információkat arról, hogy ki, mit és mikor hozott létre. Az ingest folyamat a teljes ingest folyamatról rekordokat hoz létre. A SIP-eket azonban várhatóan olyan hálózati helyre kell helyezni, amely a rendszer számára elérhető. Annak meghatározása, hogy ki másolta a SIP-eket ezekre a helyekre, nem tartozik a rendszer hatáskörébe.

### How can the system record the location of physical archives?

Ez egy metaadatmező kitöltésével kezelhető. Tipikusan <ead:physloc>.

## Keresés

### Milyen metaadat-attribútumok alapján kereshetünk?

A keresőoldal teljesen konfigurálható a konfigurációs fájlon keresztül. Beállíthatja az attribútumokat, típusokat, címkék nevét stb.

### Támogatott a teljes szöveges keresés?

Igen, eleve támogatott a részletes keresés által.

### A felhasználó kérhet-e analóg dokumentumokat az archívumból a keresési eredményből?

Nem. Egy külső rendszerrel kellene integrálni, amely ezeket a kéréseket kezeli.

### A keresési eredmények listája tükrözi a megjelenített rekordokra vonatkozó engedélyeket?

Igen. Csak azokat a rekordokat láthatja, amelyekhez hozzáférése van.

### Az ellenőrzési nyomvonal kereshető és felhasználóbarát módon hozzáférhető?

Igen. A webes felhasználói felületről közvetlenül navigálhat a műveleti naplóban (az adattárban végrehajtott műveletek teljes halmaza) vagy a megőrzési metaadatokban (az adatokon végrehajtott megőrzési műveletek listája).

## Megőrzés

### Írja le a karantén környezet működését.

A SIP-ek feldolgozása közben a bevitel során, ha nem sikerül elfogadni őket, a fájlrendszer egy speciális mappájába kerülnek. Az adatbeviteli folyamat részletes jelentést készít, amely leírja az elutasítás okait. Ettől kezdve kézi vezérléssel kell eljárni.

### Hogyan támogatja a rendszer a megőrzést?

Ez egy összetett kérdés, amelyre nem lehet néhány sornyi szövegben válaszolni. Mindazonáltal elmondhatjuk, hogy a rendszer többféleképpen kezeli a megőrzést:

- Léteznek olyan műveletek, amelyek rendszeresen ellenőrzik a beolvasott fájlok rögzítettségét, és figyelmeztetik az adattárkezelőket, ha bármilyen problémát észlelnek.
- A rendszer beágyazott kockázatkezelési GUI-val (azaz kockázati nyilvántartással) rendelkezik.
- Léteznek olyan műveletek, amelyek a fájlokkal kapcsolatos kockázatokat észlelik, és új veszélyeket adnak hozzá a kockázati nyilvántartáshoz, amelyeket manuálisan kell kezelni (pl. egy rekord nincs megfelelően leírva, egy fájl nem követi az adattár formátumpolitikáját, egy fájlformátum ismeretlen vagy nincs reprezentációs információ stb.)
- Léteznek olyan intézkedések, amelyek lehetővé teszik a megőrzésért felelősök számára a kockázatok mérséklését, pl. fájlformátum-konverziók elvégzése (több tíz formátum támogatott).

### Hogyan támogatja az alkalmazás az értékelést, a megőrzési időszakok meghatározását?

A RODA komplex munkafolyamatot biztosít a nyilvántartások megsemmisítéséhez. További információkért kérjük, olvassa el a [Disposal](Disposal.md) című dokumentumot.

### Is the system logging search interactions?

Igen. A rendszerben minden művelet naplózásra kerül.

## Követelmények

### Van-e valamilyen rendszerkövetelmény az ügyfél oldalán azok számára, akik az archívumokat látogatják?

Nem igazán. Egy modern böngésző elegendő.

## Hogyan

### Hogyan lehet új nyelvet hozzáadni a rendszerhez?

Az új nyelvek rendszerbe való felvételére vonatkozó teljes körű utasítások a következő címen érhetők el: [Fordítási útmutató](Translation_Guide.md).

### Hogyan állítsuk be a fejlesztési környezetet a RODA számára?

A fejlesztőkörnyezet beállítására vonatkozó teljes körű utasítások a következő címen érhetők el: [Fejlesztői útmutató](Developers_Guide.md).
