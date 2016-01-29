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

import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.Metadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AIP implements Serializable {

  private static final long serialVersionUID = 430629679119752757L;

  @JsonIgnore
  private String id;
  private String parentId;
  private boolean active;
  private AIPPermissions permissions;

  private Metadata metadata;

  private List<Representation> representations;

  public AIP() {
    super();
  }

  public AIP(String id, String parentId, boolean active, AIPPermissions permissions, Metadata metadata,
    List<Representation> representations) {
    super();
    this.id = id;
    this.parentId = parentId;
    this.active = active;
    this.permissions = permissions;

    this.metadata = metadata;

    this.representations = representations;

  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Get the identifier of the parent AIP or <code>null</code> if this AIP is on
   * the top-level.
   * 
   * @return
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * @return the active
   */
  public boolean isActive() {
    return active;
  }

  public void setId(String id) {
    this.id = id;

    // As id is not serialized to JSON, set the AIP id in metadata and data
    if (metadata != null) {
      if (metadata.getDescriptiveMetadata() != null) {
        for (DescriptiveMetadata descriptiveMetadata : metadata.getDescriptiveMetadata()) {
          descriptiveMetadata.setAipId(id);
        }
      }

      if (metadata.getPreservationMetadata() != null) {
        for (PreservationMetadata preservationMetadata : metadata.getPreservationMetadata()) {
          preservationMetadata.setAipId(id);
        }
      }

      if (metadata.getOtherMetadata() != null) {
        for (OtherMetadata otherMetadata : metadata.getOtherMetadata()) {
          otherMetadata.setAipId(id);
        }
      }
    }

    if (representations != null) {
      for (Representation representation : representations) {
        representation.setAipId(id);
      }
    }

  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public AIPPermissions getPermissions() {
    return permissions;
  }

  public void setPermissions(AIPPermissions permissions) {
    this.permissions = permissions;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public List<Representation> getRepresentations() {
    return representations;
  }

  public void setRepresentations(List<Representation> representations) {
    this.representations = representations;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (active ? 1231 : 1237);
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
    result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
    result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
    result = prime * result + ((representations == null) ? 0 : representations.hashCode());
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
    AIP other = (AIP) obj;
    if (active != other.active)
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (metadata == null) {
      if (other.metadata != null)
        return false;
    } else if (!metadata.equals(other.metadata))
      return false;
    if (parentId == null) {
      if (other.parentId != null)
        return false;
    } else if (!parentId.equals(other.parentId))
      return false;
    if (permissions == null) {
      if (other.permissions != null)
        return false;
    } else if (!permissions.equals(other.permissions))
      return false;
    if (representations == null) {
      if (other.representations != null)
        return false;
    } else if (!representations.equals(other.representations))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AIP [id=" + id + ", parentId=" + parentId + ", active=" + active + ", permissions=" + permissions
      + ", metadata=" + metadata + ", representations=" + representations + "]";
  }

}
