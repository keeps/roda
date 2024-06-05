package org.roda.core.data.v2.index;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IndexedFileRequest extends IndexedRepresentationRequest {
  @Serial
  private static final long serialVersionUID = -2001647419031311764L;

  private List<String> directoryPaths = new ArrayList<>();
  private String fileId;

  public IndexedFileRequest() {
    // empty constructor
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
