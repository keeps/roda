package org.roda.core.data.v2.ip.disposal;

import java.util.Date;
import java.util.Objects;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_HOLD_ASSOCIATION)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalHoldAssociation implements IsModelObject {

  private static final long serialVersionUID = 8625888155159274971L;

  private String id;
  private Date associatedOn;
  private String associatedBy;

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
      && Objects.equals(associatedBy, that.associatedBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, associatedOn, associatedBy);
  }

  @Override
  public String toString() {
    return "DisposalHoldAssociation{" + "id='" + id + '\'' + ", associatedOn=" + associatedOn + ", associatedBy='"
      + associatedBy + '\'' + '}';
  }
}
