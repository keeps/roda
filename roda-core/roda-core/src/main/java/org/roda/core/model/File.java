/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import java.io.Serializable;

import org.roda.core.storage.StoragePath;

public class File implements Serializable {

  private static final long serialVersionUID = 3303019735787641534L;
  private final StoragePath storagePath;
  private String id;
  private String aipId;
  private String representationId;
  private boolean entryPoint;
  private String originalName;
  private long size;
  boolean isFile;

  public File(String id, String aipId, String representationId, boolean entryPoint, StoragePath storagePath,
    String originalName, long size, boolean isFile) {
    this.id = id;
    this.aipId = aipId;
    this.representationId = representationId;
    this.entryPoint = entryPoint;
    this.size = size;
    this.originalName = originalName;
    this.isFile = isFile;
    this.storagePath = storagePath;
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

  public boolean isEntryPoint() {
    return entryPoint;
  }

  public void setEntryPoint(boolean entryPoint) {
    this.entryPoint = entryPoint;
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

  public boolean isFile() {
    return isFile;
  }

  public void setFile(boolean isFile) {
    this.isFile = isFile;
  }

  public StoragePath getStoragePath() {
    return storagePath;
  }

  @Override
  public String toString() {
    return "File [storagePath=" + storagePath + ", id=" + id + ", aipId=" + aipId + ", representationId="
      + representationId + ", entryPoint=" + entryPoint + ", originalName=" + originalName + ", size=" + size
      + ", isFile=" + isFile + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
    result = prime * result + (entryPoint ? 1231 : 1237);
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + (isFile ? 1231 : 1237);
    result = prime * result + ((originalName == null) ? 0 : originalName.hashCode());
    result = prime * result + ((representationId == null) ? 0 : representationId.hashCode());
    result = prime * result + (int) (size ^ (size >>> 32));
    result = prime * result + ((storagePath == null) ? 0 : storagePath.hashCode());
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
    if (!(obj instanceof File)) {
      return false;
    }
    File other = (File) obj;
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
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (isFile != other.isFile) {
      return false;
    }
    if (originalName == null) {
      if (other.originalName != null) {
        return false;
      }
    } else if (!originalName.equals(other.originalName)) {
      return false;
    }
    if (representationId == null) {
      if (other.representationId != null) {
        return false;
      }
    } else if (!representationId.equals(other.representationId)) {
      return false;
    }
    if (size != other.size) {
      return false;
    }
    if (storagePath == null) {
      if (other.storagePath != null) {
        return false;
      }
    } else if (!storagePath.equals(other.storagePath)) {
      return false;
    }
    return true;
  }

}
