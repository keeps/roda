package org.roda.core.data.v2.file;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CreateFolderRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -2232191408111318265L;

  private String aipId;
  private String representationId;
  private String folderUUID;
  private String name;
  private String details;

  public CreateFolderRequest() {
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

  public String getFolderUUID() {
    return folderUUID;
  }

  public void setFolderUUID(String folderUUID) {
    this.folderUUID = folderUUID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
}
