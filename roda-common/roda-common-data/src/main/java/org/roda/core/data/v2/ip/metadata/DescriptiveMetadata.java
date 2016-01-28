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

public class DescriptiveMetadata implements Serializable {
  private static final long serialVersionUID = 5460845130599998867L;

  private String id;
  @JsonIgnore
  private String aipId;
  private String type;

  public DescriptiveMetadata() {
    super();
  }

  public DescriptiveMetadata(String id, String aipId, String type) {
    super();
    this.id = id;
    this.aipId = aipId;
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

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "DescriptiveMetadata [id=" + id + ", aipId=" + aipId + ", type=" + type + "]";
  }

}
