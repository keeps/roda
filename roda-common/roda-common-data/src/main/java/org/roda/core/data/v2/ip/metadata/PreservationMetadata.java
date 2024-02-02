/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.ip.HasId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.v2.ip.HasInstanceID;

@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_PRESERVATION_METADATA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreservationMetadata implements IsModelObject, HasId, HasInstanceID {
  @Serial
  private static final long serialVersionUID = -4312941542769679721L;

  public enum PreservationMetadataType {
    REPRESENTATION, FILE, INTELLECTUAL_ENTITY, AGENT, EVENT, RIGHTS_STATEMENT, ENVIRONMENT, OTHER;
  }

  private String id;
  private String aipId;
  private String representationId;
  private List<String> fileDirectoryPath;
  private String fileId;
  private PreservationMetadataType type;

  private String instanceId;

  private Date createdOn;

  public PreservationMetadata() {
    super();
  }

  public PreservationMetadata(String id, PreservationMetadataType type, String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, String instanceId) {
    super();
    this.id = id;
    this.type = type;

    this.aipId = aipId;
    this.representationId = representationId;
    this.fileDirectoryPath = fileDirectoryPath;
    this.fileId = fileId;
    this.instanceId = instanceId;
  }

  public PreservationMetadata(String id, PreservationMetadataType type) {
    this(id, type, null, null, null, null, null);
  }

  public PreservationMetadata(String id, PreservationMetadataType type, String aipId) {
    this(id, type, aipId, null, null, null, null);
  }

  public PreservationMetadata(String id, PreservationMetadataType type, String aipId, String representationId) {
    this(id, type, aipId, representationId, null, null, null);
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 2;
  }

  /**
   * @return the id
   */
  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public PreservationMetadataType getType() {
    return type;
  }

  public void setType(PreservationMetadataType type) {
    this.type = type;
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

  public List<String> getFileDirectoryPath() {
    return fileDirectoryPath;
  }

  public void setFileDirectoryPath(List<String> fileDirectoryPath) {
    this.fileDirectoryPath = fileDirectoryPath;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
    result = prime * result + ((fileDirectoryPath == null) ? 0 : fileDirectoryPath.hashCode());
    result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((representationId == null) ? 0 : representationId.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((instanceId == null) ? 0 : instanceId.hashCode());
    result = prime * result + ((createdOn == null) ? 0 : createdOn.hashCode());
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
    if (instanceId == null) {
      if (other.instanceId != null)
        return false;
    } else if (!instanceId.equals(other.instanceId))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "PreservationMetadata [id=" + id + ", aipId=" + aipId + ", representationId=" + representationId
      + ", fileDirectoryPath=" + fileDirectoryPath + ", fileId=" + fileId + ", type=" + type + ", instanceId="
      + instanceId + ", createdOn=" + createdOn + "]";
  }

}
