package org.roda.core.data.v2.generics;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;

import java.io.Serial;
import java.io.Serializable;


public class DeleteRequest<T extends IsIndexed> implements Serializable {

  @Serial
  private static final long serialVersionUID = -6600408657058206155L;

  private SelectedItems<T> itemsToDelete;
  private String details;

  public DeleteRequest() {
    // empty constructor
  }

  public SelectedItems<T> getItemsToDelete() {
    return itemsToDelete;
  }

  public void setItemsToDelete(SelectedItems<T> itemsToDelete) {
    this.itemsToDelete = itemsToDelete;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
}
