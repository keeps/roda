/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.certificate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.CodeSigner;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.jobs.CertificateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class PluginCertificateUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(PluginCertificateUtils.class);

  public static CertificateInfo loadAndCheckCertificates(Path jarPath) throws IOException {
    JarFile jar = new JarFile(jarPath.toFile());
    Enumeration<JarEntry> entries = jar.entries();
    CertificateInfo certificateInfo = new CertificateInfo();
    try {
      CompositeX509TrustManager trustManager = loadTrustManager();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        // Ignore signature related files
        if (entry.getName().matches("^META-INF/(.*)[.](SF|RSA|DSA)$") || entry.isDirectory()) {
          continue;
        }
        byte buffer[] = new byte[8192];
        try (InputStream is = jar.getInputStream(entry)) {
          // need to read the entry's data to see the certs.
          while (is.read(buffer) != -1)
            ;
          Certificate[] certificates = entry.getCertificates();

          CodeSigner[] signers = entry.getCodeSigners();

          if (signers != null && certificates != null) {
            X509Certificate[] x509Certificates = Arrays.copyOf(certificates, certificates.length,
              X509Certificate[].class);

            X509Certificate pluginCertificate = x509Certificates[0];

            String issuerDN = pluginCertificate.getIssuerDN().getName();
            String subjectDN = pluginCertificate.getSubjectDN().getName();
            Date notBefore = pluginCertificate.getNotBefore();
            Date notAfter = pluginCertificate.getNotAfter();

            certificateInfo.addCertificates(issuerDN, subjectDN, notBefore, notAfter);
            try {
              trustManager.checkServerTrusted(x509Certificates,
                RodaConstants.PLUGINS_CERTIFICATE_DEFAULT_TRUSTSTORE_AUTH_TYPE);
              certificateInfo.setCertificateStatus(CertificateInfo.CertificateStatus.VERIFIED);
            } catch (CertificateException e) {
              LOGGER.error(e.getMessage(), e);
              certificateInfo.setCertificateStatus(CertificateInfo.CertificateStatus.NOT_VERIFIED);
              break;
            }
          } else {
            LOGGER.error("Plugin '{}' does not contain certificates", jarPath.getFileName());
            certificateInfo.setCertificateStatus(CertificateInfo.CertificateStatus.NOT_VERIFIED);
            break;
          }
        }
      }
    } catch (PluginCertificateException e) {
      LOGGER.error("error while checking certificates", e);
      certificateInfo.setCertificateStatus(CertificateInfo.CertificateStatus.NOT_VERIFIED);
    }
    return certificateInfo;
  }

  private static CompositeX509TrustManager loadTrustManager() throws PluginCertificateException {
    try {
      // RODA truststore
      KeyStore rodaTrustStore = KeyStore.getInstance(RodaConstants.PLUGINS_CERTIFICATE_RODA_TRUSTSTORE_TYPE);
      String rodaTrustStorePath = RodaCoreFactory.getConfigPath()
        .resolve(RodaConstants.PLUGINS_CERTIFICATE_DEFAULT_TRUSTSTORE_PATH
          + RodaConstants.PLUGINS_CERTIFICATE_RODA_TRUSTSTORE_NAME)
        .toString();
      rodaTrustStore.load(PluginCertificateUtils.class.getResourceAsStream(rodaTrustStorePath),
        RodaConstants.PLUGINS_CERTIFICATE_RODA_TRUSTSTORE_PASS.toCharArray());

      List<X509TrustManager> allTrustManagers = new ArrayList<>(
        getTrustManager(rodaTrustStore, TrustManagerFactory.getDefaultAlgorithm()));

      // Custom truststore
      if (RodaCoreFactory.getProperty(RodaConstants.PLUGINS_CERTIFICATE_CUSTOM_TRUSTSTORE_ENABLE_PROPERTY, false)) {
        String customTrustStoreType = getMandatoryCertificateProperty(
          RodaConstants.PLUGINS_CERTIFICATE_CUSTOM_TRUSTSTORE_TYPE_PROPERTY);
        String customName = getMandatoryCertificateProperty(
          RodaConstants.PLUGINS_CERTIFICATE_CUSTOM_TRUSTSTORE_NAME_PROPERTY);
        String customPass = getMandatoryCertificateProperty(
          RodaConstants.PLUGINS_CERTIFICATE_CUSTOM_TRUSTSTORE_PASS_PROPERTY);

        String customTrustStorePath = RodaConstants.PLUGINS_CERTIFICATE_CUSTOM_TRUSTSTORE_FOLDER + customName;
        KeyStore customTrustStore = KeyStore.getInstance(customTrustStoreType);
        customTrustStore.load(RodaCoreFactory.getConfigurationFileAsStream(customTrustStorePath),
          customPass.toCharArray());
        allTrustManagers.addAll(getTrustManager(customTrustStore, TrustManagerFactory.getDefaultAlgorithm()));
      }

      return new CompositeX509TrustManager(allTrustManagers);
    } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
      throw new PluginCertificateException("Error while loading truststore", e);
    }
  }

  private static String getMandatoryCertificateProperty(String propertyKey) throws PluginCertificateException {
    String property = RodaCoreFactory.getProperty(propertyKey, "");
    if (property.isEmpty()) {
      throw new PluginCertificateException("Mandatory property not defined: " + propertyKey);
    }
    return property;
  }

  private static List<X509TrustManager> getTrustManager(final KeyStore keyStore, final String algorithm)
    throws NoSuchAlgorithmException, KeyStoreException {
    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
    trustManagerFactory.init(keyStore);

    return Arrays.stream(trustManagerFactory.getTrustManagers()).filter(X509TrustManager.class::isInstance)
      .map(X509TrustManager.class::cast).collect(Collectors.toList());
  }
}
