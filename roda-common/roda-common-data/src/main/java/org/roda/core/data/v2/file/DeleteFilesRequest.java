package org.roda.core.data.v2.file;

import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedFile;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DeleteFilesRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = -6900617582311532828L;

  private SelectedItems<IndexedFile> itemsToDelete;
  private String details;

  public DeleteFilesRequest() {
    //empty constructor
  }

  public SelectedItems<IndexedFile> getItemsToDelete() {
    return itemsToDelete;
  }

  public void setItemsToDelete(SelectedItems<IndexedFile> itemsToDelete) {
    this.itemsToDelete = itemsToDelete;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
}
