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

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class DescriptiveMetadataInfo implements Serializable {
  @Serial
  private static final long serialVersionUID = -9057023345058040380L;

  private String id;
  private String label;
  private boolean hasHistory;

  public DescriptiveMetadataInfo() {
    // empty constructor
  }

  public DescriptiveMetadataInfo(String id, String label, boolean hasHistory) {
    this.id = id;
    this.label = label;
    this.hasHistory = hasHistory;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public boolean isHasHistory() {
    return hasHistory;
  }

  public void setHasHistory(boolean hasHistory) {
    this.hasHistory = hasHistory;
  }
}
