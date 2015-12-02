/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

public class SimpleFile extends RODAObject {

  private static final long serialVersionUID = 3303019735787641534L;

  private String id = null;
  private String aipId = null;
  private String representationId = null;

  private boolean entryPoint = false;
  private FileFormat fileFormat = null;
  private String originalName = null;
  private long size = 0;
  private boolean isFile = false;

  public SimpleFile() {
    super(null, null);
  }

  public SimpleFile(String id, String aipId, String representationId, boolean entryPoint, FileFormat fileFormat,
    String originalName, long size, boolean isFile) {
    super();
    this.id = id;
    this.aipId = aipId;
    this.representationId = representationId;
    this.entryPoint = entryPoint;
    this.fileFormat = fileFormat;
    this.size = size;
    this.originalName = originalName;
    this.isFile = isFile;
  }

  public boolean isFile() {
    return isFile;
  }

  public void setFile(boolean isFile) {
    this.isFile = isFile;
  }

  public String getOriginalName() {
    return originalName;
  }

  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the aipId
   */
  public String getAipId() {
    return aipId;
  }

  /**
   * @return the representationId
   */
  public String getRepresentationId() {
    return representationId;
  }

  /**
   * @return the entryPoint
   */
  public boolean isEntryPoint() {
    return entryPoint;
  }

  /**
   * @return the fileFormat
   */
  public FileFormat getFileFormat() {
    return fileFormat;
  }

  @Override
  public String toString() {
    return "File [id=" + id + ", aipId=" + aipId + ", representationId=" + representationId + ", entryPoint="
      + entryPoint + ", fileFormat=" + fileFormat + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
    result = prime * result + (entryPoint ? 1231 : 1237);
    result = prime * result + ((fileFormat == null) ? 0 : fileFormat.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((representationId == null) ? 0 : representationId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SimpleFile other = (SimpleFile) obj;
    if (aipId == null) {
      if (other.aipId != null) {
        return false;
      }
    } else if (!aipId.equals(other.aipId)) {
      return false;
    }
    if (entryPoint != other.entryPoint) {
      return false;
    }
    if (fileFormat == null) {
      if (other.fileFormat != null) {
        return false;
      }
    } else if (!fileFormat.equals(other.fileFormat)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (representationId == null) {
      if (other.representationId != null) {
        return false;
      }
    } else if (!representationId.equals(other.representationId)) {
      return false;
    }
    return true;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  public void setRepresentationId(String representationId) {
    this.representationId = representationId;
  }

  public void setEntryPoint(boolean entryPoint) {
    this.entryPoint = entryPoint;
  }

  public void setFileFormat(FileFormat fileFormat) {
    this.fileFormat = fileFormat;
  }

}
