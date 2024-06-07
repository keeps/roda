package org.roda.core.data.v2.generics;

import java.io.Serial;
import java.io.Serializable;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.Permissions;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class UpdatePermissionsRequest<T extends IsIndexed> implements Serializable {
  @Serial
  private static final long serialVersionUID = -3807428982711600007L;

  private SelectedItems<T> itemsToUpdate;
  private String details;
  private Permissions permissions;
  private boolean recursive;

  public UpdatePermissionsRequest() {
    // empty constructor
  }

  public SelectedItems<T> getItemsToUpdate() {
    return itemsToUpdate;
  }

  public void setItemsToUpdate(SelectedItems<T> itemsToUpdate) {
    this.itemsToUpdate = itemsToUpdate;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public Permissions getPermissions() {
    return permissions;
  }

  public void setPermissions(Permissions permissions) {
    this.permissions = permissions;
  }

  public boolean isRecursive() {
    return recursive;
  }

  public void setRecursive(boolean recursive) {
    this.recursive = recursive;
  }
}
