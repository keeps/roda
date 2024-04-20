# Bevitel előtti fázis

Az adatbevitel előtti folyamat azt a képességet mutatja, hogy a gyártó képes olyan adatbeviteli információs csomagokat (SIP) létrehozni, amelyek (jól meghatározott struktúrában) adatokat és metaadatokat egyaránt tartalmaznak, hogy azokat az adattárba bevihessék. A létrehozott SIP-eknek meg kell felelniük az adattár által meghatározott (vagy az adattárral egyeztetett) szabályzatoknak. 

A vizsgálatot megelőző folyamat általában a következő tevékenységek egy részét vagy mindegyikét magában foglalja:

## Megállapodás benyújtásáról

Ez a tevékenység a tartalomra vonatkozó feltételek, előfeltételek és követelmények, valamint a kísérő információk (pl. metaadatok, dokumentáció, szerződések stb.) meghatározásából áll, amelyeket a gyártónak kell elküldenie az adattárba. Ez a létrehozó és a tároló közötti írásbeli megállapodásban testesül meg, amely meghatározza a tartalom típusát és az összes olyan jogi és technikai követelményt, amelynek mindkét félnek meg kell felelnie.

## Besorolási terv

A benyújtási megállapodás aláírása során a gyártónak bele kell egyeznie egy alap besorolási rendszerbe (vagy gyűjtemények listájába), amelyre vonatkozóan kifejezett felhatalmazással rendelkezik az új információk letétbe helyezésére.

Az alap osztályozási sémát általában a tárhely hozza létre, és ebben a szakaszban letölthető gépi olvasható formátumban. A letöltött fájl betölthető a RODA-in programba a beviteli információs csomagok jobb elrendezése és előkészítése érdekében, mielőtt azokat az adattárba továbbítanák a bevitel céljából.

[Osztályozási séma letöltése](/api/v2/classification-plans) (megjegyzés: az osztályozási séma letöltéséhez RODA-példányra van szükség)

## Submission Information Packages (SIP)

Ez a tevékenység egy vagy több benyújtási információs csomag (SIP) elkészítését jelenti a benyújtási megállapodásban meghatározott műszaki és nem műszaki követelményeknek megfelelően. A SIP-ek elkészítésének megkönnyítése érdekében a gyártók igénybe vehetik a RODA-in eszközt. 

Az eszköz és dokumentációja a [http://rodain.roda-community.org](http://rodain.roda-community.org) címen érhető el.


## Anyagok átadása

Ez a tevékenység a benyújtási információs csomagoknak (SIP) a termelőtől a tárolóhoz történő átadásából áll. A SIP-eket ideiglenesen egy elkülönített területen tárolják, várva arra, hogy az adattár feldolgozza őket.

A gyártók többféle módon is átvihetik SIP-jeiket a tárolóba. Ezek közé tartoznak többek között a következő lehetőségek:

### HTTP-transzfer

1. Csatlakozzon az adattár webhelyéhez, és használja a megadott hitelesítő adatokat a bejelentkezéshez.
2. Lépjen be az Bevitel/Átvitel menübe, és adja meg a mappát a felhasználónevével (vagy hozza létre a mappát, ha szükséges).
3. Töltse fel az összes SIP-jét az új mappába.
4. Értesítse a tárolót, hogy az anyag készen áll a befogadásra.

### FTP-transzfer

1. Csatlakozzon a [ftp://address]-hoz, és használja az adattár által megadott hitelesítő adatokat a bejelentkezéshez.
2. Hozzon létre egy mappát azoknak a SIP-eknek a tárolására, amelyek egyetlen beviteli tétel részét képezik (nem kötelező).
3. Másolja az összes létrehozott SIP-et az új mappába.
4. Értesítse a tárolót, hogy az anyag készen áll a befogadásra.

### Külső adathordozó átvitele

1. SIP-ek mentése külső adathordozóra (pl. CD, USB lemez stb.)
2. A következő címre kell eljuttatni: [Adattár címe]

## Befogadási folyamat

Az átadást követően a SIP-eket a tároló munkatársai választják ki a bevitelre. A beviteli folyamat szolgáltatásokat és funkciókat biztosít a SIP-ek termelőktől történő átvételéhez, valamint a tartalmak archiválásra és kezelésre való előkészítéséhez.

A beviteli funkciók közé tartozik a SIP-ek fogadása, a SIP-ek minőségbiztosítása, az adattár adatformázási és dokumentációs szabványainak megfelelő archív információs csomag (AIP) létrehozása, az AIP-ekből a leíró információk kinyerése az adattár katalógusába való felvétel céljából, valamint az archív tárolás és az adatkezelés frissítésének koordinálása.

