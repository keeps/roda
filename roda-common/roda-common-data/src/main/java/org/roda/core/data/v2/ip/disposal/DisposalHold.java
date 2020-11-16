package org.roda.core.data.v2.ip.disposal;

import java.util.Date;
import java.util.Objects;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_HOLD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalHold implements IsModelObject {

  private static final long serialVersionUID = 8291490773422089586L;

  private String id;
  private String title;
  private String description;
  // describes the authority and jurisdiction under which the disposal hold
  // operates
  private String mandate;
  // provide additional information to users on how the disposal hold should
  // be interpreted and applied
  private String scopeNotes;

  // automatically generated upon disposal hold creation action
  private Date createdOn;
  private String createdBy;
  private Date updatedOn;
  private String updatedBy;

  // the date on which the legal or administrative order was issued
  private Date originatedOn;
  private String originatedBy;

  private Date liftedOn;
  private String liftedBy;

  // If any AIP was associated to this disposal hold the date should be set to
  // prevent this disposal hold from being deleted
  private Date firstTimeUsed = null;

  private Long aipCounter;

  private DisposalHoldState state;

  public DisposalHold() {
    super();
    this.state = DisposalHoldState.ACTIVE;
    this.aipCounter = 0L;
  }

  public DisposalHold(String disposalHoldId, String title, String description, String mandate, String scopeNotes) {
    this.id = disposalHoldId;
    this.title = title;
    this.description = description;
    this.mandate = mandate;
    this.scopeNotes = scopeNotes;
    this.state = DisposalHoldState.ACTIVE;
    this.aipCounter = 0L;
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

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getMandate() {
    return mandate;
  }

  public void setMandate(String mandate) {
    this.mandate = mandate;
  }

  public String getScopeNotes() {
    return scopeNotes;
  }

  public void setScopeNotes(String scopeNotes) {
    this.scopeNotes = scopeNotes;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  public Date getLiftedOn() {
    return liftedOn;
  }

  public void setLiftedOn(Date liftedOn) {
    this.liftedOn = liftedOn;
  }

  public Date getUpdatedOn() {
    return updatedOn;
  }

  public void setUpdatedOn(Date updatedOn) {
    this.updatedOn = updatedOn;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public String getLiftedBy() {
    return liftedBy;
  }

  public void setLiftedBy(String liftedBy) {
    this.liftedBy = liftedBy;
  }

  public Date getOriginatedOn() {
    return originatedOn;
  }

  public void setOriginatedOn(Date originatedOn) {
    this.originatedOn = originatedOn;
  }

  public String getOriginatedBy() {
    return originatedBy;
  }

  public void setOriginatedBy(String originatedBy) {
    this.originatedBy = originatedBy;
  }

  public DisposalHoldState getState() {
    return state;
  }

  public void setState(DisposalHoldState state) {
    this.state = state;
  }

  public Long getAipCounter() {
    return aipCounter;
  }

  public void setAipCounter(Long aipCounter) {
    this.aipCounter = aipCounter;
  }

  public Date getFirstTimeUsed() {
    return firstTimeUsed;
  }

  public void setFirstTimeUsed(Date firstTimeUsed) {
    if (this.firstTimeUsed == null) {
      this.firstTimeUsed = firstTimeUsed;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    DisposalHold that = (DisposalHold) o;
    return Objects.equals(id, that.id) && Objects.equals(title, that.title)
      && Objects.equals(description, that.description) && Objects.equals(mandate, that.mandate)
      && Objects.equals(scopeNotes, that.scopeNotes) && Objects.equals(createdOn, that.createdOn)
      && Objects.equals(createdBy, that.createdBy) && Objects.equals(updatedOn, that.updatedOn)
      && Objects.equals(updatedBy, that.updatedBy) && Objects.equals(originatedOn, that.originatedOn)
      && Objects.equals(originatedBy, that.originatedBy) && Objects.equals(liftedOn, that.liftedOn)
      && Objects.equals(liftedBy, that.liftedBy) && Objects.equals(firstTimeUsed, that.firstTimeUsed)
      && Objects.equals(aipCounter, that.aipCounter) && state == that.state;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, description, mandate, scopeNotes, createdOn, createdBy, updatedOn, updatedBy,
      originatedOn, originatedBy, liftedOn, liftedBy, firstTimeUsed, aipCounter, state);
  }

  @Override
  public String toString() {
    return "DisposalHold{" + "id='" + id + '\'' + ", title='" + title + '\'' + ", description='" + description + '\''
      + ", mandate='" + mandate + '\'' + ", scopeNotes='" + scopeNotes + '\'' + ", createdOn=" + createdOn
      + ", createdBy='" + createdBy + '\'' + ", updatedOn=" + updatedOn + ", updatedBy='" + updatedBy + '\''
      + ", originatedOn=" + originatedOn + ", originatedBy='" + originatedBy + '\'' + ", liftedOn=" + liftedOn
      + ", liftedBy='" + liftedBy + '\'' + ", firstTimeUsed=" + firstTimeUsed + ", aipCounter=" + aipCounter
      + ", state=" + state + '}';
  }
}
