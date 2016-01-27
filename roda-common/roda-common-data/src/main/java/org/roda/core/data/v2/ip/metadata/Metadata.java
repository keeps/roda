package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Metadata implements Serializable {

  private static final long serialVersionUID = 4656322373359633612L;

  private List<DescriptiveMetadata> descriptiveMetadata;
  private List<PreservationMetadata> preservationMetadata;
  private List<OtherMetadata> othermetadata;

  public Metadata() {
    super();
    descriptiveMetadata = new ArrayList<>();
    preservationMetadata = new ArrayList<>();
    othermetadata = new ArrayList<>();
  }

  public Metadata(List<DescriptiveMetadata> descriptiveMetadata, List<PreservationMetadata> preservationMetadata,
    List<OtherMetadata> othermetadata) {
    super();
    this.descriptiveMetadata = descriptiveMetadata;
    this.preservationMetadata = preservationMetadata;
    this.othermetadata = othermetadata;
  }

  public List<DescriptiveMetadata> getDescriptiveMetadata() {
    return descriptiveMetadata;
  }

  public void setDescriptiveMetadata(List<DescriptiveMetadata> descriptiveMetadata) {
    this.descriptiveMetadata = descriptiveMetadata;
  }

  public List<PreservationMetadata> getPreservationMetadata() {
    return preservationMetadata;
  }

  public void setPreservationMetadata(List<PreservationMetadata> preservationMetadata) {
    this.preservationMetadata = preservationMetadata;
  }

  public List<OtherMetadata> getOthermetadata() {
    return othermetadata;
  }

  public void setOthermetadata(List<OtherMetadata> othermetadata) {
    this.othermetadata = othermetadata;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((descriptiveMetadata == null) ? 0 : descriptiveMetadata.hashCode());
    result = prime * result + ((othermetadata == null) ? 0 : othermetadata.hashCode());
    result = prime * result + ((preservationMetadata == null) ? 0 : preservationMetadata.hashCode());
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
    Metadata other = (Metadata) obj;
    if (descriptiveMetadata == null) {
      if (other.descriptiveMetadata != null)
        return false;
    } else if (!descriptiveMetadata.equals(other.descriptiveMetadata))
      return false;
    if (othermetadata == null) {
      if (other.othermetadata != null)
        return false;
    } else if (!othermetadata.equals(other.othermetadata))
      return false;
    if (preservationMetadata == null) {
      if (other.preservationMetadata != null)
        return false;
    } else if (!preservationMetadata.equals(other.preservationMetadata))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Metadata [descriptiveMetadata=" + descriptiveMetadata + ", preservationMetadata=" + preservationMetadata
      + ", othermetadata=" + othermetadata + "]";
  }

}
