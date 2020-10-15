# Central Authentication Service (CAS)

O RODA suporta o [Central Authentication Service](https://en.wikipedia.org/wiki/Central_Authentication_Service) (**CAS**).
Estas instruções são para a instalação do serviço CAS fornecido pela [Apereo](https://www.apereo.org/projects/cas).

## Instale e execute o CAS com o LDAP

Para estas instruções, estamos a assumir que o servidor LDAP tem as seguintes características:

* **URL**: ldap://localhost:10389
* **Base DN**: dc=roda,dc=org
* **Users DN**: ou=users,dc=roda,dc=org
* **Admin DN**: uid=admin,ou=system
* **Admin password**: roda
* **User unique attribute**: uid

Altere os valores para corresponderem ao seu LDAP.

### Adquira o Template de Overlay do CAS

    ```
    git clone -b 4.2 https://github.com/apereo/cas-overlay-template.git
    cd cas-overlay-template
    ```

### Abra o ficheiro **etc/cas.properties** e edite a seguintes partes:

Altere o server.name para https://localhost:8443

    ```bash
    server.name=https://localhost:8443
    ```

Retire as marcas de comentário da seguinte linha e defina o valor como true

    ```bash
    cas.logout.followServiceRedirects=true
    ```

Retire as marcas de comentário da seguinte linha e defina o valor como file:/etc/cas/services

    ```bash
    service.registry.config.location=file:/etc/cas/services
    ```

Adicione estas linhas com a configuração LDAP

**NOTA**: Altere os valores do LDAP para corresponderem às suas definições de LDAP. 

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

### Crie um serviço do RODA em **etc/services/roda.json**

**NOTA**: Altere o **serviceId** para corresponder ao seu endereço do RODA.

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

### Prepare o SSL para o Jetty

Crie uma keystore em **etc/jetty/thekeystore** com a password **changeit**.

**NOTA**: Quando o comando perguntar "Qual é o seu primeiro e último nome?", escreva **localhost**.

```bash
keytool -keystore etc/jetty/thekeystore -alias jetty -genkey -keyalg RSA -sigalg SHA256withRSA
```

### Copie os ficheiros de configuração para **/etc/cas**

```bash
sudo mkdir -p /etc/cas
sudo cp -r etc/* /etc/cas
```

### Abra o ficheiro **pom.xml** e adicione as seguintes dependências

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

### Crie o ficheiro **src/main/webapp/WEB-INF/deployerConfigContext.xml** com os seguintes conteúdos:

**NOTA**: Altere o **principalIdAttribute** para corresponder ao atributo de utilizadores no seu servidor LDAP.

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

### Compile o ficheiro **cas.war**

```bash
./mvnw clean package
```

### Inicie o servidor Jetty integrado

```bash
./mvnw jetty:run-forked
```

Para executar o serviço CAS noutro servidor de aplicações, simplesmente instale o ficheiro **target/cas.war** no seu servidor aplicacional.

### Aceda ao servidor CAS e inicie sessão com as credenciais LDAP

Abra o URL [https://localhost:8443/cas](https://localhost:8443/cas) e introduza as suas credenciais LDAP de utilizador.
Se as credenciais forem válidas, verá a mensagem "**Log In Successful**".

**NOTA**: O certificado que geramos acima utiliza a tecnologia de assinatura automática e o browser irá reclamar com isso.
Para fins de teste, pode ignorar esse aviso e aceitar o certificado.


## Prepare o RODA para usar o CAS

### Abra o ficheiro **~/.roda/config/roda-wui.properties** e faça as seguintes alterações:

* Desative os filtros internos e
* Ative os filtros do CAS,
* Reveja os valores de configuração para estes corresponderem às suas definições.

**NOTA**: se o ficheiro **~/.roda/config/roda-wui.properties** não existir,
 copie o ficheiro de amostra em **~/.roda/example-config/roda-wui.properties** 
 para **~/.roda/config/roda-wui.properties** e depois faça as alterações.

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

### Confie em certificados auto-assinados

Se durante o login surgir uma exceção como **SSLHandshakeException** ou 
**ValidationException**, provavelmente significa que o RODA está a ter problemas com a validação do certificado do servidor CAS.
Se o seu servidor CAS estiver a usar o certificado auto-assinado que criamos acima, surgirá este erro.

Para corrigi-lo, pode:

1. Usar um certificado válido, ou
2. Fazer com que programas java (como o RODA) confiem no seu certificado auto-assinado.

Para fazer com que programas java confiem no seu certificado auto-assinado, siga os seguintes passos:

#### Exporte o certificado auto-assinado jetty

    ```bash
    keytool -export -keystore /etc/cas/jetty/thekeystore -alias jetty -file jetty.cer
    ```

#### Importe o certificado jetty no Java truststore

```bash
sudo keytool -import -keystore /usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -alias jetty -file jetty.cer
```

**NOTA**: Assumindo que a sua **JAVA_HOME** é **/usr/lib/jvm/java-8-oracle**. 
Caso não o seja, ajuste o diretório da keystore em conformidade.

**Depois de importar o certificado com sucesso, reinicie o RODA.**

## Notas de desenvolvimento

### Faça o debug do RODA enquanto o CAS estiver ativo

Siga as instruções para preparar o CAS e confie nos certificados auto-assinados, depois use os seguintes parâmetros de Dev mode do GWT:

```
-server :keystore=/etc/cas/jetty/thekeystore,password=changeit -bindAddress 0.0.0.0
```

E aceda ao RODA em (https://localhost:8888)[https://localhost:8888].

Se a keystore `/etc/cas/jetty/thekeystore` contiver um único certificado (que será o caso se as instruções acima tiverem sido seguidas), esse certificado será usado pelo GWT.

### Faça o debug das múltiplas aplicações do GWT que usam CAS

Para fazer o debug das múltiplas aplicações do GWT que usam CAS (ex. o RODA e o database visualization toolkit), cada aplicação:

* Deve ser acedida utilizando diferentes hostnames (no entanto, podem ser executadas no mesmo IP);
* Deve ter o seu próprio certificado (para o hostname em uso);
* Deve ter a sua própria keystore que contém apenas esse certificado.

Todos os certificados devem também ser adicionados à Java truststore (ver secção acima) para que algumas funcionalidades do CAS funcionem.

O browser deve ser ajustado para permitir a execução de conteúdo inseguro (porque o website HTTPS precisa de comunicar com o servidor de código HTTP do GWT) (ex. para o chrome, inicie com o parâmetro `--allow-running-insecure-content`)
