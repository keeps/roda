package org.roda.core.data.eadc;

import java.io.Serializable;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class Unitid implements Serializable {

  private static final long serialVersionUID = 3094680181663919365L;

  private String text = null;
  private String attributeAltrender = null;
  private String attributeRepositorycode = null;

  /** Constructs a new empty {@link Unitid} */
  public Unitid() {

  }

  /**
   * Constructs a new {@link Unitid} using the provided parameters
   * 
   * @param text
   * @param attributeAltrender
   * @param attributeRepositorycode
   */
  public Unitid(String text, String attributeAltrender, String attributeRepositorycode) {
    this.text = text;
    this.attributeAltrender = attributeAltrender;
    this.attributeRepositorycode = attributeRepositorycode;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getAttributeAltrender() {
    return attributeAltrender;
  }

  public void setAttributeAltrender(String attributeAltrender) {
    this.attributeAltrender = attributeAltrender;
  }

  public String getAttributeRepositorycode() {
    return attributeRepositorycode;
  }

  public void setAttributeRepositorycode(String attributeRepositorycode) {
    this.attributeRepositorycode = attributeRepositorycode;
  }

  @Override
  public String toString() {
    return "Unitid [text=" + text + ", attributeAltrender=" + attributeAltrender + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((attributeAltrender == null) ? 0 : attributeAltrender.hashCode());
    result = prime * result + ((text == null) ? 0 : text.hashCode());
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
    if (!(obj instanceof Unitid)) {
      return false;
    }
    Unitid other = (Unitid) obj;
    if (attributeAltrender == null) {
      if (other.attributeAltrender != null) {
        return false;
      }
    } else if (!attributeAltrender.equals(other.attributeAltrender)) {
      return false;
    }
    if (text == null) {
      if (other.text != null) {
        return false;
      }
    } else if (!text.equals(other.text)) {
      return false;
    }
    return true;
  }

}
