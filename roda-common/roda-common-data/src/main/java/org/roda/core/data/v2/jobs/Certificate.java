package org.roda.core.data.v2.jobs;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Certificate implements Serializable {
  @Serial
  private static final long serialVersionUID = -4981616458177975741L;
  private String issuerDN = null;
  private String subjectDN = null;
  private Date notBefore = null;
  private Date notAfter = null;

  public String getIssuerDN() {
    return issuerDN;
  }

  public void setIssuerDN(String issuerDN) {
    this.issuerDN = issuerDN;
  }

  public String getSubjectDN() {
    return subjectDN;
  }

  public void setSubjectDN(String subjectDN) {
    this.subjectDN = subjectDN;
  }

  public Date getNotBefore() {
    return notBefore;
  }

  public void setNotBefore(Date notBefore) {
    this.notBefore = notBefore;
  }

  public Date getNotAfter() {
    return notAfter;
  }

  public void setNotAfter(Date notAfter) {
    this.notAfter = notAfter;
  }

  public String getOrganizationName(String dn) {
    return getCertificateInfo(dn, "O");
  }

  public String getCommonName(String dn) {
    return getCertificateInfo(dn, "CN");
  }

  private String getCertificateInfo(String dn, String field) {
    for (String each : dn.split(",\\s")) {
      if (each.startsWith(field + "=")) {
        return each.substring(field.length() + 1);
      }
    }
    return "NOT FOUND";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Certificate that = (Certificate) o;
    return Objects.equals(issuerDN, that.issuerDN) && Objects.equals(subjectDN, that.subjectDN)
      && Objects.equals(notBefore, that.notBefore) && Objects.equals(notAfter, that.notAfter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(issuerDN, subjectDN, notBefore, notAfter);
  }
}
