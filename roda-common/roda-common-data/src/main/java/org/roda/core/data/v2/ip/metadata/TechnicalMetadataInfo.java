package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class TechnicalMetadataInfo implements Serializable {

  @Serial
  private static final long serialVersionUID = 1396446726755603123L;

  private String typeId;
  private String label;

  public TechnicalMetadataInfo() {
    // empty constructor
  }

  public TechnicalMetadataInfo(String typeId, String label) {
    this.typeId = typeId;
    this.label = label;
  }

  public String getTypeId() {
    return typeId;
  }

  public void setTypeId(String typeId) {
    this.typeId = typeId;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
}
