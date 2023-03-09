# Redigera beskrivande metadata

Du kan redigera beskrivande metadata direkt på arkivobjektets visningssida genom att klicka på knappen ![Edit](images/md_edit.png "Redigera metadata")

Om det beskrivande metadataschemat stöds (som standard eller i konfigurationen), kan man ha ett webbformulär för att redigera metadata. Information, exempelvis titeln, kommer vanligtvis att finnas i titelfältet.

Du kan också redigera direkt i XML:en genom att klicka på ![Edit code](images/md_edit_code.png "Redigera XML metadata") och ändra den råa XML:en.

Klicka på SPARA när du är klar.

## Beskrivande metadatatyp

Man måste definiera den beskrivande metadatatypen, som definierade reglerna för hur metadata valideras, indexeras, visas och redigeras. Beskrivande metadatatyper har ett namn och en version, till exempel Encoded Archival Description (EAD) version 2002, Dublin Core version 2002-12-12.

Du kan lägga till dina egna beskrivande metadatatyper och deras konfiguration för att validera, indexera, visa en redigering med hjälp av ett formulär. För mer information se [Metadataformats](Metadata_Formats.md).

## Redigera varningar

**Metadatafilen som genereras av formuläret följer inte exakt strukturen i originalfilen. Viss dataförlust kan inträffa.**

När den här varningen visas vid redigering av metadata betyder det att när man testar det konfigurerade formuläret genom att extrapolera fältvärdena från den ursprungliga XML-filen, generera om XML:en med formulärmallen och jämföra den med originalet så stämde dessa inte helt överens. Detta kan innebära att information går förlorad, läggs till eller har ändrat arrangemang (t.ex. ordning på fält).

Om du vill säkerställa att den ursprungliga XML-filen har ändrats, enligt dina önskemål, kan du redigera XML-filen direkt (se instruktionen ovan).

## Redigera validering

När den producerade XML:en sparas kommer den att kontrolleras mot XML-schemat (om detta är konfigurerat) eller åtminstone kontrolleras om XML:en är rätt utformad. Syntaxfel kommer att visas överst.

## Versionshantering

Metadataredigeringar versionshanteras, du kan se alla tidigare versioner genom att klicka på ![Past versions](images/md_versions.png "Tidigare versioner av beskr. metadata").

Du kan se de tidigare versionerna i en rullgardinsmeny som har information om vem som gjorde ändringen och när. Du kan också återställa en tidigare version genom att klicka på ÅTERSTÄLL och ta bort en tidigare version genom att klicka på TA BORT.

## Laddar ned

Du kan ladda ner den råa XML:en för den beskrivande metadatan genom att klicka på ![Download](images/md_download.png "Ladda ned beskr. metadata")
