/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;
import java.util.List;

public class OtherMetadata implements Serializable {

  private static final long serialVersionUID = 7643339238489130326L;

  private String id;
  private String type;

  private String aipId;
  private String representationId;
  private List<String> fileDirectoryPath;
  private String fileId;
  private String fileSuffix;

  public OtherMetadata() {
    super();
  }

  public OtherMetadata(String id, String type, String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix) {
    super();
    this.id = id;
    this.type = type;

    this.aipId = aipId;
    this.representationId = representationId;
    this.fileDirectoryPath = fileDirectoryPath;
    this.fileId = fileId;
    this.fileSuffix = fileSuffix;
  }

  public OtherMetadata(String id, String type, String aipId, String representationId) {
    this(id, type, aipId, representationId, null, null, null);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public String getFileSuffix() {
    return fileSuffix;
  }

  public void setFileSuffix(String fileSuffix) {
    this.fileSuffix = fileSuffix;
  }

  public List<String> getFileDirectoryPath() {
    return fileDirectoryPath;
  }

  public void setFileDirectoryPath(List<String> fileDirectoryPath) {
    this.fileDirectoryPath = fileDirectoryPath;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
    result = prime * result + ((fileDirectoryPath == null) ? 0 : fileDirectoryPath.hashCode());
    result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
    result = prime * result + ((fileSuffix == null) ? 0 : fileSuffix.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((representationId == null) ? 0 : representationId.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    OtherMetadata other = (OtherMetadata) obj;
    if (aipId == null) {
      if (other.aipId != null)
        return false;
    } else if (!aipId.equals(other.aipId))
      return false;
    if (fileDirectoryPath == null) {
      if (other.fileDirectoryPath != null)
        return false;
    } else if (!fileDirectoryPath.equals(other.fileDirectoryPath))
      return false;
    if (fileId == null) {
      if (other.fileId != null)
        return false;
    } else if (!fileId.equals(other.fileId))
      return false;
    if (fileSuffix == null) {
      if (other.fileSuffix != null)
        return false;
    } else if (!fileSuffix.equals(other.fileSuffix))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (representationId == null) {
      if (other.representationId != null)
        return false;
    } else if (!representationId.equals(other.representationId))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "OtherMetadata [id=" + id + ", type=" + type + ", aipId=" + aipId + ", representationId=" + representationId
      + ", fileDirectoryPath=" + fileDirectoryPath + ", fileId=" + fileId + ", fileSuffix=" + fileSuffix + "]";
  }

}
