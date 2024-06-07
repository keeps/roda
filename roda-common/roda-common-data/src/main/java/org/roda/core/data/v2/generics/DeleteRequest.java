package org.roda.core.data.v2.generics;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.select.SelectedItems;


public class DeleteRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -6600408657058206155L;

  private SelectedItemsRequest itemsToDelete;
  private String details;

  public DeleteRequest() {
    // empty constructor
  }

  public SelectedItemsRequest getItemsToDelete() {
    return itemsToDelete;
  }

  public void setItemsToDelete(SelectedItemsRequest itemsToDelete) {
    this.itemsToDelete = itemsToDelete;
  }

  @JsonIgnore
  public void setSelectedItemsToDelete(SelectedItems<?> itemsToDelete) {
    this.itemsToDelete = SelectedItemsUtils.convertToRESTRequest(itemsToDelete);
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
}
