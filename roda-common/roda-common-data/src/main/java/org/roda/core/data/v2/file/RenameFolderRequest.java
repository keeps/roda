package org.roda.core.data.v2.file;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RenameFolderRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = 1178625475371809894L;

  private String folderUUID;
  private String renameTo;
  private String details;

  public RenameFolderRequest() {
    // empty constructor
  }

  public RenameFolderRequest(String folderUUID, String renameTo, String details) {
    this.folderUUID = folderUUID;
    this.renameTo = renameTo;
    this.details = details;
  }

  public String getFolderUUID() {
    return folderUUID;
  }

  public void setFolderUUID(String folderUUID) {
    this.folderUUID = folderUUID;
  }

  public String getRenameTo() {
    return renameTo;
  }

  public void setRenameTo(String renameTo) {
    this.renameTo = renameTo;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
}
