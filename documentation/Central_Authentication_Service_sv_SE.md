# Central Autentisering Service (CAS)

RODA har stöd för [Central Authentication Service](https://en.wikipedia.org/wiki/Central_Authentication_Service) (**CAS**).
Dessa instruktioner är till för installation av CAS-tjänsten som tillhandahålls av [Apereo](https://www.apereo.org/projects/cas).

## Installera och kör CAS med LDAP

För dessa instruktioner antar vi att LDAP-servern har följande egenskaper:

* **URL**: ldap://localhost:10389
* **Base DN**: dc=roda,dc=org
* **Users DN**: ou=users,dc=roda,dc=org
* **Admin DN**: uid=admin,ou=system
* **Admin password**: roda
* **User unique attribute**: uid

Ändra värdena så att de matchar LDAP.

### Hämta CAS Overlay-mall

    ```
    git clone -b 4.2 https://github.com/apereo/cas-overlay-template.git
    cd cas-overlay-template
    ```

### Öppna filen **etc/cas.properties** och redigera följande delar:

Ändra server.name till https://localhost:8443

    ```bash
    server.name=https://localhost:8443
    ```

Avkommentera följande rad och ställ in värdet till true

    ```bash
    cas.logout.followServiceRedirects=true
    ```

Avkommentera följande rad och ställ in värdet till file:/etc/cas/services

    ```bash
    service.registry.config.location=file:/etc/cas/services
    ```

Lägg till dessa rader i LDAP-konfigurationen

**OBS**: Ändra LDAP-värdena så att de matchar din LDAP-inställning.

```bash
#========================================
# LDAP General properties
#========================================
ldap.url=ldap://localhost:10389

# Start TLS for SSL connections
ldap.useStartTLS=false

# Directory root DN
ldap.rootDn=dc=roda,dc=org

# Base DN of users to be authenticated
ldap.baseDn=ou=users,dc=roda,dc=org

# LDAP connection timeout in milliseconds
ldap.connectTimeout=3000

# Manager credential DN
ldap.managerDn=uid=admin,ou=system

# Manager credential password
ldap.managerPassword=roda

#========================================
# LDAP connection pool configuration
#========================================
ldap.pool.minSize=1
ldap.pool.maxSize=10
ldap.pool.validateOnCheckout=false
ldap.pool.validatePeriodically=true

# Amount of time in milliseconds to block on pool exhausted condition
# before giving up.
ldap.pool.blockWaitTime=3000

# Frequency of connection validation in seconds
# Only applies if validatePeriodically=true
ldap.pool.validatePeriod=300

# Attempt to prune connections every N seconds
ldap.pool.prunePeriod=300

# Maximum amount of time an idle connection is allowed to be in
# pool before it is liable to be removed/destroyed
ldap.pool.idleTime=600

#========================================
# LDAP Authentication
#========================================
ldap.authn.searchFilter=uid={user}

# Ldap domain used to resolve dn
ldap.domain=roda.org

# Should LDAP Password Policy be enabled?
ldap.usePpolicy=false

# Allow multiple DNs during authentication?
ldap.allowMultipleDns=false
```

### Skapa en RODA-tjänst i **etc/services/roda.json**

**OBS**: Ändra **serviceId** för att matcha din RODA-adress.

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

### Konfigurera SSL för Jetty

Skapa en keystore i **etc/jetty/thekeystore** med lösenordet **changeit**.

**OBS**: När kommandot frågar "Vad är ditt för- och efternamn?", skriv **localhost**.

```bash
keytool -keystore etc/jetty/thekeystore -alias jetty -genkey -keyalg RSA -sigalg SHA256withRSA
```

### Kopiera konfigurationsfilerna till **/etc/cas**

```bash
sudo mkdir -p /etc/cas
sudo cp -r etc/* /etc/cas
```

### Öppna filen **pom.xml** och lägg till följande beroenden

```xml
<!-- Add support for REST API -->
<dependency>
    <groupId>org.jasig.cas</groupId>
    <artifactId>cas-server-support-rest</artifactId>
    <version>${cas.version}</version>
    <scope>runtime</scope>
</dependency>
```

```xml
<!-- Add support for LDAP authentication -->
<dependency>
    <groupId>org.jasig.cas</groupId>
    <artifactId>cas-server-support-ldap</artifactId>
    <version>${cas.version}</version>
</dependency>
```

### Skapa filen **src/main/webapp/WEB-INF/deployerConfigContext.xml** med följande innehåll:

**OBS**: Ändra **principalIdAttribute** så det matchar attributet hos användarna på din LDAP-server.

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

### Skapa **cas.war** filen

```bash
./mvnw clean package
```

### Starta den inbäddade Jetty-servern

```bash
./mvnw jetty:run-forked
```

För att köra CAS-tjänsten på en annan applikationsserver, installera bara filen **target/cas.war** på den andra applikationsservern.

### Anslut till CAS-servern och logga in med LDAP-uppgifter

Öppna URL:n [https://localhost:8443/cas](https://localhost:8443/cas) och ange dina användaruppgifter för LDAP.
Om användaruppgifterna är giltiga kommer du att se meddelandet "**Inloggning lyckad**".

**OBS**: Certifikatet vi genererade ovan är självsignerat och webbläsaren kommer att klaga på det.
För teständamål kan du ignorera den varningen och acceptera certifikatet.


## Konfigurera RODA för att använda CAS

### Öppna filen **~/.roda/config/roda-wui.properties** och gör följande ändringar:

* Inaktivera interna filter och
* Aktivera CAS-filter,
* Granska konfigurationen för att matcha dina inställningar.

**OBS**: om filen **~/.roda/config/roda-wui.properties** inte finns,
kopiera exempelfilen i **~/.roda/example-config/roda-wui.properties**
till **~/.roda/config/roda-wui.properties** och gör sedan ändringarna.

```bash
...
##############################################
# Internal filters
##############################################

# Disable internal filters
ui.filter.internal.enabled = false
ui.filter.internal.realm = RODA REST API
ui.filter.internal.exclusions = ^/swagger.json,^/v1/theme/?

##############################################
# CAS filters
##############################################

# Enable CAS filters
ui.filter.cas.enabled = true
ui.filter.cas.casServerUrlPrefix = https://localhost:8443/cas
ui.filter.cas.casServerLoginUrl = https://localhost:8443/cas/login
ui.filter.cas.exclusions = ^/swagger.json,^/v1/theme/?,^/v1/auth/ticket?
# RODA base address
ui.filter.cas.serverName = https://localhost:8888
ui.filter.cas.exceptionOnValidationFailure = false
ui.filter.cas.redirectAfterValidation = false
...
```

### Lita på självsignerade certifikat

Om du under inloggningen ser ett undantag som **SSLHandshakeException** eller
**ValidationException**, betyder det förmodligen att RODA har problem med att validera certifikatet från CAS-servern.
Om din CAS-server använder det självsignerade certifikatet som vi skapade ovan, kommer du att se det här felet.

För att fixa det kan du:

1. Använda ett giltigt certifikat, eller
2. Gör så att java-program (som RODA) litar på ditt självsignerade certifikat.

Gör följande steg för att få Java-program att lita på ditt självsignerade certifikat:

#### Exporta det självsignerade certifikatet från jetty

    ```bash
    keytool -export -keystore /etc/cas/jetty/thekeystore -alias jetty -file jetty.cer
    ```

#### Importera jetty-certifikatet i Java truststore

```bash
sudo keytool -import -keystore /usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -alias jetty -file jetty.cer
```

**OBS**: Förutsatt att ditt **JAVA_HOME** är **/usr/lib/jvm/java-8-oracle**.
Är den inte det så justera sökvägen för keystore:n i enlighet med detta.

**När du har importerat certifikatet, starta om RODA.**

## Utvecklingsanteckningar

### Felsök RODA medan CAS är aktivt

Följ instruktionerna för att ställa in CAS och lita på de självsignerade certifikaten, använd sedan följande parametrar för GWT Dev mode:

```
-server :keystore=/etc/cas/jetty/thekeystore,password=changeit -bindAddress 0.0.0.0
```

Anslut till RODA på (https://localhost:8888)[https://localhost:8888].

Om keystore:n `/etc/cas/jetty/thekeystore` innehåller ett enda certifikat (vilket kommer att vara fallet om instruktionerna ovan följdes), kommer det certifikatet att användas av GWT.

### Felsök flera GWT-applikationer som använder CAS

För att felsöka flera GWT-applikationer som använder CAS (t.ex. RODA och databasvisualiseringsverktyget), gäller att varje applikation:

* Måste kunna nås via ett annat hostname (även om de kan köras på samma IP);
* Måste ha ett eget certifikat (för det hostname som används);
* Måste ha ett eget keystore som endast innehåller endast dens certifikatet.

Alla certifikat måste också läggas till i Javas truststore (se avsnittet ovan) för att vissa CAS-funktioner ska fungera.

Webbläsaren måste vara inställd för att tillåta körning av osäkert innehåll (eftersom HTTPS-webbplatsen måste kommunicera med HTTP GWT-kodservern) (t.ex. för chrome så starta applikationen med parametern `--allow-running-insecure-content`)
