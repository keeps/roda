package org.roda.core.data.v2.ip;

import java.io.Serializable;
import java.util.List;

public class FileLink extends RepresentationLink implements Serializable {
  private static final long serialVersionUID = 7553550787359540332L;

  private List<String> path;
  private String fileId;

  public FileLink() {
    super();
  }

  public FileLink(String aipId, String representationId, List<String> path, String fileId) {
    super(aipId, representationId);
    this.path = path;
    this.fileId = fileId;
  }

  public List<String> getPath() {
    return path;
  }

  public void setPath(List<String> path) {
    this.path = path;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  @Override
  public String toString() {
    return "FileLink [path=" + path + ", fileId=" + fileId + "]";
  }

}
