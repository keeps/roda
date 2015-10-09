package org.roda.model;

import org.roda.storage.StoragePath;

public class OtherMetadata {
  private final String type;
  private final StoragePath storagePath;

  public OtherMetadata(String type, StoragePath storagePath) {
    super();
    this.type = type;
    this.storagePath = storagePath;
  }
  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @return the storagePath
   */
  public StoragePath getStoragePath() {
    return storagePath;
  }

  @Override
  public String toString() {
    return "DescriptiveMetadata [type=" + type + ", storagePath=" + storagePath
      + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((storagePath == null) ? 0 : storagePath.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    OtherMetadata other = (OtherMetadata) obj;
    if (storagePath == null) {
      if (other.storagePath != null) {
        return false;
      }
    } else if (!storagePath.equals(other.storagePath)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    return true;
  }

}
