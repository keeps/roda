# Vodič za dokumentaciju

Sav statički tekst u RODI, koji uključuje `stranice pomoći`,` opis funkcionalnosti` i statične `html stranice`, nalazi se pod `[RODA_HOME]/example-config/theme/`.

Kako biste ažurirali postojeći sadržaj, trebali biste kopirati datoteku koju želite ažurirati iz `[RODA_HOME]/example-config/theme/` to `[RODA_HOME]/config/theme/` i urediti je u odredišnoj mapi.

## Dodavanje novih stranica za pomoć

Da biste dodali nove teme u izbornik pomoći, morate kopirati datoteku `[RODA_HOME]/example-config/theme/README.md` (i sve njezine datoteke za prijevod, npr. `README_pt_PT.md`) u `[RODA_HOME]/config/theme/documentation`.

Uredite novu datoteku `README.md` kako biste uključili vezu do nove teme za pomoć koja će se stvoriti:

```
- (Link text)[The_New_Topic_Page.md]
```

Nakon dodavanja novog unosa u Sadržaj, treba stvoriti novu datoteku [Markdown] (https://guides.github.com/features/mastering-markdown/) i staviti je u mapu `[RODA_HOME]/config/theme/documentation`. Naziv nove datoteke trebao bi odgovarati onom navedenom u Sadržaju (tj. `The_New_Topic_Page.md` u ovom primjeru).

## Uređivanje HTML stranica

Neke HTML stranice (ili dijelove stranica) moguće je prilagoditi promjenom odgovarajuće HTML stranice na `[RODA_HOME]/config/theme/some_specific_page.html`. 

Predlošci stranica postoje u `[RODA_HOME]/example-config/theme/`. Treba ih kopirati s izvornog mjesta u `[RODA_HOME]/config/theme/` kako je objašnjeno na početku članka.

Na primjer, stranicu sa statistikama možete prilagoditi promjenom datoteke `[RODA_HOME]/config/theme/Statistics.html`.
