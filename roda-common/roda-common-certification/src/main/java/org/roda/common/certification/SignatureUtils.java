package org.roda.common.certification;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;

public class SignatureUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SignatureUtils.class);

  public static boolean isCertificateSelfSigned(Certificate cert) {

    try {
      cert.verify(cert.getPublicKey());
      return true;
    } catch (SignatureException | InvalidKeyException | CertificateException | NoSuchAlgorithmException
      | NoSuchProviderException e) {
      return false;
    }
  }

  public static void verifyCertificateChain(Set<Certificate> trustedRootCerts, Set<Certificate> intermediateCerts,
    X509Certificate cert) {

    try {
      if (trustedRootCerts.size() > 0) {
        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(cert);
        Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
        for (Certificate trustedRootCert : trustedRootCerts) {
          trustAnchors.add(new TrustAnchor((X509Certificate) trustedRootCert, null));
        }

        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, selector);
        pkixParams.setRevocationEnabled(false);
        CertStore intermediateCertStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(
          intermediateCerts), "BC");
        pkixParams.addCertStore(intermediateCertStore);
        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX", "BC");
        builder.build(pkixParams);
      }
    } catch (CertPathBuilderException | NoSuchAlgorithmException | NoSuchProviderException
      | InvalidAlgorithmParameterException e) {
      LOGGER.warn("Certificate chain verification did not run as expected");
    }
  }

}
