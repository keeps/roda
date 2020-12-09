package org.roda.core.data.v2.ip.disposal.aipMetadata;

import java.util.Date;
import java.util.Objects;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Gabriel Barros <gbarrps@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_HOLD_AIP_METADATA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalHoldAIPMetadata implements IsModelObject {
  private static final long serialVersionUID = 2487191107131691113L;

  private String id;
  private Date associatedOn;
  private String AssociatedBy;

  public DisposalHoldAIPMetadata() {
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getAssociatedOn() {
    return associatedOn;
  }

  public void setAssociatedOn(Date associatedOn) {
    this.associatedOn = associatedOn;
  }

  public String getAssociatedBy() {
    return AssociatedBy;
  }

  public void setAssociatedBy(String associatedBy) {
    AssociatedBy = associatedBy;
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 0;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DisposalHoldAIPMetadata that = (DisposalHoldAIPMetadata) o;

    if (!Objects.equals(id, that.id))
      return false;
    if (!Objects.equals(associatedOn, that.associatedOn))
      return false;
    return Objects.equals(AssociatedBy, that.AssociatedBy);
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (associatedOn != null ? associatedOn.hashCode() : 0);
    result = 31 * result + (AssociatedBy != null ? AssociatedBy.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DisposalHoldAIPMetadata{" + "id='" + id + '\'' + ", associatedOn='" + associatedOn + '\''
      + ", AssociatedBy='" + AssociatedBy + '}';
  }
}
