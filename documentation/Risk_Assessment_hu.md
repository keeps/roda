# Kockázatértékelés

A RODA a [Digital Curation Centre (DCC)](http://www.dcc.ac.uk) és a DigitalPreservationEurope (DPE) által kifejlesztett  [Digital Repository Audit Method Based on Risk Assessment (DRAMBORA)](http://www.repositoryaudit.eu) eszközkészletből származó több mint 80 megőrzési kockázattal előre feltöltött kockázati nyilvántartással rendelkezik.

Tartalmaz továbbá egy kockázati nyilvántartást, amelyet a felhasználói felületről lehet kezelni, valamint számos kockázatértékelési bővítményt, amelyek frissítik a kockázati nyilvántartás információit.

## Hogyan értékeljük és mérsékeljük a megőrzési kockázatokat a RODA-ban?

Tehát el akarja kezdeni a kockázatértékelési folyamatokat a repozitóriumában. Például el akar indítani egy olyan folyamatot, amely a már nem fenntartható formátumokból származó fájlok konvertálására irányul (pl. mert új kockázat merül fel, hogy egy adott fájlformátum a jövőben nem lesz támogatott).

Alapvetően a következő hipotetikus forgatókönyvhöz szeretne egy munkafolyamatot:

1. Létrehozott egy SIP-et, amely egy Word 95 .doc fájlt tartalmaz.
1. Ön azonosított egy (hipotetikus) kockázatot a Word 95 .doc fájlokkal kapcsolatban (pl. az intézetünkben már egyetlen szoftver sem képes olvasni ezt a formátumot).
1. Mivel a kockázatot azonosították, szeretnék minden Word 95 .doc fájlt DOCX és PDF/A formátumba konvertálni.

Nos, számos módja van annak, hogyan kezelheti az új kockázatokat, és hogyan indíthat megőrzési akciót azok mérséklésére, ezért most csak arra koncentrálunk, hogyan oldanánk meg az Ön konkrét példáját:

Képzelje el, hogy én, mint megőrzési szakértő, tudom, hogy a Word 95 egy veszélyeztetett formátum. Elmennék a kockázati nyilvántartásba, és nyilvántartásba venném ezt a kockázatot, részletezve mindazt, amit az adott kockázatról tudok, és kijelölném a lehetséges intézkedéseket a kockázat csökkentésére (pl. áttelepítés egy új formátumba).

(Egy másik lehetőség egy olyan bővítmény használata lenne, amely automatikusan elvégezné ezt a fajta elemzést, azonban jelenleg nincs ilyen bővítmény. Ezt még ki kellene fejleszteni.)

Ezután a Keresés funkcióval megkeresheti az összes Word 95 fájlt az adattárban. Az összes fájlformátumot azonosítottuk a beolvasási folyamat során, így ez a feladat meglehetősen egyszerű. Ezután a rendelkezésre álló Kockázati társítás bővítményt használnám, hogy ezeket a fájlokat a nemrég létrehozott kockázat példányaiként állítsam be. Ez a megőrzési szakértő által hozott megőrzési döntések dokumentációjaként és a következő lépések indoklásaként szolgál - ez tulajdonképpen a megőrzés tervezése.

A következő lépés a fájlok áthelyezése lenne. Nagyjából ugyanazt teheti, mint korábban, azaz a Keresés menüben kijelölheti az összes word 95 fájlt, és futtathat egy megőrzési műveletet, hogy áthelyezze őket mondjuk PDF-be.

Ezután csökkentheti a kockázati szintet, mivel nincs több word 95 fájl a rendszerben. Az incidenseket " mérsékeltként" lehet megjelölni.

Amit most elmagyaráztam, az a kézi munkafolyamat, mivel jelenleg nem rendelkezünk egy formátum elavulásának kockázatát felismerő bővítménnyel. De ezt a bővítményt nagyon is lehetséges lenne kifejleszteni. Ebben az esetben a kockázatcsökkentési lépéseket közvetlenül a kockázatkezelési felületről lehetne elindítani.

Ami a rendelkezésre álló konverziós bővítményeket illeti, a RODA jelenleg a szokásos feltételezetteket támogatja (a főbb kép-, videó-, szöveg- és hangformátumokat). Minden intézményben mindig lesznek hiányos formátumok, és ebben az esetben speciális célú bővítményeket kell kifejleszteni.

## Van ötlete egy kockázatfelmérő bővítményhez?

Ha érdekli egy új kockázatfelmérő bővítmény kifejlesztése, kérjük, további információkért lépjen kapcsolatba a termékért felelős csapattal.
