/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.disposal.hold;

import java.util.Date;
import java.util.Objects;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_HOLD_ASSOCIATION)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalHoldAssociation implements IsModelObject, Comparable<DisposalHoldAssociation> {

  private static final long serialVersionUID = 8625888155159274971L;

  private String id;
  private Date associatedOn;
  private String associatedBy;
  private Date liftedOn;
  private String liftedBy;

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

  public Date getLiftedOn() {
    return liftedOn;
  }

  public void setLiftedOn(Date liftedOn) {
    this.liftedOn = liftedOn;
  }

  public String getLiftedBy() {
    return liftedBy;
  }

  public void setLiftedBy(String liftedBy) {
    this.liftedBy = liftedBy;
  }

  public DisposalHoldAssociation() {
    super();
  }

  public DisposalHoldAssociation(String id, Date associatedOn, String associatedBy) {
    this.id = id;
    this.associatedOn = associatedOn;
    this.associatedBy = associatedBy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    DisposalHoldAssociation that = (DisposalHoldAssociation) o;
    return Objects.equals(id, that.id) && Objects.equals(associatedOn, that.associatedOn)
      && Objects.equals(associatedBy, that.associatedBy) && Objects.equals(liftedOn, that.liftedOn)
      && Objects.equals(liftedBy, that.liftedBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, associatedOn, associatedBy, liftedOn, liftedBy);
  }

  @Override
  public String toString() {
    return "DisposalHoldAssociation{" + "id='" + id + '\'' + ", associatedOn=" + associatedOn + ", associatedBy='"
      + associatedBy + '\'' + ", liftedOn=" + liftedOn + ", liftedBy='" + liftedBy + '\'' + '}';
  }

  @Override
  public int compareTo(DisposalHoldAssociation association) {
    return this.getAssociatedOn().compareTo(association.getAssociatedOn());
  }
}
