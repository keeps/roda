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

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((descriptiveMetadata == null) ? 0 : descriptiveMetadata.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((ingestJobId == null) ? 0 : ingestJobId.hashCode());
    result = prime * result + ((ingestSIPId == null) ? 0 : ingestSIPId.hashCode());
    result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
    result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
    result = prime * result + ((representations == null) ? 0 : representations.hashCode());
    result = prime * result + ((state == null) ? 0 : state.hashCode());
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
    if (!(obj instanceof AIP)) {
      return false;
    }
    AIP other = (AIP) obj;
    if (descriptiveMetadata == null) {
      if (other.descriptiveMetadata != null) {
        return false;
      }
    } else if (!descriptiveMetadata.equals(other.descriptiveMetadata)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (ingestJobId == null) {
      if (other.ingestJobId != null) {
        return false;
      }
    } else if (!ingestJobId.equals(other.ingestJobId)) {
      return false;
    }
    if (ingestSIPId == null) {
      if (other.ingestSIPId != null) {
        return false;
      }
    } else if (!ingestSIPId.equals(other.ingestSIPId)) {
      return false;
    }
    if (parentId == null) {
      if (other.parentId != null) {
        return false;
      }
    } else if (!parentId.equals(other.parentId)) {
      return false;
    }
    if (permissions == null) {
      if (other.permissions != null) {
        return false;
      }
    } else if (!permissions.equals(other.permissions)) {
      return false;
    }
    if (representations == null) {
      if (other.representations != null) {
        return false;
      }
    } else if (!representations.equals(other.representations)) {
      return false;
    }
    if (state != other.state) {
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

  @Override
  public String toString() {
    return "AIP [id=" + id + ", parentId=" + parentId + ", type=" + type + ", state=" + state + ", permissions="
      + permissions + ", descriptiveMetadata=" + descriptiveMetadata + ", representations=" + representations
      + ", ingestSIPId=" + ingestSIPId + ", ingestJobId=" + ingestJobId + "]";
  }

}
