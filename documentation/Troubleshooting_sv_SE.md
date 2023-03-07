# Felsökning

Felsökning, eller problemlösning, används när ett fel upptäcks i systemet. För att lösa problemet krävs en logisk och systematisk analys, för att kunna göra systemet användbart igen.  

Här hittar du vanliga problem som kan uppstå, och förslag på hur de kan lösas. 

## Error: För många öppna filer

I loggarna kan du se fel som:

```
RODA_HOME/logs/roda-wui.log:pt.gov.dgarq.roda.core.common.RODAClientException: Error connecting to Login service - Too many open files
RODA_HOME/logs/roda-wui.log:Caused by: java.net.SocketException: Too many open file
```

Detta fel kan uppstå om det är för många filer öppna på servern. För att kontrollera antalet öppna filer, ta reda på PID på processen, kör Isof | grep | <PID>| wc. På många datorer är maxvärdet på hur många filer som kan vara öppna samtidigt lågt. (1024). 

För att ändra defaultvärdet, editera `/etc/security/limits.conf` genom att lägga till: 

```
* soft nofile 2048
* hard nofile 2048
```

Detta gör att processen kan köras med 2048 öppna filer. För att ändringen ska slå igenom behöver du starta om datorn. Det går också att använda kommandot `ulimit` för att ändra vid körning, men den ändringen kommer inte vara kvar efter omstart. 
