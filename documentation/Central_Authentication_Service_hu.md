# Központi hitelesítési szolgáltatás (CAS - Central Authentication Service)

A RODA támogatja a [Központi hitelesítési szolgáltatást](https://en.wikipedia.org/wiki/Central_Authentication_Service) (**CAS**).
Ezek az utasítások az [Apereo](https://www.apereo.org/projects/cas) által nyújtott CAS szolgáltatás telepítésére vonatkoznak.

## A CAS telepítése és futtatása LDAP-pal

Az alábbi instrukciók esetében feltételezzük, hogy az LDAP-kiszolgáló a következő jellemzőkkel rendelkezik:

* **URL**: ldap://localhost:10389
* **Bázis DN**: dc=roda,dc=org
* **Felhasználók DN**: ou=users,dc=roda,dc=org
* **Admin DN**: uid=admin,ou=system
* **Admin jelszó**: roda
* **Felhasználó egyedi attribútuma**: uid

Módosítsa az értékeket az LDAP-nak megfelelően.

### CAS Overlay sablon beszerzése

    ```
    git clone -b 4.2 https://github.com/apereo/cas-overlay-template.git
    cd cas-overlay-template
    ```

### Nyissa meg az **etc/cas.properties** fájlt, és szerkessze a következő részeket:

Változtassa meg a server.name-t https://localhost:8443-ra

    ```bash
    server.name=https://localhost:8443
    ```

Vegye ki a megjegyzést a következő sorból és állítsa az értéket true-ra

    ```bash
    cas.logout.followServiceRedirects=true
    ```

Vegye ki a megjegyzést a következő sorból, és állítsa be a file:/etc/cas/services értékét.

    ```bash
    service.registry.config.location=file:/etc/cas/services
    ```

Adja hozzá ezeket a sorokat az LDAP konfigurációval

**FIGYELEM**: Módosítsa az LDAP-értékeket az LDAP-beállításoknak megfelelően.

```bash
#========================================
# LDAP általános tulajdonságok
#========================================
ldap.url=ldap://localhost:10389

# TLS indítása SSL kapcsolatokhoz
ldap.useStartTLS=false

# Gyökér DN könyvtár
ldap.rootDn=dc=roda,dc=org

# A hitelesítendő felhasználók alap DN-je
ldap.baseDn=ou=users,dc=roda,dc=org

# LDAP kapcsolat időkorlátja milliszekundumban
ldap.connectTimeout=3000

# Manager credential DN
ldap.managerDn=uid=admin,ou=system

# Menedzser hitelesítő jelszó
ldap.managerPassword=roda

#========================================
# LDAP kapcsolattartási pool konfiguráció
#========================================
ldap.pool.minSize=1
ldap.pool.maxSize=10
ldap.pool.validateOnCheckout=false
ldap.pool.validatePeriodically=true

# Az idő milliszekundumban kifejezve, amíg a pool kimerült állapotát blokkolni kell.
# mielőtt feladná.
ldap.pool.blockWaitTime=3000

# A kapcsolat érvényesítésének gyakorisága másodpercben
# Csak akkor érvényes, ha validatePeriodically=true
ldap.pool.validatePeriod=300

# N másodpercenként próbálja meg a kapcsolatokat selejtezni
ldap.pool.prunePeriod=300

# Maximális idő, amíg egy üresjárati kapcsolat fennmaradhat
# poolban, mielőtt eltávolításra/megsemmisítésre kerülne
ldap.pool.idleTime=600

#========================================
# LDAP hitelesítés
#========================================
ldap.authn.searchFilter=uid={user}

# Ldap tartomány, amelyet a dn feloldásához használnak
ldap.domain=roda.org

# Az LDAP jelszó házirend engedélyezve legyen?
ldap.usePpolicy=false

# Engedélyezzük a több DN-t a hitelesítés során?
ldap.allowMultipleDns=false
```

### Hozzon létre egy RODA szolgáltatást az **etc/services/roda.json** fájlban.

**FIGYELEM**: Módosítsa a **serviceId**-t a RODA címének megfelelőre.

```json
{
  "@class": "org.jasig.cas.services.RegexRegisteredService",
  "serviceId": "^https://localhost:8888/.*",
  "name": "RODA",
  "id": "16226673791703",
  "description": "RODA",
  "evaluationOrder": 3,
  "logoutType": "BACK_CHANNEL",
  "logoutUrl" : "https://localhost:8888/logout",
  "attributeReleasePolicy": {
    "@class": "org.jasig.cas.services.ReturnAllAttributeReleasePolicy",
    "principalAttributesRepository": {
      "@class": "org.jasig.cas.authentication.principal.DefaultPrincipalAttributesRepository",
      "expiration": 2,
      "timeUnit": [
        "java.util.concurrent.TimeUnit",
        "HOURS"
      ]
    },
    "authorizedToReleaseCredentialPassword": false,
    "authorizedToReleaseProxyGrantingTicket": true
  },
  "accessStrategy": {
    "@class": "org.jasig.cas.services.TimeBasedRegisteredServiceAccessStrategy",
    "enabled": true,
    "ssoEnabled": true,
    "requireAllAttributes": false,
    "caseInsensitive": false
  },
  "proxyPolicy" : {
    "@class" : "org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy",
    "pattern" : "^https?://.*"
  }
}
```

### SSL beállítása a Jetty számára

Hozzon létre egy tárolót az **etc/jetty/thekeystore** címen a **changeit** jelszóval.

**FIGYELEM**: Amikor a parancs azt kérdezi, hogy "Mi a vezeték- és keresztneve?", írja **localhost**.

```bash
keytool -keystore etc/jetty/thekeystore -alias jetty -genkey -keyalg RSA -sigalg SHA256withRSA
```

### Konfigurációs fájlok másolása a **/etc/cas** könyvtárba

```bash
sudo mkdir -p /etc/cas
sudo cp -r etc/* /etc/cas
```

### Nyissa meg a **pom.xml** fájlt és adja hozzá a következő függőségi feltételeket

```xml
<!-- REST API támogatás hozzáadása -->
<dependency>
    <groupId>org.jasig.cas</groupId>
    <artifactId>cas-server-support-rest</artifactId>
    <version>${cas.version}</version>
    <scope>runtime</scope>
</dependency>
```

```xml
<!--  LDAP hitelesítés támogatása -->
<dependency>
    <groupId>org.jasig.cas</groupId>
    <artifactId>cas-server-support-ldap</artifactId>
    <version>${cas.version}</version>
</dependency>
```

### Hozzon létre egy **src/main/webapp/WEB-INF/deployerConfigContext.xml** fájlt a következő tartalommal:

**FIGYELEM**: Módosítsa a **principalIdAttribute**-t úgy, hogy az megfeleljen az LDAP-kiszolgálón lévő felhasználók attribútumának.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:c="http://www.springframework.org/schema/c"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:util="http://www.springframework.org/schema/util"
       xmlns:sec="http://www.springframework.org/schema/security"
       xmlns:ldaptive="http://www.ldaptive.org/schema/spring-ext"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd
       http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd
       http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
       http://www.springframework.org/schema/security http://www.springframework.org/schema/security/spring-security.xsd
       http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd
       http://www.ldaptive.org/schema/spring-ext http://www.ldaptive.org/schema/spring-ext.xsd">

    <bean id="ldapAuthenticationHandler"
          class="org.jasig.cas.authentication.LdapAuthenticationHandler"
          p:principalIdAttribute="uid"
          c:authenticator-ref="authenticator">
        <property name="principalAttributeMap">
            <map>
                <entry key="cn" value="cn" />
                <entry key="email" value="email" />
            </map>
        </property>
    </bean>

    <ldaptive:ad-authenticator id="authenticator"
                               ldapUrl="${ldap.url}"
                               userFilter="${ldap.authn.searchFilter}"
                               bindDn="${ldap.managerDn}"
                               bindCredential="${ldap.managerPassword}"
                               allowMultipleDns="${ldap.allowMultipleDns:false}"
                               connectTimeout="${ldap.connectTimeout}"
                               validateOnCheckOut="${ldap.pool.validateOnCheckout}"
                               failFastInitialize="true"
                               blockWaitTime="${ldap.pool.blockWaitTime}"
                               idleTime="${ldap.pool.idleTime}"
                               baseDn="${ldap.baseDn}"
                               maxPoolSize="${ldap.pool.maxSize}"
                               minPoolSize="${ldap.pool.minSize}"
                               validatePeriodically="${ldap.pool.validatePeriodically}"
                               validatePeriod="${ldap.pool.validatePeriod}"
                               prunePeriod="${ldap.pool.prunePeriod}"
                               useSSL="${ldap.use.ssl:false}"
                               subtreeSearch="${ldap.subtree.search:true}"
                               useStartTLS="${ldap.useStartTLS}" />

    <util:map id="authenticationHandlersResolvers">
        <entry key-ref="ldapAuthenticationHandler" value-ref="primaryPrincipalResolver" />
        <!--
        <entry key-ref="proxyAuthenticationHandler" value-ref="proxyPrincipalResolver" />
        <entry key-ref="primaryAuthenticationHandler" value-ref="primaryPrincipalResolver" />
        -->
    </util:map>

    <util:list id="authenticationMetadataPopulators">
        <ref bean="successfulHandlerMetaDataPopulator" />
        <ref bean="rememberMeAuthenticationMetaDataPopulator" />
    </util:list>

    <bean id="attributeRepository" class="org.jasig.services.persondir.support.NamedStubPersonAttributeDao"
          p:backingMap-ref="attrRepoBackingMap" />

    <alias name="acceptUsersAuthenticationHandler" alias="primaryAuthenticationHandler" />
    <alias name="personDirectoryPrincipalResolver" alias="primaryPrincipalResolver" />

    <util:map id="attrRepoBackingMap">
        <entry key="uid" value="uid" />
        <entry key="eduPersonAffiliation" value="eduPersonAffiliation" />
        <entry key="groupMembership" value="groupMembership" />
        <entry>
            <key><value>memberOf</value></key>
            <list>
                <value>faculty</value>
                <value>staff</value>
                <value>org</value>
            </list>
        </entry>
    </util:map>

    <alias name="serviceThemeResolver" alias="themeResolver" />

    <alias name="jsonServiceRegistryDao" alias="serviceRegistryDao" />

    <alias name="defaultTicketRegistry" alias="ticketRegistry" />

    <alias name="ticketGrantingTicketExpirationPolicy" alias="grantingTicketExpirationPolicy" />
    <alias name="multiTimeUseOrTimeoutExpirationPolicy" alias="serviceTicketExpirationPolicy" />

    <alias name="anyAuthenticationPolicy" alias="authenticationPolicy" />
    <alias name="acceptAnyAuthenticationPolicyFactory" alias="authenticationPolicyFactory" />

    <bean id="auditTrailManager"
          class="org.jasig.inspektr.audit.support.Slf4jLoggingAuditTrailManager"
          p:entrySeparator="${cas.audit.singleline.separator:|}"
          p:useSingleLine="${cas.audit.singleline:false}"/>

    <alias name="neverThrottle" alias="authenticationThrottle" />

    <util:list id="monitorsList">
        <ref bean="memoryMonitor" />
        <ref bean="sessionMonitor" />
    </util:list>

    <alias name="defaultPrincipalFactory" alias="principalFactory" />
    <alias name="defaultAuthenticationTransactionManager" alias="authenticationTransactionManager" />
    <alias name="defaultPrincipalElectionStrategy" alias="principalElectionStrategy" />
    <alias name="tgcCipherExecutor" alias="defaultCookieCipherExecutor" />
</beans>
```

### Készítse el a **cas.war** fájlt

```bash
./mvnw clean package
```

### Indítsa el a beágyazott Jetty szervert

```bash
./mvnw jetty:run-forked
```

A CAS szolgáltatás másik alkalmazáskiszolgálón történő futtatásához csak telepítse a **target/cas.war** fájlt az alkalmazáskiszolgálóra.

### Hozzáférés a CAS-kiszolgálóhoz és bejelentkezés az LDAP hitelesítő adatokkal

Nyissa meg az URL-t [https://localhost:8443/cas](https://localhost:8443/cas) és adja meg a felhasználó LDAP hitelesítő adatait.
Ha a hitelesítő adatok érvényesek, akkor megjelenik a "**Sikeres bejelentkezés**" üzenet.

**FIGYELEM**: A fent generált tanúsítvány saját aláírású, és a böngésző ezt kifogásolni fogja. 
Tesztelési célokra figyelmen kívül hagyhatja ezt a figyelmeztetést, és elfogadhatja a tanúsítványt.


## A RODA beállítása a CAS használatára

### Nyissa meg a **~/.roda/config/roda-wui.properties** fájlt, és végezze el a következő módosításokat:

* A belső szűrők kikapcsolása és
* CAS-szűrők engedélyezése,
* Tekintse át a konfigurációs értékeket, hogy azok megfeleljenek az Ön beállításainak.

**FIGYELEM**: ha a **~/.roda/config/roda-wui.properties** fájl nem létezik,
másoljuk a mintafájlt a **~/.roda/example-config/roda-wui.properties** állományba .
a **~/.roda/config/roda-wui.properties** fájlba, majd végezze el a módosításokat.

```bash
...
##############################################
# Belső szűrők
##############################################

# Belső szűrők kikapcsolása
ui.filter.internal.enabled = false
ui.filter.internal.realm = RODA REST API
ui.filter.internal.exclusions = ^/swagger.json,^/v1/theme/?

##############################################
# CAS filterek
##############################################

# Engedélyezze a CAS szűrőket
ui.filter.cas.enabled = true
ui.filter.cas.cas.cas.casServerUrlPrefix = https://localhost:8443/cas
ui.filter.cas.cas.cas.casServerLoginUrl = https://localhost:8443/cas/login
ui.filter.cas.exclusions = ^/swagger.json,^/v1/theme/?,^/v1/auth/ticket?
# RODA alapcím
ui.filter.cas.serverName = https://localhost:8888
ui.filter.cas.exceptionOnValidationFailure = false
ui.filter.cas.redirectAfterValidation = false
...
```

### Saját aláírt tanúsítványok megbízhatósága

Ha a bejelentkezés során olyan kivételt lát, mint az **SSLHandshakeException** vagy a 
**ValidationException**, az valószínűleg azt jelenti, hogy a RODA-nak problémái vannak a CAS-kiszolgálótól származó tanúsítvány érvényesítésével.
Ha a CAS-kiszolgáló a fentiekben létrehozott saját aláírt tanúsítványt használja, akkor ezt a hibát fogja látni.

A javításhoz megteheti:

1. Használjon érvényes tanúsítványt, vagy
2. A java programok (például a RODA) bízzanak a saját aláírt tanúsítványban.

A következő lépésekkel érheti el, hogy a Java programok megbízzanak a saját aláírt tanúsítványában:

#### Exportálja a jetty saját aláírt tanúsítványát

    ```bash
    keytool -export -keystore /etc/cas/jetty/thekeystore -alias jetty -file jetty.cer
    ```

#### Jetty tanúsítvány importálása Java truststore-ban

```bash
sudo keytool -import -keystore /usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -alias jetty -file jetty.cer
```

**FIGYELEM**: Feltételezve, hogy a **JAVA_HOME** a **/usr/lib/jvm/java-8-oracle**. 
Ha ez nem így van, állítsd be a keystore elérési útvonalát ennek megfelelően.

**A tanúsítvány sikeres importálása után indítsa újra a RODA-t.**

## Fejlesztési megjegyzések

### RODA hibakeresés, amíg a CAS aktív

Kövesse az utasításokat a CAS beállításához és az önaláírt tanúsítványok megbízhatóságához, majd használja a következő GWT Dev mód paramétereket:

```
-server :keystore=/etc/cas/jetty/thekeystore,password=changeit -bindAddress 0.0.0.0
```

A RODA-t pedig a (https://localhost:8888)[https://localhost:8888] címen érheti el.

Ha a `/etc/cas/jetty/thekeystore` kulcstároló egyetlen tanúsítványt tartalmaz (ami a fenti utasítások követése esetén így lesz), akkor a GWT ezt a tanúsítványt fogja használni.

### A CAS-t használó alkalmazások hibakeresése több GWT-alkalmazás esetén

Több, CAS-t használó GWT-alkalmazás (pl. RODA és az adatbázis-vizualizációs eszközkészlet) hibakereséséhez minden egyes alkalmazás:

* Más hostnévvel kell elérni (bár futhatnak ugyanazon az IP címen is);
* Saját tanúsítvánnyal kell rendelkeznie (a használt állomásnévhez);
* Saját tárral kell rendelkeznie, amely csak ezt a tanúsítványt tartalmazza.

A CAS egyes funkcióinak működéséhez az összes tanúsítványt hozzá kell adni a Java megbízhatósági tárolóhoz (lásd a fenti szakaszt).

A böngészőt úgy kell beállítani, hogy engedélyezze a nem biztonságos tartalmak futtatását (mivel a HTTPS weboldalnak kommunikálnia kell a HTTP GWT kódkiszolgálóval) (pl. a Chrome esetében a `--allow-running-insecure-content` paraméterrel kell elindítani).
