/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CertificateInfo implements Serializable {
  @Serial
  private static final long serialVersionUID = 5755199988566186742L;

  public enum CertificateStatus {
    INTERNAL, VERIFIED, NOT_VERIFIED
  }

  private CertificateStatus certificateStatus = CertificateStatus.INTERNAL;
  private Set<Certificate> certificates = new HashSet<>();

  public CertificateStatus getCertificateStatus() {
    return certificateStatus;
  }

  public void setCertificateStatus(CertificateStatus status) {
    certificateStatus = status;
  }

  public Set<Certificate> getCertificates() {
    return certificates;
  }

  public void setCertificates(Set<Certificate> certificates) {
    this.certificates = certificates;
  }

  @JsonIgnore
  public void addCertificates(String issuer, String subject, Date notBefore, Date notAfter) {
    Certificate certificate = new Certificate();
    certificate.setIssuerDN(issuer);
    certificate.setSubjectDN(subject);
    certificate.setNotBefore(notBefore);
    certificate.setNotAfter(notAfter);
    this.certificates.add(certificate);
  }

  @JsonIgnore
  public boolean isNotVerified() {
    return getCertificateStatus().equals(CertificateStatus.NOT_VERIFIED);
  }
}
