# Eltávolítás

## Eltávolítási ütemterv

Az ártalmatlanítási ütemtervekkel kapcsolatos további információkat a *Súgó* > *Használat* > *Eltávolítási irányelvek* menüpontban talál.

### 1. Konfigurálja a RODA-t úgy, hogy a visszatartást kiváltó elem azonosítójának mezőit megjelenítse.

A visszatartást kiváltó elem azonosítója a speciális keresőmező elemeinek használatával töltődik fel. Ezek közül a mezők közül a `date_interval` típusú mezők kerülnek kiválasztásra és felhasználásra a megőrzési időszak kiszámításához.
A *Súgó* > *Használat* > *Külső keresés* menüpontban talál további információkat az új, külső keresési mezőelemek hozzáadásáról.

## Eltávolítási szabály

Az ártalmatlanítási szabályokkal kapcsolatos további információkat a *Súgó* > *Használat* > *Eltávolítási irányelvek* menüpontban talál.

### 1. A RODA konfigurálása a mezők megjelenítésére a "metaadat mező" kiválasztási módszerben.

A metaadat mezőt a speciális keresési mező elemeinek használatával tölti ki. Ezek közül a mezők közül a `szöveg` típusúak kerülnek kiválasztásra. A RODA úgy is beállítható, hogy néhány ilyen mezőt figyelmen kívül hagyjon. Ehhez módosítsa a `roda-wui.properties` állományt úgy, hogy hozzáadjon egy új feketelista metaadatot. Alapértelmezés szerint a RODA minden `text` típusú leíró metaadatot megjelenít.

```javaproperties
ui.disposal.rule.blacklist.condition = description
```

A *Súgó* > *Használat* > *Különleges keresés* menüpontban talál további információkat az új, bővített keresési mezőelem hozzáadásáról.

A RODA leíró metaadatok konfigurálásával kapcsolatos további információkat a *Súgó* > *Konfiguráció* > *Metaadatformátumok* menüpontban talál.
