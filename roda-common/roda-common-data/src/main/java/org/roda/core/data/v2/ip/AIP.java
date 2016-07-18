/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;

import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AIP implements IsRODAObject {

  private static final long serialVersionUID = 430629679119752757L;

  @JsonIgnore
  private String id;
  private String parentId;
  private String type;
  private AIPState state = AIPState.getDefault();
  private Permissions permissions;

  private List<DescriptiveMetadata> descriptiveMetadata;

  private List<Representation> representations;

  private String ingestSIPId = "";
  private String ingestJobId = "";

  private Boolean ghost = null;

  public AIP() {
    super();
  }

  public AIP(String id, String parentId, String type, AIPState state, Permissions permissions) {
    this(id, parentId, type, state, permissions, new ArrayList<DescriptiveMetadata>(), new ArrayList<Representation>());
  }

  public AIP(String id, String parentId, String type, AIPState state, Permissions permissions,
    List<DescriptiveMetadata> descriptiveMetadata, List<Representation> representations) {
    super();
    this.id = id;
    this.parentId = parentId;
    this.type = type;
    this.state = state;
    this.permissions = permissions;

    this.descriptiveMetadata = descriptiveMetadata;

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

  public void setId(String id) {
    this.id = id;

    // As id is not serialized to JSON, set the AIP id in metadata and data
    if (descriptiveMetadata != null) {
      for (DescriptiveMetadata dm : descriptiveMetadata) {
        dm.setAipId(id);
      }
    }

    if (representations != null) {
      for (Representation representation : representations) {
        representation.setAipId(id);
        for (DescriptiveMetadata repDm : representation.getDescriptiveMetadata()) {
          repDm.setAipId(id);
        }
      }
    }

  }

  public Boolean getGhost() {
    return ghost;
  }

  public void setGhost(Boolean ghost) {
    this.ghost = ghost.equals(Boolean.FALSE) ? null : ghost;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public AIPState getState() {
    return state;
  }

  public void setState(AIPState state) {
    this.state = state;
  }

  public Permissions getPermissions() {
    return permissions;
  }

  public void setPermissions(Permissions permissions) {
    this.permissions = permissions;
  }

  public List<DescriptiveMetadata> getDescriptiveMetadata() {
    return descriptiveMetadata;
  }

  public void setDescriptiveMetadata(List<DescriptiveMetadata> descriptiveMetadata) {
    this.descriptiveMetadata = descriptiveMetadata;
  }

  public List<Representation> getRepresentations() {
    return representations;
  }

  public void setRepresentations(List<Representation> representations) {
    this.representations = representations;
  }

  public String getIngestSIPId() {
    return ingestSIPId;
  }

  public AIP setIngestSIPId(String ingestSIPId) {
    this.ingestSIPId = ingestSIPId;
    return this;
  }

  public String getIngestJobId() {
    return ingestJobId;
  }

  public AIP setIngestJobId(String ingestJobId) {
    this.ingestJobId = ingestJobId;
    return this;
  }

  public void addDescriptiveMetadata(DescriptiveMetadata descriptiveMetadata) {
    if (descriptiveMetadata.isFromAIP()) {
      this.descriptiveMetadata.add(descriptiveMetadata);
    } else {
      for (Representation representation : this.representations) {
        if (representation.getId().equals(descriptiveMetadata.getRepresentationId())) {
          representation.addDescriptiveMetadata(descriptiveMetadata);
          break;
        }
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AIP aip = (AIP) o;

    if (id != null ? !id.equals(aip.id) : aip.id != null) return false;
    if (parentId != null ? !parentId.equals(aip.parentId) : aip.parentId != null) return false;
    if (type != null ? !type.equals(aip.type) : aip.type != null) return false;
    if (state != aip.state) return false;
    if (permissions != null ? !permissions.equals(aip.permissions) : aip.permissions != null) return false;
    if (descriptiveMetadata != null ? !descriptiveMetadata.equals(aip.descriptiveMetadata) : aip.descriptiveMetadata != null) return false;
    if (representations != null ? !representations.equals(aip.representations) : aip.representations != null) return false;
    if (ingestSIPId != null ? !ingestSIPId.equals(aip.ingestSIPId) : aip.ingestSIPId != null) return false;
    if (ingestJobId != null ? !ingestJobId.equals(aip.ingestJobId) : aip.ingestJobId != null) return false;
    return ghost != null ? ghost.equals(aip.ghost) : aip.ghost == null;

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (state != null ? state.hashCode() : 0);
    result = 31 * result + (permissions != null ? permissions.hashCode() : 0);
    result = 31 * result + (descriptiveMetadata != null ? descriptiveMetadata.hashCode() : 0);
    result = 31 * result + (representations != null ? representations.hashCode() : 0);
    result = 31 * result + (ingestSIPId != null ? ingestSIPId.hashCode() : 0);
    result = 31 * result + (ingestJobId != null ? ingestJobId.hashCode() : 0);
    result = 31 * result + (ghost != null ? ghost.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "AIP{" +
            "id='" + id + '\'' +
            ", parentId='" + parentId + '\'' +
            ", type='" + type + '\'' +
            ", state=" + state +
            ", permissions=" + permissions +
            ", descriptiveMetadata=" + descriptiveMetadata +
            ", representations=" + representations +
            ", ingestSIPId='" + ingestSIPId + '\'' +
            ", ingestJobId='" + ingestJobId + '\'' +
            ", ghost=" + ghost +
            '}';
  }

}
