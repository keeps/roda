/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.characterization.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.loc.gov/premis/v3")
public class TechnicalMetadataElement {

  @XmlElement(name = "field", namespace = "http://www.loc.gov/premis/v3")
  private List<TechnicalMetadataField> technicalMetadataFields = new ArrayList<>();

  public TechnicalMetadataElement() {
    // empty constructor
  }

  public List<TechnicalMetadataField> getTechnicalMetadataFields() {
    return technicalMetadataFields;
  }

  public void setTechnicalMetadataFields(List<TechnicalMetadataField> technicalMetadataFields) {
    this.technicalMetadataFields = technicalMetadataFields;
  }


}
