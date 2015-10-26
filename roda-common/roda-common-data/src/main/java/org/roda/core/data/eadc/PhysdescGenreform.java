/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.eadc;

import java.io.Serializable;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class PhysdescGenreform implements EadCValue, Serializable {

  private static final long serialVersionUID = 2173846831654370270L;

  private String attributeSource = null;
  private String attributeAuthfilenumber = null;
  private String attributeNormal = null;
  private String text = null;

  /**
   * Constructs a new empty {@link PhysdescGenreform}
   * */
  public PhysdescGenreform() {

  }

  /**
   * Constructs a new {@link PhysdescGenreform} using the provided parameters
   * 
   * @param attributeSource
   * @param attributeAuthfilenumber
   * @param attributeNormal
   * @param text
   */
  public PhysdescGenreform(String attributeSource, String attributeAuthfilenumber, String attributeNormal, String text) {
    this.attributeSource = attributeSource;
    this.attributeAuthfilenumber = attributeAuthfilenumber;
    this.attributeNormal = attributeNormal;
    this.text = text;
  }

  public String getAttributeSource() {
    return attributeSource;
  }

  public void setAttributeSource(String attributeSource) {
    this.attributeSource = attributeSource;
  }

  public String getAttributeAuthfilenumber() {
    return attributeAuthfilenumber;
  }

  public void setAttributeAuthfilenumber(String attributeAuthfilenumber) {
    this.attributeAuthfilenumber = attributeAuthfilenumber;
  }

  public String getAttributeNormal() {
    return attributeNormal;
  }

  public void setAttributeNormal(String attributeNormal) {
    this.attributeNormal = attributeNormal;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return "PhysdescGenreform [attributeSource=" + attributeSource + ", attributeAuthfilenumber="
      + attributeAuthfilenumber + ", attributeNormal=" + attributeNormal + ", text=" + text + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((attributeAuthfilenumber == null) ? 0 : attributeAuthfilenumber.hashCode());
    result = prime * result + ((attributeNormal == null) ? 0 : attributeNormal.hashCode());
    result = prime * result + ((attributeSource == null) ? 0 : attributeSource.hashCode());
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
    if (!(obj instanceof PhysdescGenreform)) {
      return false;
    }
    PhysdescGenreform other = (PhysdescGenreform) obj;
    if (attributeAuthfilenumber == null) {
      if (other.attributeAuthfilenumber != null) {
        return false;
      }
    } else if (!attributeAuthfilenumber.equals(other.attributeAuthfilenumber)) {
      return false;
    }
    if (attributeNormal == null) {
      if (other.attributeNormal != null) {
        return false;
      }
    } else if (!attributeNormal.equals(other.attributeNormal)) {
      return false;
    }
    if (attributeSource == null) {
      if (other.attributeSource != null) {
        return false;
      }
    } else if (!attributeSource.equals(other.attributeSource)) {
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
