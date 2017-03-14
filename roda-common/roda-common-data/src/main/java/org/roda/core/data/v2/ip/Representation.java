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

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = RodaConstants.RODA_OBJECT_REPRESENTATION)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Representation implements IsModelObject {

  private static final long serialVersionUID = 3658011895150894795L;

  @JsonIgnore
  private String aipId;

  private String id;
  private boolean original;

  private String type;

  private List<DescriptiveMetadata> descriptiveMetadata = new ArrayList<>();

  public Representation() {
    super();
  }

  public Representation(String id, String aipId, boolean original, String type) {
    this(id, aipId, original, type, new ArrayList<DescriptiveMetadata>());
  }

  public Representation(String id, String aipId, boolean original, String type,
    List<DescriptiveMetadata> descriptiveMetadata) {
    super();
    this.id = id;
    this.aipId = aipId;
    this.original = original;
    this.type = type;
    this.descriptiveMetadata = descriptiveMetadata;
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

  public boolean isOriginal() {
    return original;
  }

  public void setOriginal(boolean original) {
    this.original = original;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<DescriptiveMetadata> getDescriptiveMetadata() {
    return descriptiveMetadata;
  }

  public void setDescriptiveMetadata(List<DescriptiveMetadata> descriptiveMetadata) {
    this.descriptiveMetadata = descriptiveMetadata;
  }

  public void addDescriptiveMetadata(DescriptiveMetadata descriptiveMetadata) {
    this.descriptiveMetadata.add(descriptiveMetadata);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
    result = prime * result + ((descriptiveMetadata == null) ? 0 : descriptiveMetadata.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + (original ? 1231 : 1237);
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof Representation))
      return false;
    Representation other = (Representation) obj;
    if (aipId == null) {
      if (other.aipId != null)
        return false;
    } else if (!aipId.equals(other.aipId))
      return false;
    if (descriptiveMetadata == null) {
      if (other.descriptiveMetadata != null)
        return false;
    } else if (!descriptiveMetadata.equals(other.descriptiveMetadata))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (original != other.original)
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
    return "Representation [aipId=" + aipId + ", id=" + id + ", original=" + original + ", type=" + type
      + ", descriptiveMetadata=" + descriptiveMetadata + "]";
  }

}
