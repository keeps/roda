package org.roda.core.common;

import jakarta.xml.bind.annotation.XmlTransient;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */

public class TechnicalMetadataElement {

  @XmlTransient
  private Class subClass;

  @XmlTransient
  public Class getSubClass() {
    return subClass;
  }

  public void setSubClass(Class subClass) {
    this.subClass = subClass;
  }
}