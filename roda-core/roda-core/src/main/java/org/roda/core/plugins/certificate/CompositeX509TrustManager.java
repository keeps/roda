/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.certificate;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CompositeX509TrustManager implements X509TrustManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(CompositeX509TrustManager.class);
  private final List<X509TrustManager> trustManagers;

  public CompositeX509TrustManager(List<X509TrustManager> trustManagers) {
    this.trustManagers = trustManagers;
  }

  @Override
  public void checkClientTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
    boolean isTrusted = trustManagers.stream().anyMatch(trustManager -> {
      try {
        trustManager.checkClientTrusted(x509Certificates, authType);
        return true;
      } catch (CertificateException e) {
        LOGGER.debug("Unable to trust the client certificates ", e);
        return false;
      }
    });

    if (!isTrusted) {
      throw new CertificateException("None of the TrustManagers can trust this client certificate chain");
    }
  }

  @Override
  public void checkServerTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
    boolean isTrusted = trustManagers.stream().anyMatch(trustManager -> {
      try {
        trustManager.checkServerTrusted(x509Certificates, authType);
        return true;
      } catch (CertificateException e) {
        LOGGER.debug("Unable to trust the server certificates ", e);
        return false;
      }
    });

    if (!isTrusted) {
      throw new CertificateException("None of the TrustManagers can trust this client certificate chain");
    }
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    ArrayList<X509Certificate> x509Certificates = new ArrayList<>(trustManagers.size());
    for (X509TrustManager trustManager : trustManagers) {
      x509Certificates.addAll(Arrays.asList(trustManager.getAcceptedIssuers()));
    }
    return x509Certificates.toArray(X509Certificate[]::new);
  }
}
