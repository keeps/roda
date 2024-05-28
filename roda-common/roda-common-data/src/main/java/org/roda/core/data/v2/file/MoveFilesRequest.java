package org.roda.core.data.v2.file;

import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedFile;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MoveFilesRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -4212000507336383174L;

  private String aipId;
  private String representationId;
  private String fileUUIDtoMove;
  private SelectedItems<IndexedFile> itemsToMove;
  private String details;

  public MoveFilesRequest() {
    // empty constructor
  }

  public String getAipId() {
    return aipId;
  }

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  public String getRepresentationId() {
    return representationId;
  }

  public void setRepresentationId(String representationId) {
    this.representationId = representationId;
  }

  public String getFileUUIDtoMove() {
    return fileUUIDtoMove;
  }

  public void setFileUUIDtoMove(String fileUUIDtoMove) {
    this.fileUUIDtoMove = fileUUIDtoMove;
  }

  public SelectedItems<IndexedFile> getItemsToMove() {
    return itemsToMove;
  }

  public void setItemsToMove(SelectedItems<IndexedFile> itemsToMove) {
    this.itemsToMove = itemsToMove;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
}
