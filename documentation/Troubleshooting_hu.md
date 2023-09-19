# Hibaelhárítás

A hibaelhárítás a problémamegoldás egy formája, amelyet gyakran a meghibásodott termékek javítására alkalmaznak. Azaz probléma forrásának logikus, szisztematikus keresése annak megoldása érdekében, hogy a termék vagy folyamat ismét működőképessé váljon.

Ebben a pontban a terméket érintő gyakori problémákat és a megoldási lehetőségeket találja.

## Hibajelzés: túl sok megnyitott fájl

Esetenként a naplókban olyan hibákat láthat, mint:

```
RODA_HOME/logs/roda-wui.log:pt.gov.dgarq.roda.core.common.RODAClientException: Hiba a bejelentkezési szolgáltatáshoz való csatlakozásban - Túl sok a nyitott fájl
RODA_HOME/logs/roda-wui.log:Caused by: java.net.SocketException: Too many open files
```

Ez akkor fordulhat elő, ha a kiszolgálóra sok fájlt telepítettek. Ha látni szeretné, hány fájl van nyitva a kiszolgálón, kérdezze meg a folyamat PID-jét, majd futtassa az lsof | grep<PID> | wc parancsot.  Sok számítógépen az egy folyamat által megnyitható fájlok alapértelmezett maximális száma alacsony (pl. 1024).

A korlátozás módosításához szerkessze az `/etc/security/limits.conf` állományt a következők hozzáadásával:

```
* soft nofile 2048
* hard nofile 2048
```

Ez lehetővé teszi, hogy a bárki által futtatott folyamat 2048 fájlt nyisson meg. A módosítások érvényesítéséhez újra kell indítania a számítógépet. Az `ulimit` paranccsal is módosíthatja ezt a számot futás közben, de ez a parancs nem marad meg a következő indításkor.
