# Fejlesztői útmutató

Ez egy gyors útmutató arról, hogyan kezdj el kódolni a RODA-n.

## A forráskód beszerzése

A forráskódot könnyen megkaphatod, ha a projektet klónozod a gépedre (csak a git-et kell telepítened):

```bash
$ git clone https://github.com/keeps/roda.git
```

Ha hozzá kívánsz járulni a RODA-hoz, akkor először a saját GitHub fiókodba kell elágaznod a tárolót, majd klónoznod kell a gépedre. Hogy hogyan kell ezt megtenni, azt ebben a [GitHub cikk](https://help.github.com/articles/fork-a-repo) olvashatod el.


<!-- FIGYELEM: a cím megváltoztatása megszakítja a linkeket -->
## Hogyan kell építeni és futtatni

A RODA az [Apache Maven](http://maven.apache.org/) építési rendszert használja. Mivel egy több modulos Maven projektről van szó, a gyökér **pom.xml**-ben a RODA összes moduljának minden fontos információja deklarálva van, mint például:

* Az alapértelmezett építési ciklusba beépítendő modulok
* Használandó Maven tárolók
* Függőségkezelés (a verziószámok itt kerülnek deklarálásra és öröklődnek az almodulokra)
* Plugin menedzsment (a verziószámok itt kerülnek deklarálásra és öröklődnek az almodulokra)
* Elérhető profilok (Rengeteg használható profil van. Az egyik csak a core projekteket tartalmazza (**core**), a másik a felhasználói interfész projekteket (**wui**), a másik a RODA wui docker image-et építi (**wui,roda-wui-docker**), és néhány más, amely például külső plugin projekteket is tartalmazhat, amelyek integrálhatók a RODA-ba (**all**)).0

### Függőségek

A RODA építésének előfeltételei a következők:

* Git kliens
* Apache Maven
* Oracle Java 8

Az összes függőség telepítéséhez a Debian alapú rendszereken hajtsa végre:

```bash
$ sudo add-apt-repository ppa:webupd8team/java
$ sudo apt-get update
$ sudo apt-get install oracle-java8-installer oracle-java8-set-default git maven ant
```

### Összeállítás

Az összeállításhoz lépjen a RODA forrásmappájába, és hajtsa végre a parancsot:

```bash
$ mvn clean package
```

A következő paranccsal kihagyhatja az egységteszteket (gyorsabban).

```bash
$ mvn clean package -Dmaven.test.skip=true
```


A sikeres fordítás után a RODA webes alkalmazás elérhető lesz a `roda-ui/roda-wui/target/roda-wui-VERSION.war` címen. A telepítéshez csak helyezze be a kedvenc servlet konténerébe (pl. Apache Tomcat) és kész.

## A fejlesztői környezet beállítása

### Szükséges szoftver

A RODA létrehozásához szükséges szoftveren kívül a következőkre van szükséged:

* Eclipse for Java ([Letöltési oldal](http://www.eclipse.org/downloads/))
* Eclipse Maven Plugin ([Letöltési és telepítési útmutató](http://www.eclipse.org/m2e/))

Opcionálisan telepítheti a következő eszközöket:

* A Google Plugin for Eclipse ([Letöltési és telepítési útmutató](https://developers.google.com/eclipse/docs/getting_started)) grafikus felhasználói felület fejlesztések fejlesztéséhez és teszteléséhez használható.

**MEGJEGYZÉS:** Ez nem egy korlátozó lista a RODA fejlesztéséhez használandó szoftverekről (mivel más szoftverek, például IDE-k is használhatók a javasoltak helyett).

### Hogyan importáljuk a kódot az Eclipse-ben

1. Eclipse indítása
2. Válassza a "Fájl > Importálás" lehetőséget. Ezután válassza a "Maven > Meglévő Maven projektek" lehetőséget, majd kattintson a "Tovább" gombra.
3. A "Gyökérkönyvtárban" keresse meg a RODA forráskód könyvtárát a fájlrendszerén, és válassza a "Megnyitás" lehetőséget.
4. Opcionálisan hozzáadhatja egy "munkakészlethez".
5. Kattintson a "Befejezés" gombra


## Kódszerkezet

A RODA a következőképpen épül fel:

### /

* **pom.xml** - a Maven projekt objektum modelljének gyökere
* **code-style** - checkstyle & Eclipse kódformázó fájlok
* **roda-common/** - ez a modul más modulok/projektek által használt közös komponenseket tartalmaz.
  * **roda-common-data** - ez a modul tartalmazza az összes többi modulban/projektben használt, RODA-val kapcsolatos modellobjektumot.
  * **roda-common-utils** - ez a modul tartalmazza az alap segédprogramokat, amelyeket más modulok/projektek használhatnak.

### /roda-core/

  * **roda-core** - ez a modul modell-, index- és tárolási szolgáltatásokat tartalmaz, különös tekintettel a következő csomagokra:
    * **common** - ez a csomag a roda-core-hoz kapcsolódó segédprogramokat tartalmazza.
    * **storage** - ez a csomag tartalmaz egy tárolási absztrakciót (OpenStack Swift ihlette) és néhány implementációt (ATM egy fájlrendszer és Fedora 4 alapú implementáció).
    * **model** - ez a csomag tartalmazza a RODA objektumok körüli logikát (pl. CRUD műveletek stb.), a RODA tárolási absztrakcióra építve.
    * **index** - ez a csomag tartalmazza az összes indexelési logikát a RODA modell objektumokhoz, a RODA modellel együttműködve az Observable mintán keresztül.
    * **migráció** - ez a csomag tartalmazza az összes migrációs logikát (pl. minden alkalommal, amikor egy modellobjektumban változás történik, migrációra lehet szükség).
  * **roda-core-tests** - ez a modul a roda-core modul tesztjeit és segédprogramjait tartalmazza. Ezen kívül ez a modul függőségként adható hozzá más projektekhez, amelyeknek például pluginjaik vannak, és azokat könnyebben akarják tesztelni.

### /roda-ui/

* **roda-wui**- ez a modul tartalmazza a webes felhasználói felület (WUI) webes alkalmazást és a REST webes szolgáltatásokat. Alapvetően a RODA-val való programozott interakciót lehetővé tevő komponensek.

### /roda-common/

* **roda-common-data** - ez a modul tartalmazza az összes többi modulban/projektben használt, RODA-val kapcsolatos modellobjektumot.
* **roda-common-utils** - ez a modul tartalmazza az alap segédprogramokat, amelyeket más modulok/projektek használhatnak.


## Hozzájárulás

### Forrás kód

1. [Fork the RODA GitHub project](https://help.github.com/articles/fork-a-repo)
2. Változtassa meg a kódot és juttassa be a leágazó projektbe.
3. [Pull-kérelem benyújtása](https://help.github.com/articles/using-pull-requests)

Annak érdekében, hogy növelje a kódjainak elfogadását és a RODA-forrásba való beolvasztását, itt egy ellenőrző lista azokról a dolgokról, amelyeket át kell néznie, mielőtt elküldi a hozzájárulását. Például:

* Rendelkezik egységtesztekkel (amelyek a kód legalább 80%-át lefedik)
* Dokumentációval rendelkezik (a nyilvános API legalább 80%-a)
* Elfogadja a hozzájárulói licencszerződést, amely igazolja, hogy minden hozzájáruló kód eredeti munka, és hogy a szerzői jogokat a projektre ruházza át.

### Fordítások

Ha le szeretné fordítani a RODA-t egy új nyelvre, kérjük, olvassa el a [Fordítási útmutatót](Translation_Guide.md).

### Külső bővítmények

Új bővítmények létrehozásához és RODA használatához a következőkre van szükség:

1. Hozzon létre egy új Maven projektet, amely függ a roda-core-tól, és deklarálja a plugin osztály minősített nevét a _pom.xml_ fájlban.
2. A plugin osztálynak ki kell bővítenie az **AbstractPlugin** osztályt, és implementálnia kell a szükséges metódusokat.
3. A plugin létrehozása után szükséges egy jar fájlt létrehozni
4. Ezt a jar fájlt a RODA alap telepítési mappájában kell elhelyezni, mégpedig a **config/plugins/PLUGIN_NAME/**

## REST API

A RODA teljes mértékben egy REST API-n keresztül vezérelhető. Ez kiválóan alkalmas külső szolgáltatások fejlesztésére vagy más alkalmazások integrálására az adattárral. Az API dokumentációja a [https://demo.roda-community.org/api-docs/](https://demo.roda-community.org/api-docs/) címen érhető el.

### 3rd party integrációk fejlesztése

Ha a REST API-n keresztül szeretne integrációt fejleszteni a RODA-val, kérjük, további információkért lépjen kapcsolatba a termékcsapattal, és hagyjon kérdést vagy hozzászólást a https://github.com/keeps/roda/issues oldalon.
