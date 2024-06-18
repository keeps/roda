package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */

public class ConfiguredDescriptiveMetadata implements Serializable {

  @Serial
  private static final long serialVersionUID = -4876421800298148087L;

  private String label;
  private String id;

  public ConfiguredDescriptiveMetadata() {
    // do nothing
  }

  public ConfiguredDescriptiveMetadata(String label, String id) {
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
