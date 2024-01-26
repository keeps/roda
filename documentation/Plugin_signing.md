# Signed Plugins

The introduction of the digital signature verification feature for plugins in RODA represents an advanced approach to
ensuring the security, integrity, and authenticity of the plugins installed in the repository. All plugins published on
the [https://market.roda-community.org/](https://market.roda-community.org) are certified with a digital signature, and
the authenticity validation is performed by RODA through its internal truststore.

## Trusting External Sources

RODA provides the flexibility to configure a custom truststore, allowing advanced users to install plugins from an
external source they consider trustworthy. To achieve this, the following properties must be modified in the
roda-core.properties file:

```
core.plugins.external.certificates.custom.truststore.enable = true
core.plugins.external.certificates.custom.truststore.type = PKCS12
core.plugins.external.certificates.custom.truststore.name = custom-truststore.p12
core.plugins.external.certificates.custom.truststore.pass = changeit
```

The custom truststore provided by the external source should be placed in:
`$RODA_HOME/config/market/truststore/custom-truststore.p12`

## Providing trusted plugins

For developers who want their plugins to be integrated and recognized as trusted in specific RODA installations without
the need to publish them on the [https://market.roda-community.org/](https://market.roda-community.org), the following
procedures should be followed:

1. Create a keystore and key pair for the certificate authority:

```bash
# Note that the CN of this certificate should identify who develops the plugins.
keytool -genkeypair -keyalg RSA -validity 365 -alias ca -keystore keystore.p12 -dname "CN=Developer Name, O=Developer Name" -storepass <changeit> -keypass <changeit> -ext bc=ca:true
```

2. Extract the certificate of the certificate authority from the keystore

```bash
keytool -export -keystore keystore.p12 -storepass <changeit> -alias ca -file ca.crt
```

3. Create the custom truststore with the certificates from the certificate authority

```bash
keytool -importcert -keystore custom-truststore.p12 -storepass <changeit> -file ca.crt -alias licence -noprompt
```

4. Create a keystore and key pair for the plugin

```bash
# The CN of this certificate will identify to which institution the plugins will be licensed to.
keytool -genkeypair -keyalg RSA -validity 365 -alias licence -keystore keystore.p12 -dname "CN=User institution name, O=User institution name" -storepass <changeit> -keypass <changeit>
```

5. Create a certificate request for the plugin certificate

```bash
keytool -certreq -keystore keystore.p12 -storepass <changeit> -alias licence -file licence.csr
```

6. Create the signed plugin certificate using the certificate authority keystore

```bash
keytool -gencert -keystore keystore.p12 -storepass <changeit> -alias ca -infile licence.csr -outfile licence.crt
```

7. Add the certificate to the plugin keystore

```bash
keytool -importcert -keystore keystore.p12 -storepass <changeit> -file licence.crt -alias licence
```

8. Sign your plugin.

```bash
jarsigner -keystore keystore.p12 -storepass <changeit> -signedjar "roda-plugin-example-1.0.0-signed.jar"  "roda-plugin-example-1.0.0.jar" licence
```

9. Remove the unsigned plugin from the plugin package and install the plugin in RODA.

This can be done as an intermediate step between maven compilation of a plugin and the docker build of the RODA image in
a build script:

https://github.com/keeps/roda-plugin-template/blob/master/build.sh

By changing the last lines to:

```bash
# Build plugin
mvn clean package -Dmaven.test.skip -Denforcer.skip

# Sign
jarsigner -keystore signing-keystore.p12 -storepass password -signedjar target/${PLUGIN}/${PLUGIN}-1.0.0-signed.jar target/${PLUGIN}/${PLUGIN}-1.0.0.jar
rm -f target/${PLUGIN}/${PLUGIN}-1.0.0.jar

# Build RODA with plugin
docker build $RODA_VERSION_ARG --build-arg PLUGIN=${PLUGIN} -t ${PLUGIN}  .
```