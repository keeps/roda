package org.roda.core.data.v2.ip.disposalhold;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import org.roda.core.data.v2.generics.select.SelectedItemsRequest;

public class ApplyDisposalHoldRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = 1234567890123456789L;

  private SelectedItemsRequest selectedItems;
  private List<String> holdIds;
  private boolean override;

  public ApplyDisposalHoldRequest() {
  }

  public SelectedItemsRequest getSelectedItems() {
    return selectedItems;
  }

  public void setSelectedItems(SelectedItemsRequest selectedItems) {
    this.selectedItems = selectedItems;
  }

  public List<String> getHoldIds() {
    return holdIds;
  }

  public void setHoldIds(List<String> holdIds) {
    this.holdIds = holdIds;
  }

  public boolean isOverride() {
    return override;
  }

  public void setOverride(boolean override) {
    this.override = override;
  }
}
