# Typer av beskrivande metadata

När du skapar en ny intellektuell enhet är ett av stegen att välja "typ" av beskrivande metadata.

Detta hänvisar till det beskrivande metadataschemat som kommer att användas, och som standard stöder RODA följande alternativ:

* **[EAD 2002](https://www.loc.gov/ead/)**: Encoded Archival Description (EAD) version 2002 är en XML-standard för kodning av arkivinformation, som underhålls av Technical Subcommittee on Encoded Archival Standards (TS-EAS), i samarbete med Library of Congress. Det används främst av arkiv för att beskriva både digitalt födda och analoga dokument.
* **[Dublin Core](https://www.dublincore.org/schemas/xmls/)**: Dublin Core (DC) Metadata Initiative stödjer innovation inom metadatadesign och bästa praxis. För närvarande rekommenderade scheman inkluderar *Simple DC XML-schemat, version 2002-12-12*, som definierar termer för Simple Dublin Core, dvs. de 15 elementen från http://purl.org/dc/elements/1.1/ namnrymden, utan behov av användning av kodningsscheman eller elementförfiningar.
* **[Key-value](https://github.com/keeps/roda/blob/master/roda-core/roda-core/src/main/resources/config/schemas/key-value.xsd)** : Ett internt enkelt beskrivningsschema för RODA för definition av nyckel-värde metadata, där metadatanyckeln identifierar elementet (t.ex. "titel") och värdet innehållet i metadataelementet.
*  **Övrigt**: Generisk XML-typ där inget schema är definierat.

Nya metadatatyper kan läggas till i RODA enligt dokumentationen [Metadata formats](Metadata_Formats.md).

| Beskrivande metadatatyp | Validering           | Indexering         | Visualisering         | Version      |
|---------------------------|----------------------|------------------|-----------------------|--------------|
| EAD 2002                  | Schemavalidering    | Indexeringsregler   | Visualiseringsregler   | Redigeringsformulär |
| Dublin Core               | Schema validering    | Indexeringsregler   | Visualiseringsregler   | Redigeringsformulär |
| Nyckel-värde                 | Schema validering    | Indexeringsregler   | Visualiseringsregler   | Redigeringsformulär |
| Annan                     | Grammatikalitetsbedömning | Generell indexering | Generell visualisering | XML editering     |

Förklaring:
* **Schemavalidering**: Systemet erbjuder ett XML-schema för att validera strukturen och datatyperna för den tillhandahållna metadatafilen. Valideringsschemat kommer att användas under inleveransprocessen för att kontrollera om metadata som ingår i SIP:en är giltigt enligt de fastställda begränsningarna, samt när metadata redigeras i katalogen.
* **Grammatikalitetsbedömning**: Systemet kontrollerar endast om XML-filen för metadata är välformaterad och eftersom inget schema är definierat kommer systemet inte att verifiera om filen är giltig.
* **Indexeringsregler**: Systemet tillhandahåller en standard XSLT som omvandlar den XML-baserade metadatan till något som indexeringsmotorn kan förstå. Detta möjliggör avancerad sökning på beskrivande metadata.
* **Generell indexering**: Förvaret kommer att indexera alla textelement och attributvärden som finns i metadatafilen, men eftersom systemet inte känner till rätt mappning mellan XML-elementen och den inre datamodellen kommer endast grundläggande sökning att vara möjlig på tillhandahållen metadata.
* **Visualiseringsregler**: Systemet tillhandahåller en standard XSLT som omvandlar den XML-baserade metadatan till en HTML-fil som kommer att visas för användaren när man tittar i en befintlig AIP i katalogen.
* **Generisk visualisering**: Systemet tillhandahåller en generisk metadatavisare för att visa den XML-baserade metadatan. Alla textelement och attribut kommer att visas upp i ingen speciell ordning och deras XPath (sökväg) kommer att användas som etikett.
* **Redigeringsformulär**: Systemet tillhandahåller en konfigurationsfil som instruerar hur man visar ett formulär för att redigera befintlig metadata.
* **XML-redigering**: Systemet kommer att visa ett textområde där användaren kan redigera XML direkt.
