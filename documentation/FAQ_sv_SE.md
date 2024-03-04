# Frågor och svar

Vanliga frågor från RODA-användare och deras svar.

Har du en viktig fråga du inte hittar svaret på? Skapa en fråga [create an issue](https://github.com/keeps/roda/issues/new) på GitHub och markera det som "question".

## Visare

### Kan vi förhandsgranska filer direkt i RODAs webbgränssnitt?

Systemet kommer med ett antal fördefinierade visare för vissa standardformat (t.ex. PDF, bilder, HTML 5 multimedia format, etc.).

Specialformat behöver specialvisare eller konverterare som kan omvandla dem till existerande visare, t.ex. SIARD 2 viewer. Detta är utveckling som behöver hanteras från fall till fall.

## Metadata

### Vilka beskrivande metadata supporteras av RODA?

Alla beskrivande metadataformat supporteras så länge det finns grammatik i ett XML-Schema (XSD) som kan validera det.  Som standard kommer RODA konfigurerat med Dublin Core och Encoded Archival Description 2002. Fler scheman kan läggas till.

### Kan RODA stödja flera klassificeringsscheman?

Systemet möjliggör definition av flera hierarkiska strukturer där man kan placera informationsposter. Till var och en av noderna i dessa strukturer kan vi tilldela beskrivande metadata. Föreställ dig detta som ett fil-/mappsystem där varje mapp kan ha anpassad metadata i EAD- eller DC-format (eller något annat format för den delen). Var och en av dessa "mappar" (eller platshållare) kan vara ett arkiv, en samling, en serie eller en ackumulation, etc.

### Ger systemet möjligheter att ärva metadata från högre nivåer i strukturen?

Inte just nu. En plugin skulle behöva utvecklas.

### Kan beskrivningsenheten kopplas till en eller flera filer i ett annat arkiv eller system?

Beskrivningsenheten är en del av AIP (Archival Information Package), vilket innebär att representationer och filer vanligtvis är nära knutna till postens metadata. Det är dock möjligt att lägga till HTTP-länkar till andra resurser som sitter utanför förvaret genom att placera dem i den beskrivande metadatan.

### Är det möjligt att koppla en arkivbeskrivning till en kontextuell enhet (t.ex. ISAAR-myndighet)?

Systemet stöder inte behörighetsposter internt, men om du hanterar dessa poster externt kan du länka till dem genom att redigera den beskrivande metadatan.

### Hur stödjer man hybridarkiv (papper och digitala)?

Det är möjligt att ha poster utan digitala representationer, det vill säga endast med metadata. Ur ett katalogperspektiv är detta vanligtvis tillräckligt för att stödja pappersarkiv.

### Kan applikationen registrera nivån på leveransen, t.ex. vem överförde vad, när?

SIP:ar innehåller vanligtvis information om vem, vad och när de har skapats. Ingestprocessen skapar register över hela ingestprocessen. SIP:ar förväntas dock placeras på en nätverksplats som är tillgänglig för systemet. Att fastställa vem som kopierade SIP:ar till dessa platser ligger utanför systemets omfattning.

### Hur kan systemet registrera var de fysiska arkiven finns?

Det kan hanteras genom att fylla i ett metadatafält. Vanligtvis <ead:physloc>.

## Sök

### Vilka metadataattribut kan vi söka på?

Söksidan är helt konfigurerbar via en konfigurationsfil. Du kan ställa in attribut, typer, etikettnamn etc.

### Stöds fulltextsökning?

Ja, avancerad sökning är inbyggt som standard.

### Kan en användare begära analoga dokument från arkiven från sökresultatet?

Nej. Det skulle behöva integreras med ett externt system som kan hantera dessa förfrågningar.

### Återspeglar sökresultatlistan de behörigheter som tillämpas på posterna som presenteras?

Ja. Du kan bara se de bevarandeobjekt som du har tillgång till.

### Är verifieringskedjan sökbar och tillgänglig på ett användarvänligt sätt?

Ja. Du kan navigera i åtgärdsloggen (hela uppsättningen av åtgärder som utförts på arkivet) eller på bevarandemetadata (lista över bevarandeåtgärder som utförts på data) direkt från webbgränssnittet.

## Bevarande

### Beskriv hur karantänmiljön fungerar.

När SIP:ar under bearbetningen av inleveransen inte accepteras, flyttas de till en speciell mapp i filsystemet. Inleveransprocessen genererar en detaljerad rapport som beskriver orsakerna till avslaget. Vidare steg behöver ske via manuell hantering.

### Hur stödjer systemet bevarande?

Detta är en komplex fråga som inte kan besvaras med bara några rader text. Med det sagt kan vi säga att systemet hanterar bevarande på flera sätt:

- Det finns åtgärder som utför regelbundna beständighetskontroller av de inlevererade filerna och varnar förvaltarna om något problem upptäcks
- Systemet levereras med ett inbyggt riskhanteringsgränssnitt (dvs riskregister)
- Det finns åtgärder som upptäcker risker på filer och lägger till nya hot till riskregistret som måste hanteras manuellt (t.ex. en post som inte innehåller tillräckligt med beskrivande metadata, en fil följer inte formatpolicyn för förvaret, ett filformat är okänt eller det finns ingen representationsinformation etc.).
- Åtgärder finns tillgängliga som gör att förvaltarna kan minska risker, t.ex. utföra filformatkonverteringar (tiotals format stöds).

### Hur stödjer systemet infromationsvärderingar, val av bevarandeperioder och gallringsfrister?

RODA tillhandahåller ett komplext arbetsflöde för gallring av poster. Se [Disposal](Disposal.md) för mer information.

### Loggar systemet sökinteraktioner?

Ja. Varje aktivitet i systemet loggas.

## Krav

### Finns det några systemkrav på klientsidan för de som konsulterar arkiven?

Inte riktigt. En modern webbläsare räcker.

## Hur

### Hur lägger man till ett nytt språk i systemet?

Fullständiga instruktioner för hur man lägger till ett nytt språk i systemet finns på: [Translation guide](Translation_Guide.md).

### Hur sätter man upp en utvecklingsmiljö för RODA?

Fullständiga instruktioner för hur man sätter upp en utvecklingsmiljö finns på: [Developers guide](Developers_Guide.md).
