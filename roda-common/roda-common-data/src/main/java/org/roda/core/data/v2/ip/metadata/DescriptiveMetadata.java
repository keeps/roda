/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = RodaConstants.RODA_OBJECT_DESCRIPTIVE_METADATA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DescriptiveMetadata implements IsModelObject {
  private static final long serialVersionUID = 5460845130599998867L;

  private String id;
  private String aipId;
  private String representationId;
  private String type;
  private String version;

  public DescriptiveMetadata() {
    super();
  }

  public DescriptiveMetadata(String id, String aipId, String type, String version) {
    this(id, aipId, null, type, version);
  }

  public DescriptiveMetadata(String id, String aipId, String representationId, String type, String version) {
    super();
    this.id = id;
    this.aipId = aipId;
    this.representationId = representationId;
    this.type = type;
    this.version = version;
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 1;
  }

  @Override
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @JsonIgnore
  public boolean isFromAIP() {
    return representationId == null;
  }

  @JsonIgnore
  public boolean isFromRepresentation() {
    return !isFromAIP();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((representationId == null) ? 0 : representationId.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof DescriptiveMetadata))
      return false;
    DescriptiveMetadata other = (DescriptiveMetadata) obj;
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
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "DescriptiveMetadata [id=" + id + ", aipId=" + aipId + ", representationId=" + representationId + ", type="
      + type + ", version=" + version + "]";
  }

}
