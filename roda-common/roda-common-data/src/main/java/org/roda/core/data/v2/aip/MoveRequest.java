package org.roda.core.data.v2.aip;

import java.io.Serial;
import java.io.Serializable;

import org.roda.core.data.v2.generics.select.SelectedItemsRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MoveRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = -9090283233252301880L;

  private String parentId;
  private SelectedItemsRequest itemsToMove;
  private String details;

  public MoveRequest() {
    // empty constructor
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public SelectedItemsRequest getItemsToMove() {
    return itemsToMove;
  }

  public void setItemsToMove(SelectedItemsRequest itemsToMove) {
    this.itemsToMove = itemsToMove;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
}
