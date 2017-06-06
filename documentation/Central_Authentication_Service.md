# Central Authentication Service (CAS)

RODA has support for [Central Authentication Service](https://en.wikipedia.org/wiki/Central_Authentication_Service) (**CAS**).
These instructions are for installing the CAS service provided by [Apereo](https://www.apereo.org/projects/cas).

## Install and run CAS with LDAP

For these instructions, we're assuming the LDAP server has the following characteristics:

* **URL**: ldap://localhost:10389
* **Base DN**: dc=roda,dc=org
* **Users DN**: ou=users,dc=roda,dc=org
* **Admin DN**: uid=admin,ou=system
* **Admin password**: roda
* **User unique attribute**: uid

Change the values to match your LDAP.

### Get CAS Overlay Template

    ```
    git clone -b 4.2 https://github.com/apereo/cas-overlay-template.git
    cd cas-overlay-template
    ```

### Open file **etc/cas.properties** and edit the following parts:

Change server.name to https://localhost:8443

    ```bash
    server.name=https://localhost:8443
    ```

Uncomment the following line and set the value to true

    ```bash
    cas.logout.followServiceRedirects=true
    ```

Uncomment the following line and set the value to file:/etc/cas/services

    ```bash
    service.registry.config.location=file:/etc/cas/services
    ```

Add these lines with LDAP configuration

**NOTE**: Change the LDAP values to match your LDAP setup.

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

### Create a RODA service in **etc/services/roda.json**

**NOTE**: Change the **serviceId** to match your RODA address.

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

### Setup SSL for Jetty

Create a keystore at **etc/jetty/thekeystore** with the password **changeit**.
    
**NOTE**: When the command asks "What is your first and last name?", write **localhost**.
    
```bash
keytool -keystore etc/jetty/thekeystore -alias jetty -genkey -keyalg RSA -sigalg SHA256withRSA
```

### Copy configuration files to **/etc/cas**

```bash
sudo mkdir -p /etc/cas
sudo cp -r etc/* /etc/cas
```

### Open file **pom.xml** and add the following dependencies

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

### Create file **src/main/webapp/WEB-INF/deployerConfigContext.xml** with the following contents:

**NOTE**: Change the **principalIdAttribute** to match the attribute of users on your LDAP server.

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

### Build the **cas.war** file

```bash
./mvnw clean package
```

### Start the embedded Jetty server

```bash
./mvnw jetty:run-forked
```

To run the CAS service in another application server, just install the file **target/cas.war** in your application server.

### Access the CAS server and login with LDAP credentials

Open URL [https://localhost:8443/cas](https://localhost:8443/cas) and enter your user LDAP credentials.
If the credentials are valid you'll see a message "**Log In Successful**".

**NOTE**: The certificate we generated above is self-signed and the browser will complaint about that. 
For testing purposes you can ignore that warning and accept the certificate.
    

## Setup RODA to use CAS

### Open file **~/.roda/config/roda-wui.properties** and make the following changes:

* Disable internal filters and
* Enable CAS filters,
* Review the configuration values to match your setup.

**NOTE**: if file **~/.roda/config/roda-wui.properties** doesn't exist,
 copy the sample file in **~/.roda/example-config/roda-wui.properties** 
 to **~/.roda/config/roda-wui.properties** and then make the changes.

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

### Trust self-signed certificates

If during the login you see an exception like **SSLHandshakeException** or 
**ValidationException**, it probably means RODA is having issues validating the certificate from the CAS server.
If your CAS server is using the self-signed certificate we created above, you'll see this error.

To fix it, you can:

1. Use a valid certificate, or
2. Make java programs (like RODA) trust your self-signed certificate.

To make java programs trust your self-signed certificate do the following steps:

#### Export jetty self-signed certificate

    ```bash
    keytool -export -keystore /etc/cas/jetty/thekeystore -alias jetty -file jetty.cer
    ```

#### Import jetty certificate in Java truststore

```bash
sudo keytool -import -keystore /usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -alias jetty -file jetty.cer
```

**NOTE**: Assuming your **JAVA_HOME** is **/usr/lib/jvm/java-8-oracle**. 
If it's not, adjust the keystore path accordingly.

**After successfully import the certificate, restart RODA.**

## Development notes

### Debug RODA while CAS is active

Follow the instructions to set up CAS and trust the self-signed certificates, then use the following GWT Dev mode parameters:

```
-server :keystore=/etc/cas/jetty/thekeystore,password=changeit -bindAddress 0.0.0.0
```

And access RODA on (https://localhost:8888)[https://localhost:8888].

If the keystore `/etc/cas/jetty/thekeystore` contains a single certificate (which will be the case when the instructions above were followed), that certificate will be used by GWT.

### Debug multiple GWT applications that use CAS

To debug multiple GWT applications that use CAS (eg. RODA and the database visualization toolkit), each application:

* Must be accessed through a different hostname (although they can be running on the same IP);
* Must have its own certificate (for the hostname in use);
* Must have its own keystore containing only that certificate.

All the certificates must also be added to the Java truststore (see section above) for some CAS features to work.

The browser must be set to allow running insecure content (because the HTTPS website needs to communicate with the HTTP GWT code server) (eg. for chrome start it with the parameter `--allow-running-insecure-content`)
