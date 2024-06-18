package org.roda.core.data.v2.aip;

import java.io.Serial;
import java.io.Serializable;

import org.roda.core.data.v2.generics.select.SelectedItemsRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class AssessmentRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = 5491507474053150021L;

  private SelectedItemsRequest items;
  private boolean accept;
  private String rejectReason;

  public AssessmentRequest() {
    // empty constructor
  }

  public SelectedItemsRequest getItems() {
    return items;
  }

  public void setItems(SelectedItemsRequest items) {
    this.items = items;
  }

  public boolean isAccept() {
    return accept;
  }

  public void setAccept(boolean accept) {
    this.accept = accept;
  }

  public String getRejectReason() {
    return rejectReason;
  }

  public void setRejectReason(String rejectReason) {
    this.rejectReason = rejectReason;
  }
}
