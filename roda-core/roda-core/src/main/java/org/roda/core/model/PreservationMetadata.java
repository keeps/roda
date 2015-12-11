/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import org.roda.core.storage.StoragePath;

public class PreservationMetadata {

  private final String id;
  private final String aipId;
  private final String representationID;
  private final StoragePath storagePath;
  private final String type;

  public PreservationMetadata(String id, String aipId, String representationId, StoragePath storagePath, String type) {
    super();
    this.id = id;
    this.aipId = aipId;
    this.representationID = representationId;
    this.storagePath = storagePath;
    this.type = type;
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

  public String getRepresentationID() {
    return representationID;
  }

  /**
   * @return the storagePath
   */
  public StoragePath getStoragePath() {
    return storagePath;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return "PreservationMetadata [id=" + id + ", aipId=" + aipId + ", representationID=" + representationID
      + ", storagePath=" + storagePath + ", type=" + type + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
    result = prime * result + ((getRepresentationID() == null) ? 0 : getRepresentationID().hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    PreservationMetadata other = (PreservationMetadata) obj;
    if (aipId == null) {
      if (other.aipId != null) {
        return false;
      }
    } else if (!aipId.equals(other.aipId)) {
      return false;
    }
    if (getRepresentationID() == null) {
      if (other.getRepresentationID() != null) {
        return false;
      }
    } else if (!getRepresentationID().equals(other.getRepresentationID())) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
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
