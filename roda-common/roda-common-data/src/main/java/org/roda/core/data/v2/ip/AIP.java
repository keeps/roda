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

import org.roda.core.data.v2.ip.metadata.Metadata;

public class AIP implements Serializable {

  private static final long serialVersionUID = 430629679119752757L;

  private String id;
  private String parentId;
  private boolean active;
  private RODAObjectPermissions permissions;

  private Metadata metadata;

  private List<String> representationIds;

  public AIP() {
    super();
  }

  public AIP(String id, String parentId, boolean active, RODAObjectPermissions permissions, Metadata metadata,
    List<String> representationIds) {
    super();
    this.id = id;
    this.parentId = parentId;
    this.active = active;
    this.permissions = permissions;

    this.metadata = metadata;

    this.representationIds = representationIds;

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
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public RODAObjectPermissions getPermissions() {
    return permissions;
  }

  public void setPermissions(RODAObjectPermissions permissions) {
    this.permissions = permissions;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  /**
   * @return the representationIds
   */
  public List<String> getRepresentationIds() {
    return representationIds;
  }

  public void setRepresentationIds(List<String> representationIds) {
    this.representationIds = representationIds;
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
    result = prime * result + ((representationIds == null) ? 0 : representationIds.hashCode());
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
    if (representationIds == null) {
      if (other.representationIds != null)
        return false;
    } else if (!representationIds.equals(other.representationIds))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AIP [id=" + id + ", parentId=" + parentId + ", active=" + active + ", permissions=" + permissions
      + ", metadata=" + metadata + ", representationIds=" + representationIds + "]";
  }

}
