package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */

@JsonInclude(JsonInclude.Include.ALWAYS)
public class SupportedMetadata implements Serializable {

  private String label;
  private String id;

  public SupportedMetadata() {
    // do nothing
  }

  public SupportedMetadata(String label, String id) {
    this.label = label;
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "SupportedMetadata{" + "label='" + label + '\'' + ", id='" + id + '\'' + '}';
  }
}
