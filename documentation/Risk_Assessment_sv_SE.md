# Riskbedömning

RODA levereras med ett riskregister förinstallerat med över 80 stycken bevaranderisker som erhållits från verktygssatsen [Digital Repository Audit Method Based on Risk Assessment (DRAMBORA)](http://www.repositoryaudit.eu) utvecklad av [Digital Curation Center ( DCC)](http://www.dcc.ac.uk) och DigitalPreservationEurope (DPE).

Den innehåller också ett riskregister som kan hanteras från användargränssnittet och flera insticksprogram för riskbedömning som uppdaterar information om riskregistret.

## Hur bedömer och minskar man bevaranderisker i RODA?

Du vill börja göra riskbedömningsprocesser i ditt arkiv. Till exempel så kanske du vill starta en process för att konvertera filer från format som inte längre är hållbara (t.ex. för att det uppstår en ny risk för att ett givet filformat inte kommer att stödjas i framtiden).

I grund och botten skulle du vilja ha ett arbetsflöde för följande hypotetiska scenario:

1. Du har skapat en SIP som innehåller en Word 95 .doc-fil
1. Du har identifierat en (hypotetisk) risk angående Word 95 .doc-fil (t.ex. för att ingen programvara kan läsa det formatet längre)
1. Eftersom risken är identifierad skulle jag vilja starta en konvertering av alla Word 95 .doc-filer till både DOCX och PDF/A

Det finns flera sätt att hantera nya risker och påbörja en bevarandeåtgärd för att minska dessa, så vi fokuserar bara på hur vi skulle lösa just detta exempel:

Föreställ dig att jag, som har rätt behörigheter i RODA, vet att Word 95 är ett format i riskzonen. Då skulle jag gå till riskregistret och registrera den risken, beskriva alla saker jag vet om just den risken och utse möjliga åtgärder för att minska den (t.ex. migrera den till ett nytt format).

(En annan möjlighet skulle vara att använda ett plugin som skulle göra denna typ av analys automatiskt, men det finns inget sådant plugin för tillfället. Det skulle behöva utvecklas.)

Du kan sedan använda sökfunktionen för att hitta alla Word 95-filer i e-arkivet. Alla filformat har identifierats under inmatningsprocessen så den uppgiften är ganska enkel. Jag skulle sedan använda den tillgängliga riskassociation-pluginet för att ställa in dessa filer som instanser av den nyligen skapade risken. Detta fungerar som dokumentation av bevarandebesluten som har fattats och som motivering till vad vi ska göra härnäst - det här är bevarandeplanering.

Nästa steg skulle vara att migrera filerna. Du kan göra ungefär samma sak som tidigare, d.v.s. välja alla word 95-filer i sökmenyn och sedan köra en bevarandeåtgär för att migrera dem till, exempelvis, PDF.

Du kan sedan sänka risknivån eftersom det inte finns fler Word 95-filer i systemet. Incidenten kan markeras som "begränsad".

Det jag just förklarade är det manuella arbetsflödet, eftersom vi för närvarande inte har ett plugin för riskupptäckt av föråldrade format. Det pluginet skulle dock mycket väl kunna utvecklas. Begränsningsstegen skulle i så fall startas direkt från riskhanteringsgränssnittet.

När det gäller tillgängliga konverteringsplugin stödjer RODA för närvarande de vanliga (stora bild-, video-, ord- och ljudformat). Nischformat kommer alltid att finnas i varje verksamhet och i så fall måste plugins för särskilda ändamål utvecklas.

## Har du en idé om ett plugin för riskbedömning?

Om du är intresserad av att utveckla ett nytt riskbedömningsplugin, vänligen kontakta produktteamet för mer information.
