package org.roda.core.data.eadc;

import java.io.Serializable;

public class Acqinfo implements Serializable {

  private static final long serialVersionUID = 6996274684773847183L;

  private String attributeAltrender = null;
  private P p = null;

  /**
   * Constructs an new empty {@link Acqinfo}
   * */
  public Acqinfo() {

  }

  /**
   * Constructs an new {@link Acqinfo} using provided parameters
   * */
  public Acqinfo(P p, String attributeAltrender) {
    this.p = p;
    this.attributeAltrender = attributeAltrender;
  }

  public Acqinfo(String p, String attributeAltrender) {
    this.p = new P(null, p, null, null, null, null, null);
    this.attributeAltrender = attributeAltrender;
  }

  public P getP() {
    return p;
  }

  public void setP(P p) {
    this.p = p;
  }

  public String getAttributeAltrender() {
    return attributeAltrender;
  }

  public void setAttributeAltrender(String attributeAltrender) {
    this.attributeAltrender = attributeAltrender;
  }

  @Override
  public String toString() {
    return "Acqinfo [attributeAltrender=" + attributeAltrender + ", p=" + p + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((attributeAltrender == null) ? 0 : attributeAltrender.hashCode());
    result = prime * result + ((p == null) ? 0 : p.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Acqinfo)) {
      return false;
    }
    Acqinfo other = (Acqinfo) obj;
    if (attributeAltrender == null) {
      if (other.attributeAltrender != null) {
        return false;
      }
    } else if (!attributeAltrender.equals(other.attributeAltrender)) {
      return false;
    }
    if (p == null) {
      if (other.p != null) {
        return false;
      }
    } else if (!p.equals(other.p)) {
      return false;
    }
    return true;
  }

}
