package org.roda.core.data.v2.ip.disposalhold;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

import java.io.Serial;
import java.io.Serializable;

import org.roda.core.data.v2.generics.select.SelectedItemsRequest;

public class DisassociateDisposalHoldRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = 1324253414733503493L;

  private SelectedItemsRequest selectedItems;
  private String details;
  private boolean clear;

  public DisassociateDisposalHoldRequest() {
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

  public boolean getClear() {
    return clear;
  }

  public void setClear(boolean clear) {
    this.clear = clear;
  }
}
