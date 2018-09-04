/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.bundle;

public class DescriptiveMetadataViewBundle implements Bundle {

  private static final long serialVersionUID = 515251862250083594L;

  private String id;
  private String label;
  private boolean hasHistory;

  public DescriptiveMetadataViewBundle() {
    super();
  }

  public DescriptiveMetadataViewBundle(String id, String label, boolean hasHistory) {
    super();
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

  public boolean hasHistory() {
    return hasHistory;
  }

  public void setHasHistory(boolean hasHistory) {
    this.hasHistory = hasHistory;
  }
}
