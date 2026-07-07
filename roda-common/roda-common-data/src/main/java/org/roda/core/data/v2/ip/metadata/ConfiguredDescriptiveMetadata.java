/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
  private String type;
  private String version;

  public ConfiguredDescriptiveMetadata() {
    // do nothing
  }

  public ConfiguredDescriptiveMetadata(String label, String id, String type, String version) {
    this.label = label;
    this.id = id;
    this.type = type;
    this.version = version;
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return "ConfiguredDescriptiveMetadata{" + "label='" + label + '\'' + ", id='" + id + '\'' + ", type='" + type + '\''
      + ", version='" + version + '\'' + '}';
  }
}
