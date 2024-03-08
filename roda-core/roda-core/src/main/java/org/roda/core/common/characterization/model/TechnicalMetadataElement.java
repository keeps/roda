package org.roda.core.common.characterization.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TechnicalMetadataElement {

  @XmlElement(name = "field")
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
