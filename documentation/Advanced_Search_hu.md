# Összetett keresés

A keresőoldalon kereshet a következőkre: szellemi entitások, reprezentációk vagy fájlok (a lefelé mutató nyíllal válassza ki a keresési tartományt). E tartományok mindegyikénél kereshet az összes tulajdonságában vagy bizonyos tulajdonságokban (a lefelé mutató nyíl segítségével bővítheti a speciális keresést). Ha például a szellemi entitások lehetőséget választja, akkor a leíró metaadatok egy adott mezőjében kereshet, vagy a fájlok bővített keresés kiválasztása esetén egy bizonyos formátumú fájlokat kereshet.

A kereső csak egész szavakat keres. Ha részleges kifejezésekre szeretne keresni, akkor a '*' műveletet kell használnia.

## Kereső műveletek

A következő kereső műveletek állnak az Ön rendelkezésére:

- Pontos mondat (pl. "Miguel Ferreira")
- A kifejezések kezdőbetűi (pl. Miguel F*)
- Hagyja figyelmen kívül a karaktert (pl. Miguel Ferreir?)
- A kifejezés kizárása (pl. -Miguel Ferreira)
- Hasonló kifejezések (pl. Ferreir~)
- Számtartomány (pl. 1900..2000)
- Újraegyesítés (pl. Miguel OR Ferreira)

## Egyedi metaadatmezők keresése

Ehhez több lépés szükséges:

1. SIP-ek generálása az új leíró metaadattípussal és verzióval
2. A RODA konfigurálása az új leíró metaadat-formátum indexelésére
3. A RODA konfigurálása a mezők megjelenítésére az összetett keresés menüben

Választható:
* A RODA konfigurálása a metaadatok megjelenítésére
* RODA konfigurálása, hogy lehetővé tegye a metaadatok szerkesztését egy űrlap segítségével


### 1. Generálja a SIP-eket az új leíró metaadat típusával és verziójával.
A SIP-en meg kell határoznia a leíró metaadatok típusát és verzióját. Mivel sajátot használ, meg kell határoznia a metaadat típusát MÁS, más metaadat típusát pl. "GolikSwe" és a metaadat típus verzióját pl. "1". Ez megtehető közvetlenül a METS-ben, vagy a [RODA-in alkalmazás](http://rodain.roda-community.org/) vagy a [commons-ip könyvtár](https://github.com/keeps/commons-ip) segítségével.

### 2. RODA konfigurálása az új leíró metaadat-formátum indexelésére.
A RODA-nál be kell állítania, hogyan indexelheti ezt a fájlt. Ehhez meg kell határoznia az XSLT-t a `$RODA_HOME/config/crosswalks/ingest/` alatt egy olyan névvel, amelyet a metaadatok típusa és verziója alapján számítanak ki.

A metaadat típusa=OTHER, egyéb metaadat típusa="GolikSwe" és metaadat 1-es verziója esetén létre kell hoznia a `$$RODA_HOME/config/crosswalks/ingest/golikswe_1.xslt` fájlt.

Példákat a `$$RODA_HOME/example-config/crosswalks/dissemination/ingest/` vagy az [online változatban](https://github.com/keeps/roda/tree/master/roda-core/roda-core/src/main/resources/config/crosswalks/ingest) találsz.

Az eredményül kapott XML-nek hasonlónak kell lennie:
```xml
<doc>
  <field name="title">abcdefgh</field>
  <field name="origdesc_txt">abcdefgh</field>
  <field name="destructiondate_txt">2020-01-01</field>
  <field name="destructiondate_dd">2020-01-01T00:00:00Z</field>
</doc>
```
Szabályok:
- Van néhány fenntartott mezőnév, különösen a `cím`, `kezdődátum` és `záródátum`, amelyek meghatározzák, hogy mi jelenjen meg a listákon.
- Új specifikus mezőket adhat hozzá, de mindig hozzá kell adnia egy utótagot az adattípushoz. A leggyakrabban használt utótagok a következők: "\_txt" (bármilyen karakterlánc tokenizálva), "\_ss" (nem tokenizált karakterláncok az azonosítókhoz), "\_dd" az ISO1601 dátumokhoz.
- A fenntartott mezők nevének meghatározása [itt](https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/java/org/roda/core/index/schema/collections/AIPCollection.java#L61) történik, de a végleges név megismeréséhez szükség lehet a [itt](https://github.com/keeps/roda/blob/master/roda-common/roda-common-data/src/main/java/org/roda/core/data/common/RodaConstants.java#L604) elérésére is.
- Az utótagok és mezőtípusok teljes listája a [SOLR alapséma](https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/resources/config/index/common/conf/managed-schema) oldalon található.

A stílusjegyzékre vonatkozó módosítások alkalmazásához új tartalmat kell bevinnie vagy a meglévő tartalmat újraindexelnie.

### 3. A RODA konfigurálása a mezők megjelenítésére az összetett keresés menüben

Változtassa meg a `roda-wui.properties` állományt a következőre: [adjon hozzá egy új összetett keresés mezőt](https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/roda-wui.properties#L165):

```javaproperties
ui.search.fields.IndexedAIP = destructiondate # új mező hozzáadása az elemek (azaz az AIP-ek) mezőinek listájához, a többi lehetőség a reprezentációk vagy a fájlok.
ui.search.fields.IndexedAIP.destructiondate.fields = destructiondate_txt # a mező azonosítója az indexben, megegyezik a létrehozott stílustáblán szereplővel.
ui.search.fields.IndexedAIP.destructiondate.i18n = ui.search.fields.IndexedAIP.destructiondate # kulcs a fordításhoz a ServerMessages.properties fájlban.
ui.search.fields.IndexedAIP.destructiondate.type = text # a mező típusa, amely befolyásolja a keresési űrlap bevitelét.
ui.search.fields.IndexedAIP.destructiondate.fixed = true # ha alapértelmezés szerint megjelenik a speciális keresésben, vagy ha a "ADD SEARCH FIELD" gomb segítségével kell hozzáadni.
```
A szükséges fordításokat a `$$RODA_HOME/config/i18n/ServerMessages.properties` fájlhoz is hozzá kell adnod, mégpedig minden olyan nyelven, amelyet támogatni szeretnél.

Adja hozzá [az új metaadattípus és verzió fordítását](https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/i18n/ServerMessages.properties#L121):

```javaproperties
ui.browse.metadata.descriptive.type.golikswe_1=Golik SWE (1. verzió)
```

Add hozzá [fordítások a mezőkhöz](https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/i18n/ServerMessages.properties#L2):

```javaproperties
ui.search.fields.IndexedAIP.destructiondate= Megsemmisítés dátuma
```
