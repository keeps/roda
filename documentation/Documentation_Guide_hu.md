# Dokumentációs segédlet

A RODA összes statikus szövege, beleértve a `segédoldalakat`, a `funkcionalitás leírását` és a statikus `html oldalakat`, a `[RODA_HOME]/example-config/theme/` alatt található.

A meglévő tartalom frissítéséhez másolja a frissíteni kívánt fájlt a `[RODA_HOME]/example-config/theme/` mappából a `[RODA_HOME]/config/theme/` mappába, és szerkessze azt a célmappában.

## Új segédoldalak hozzáadása

Ahhoz, hogy új témákat adjon hozzá a súgó menühöz, a `[RODA_HOME]/example-config/theme/README.md` fájlt (és annak összes fordítási fájlját, pl. `README_pt_PT.md`) a `[RODA_HOME]/config/theme/documentation` fájlba kell másolnia.

Szerkessze az új `README.md` fájlt, hogy tartalmazzon egy linket a létrehozandó új súgó témára:

```
- (Link szöveg)[The_New_Topic_Page.md]
```

Miután hozzáadta az új bejegyzést a tartalomjegyzékhez, létre kell hozni egy új [Markdown](https://guides.github.com/features/mastering-markdown/) fájlt, amelyet a `[RODA_HOME]/config/theme/documentation` mappa alá kell helyezni. Az új fájl nevének meg kell egyeznie a tartalomjegyzékben megadottal (azaz ebben a példában `The_New_Topic_Page.md`).

## HTML oldalak szerkesztése

Néhány HTML oldal (vagy oldalrészlet) testreszabható a megfelelő HTML oldal módosításával a `[RODA_HOME]/config/theme/some_specific_page.html` címen. 

Az oldalsablonok a `[RODA_HOME]/example-config/theme/` alatt léteznek. Ezeket az eredeti helyükről át kell másolni a `[RODA_HOME]/config/theme/`-be, ahogyan azt a cikk elején elmagyaráztuk.

Például a statisztikai oldal testre szabható a `[RODA_HOME]/config/theme/Statistics.html` fájl módosításával.
