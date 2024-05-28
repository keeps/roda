package org.roda.core.data.v2.index;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IndexedFileRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = -2001647419031311764L;

  private String aipId;
  private String representationId;
  private List<String> directoryPaths = new ArrayList<>();
  private String fileId;

  public IndexedFileRequest() {
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

  public List<String> getDirectoryPaths() {
    return directoryPaths;
  }

  public void setDirectoryPaths(List<String> directoryPaths) {
    this.directoryPaths = directoryPaths;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }
}
