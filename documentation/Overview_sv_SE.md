
# Överblick

RODA är ett komplett digitalt arkiv som levererar funktionalitet för alla huvudenheterna i OAIS-modellen. RODA kan ta in, hantera och ge tillgång till olika typer av digitala objekt som produceras av stora företag eller offentliga förvaltningar. RODA är baserat på öppen källkodsteknologi och stöds av befintliga standarder så som OAIS, METS, EAD och PREMIS.

RODA implementerar också en rad specifikationer och standarder. För att veta mer om OAIS-informationspaketen som RODA implementerar, vänligen läs [Digital Information LifeCycle Interoperability Standards Board](http://www.dilcis.eu/)  på GitHub https://github.com/dilcisboard.

## Funktioner

* Användarvänligt grafiskt användargränssnitt baserat på HTML 5 och CSS 3
* Lagring och hantering av digitala objekt
* Katalog baserad på omfattande metadata (stödjer alla XML-baserade format som beskrivande metadata)
* Full support för Dublin Core och Encoded Archival Description.
* Konfigurerbart arbetsflöde för inleverans i flera steg
* PREMIS 3 för metadata
* Autentisering och rättigheter via LDAP och CAS
* Rapportering och statistik
* REST API
* Stödjer utbyggbara bevarandeåtgärder
* Integrerad riskhantering
* Integrerat formatregister
* Använder inbyggt filsystem för datalagring
* 100% kompatibelt med E-ARK SIP, AIP, och DIP specifikationer
* Stöd för teman

För mer information, besök gärna RODA:s webbplats:
**<https://www.roda-community.org>**


## Funktioner

RODA har UI-stöd för följande funktionella enheter.

### Katalog

Katalogen är en inventering av alla artiklar och poster i arkivet. En post kan representera olika information i arkivet (t ex böcker, elektroniska dokument, bild, databas export mm). Poster är vanligtvis samlat i en samling (eller arkivbestånd) och vidare indelat i undersamlingar, sektioner, serier, filer osv. Den här sidan listar alla samlingar på högsta nivå i arkivet. Du kan komma ner till undersamlingar genom att klicka i tabellen nedan.

### Sök

På söksidan kan du söka efter intellektuella enheter, representationer eller filer (använd nedåtpilen för att välja sökdomän). För var och en av dessa domäner kan du söka i alla dess egenskaper eller i specifika egenskaper (använd nedåtpilen för att utöka den avancerade sökningen). Om du till exempel väljer intellektuella enheter kan du söka i ett specifikt fält av beskrivande metadata, eller hitta filer av ett visst format om filer avancerad sökning är vald.

Sökmotorn hittar endast hela ord. Om du vill söka efter delar av ord så använd '*'-tecken. För mer information om sökverktyg, se nästa sektion.

### Avancerad sökning

På söksidan kan du söka efter intellektuella enheter, representationer eller filer (använd nedåtpilen för att välja sökdomän). För var och en av dessa domäner kan du söka i alla dess egenskaper eller i specifika egenskaper (använd nedåtpilen för att utöka den avancerade sökningen). Om du till exempel väljer intellektuella enheter kan du söka i ett specifikt fält av beskrivande metadata eller hitta filer av ett visst format om filer avancerad sökning är vald.

### Leveransförberedelse

Processen för leveransförberedelse åskådliggör möjligheten för en arkivbildare att skapa Submission Information Packages (SIP) som innehåller data och metadata (i en definierad struktur) för att kunna överlämna dem till systemet för inleverans. SIP:arna som skapas förväntas följa policys som är fastställda med (eller genom) systemet. 

### Inleverans

Inleveransytan är en tillfällig lagringsyta för att ta emot inlämningsinformationspaket (SIP) från producenter. SIP:ar kan levereras via elektronisk överföring (t.ex. FTP) eller laddas in från media som är anslutna till systemet. Den här sidan gör det också möjligt för användaren att söka efter filer i den tillfälliga lagringsytan, skapa/ta bort mappar och ladda upp flera SIP:er samtidigt till systemet för vidare bearbetning och inleverans. Inleveransprocessen kan initieras genom att välja de SIP:er som du vill inkludera i bearbetningsbatchen. Klicka på knappen "Bearbeta" för att initiera intagningsprocessen.

### Leverans

Inleveransprocessen innehåller tjänster och funktioner för att acceptera inlämningspaket (SIP) från producenter, förbereda arkivpaket (AIP) för lagring och säkerställa att arkivpaket och deras stödjande beskrivande information etableras i e-arkivet. Den här sidan listar alla inleveranser som för närvarande körs och alla leveranser som har körts tidigare. I den högra sidopanelen är det möjligt att filtrera jobb baserat på deras tillstånd, användare som initierade jobbet och startdatum. Genom att klicka på ett objekt i tabellen är det möjligt att se hur arbetet fortskrider samt ytterligare detaljer.

### Ankomstkontroll

Ankomstkontroll är en process för att avgöra om informationen och annat material har bevarandevärde. Bedömning kan göras på samling-, skapar-, serie-, fil- eller objektsnivå. Ankomstkontrollen kan ske före eller efter överföringen. Grunden för beslut kan omfatta ett antal faktorer inklusive informationens härkomst, innehåll, autenticitet, tillförlitlighet, ordning, fullständighet, skick, kostnader för bevarandet samt informationens egenvärde.

### Arkivvårdsjobb

Arkivvårdsjobb är åtgärder som utförs på innehållet i arkivet och som syftar till att förbättra tillgängligheten till arkiverade filer eller att minska riskerna vid digital bevarande. Inom RODA hanteras arkivvårdsjobb av en exekveringsmodul. Exekveringsmodulen tillåter användare med rätt behörighet att köra arkivvårdsjobb för en given uppsättning data (AIP, representationer eller filer). Arkivvårdsjobben inkluderar formatkonverteringar, verifieringar av kontrollsummor, rapportering (t.ex. för att automatiskt skickade SIP-gokännande/avvisning-meddelanden) och viruskontroller etc. 

### Interna åtgärder

Interna åtgärder är komplexa uppgifter som utförs av systemet som bakgrundsjobb, vilka förbättrar användarupplevelsen genom att inte blockera användargränssnittet under arbeten som tar längre tid. Exempel på sådana arbeten är att flytta AIP:er, återindexera delar av e-arkivet eller att radera ett stort antal filer. 

### Användare och grupper

Användarhanteringstjänsten gör det möjligt för arkivarien att skapa eller ändra inloggningsuppgifter för varje användare i systemet. Den här tjänsten tillåter också arkivarien att definiera grupper och behörigheter för var och en av de registrerade användarna. Arkivarien kan också filtrera de användare och grupper som visas genom att klicka på de tillgängliga alternativen i den högra sidopanelen. För att skapa en ny användare, klicka på knappen "Lägg till användare". För att skapa en ny användargrupp, klicka på knappen "Lägg till grupp". För att redigera en befintlig användare eller grupp, klicka på ett objekt i listan.

### Aktivitetslogg

Händelseloggar är speciella filer som registrerar viktiga händelser som sker i systemet. Till exempel registrerar systemet varje gång en användare loggar in, när en nedladdning utförs eller när en ändring görs i en beskrivande metadatafil. Närhelst dessa händelser inträffar registrerar systemet den nödvändiga informationen i händelseloggen för att möjliggöra framtida granskning av systemaktiviteten. För varje händelse registreras följande information: Datum, involverad komponent, systemmetod eller	funktion, målobjekt, användare som utförde åtgärden, åtgärdens varaktighet och IP-adressen till användaren som utförde åtgärden . Användare kan filtrera händelser efter typ, datum och andra attribut genom att välja de tillgängliga alternativen i den högra sidopanelen.

### Notiser

Notifieringar i RODA är ett sätt att informera användaren om specifika händelser i systemet. Informationen skickas i ett mail, som innehåller en beskrivning av händelsen och en länk där användaren kan bekräfta. 

### Statistik

Den här sidan visar en instrumentpanel med statistik som rör flera olika delar av systemet. Statistiken är organiserad efter sektioner där var och en av dessa fokuserar på en viss aspekt av systemet, t.ex. frågor som har med metadata, information, statistik som rör inleverans samt arkivvårdsjobb, användarstatistik och frågor rörande autentisering, bevarandeaktiviteter, riskhantering och notiser.

### Riskregister

Riskregistret listar alla identifierade risker som kan påverka systemet. Det bör vara så omfattande som möjligt och inkludera alla identifierade risker och en uppskattad sannolikhet att respektive risk inträffar, hur risken kan påverka om den inträffar, när den kan inträffa och hur ofta. Riskhantering används för att minimera sannolikheten att risken inträffar. 

### Representationsnätverk

Representationsinformation är all information som krävs för att kunna förstå och tolka både det digitala materialet samt tillhörande metadata. Digitala objekt lagras som bitströmmar som inte kan förstås av en människa om inte finns ytterligare data för att tolka dem. Representationsinformation är den tillkommande information om struktur eller semantik som omvandlar rådata till något mer meningsfullt.

### Formatregister (utfasat)

Formatregistret är ett tekniskt register för att stödja digitala bevarandetjänster för e-arkivet.
