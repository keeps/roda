package org.roda.core.data.v2.ip.disposalhold;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

import java.io.Serial;
import java.io.Serializable;

import org.roda.core.data.v2.generics.select.SelectedItemsRequest;

public class LiftDisposalHoldRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -6983980156805237858L;

  private SelectedItemsRequest selectedItems;
  private String details;

  public LiftDisposalHoldRequest() {
    // empty constructor
  }

  public SelectedItemsRequest getSelectedItems() {
    return selectedItems;
  }

  public void setSelectedItems(SelectedItemsRequest selectedItems) {
    this.selectedItems = selectedItems;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
}
