# Napredno pretraživanje

Na stranici za pretraživanje možete tražiti intelektualne entitete, predstavništva ili datoteke (strelicom prema dolje odaberite domenu za pretraživanje). Za svaku od ovih domena možete pretraživati u svim njezinim svojstvima ili u određenim svojstvima (upotrijebite strelicu prema dolje da proširite napredno pretraživanje). Na primjer, ako odaberete "Intelektualni entiteti", možete pretraživati u određenom polju opisnih metapodataka ili pronaći datoteke određenog formata ako je odabrano Napredno pretraživanje datoteka.

Pretraživanje locira samo cijele riječi. Ako želite tražiti djelomične pojmove, upotrijebite operator '*'.

## Operatori pretraživanja

Na raspolaganju su vam sljedeći operateri pretraživanja:

- Precizna rečenica (npr. "Miguel Ferreira")
- Pojmovi koji počinju s (npr. Miguel F *)
- Zanemariti simbol (npr. Miguel Ferreir?)
- Izuzeće pojma (npr. -Miguel Ferreira)
- Slični pojmovi (npr. Ferreir ~)
- Raspon brojeva (npr. 1900..2000)
- Pojam okupljanje (npr. Miguel OR Ferreira)

## Pretražite prilagođena polja metapodataka

To možete učiniti u nekoliko koraka:

1. Generirajte SIP-ove s novom opisnom vrstu i verziju metapodataka
2. Konfigurirajte RODU da indeksira vaš novi opisni format metapodataka
3. Konfigurirajte RODA tako da prikazuje polja u izborniku naprednog pretraživanja

Prema izboru:
* Konfigurirajte RODU za prikaz vaših metapodataka
* Konfigurirajte RODA tako da je omogućeno uređivanje vaših metapodataka pomoću obrasca


### 1. Generirajte SIP-ove s novim opisom vrstu i verziju metapodataka
Na SIP-u morate definirati opisnu vrstu i verziju metapodataka. Kako koristite svoju, trebali biste definirati vrstu metapodataka OSTALO, drugu vrstu metapodataka, npr. "GolikSwe" i verzija vrste metapodataka npr. "1". To se može učiniti izravno u METS-u ili pomoću [aplikacije RODA-in] (http://rodain.roda-community.org/) ili [commons-ip biblioteke] (https://github.com/keeps/commons-ip).

### 2. Konfigurirajte RODA za indeksiranje vašeg opisnog formata metapodataka 
Na RODI je potrebno konfigurirati kako se može indeksirati datoteka. Kako biste to učinili, morate definirati XSLT pod `$RODA_HOME/config/crosswalks/ingest/` s nazivom koji se izračunava prema vrsti metapodataka i verziji.

U primjeru s vrstom metapodataka=OSTALO, ostalim vrstama metapodataka="GolikSwe" a i verzijom metapodataka 1, morate stvoriti datoteku `$RODA_HOME/config/crosswalks/ingest/golikswe_1.xslt`. 

Primjere možete pronaći u `$RODA_HOME/example-config/crosswalks/dissemination/ingest/` or the [online version](https://github.com/keeps/roda/tree/master/roda-core/roda-core/src/main/resources/config/crosswalks/ingest).

Dobiveni XML mora biti otprilike:
```xml
<doc>
  <field name="title">abcdefgh</field>
  <field name="origdesc_txt">abcdefgh</field>
  <field name="destructiondate_txt">2020-01-01</field>
  <field name="destructiondate_dd">2020-01-01T00:00:00Z</field>
</doc>
```
Pravila:
- Postoje neki rezervirani nazivi polja, naročito `title`,`dateInitial` i `dateFinal`, koji definiraju što će se pojaviti na popisima
- Možete dodati nova specifična polja, ali uvijek morate dodati sufiks za vrstu podataka. Najčešće korišteni sufiksi su "\_txt" (bilo koji niz koji je označen), "\_ss" (ne-tokenizirani nizovi za identifikatore), "\_dd" za ISO1601 datume.
- Definicija naziva rezerviranih polja izrađena je [ovdje] (https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/java/org/roda/core/index/schema/collections/AIPCollection.java#L61) ali možda ćete morati pristupiti [ovdje](https://github.com/keeps/roda/blob/master/roda-common/roda-common-data/src/main/java/org/roda/core/data/common/RodaConstants.java#L604) kako bi saznali konačan naziv.
- Cjelovit popis sufiksa i vrsta polja dostupan je na [SOLR osnovnoj shemi] (https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/resources/config/index/common/conf/managed-schema).

Kako biste primijenili promjene na tablici stilova, morate unijeti novi sadržaj ili ponovno indeksirati postojeći sadržaj.

### 3. Konfigurirajte RODU tako da prikazuje polja u izborniku naprednog pretraživanja

Promijenite `roda-wui.properties` u [dodajte novu stavku polja za napredno pretraživanje] (https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/roda-wui.properties#L165):

```javaproperties
ui.search.fields.IndexedAIP = destructiondate # dodajte novo polje na popis polja za stavke (tj. AIP-ove), ostale opcije su prikazi ili datoteke
ui.search.fields.IndexedAIP.destructiondate.fields = destructiondate_txt # id polja u indeksu, jednak je onome na listi stilova koji kreirate
ui.search.fields.IndexedAIP.destructiondate.i18n = ui.search.fields.IndexedAIP.destructiondate # ključ za prijevod u ServerMessages.properties
ui.search.fields.IndexedAIP.destructiondate.type = text # vrsta polja koja utječe na unos obrasca za pretraživanje
ui.search.fields.IndexedAIP.destructiondate.fixed = true # ako se prema zadanim postavkama pojavi na naprednom pretraživanju ili ako ga treba dodati pomoću gumba "DODAJ POLJE PRETRAŽIVANJA".
```
Također, trebali biste dodat potrebne prijevode u`$RODA_HOME/config/i18n/ServerMessages.properties`, i na svim jezicima jezike koje treba podržati.

Dodajte [prijevod za vašu novu vrstu i verziju metapodataka](https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/i18n/ServerMessages.properties#L121):

```javaproperties
ui.browse.metadata.descriptive.type.golikswe_1=Golik SWE (verzija 1)
```

Dodajte [prijevode za vaša polja](https://github.com/keeps/roda/blob/master/roda-ui/roda-wui/src/main/resources/config/i18n/ServerMessages.properties#L2):

```javaproperties
ui.search.fields.IndexedAIP.destructiondate= Datum uništenja
```
