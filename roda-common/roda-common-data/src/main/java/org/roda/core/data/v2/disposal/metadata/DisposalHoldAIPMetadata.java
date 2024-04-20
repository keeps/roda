/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.disposal.metadata;

import java.io.Serial;
import java.util.Date;
import java.util.Objects;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_HOLD_AIP_METADATA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalHoldAIPMetadata implements IsModelObject {
  @Serial
  private static final long serialVersionUID = 2487191107131691113L;

  private String id;
  private Date associatedOn;
  private String associatedBy;

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
    return associatedBy;
  }

  public void setAssociatedBy(String associatedBy) {
    this.associatedBy = associatedBy;
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
    return Objects.equals(associatedBy, that.associatedBy);
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (associatedOn != null ? associatedOn.hashCode() : 0);
    result = 31 * result + (associatedBy != null ? associatedBy.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DisposalHoldAIPMetadata{" + "id='" + id + '\'' + ", associatedOn='" + associatedOn + '\''
      + ", AssociatedBy='" + associatedBy + '}';
  }
}
