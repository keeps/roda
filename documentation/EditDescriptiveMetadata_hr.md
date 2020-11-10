# Uređivanje opisnih metapodataka

Opisne metapodatke možete uređivati izravno na stranici za pregledavanje Intelektualnih entiteta klikom na gumb ![Uredi](images/md_edit.png "Edit metadata").

Ako je opisna shema metapodataka podržana (prema zadanim postavkama ili u vašoj konfiguraciji), tada možete imati web obrazac za uređivanje metapodataka. Informacije poput naslova obično se nalaze u polju Naslov.

Također možete izravno urediti XML, klikom na ![Uredi kod](images/md_edit_code.png" Edit metadata XML") i promjenom neobrađenog XML-a.

Po završetku kliknite SPREMI.

## Opisna vrsta metapodataka

Morate definirati deskriptivnu vrstu metapodataka koja je definirala pravila o načinu provjere, indeksiranja, pregledavanja i uređivanja metapodataka. Opisne vrste metapodataka imaju naziv i verziju, na primjer Encoded Archival Description (EAD) verzija 2002, Dublin Core verzija 2002-12-12.

Možete dodati vlastite opisne vrste metapodataka i njihovu konfiguraciju za provjeru valjanosti, indeksiranje, pregled uređivanja s obrascem, za više informacija pogledajte [Formati metapodataka] (Metadata_Formats.md).

## Uredi upozorenja

**Datoteka s metapodacima koju generira obrazac ne slijedi točno strukturu izvorne datoteke. Može doći do gubitka podataka.**

Kada se ovo upozorenje pojavi pri uređivanju metapodataka, to znači da prilikom testiranja konfiguriranog obrasca ekstrapolacijom vrijednosti polja iz izvornog XML-a, ponovnim generiranjem XML-a pomoću predloška obrasca i usporedbom s izvornikom, nije bilo savršeno uklapanje. To može značiti da su podaci izgubljeni, dodani ili promijenili raspored (npr. redoslijed polja).

Ako želite osigurati da se izvorni XML promijeni prema Vašoj želji, možete ga izravno urediti (gornja uputa).

## Uredi validaciju

Pri spremanju proizvedeni XML provjerit će se prema XML shemi (ako je konfiguriran) ili barem provjeriti je li XML dobro oblikovan. Sintaksne pogreške pojavit će se na vrhu.

## Verziranje

Izdanja metapodataka imaju verzije, sve prethodne verzije možete pregledati klikom na ![Prethodne verzije](images/md_versions.png "Past versions of desc. metadata").

Prethodne verzije možete pregledati u padajućem izborniku koji sadrži informacije o tome tko je i kada izvršio promjenu. Također možete vratiti prošlu verziju klikom na VRATI, a ukloniti prošlu verziju klikom na UKLONI.

## Preuzimanje

Možete preuzeti deskriptivni metapodatak neobrađenog XML-a klikom na ![Preuzmi](images/md_download.png "Download desc. metadata")
