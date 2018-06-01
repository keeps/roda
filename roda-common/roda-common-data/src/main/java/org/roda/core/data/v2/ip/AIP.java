/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = RodaConstants.RODA_OBJECT_AIP)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AIP implements IsModelObject, HasId, HasState, HasPermissions {

  private static final long serialVersionUID = 430629679119752757L;
  private static final int VERSION = 1;

  private String id;
  private String parentId;
  private String type;
  private AIPState state = AIPState.getDefault();
  private Permissions permissions;

  private List<DescriptiveMetadata> descriptiveMetadata;

  private List<Representation> representations;

  private List<String> ingestSIPIds;
  private String ingestJobId = "";
  private List<String> ingestUpdateJobIds = new ArrayList<>();

  private Boolean ghost = null;

  private AIPFormat format;
  private List<Relationship> relationships;

  private Date createdOn = null;
  private String createdBy = null;
  private Date updatedOn = null;
  private String updatedBy = null;

  public AIP() {
    super();
  }

  public AIP(String id, String parentId, String type, AIPState state, Permissions permissions) {
    this(id, parentId, type, state, permissions, new ArrayList<DescriptiveMetadata>(), new ArrayList<Representation>(),
      new AIPFormat(), new ArrayList<Relationship>(), new Date(), null, new Date(), null);
  }

  public AIP(String id, String parentId, String type, AIPState state, Permissions permissions, String createdBy) {
    this(id, parentId, type, state, permissions, new ArrayList<DescriptiveMetadata>(), new ArrayList<Representation>(),
      new AIPFormat(), new ArrayList<Relationship>(), new Date(), createdBy, new Date(), createdBy);
  }

  public AIP(String id, String parentId, String type, AIPState state, Permissions permissions,
    List<DescriptiveMetadata> descriptiveMetadata, List<Representation> representations, AIPFormat format,
    List<Relationship> relationships, Date createdOn, String createdBy, Date updatedOn, String updatedBy) {
    super();
    this.id = id;
    this.parentId = parentId;
    this.type = type;
    this.state = state;
    this.permissions = permissions;
    this.relationships = relationships;

    this.descriptiveMetadata = descriptiveMetadata;
    this.representations = representations;
    this.format = format;

    this.createdOn = createdOn;
    this.createdBy = createdBy;
    this.updatedOn = updatedOn;
    this.updatedBy = updatedBy;
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return VERSION;
  }

  /**
   * @return the id
   */
  @Override
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
          repDm.setRepresentationId(representation.getId());
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

  public List<String> getIngestSIPIds() {
    return ingestSIPIds;
  }

  public AIP setIngestSIPIds(List<String> ingestSIPIds) {
    this.ingestSIPIds = ingestSIPIds;
    return this;
  }

  public String getIngestJobId() {
    return ingestJobId;
  }

  public AIP setIngestJobId(String ingestJobId) {
    this.ingestJobId = ingestJobId;
    return this;
  }

  public List<String> getIngestUpdateJobIds() {
    return ingestUpdateJobIds;
  }

  public AIP setIngestUpdateJobIds(List<String> ingestUpdateJobIds) {
    this.ingestUpdateJobIds = ingestUpdateJobIds;
    return this;
  }

  public AIP addIngestUpdateJobId(String ingestUpdateJobId) {
    this.ingestUpdateJobIds.add(ingestUpdateJobId);
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

  public AIPFormat getFormat() {
    return format;
  }

  public void setFormat(AIPFormat format) {
    this.format = format;
  }

  public void setFormat(String name, String version) {
    this.format = new AIPFormat(name, version);
  }

  public List<Relationship> getRelationships() {
    return relationships;
  }

  public void setRelationships(List<Relationship> relationships) {
    this.relationships = relationships;
  }

  public void addRelationship(Relationship relationship) {
    if (relationships == null) {
      relationships = new ArrayList<>();
    }

    relationships.add(relationship);
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getUpdatedOn() {
    return updatedOn;
  }

  public void setUpdatedOn(Date updatedOn) {
    this.updatedOn = updatedOn;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    AIP aip = (AIP) o;

    if (id != null ? !id.equals(aip.id) : aip.id != null)
      return false;
    if (parentId != null ? !parentId.equals(aip.parentId) : aip.parentId != null)
      return false;
    if (type != null ? !type.equals(aip.type) : aip.type != null)
      return false;
    if (state != aip.state)
      return false;
    if (permissions != null ? !permissions.equals(aip.permissions) : aip.permissions != null)
      return false;
    if (descriptiveMetadata != null ? !descriptiveMetadata.equals(aip.descriptiveMetadata)
      : aip.descriptiveMetadata != null)
      return false;
    if (representations != null ? !representations.equals(aip.representations) : aip.representations != null)
      return false;
    if (ingestSIPIds != null ? !ingestSIPIds.equals(aip.ingestSIPIds) : aip.ingestSIPIds != null)
      return false;
    if (ingestJobId != null ? !ingestJobId.equals(aip.ingestJobId) : aip.ingestJobId != null)
      return false;
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
    result = 31 * result + (ingestSIPIds != null ? ingestSIPIds.hashCode() : 0);
    result = 31 * result + (ingestJobId != null ? ingestJobId.hashCode() : 0);
    result = 31 * result + (ghost != null ? ghost.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "AIP{" + "id='" + id + '\'' + ", parentId='" + parentId + '\'' + ", type='" + type + '\'' + ", state="
      + state + ", permissions=" + permissions + ", descriptiveMetadata=" + descriptiveMetadata + ", representations="
      + representations + ", ingestSIPId='" + ingestSIPIds + '\'' + ", ingestJobId='" + ingestJobId + '\''
      + ", ingestJobIds='" + ingestUpdateJobIds + '\'' + ", ghost=" + ghost + ", format=" + format + ", relationships="
      + relationships + ", createdOn=" + createdOn + ", createdBy=" + createdBy + ", updatedOn=" + updatedOn
      + ", updatedBy=" + updatedBy + '}';
  }

}
