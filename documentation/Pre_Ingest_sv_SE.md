#  Leveransförberedelse

Processen för leveransförberedelse åskådliggör möjligheten för en arkivbildare att skapa Submission Information Packages (SIP:ar) som innehåller data och metadata (i en tydligt definierad struktur) för att kunna överlämna dem till systemet för inleverans. SIP:arna som skapas förväntas följa policys som är fastställda av eller överenskomna med systemet. 

Leveransförberedelserna omfattar vanligtvis följande aktiviteter:

## Inlämningskontrakt

Denna aktivitet består av att definiera villkoren, förutsättningar och krav för innehållet och för åtföljande information (t.ex. metadata, dokument, kontrakt etc.), som kommer att levereras till systemet av arkivbildaren. Det skapas en skriftlig överenskommelse mellan arkivbildare och systemet som specificerar typ av innehåll, juridiska samt tekniska krav som båda parter förväntas följa. 

## Klassificering

Vid undertecknande av inlämningskontraktet måste producenten ha godkänt en klassificeringsstruktur (eller lista över samlingar) där hen kommer ha tillstånd att lagra ny information. 

Grundklassificeringsschemat skapas vanligtvis av arkivarien och kan laddas ner på den här sidan i ett maskinläsbart format. Den nedladdade filen kan läsas in i RODA-in för att på ett bättre sätt kunna förbereda SIP:arna inför inläsning i systemet. 

[Ladda ner klassificeringsstruktur] (/api/v1/classification_plans) (Notera: för att ladda ner klassificeringsstrukturen krävs en RODA instans)

## Submission Information Packages (SIP)

Denna aktivitet består av att förbereda ett eller flera Submission Information Package (SIP) enligt de tekniska och icke-tekniska krav som definierats i inlämningskontraktet. För att underlätta skapandet av SIP:ar kan verktyget RODA-in användas. 

Verktyget och dokumentation är tillgänglig på [https://rodain.roda-community.org](https://rodain.roda-community.org).


## Överföring

Aktiviteten består av överföring av Submission Information Package (SIP) från arkivbildare till systemet. SIP:arna lagras tillfälligt i karantän i väntan på att bli hanterade av systemet.

Det finns flera sätt att överföra SIP:ar till systemet. Dessa inkluderar men är inte begränsade till följande alternativ:

### HTTP överföring

1. Anslut till systemets webbsida och logga in med användarnamn och lösenord. 
2. Gå till menyn Inleverans/Överföring och gå till mappen med ditt användarnamn (eller skapa en sådan mapp om den saknas)
3. Ladda upp dina SIP:s i den nya mappen.
4. Informera systemet om att det nu finns material som är redo att läsas in.

### Överföring via FTP

1. Anslut till [ftp://address] och logga in med användarnamn och lösenord.
2. Skapa en mapp för SIP:ar som ska läsas in (frivilligt)
3. Kopiera de skapade SIP:arna till den nya mappen.
4. Informera systemet om att det nu finns material som är redo att läsas in.

### Överföring från extern media

1. Spara SIP:arna på externt lagringsmedia (t.ex CD, USB, etc.)
2. Leverera till följande adress: [e-arkivets address]

## Inleveransprocess

Efter överföringen kommer arkivarien att välja ut SIP:ar för inleverans. Inleveransprocessen innehåller tjänster och funktioner för att acceptera SIP:ar från producenter och förbereda innehållet för lagring.

Inleveransfunktioner inkluderar att ta emot SIP:ar, utföra kvalitetssäkring av SIP:ar, generera ett Archival Information Package (AIP) som överensstämmer med e-arkivets dataformaterings- och dokumentationsstandarder, extrahera beskrivande information från AIP:erna i arkivets katalog och koordinera uppdateringar av lagring och datahantering.

