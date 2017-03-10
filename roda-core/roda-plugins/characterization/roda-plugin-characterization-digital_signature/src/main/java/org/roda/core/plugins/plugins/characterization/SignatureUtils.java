/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

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

public final class SignatureUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SignatureUtils.class);

  /** Private empty constructor */
  private SignatureUtils() {

  }

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
      if (!trustedRootCerts.isEmpty()) {
        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(cert);
        Set<TrustAnchor> trustAnchors = new HashSet<>();
        for (Certificate trustedRootCert : trustedRootCerts) {
          trustAnchors.add(new TrustAnchor((X509Certificate) trustedRootCert, null));
        }

        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, selector);
        pkixParams.setRevocationEnabled(false);
        CertStore intermediateCertStore = CertStore.getInstance("Collection",
          new CollectionCertStoreParameters(intermediateCerts), "BC");
        pkixParams.addCertStore(intermediateCertStore);
        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX", "BC");
        builder.build(pkixParams);
      }
    } catch (CertPathBuilderException | NoSuchAlgorithmException | NoSuchProviderException
      | InvalidAlgorithmParameterException e) {
      LOGGER.warn("Certificate chain verification did not run as expected");
    }
  }

  public static String canHaveEmbeddedSignature(String fileFormat, String mimetype) {
    if ("pdf".equals(fileFormat) || "application/pdf".equals(mimetype)) {
      return "pdf";
    } else if ("docx".equals(fileFormat) || "xlsx".equals(fileFormat) || "pptx".equals(fileFormat)
      || "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimetype)
      || "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(mimetype)
      || "application/vnd.openxmlformats-officedocument.presentationml.presentation".equals(mimetype)) {
      return "ooxml";
    } else if ("odt".equals(fileFormat) || "ods".equals(fileFormat) || "odp".equals(fileFormat)
      || "application/vnd.oasis.opendocument.text".equals(mimetype)
      || "application/vnd.oasis.opendocument.spreadsheet".equals(mimetype)
      || "application/vnd.oasis.opendocument.presentation".equals(mimetype)) {
      return "odf";
    }

    return "";
  }
}
