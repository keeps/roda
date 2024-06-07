package org.roda.core.data.v2.user.requests;

import java.io.Serial;
import java.io.Serializable;

import org.roda.core.data.v2.generics.select.SelectedItemsRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ChangeUserStatusRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = 4980327259554217630L;

  private SelectedItemsRequest items;
  private boolean activate;

  public ChangeUserStatusRequest() {
    // empty constructor
  }

  public ChangeUserStatusRequest(SelectedItemsRequest items, boolean activate) {
    this.items = items;
    this.activate = activate;
  }

  public SelectedItemsRequest getItems() {
    return items;
  }

  public void setItems(SelectedItemsRequest items) {
    this.items = items;
  }

  public boolean isActivate() {
    return activate;
  }

  public void setActivate(boolean activate) {
    this.activate = activate;
  }
}
