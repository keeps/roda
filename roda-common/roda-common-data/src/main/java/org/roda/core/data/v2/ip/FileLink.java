/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
    StringBuilder builder = new StringBuilder();
    builder.append("FileLink [");
    
    if (getAipId() != null) {
      builder.append("getAipId()=");
      builder.append(getAipId());
    }
    if (getRepresentationId() != null) {
      builder.append("getRepresentationId()=");
      builder.append(getRepresentationId());
      builder.append(", ");
    }
    if (path != null) {
      builder.append("path=");
      builder.append(path);
      builder.append(", ");
    }
    if (fileId != null) {
      builder.append("fileId=");
      builder.append(fileId);
      builder.append(", ");
    }
    builder.append("]");
    return builder.toString();
  }

 

}
