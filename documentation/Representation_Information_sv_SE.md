# Representationsinformation



*Innehållet är en ordagrann svensk översättning av artikeln med namnet "OAIS 7: Representation Information" publicerad på [Blog Alan's Notes on Digital Preservation](https://alanake.wordpress.com/2008/01/24/oais-7-representation-information/ ).*


Representationsinformation är ett avgörande koncept, eftersom det endast är genom vår förståelse av representationsinformationen som ett dataobjekt kan öppnas och tittas på. Själva representationsinformationen kan endast tolkas på den kunskapsbas som avses.

Representationsinformationskonceptet är också oupplösligt kopplat till begreppet Designated Community, eftersom hur vi definierar Designated Community (och dess associerade kunskapsbas) avgör hur mycket representationsinformation vi behöver. "OAIS måste förstå kunskapsbasen för dess utsedda gemenskap för att förstå den minsta representationsinformation som måste underhållas... Med tiden kan utvecklingen av Designated Commynity's kunskapsbas kräva uppdateringar av representationsinformationen för att säkerställa fortsatt förståelse" (2.2.1).


Själva dataobjektet, i ett digitalt arkiv, är helt enkelt en sträng av bitar. Vad representationsinformationen gör är att konvertera (eller berätta för oss hur man konverterar) dessa bitar till något mer meningsfullt. Den beskriver format- eller datastrukturkoncepten som bör tillämpas på bitsekvenserna, som i sin tur resulterar i mer meningsfulla värden, såsom tecken, pixlar och tabeller etc.

Detta kallas **strukturinformation**. Helst bör representationsinformation också innehålla **semantisk information**, t.ex. vilket mänskligt språk texten är skriven på, vad någon vetenskaplig terminologi betyder och så vidare (4.2.1.3.1). Genom att inkludera både struktur- och semantisk information framtidssäkrar vi oss själva så mycket som möjligt.

Bevarande av RI görs enklast när representationsinformationen uttrycks i en lättförståelig form, "såsom ASCII" (4.2.1.3.2). Vad modellen säger här är att det skulle vara dumt att spara representationsinformationen i ett proprietärt eller svagt stödd filformat, även om själva dataobjektet är i ett sådant format. Representationsinformationen kan skrivas ut på papper om det hjälper.

## Vad är det minsta som en representationsinformation behöver uppnå?

Representationsinformationen måste möjliggöra eller tillåta återskapandet av det ursprungliga dataobjektets betydande egenskaper. I huvudsak betyder det att representationsinformation ska kunna återskapa en kopia av originalet.

## Representationsnätverk

Representationsinformation kan innehålla hänvisningar till annan representationsinformation. Eftersom representationsinformationen i sig är ett informationsobjekt, med sitt eget dataobjekt och relaterad representationsinformation, kan ett helt nät av representationsinformation byggas upp. Detta kallas ett representationsnätverk (4.3.1.3.2). Till exempel kan representationsinformationen för ett objekt helt enkelt ange att en post är i ASCII. Beroende på hur vi definierar vår Designated Community kan vi behöva bidra med ytterligare representationsinformation, till exempel vad ASCII-standarden faktiskt är.

## Representationsinformation vid inleverans

En SIP kan dyka upp med mycket dålig representationsinformation – kanske bara en tryckt manual, eller två, eller några PDF-filer i dokumentationsmappen (se E-ARK SIP-specifikationen).

OAIS behöver mycket mer. Men detta bör inte avskräcka en OAIS från att acceptera saker. Bara för att en SIP har anlänt med endast 4 metadatafält ifyllda av ett obligatoriskt 700-tal är det inte riktigt ett tillräckligt bra skäl att avvisa det, om det är ett register med permanent värde.

## Representationsprogramvara

Representationsinformationen kan vara **körbar programvara**, om det är till hjälp. Exemplet som ges i modellen (4.2.1.3.2) är ett där representationsinformationen finns som en PDF-fil. Istället för att ha ytterligare representationsinformation som definierar vad en PDF är, är det mer användbart att helt enkelt använda en PDF-visare istället.

OAIS måste dock spåra detta noggrant, för en dag kommer det inte att finnas något sådant såsom PDF-visare, och den ursprungliga representationsinformationen skulle då behöva migreras till en ny form.

## Programvara för åtkomst och emulering

På kort sikt öppnas förmodligen de flesta arkivobjekt av exakt samma programvara som de skapades i. Någon som öppnar ett arkiverat, men nyligen skapat, Word-dokument kommer sannolikt att använda sin egen MS Word-app. Detta leder till möjligheten att utökade representationsnätverk kan överges. Vi använder helt enkelt originalmjukvaran eller något liknande.

OAIS kallar denna Access-mjukvara och varnar för den eftersom det betyder att vi måste fortsätta att försöka ha kvar fungerande mjukvara. Det slår mig dock att detta är hela poängen med HW-emulering. Om du har den enda frasen "MS Word-dokument" som din representationsinformation och du har en arbetskopia av Word-appen på emulerad HW och OS, behöver du inget representationsnätverk alls. Åtminstone inte förrän allt går fel!

## Representationsinformation i praktiken

Låt oss ta en JPEG-bildfil som exempel. Jag tror att vi kan ta det som självklart att vår nuvarande Designated Community förstår vad en JPEG är: i OAIS-terminologi inkluderar konceptet i Designated Community's Knowledge Base termen "JPEG". Så vår representationsinformation för den filen kan i teorin helt enkelt vara ett uttalande som säger att det är en JPEG-bild. Det räcker. För JPEG-formatet, och för många andra, är den mest användbara representationsinformationen faktiskt en app som kan öppna och visa den. (Vilken nuvarande PC som helst kommer att kunna öppna JPEG ändå.)

På längre sikt måste vi dock förbereda oss för en värld där Designated Community har gått från JPEG. Så vi bör lägga till en länk till JPEG-standardens webbplats också för att förklara vad en JPEG är och inkludera information om vilka programvaror som kan öppna en JPEG-bild. Mer användbart är att det borde finnas en länk på representationsnätverket till en plats på webben där vi faktiskt kan ladda ner en JPEG-visare.

Det innebär att representationsnätverket förändras över tid. Vi måste kunna uppdatera den i takt med att tekniken utvecklas.
