/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PreservationMetadata implements Serializable {
  private static final long serialVersionUID = -4312941542769679721L;

  public static enum PreservationMetadataType {
    OBJECT_REPRESENTATION, OBJECT_FILE, OBJECT_INTELLECTUAL_ENTITY, AGENT, EVENT, RIGHTS_STATEMENT, ENVIRONMENT;
  }

  private String id;
  @JsonIgnore
  private String aipId;
  private String representationId;
  private PreservationMetadataType type;

  public PreservationMetadata() {
    super();
  }

  public PreservationMetadata(String id, String aipId, String representationId, PreservationMetadataType type) {
    super();
    this.id = id;
    this.aipId = aipId;
    this.representationId = representationId;
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

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  public String getRepresentationId() {
    return representationId;
  }

  public void setRepresentationId(String representationId) {
    this.representationId = representationId;
  }

  public PreservationMetadataType getType() {
    return type;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setType(PreservationMetadataType type) {
    this.type = type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
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
    PreservationMetadata other = (PreservationMetadata) obj;
    if (aipId == null) {
      if (other.aipId != null)
        return false;
    } else if (!aipId.equals(other.aipId))
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
    if (type != other.type)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "PreservationMetadata [id=" + id + ", aipId=" + aipId + ", representationID=" + representationId + ", type="
      + type + "]";
  }

}
