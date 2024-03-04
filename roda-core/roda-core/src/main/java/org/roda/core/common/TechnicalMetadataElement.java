/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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