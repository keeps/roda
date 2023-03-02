# Gallring

## Gallringsschema

Se *Hjälp* > *Användning* > *Gallringspolicys* för mer information om gallringsscheman.

### 1. Konfigurera RODA för att fält ska överensstämma

För att identifierade element ska överensstämma populeras de genom att använda avancerad sökning. De fält som har `datum_intervall` som typ, kommer väljas och användas som räknare i perioden. 
Gå till *Hjälp* > *Användning* > *Avancerad sök* för mer information om hur man lägger till nya fält. 

## Gallringsregel

Se *Hjälp* > *Användning* > *Gallringspolicys* för mer information om gallringsregler.

### 1. Konfigurera RODA för att visa fält i urvalsmetoden 'metadatafält'

Metadatafältet finns under det avancerade sökfältet. Här kan fält som är av `text`-typ väljas. RODA kan konfigureras för att utesluta vissa av dessa fält, vilket görs genom att lägga till en ny lista för icke-godkända metadata i din `roda-wui.properties`. Som standard visar RODA all metadata av typen `text`.

```javaproperties
ui.disposal.rule.blacklist.condition = description
```

Se *Hjälp* > *Användning* > *Avancerad sökning* för mer information om hur du lägger till ett nytt avancerat sökfält.

Se *Hjälp* > *Konfiguration* > *Metadataformat* för mer information om beskrivande metadatakonfiguration i RODA.
