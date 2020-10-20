package org.roda.core.data.v2.ip.disposal;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

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

  // the date on which the legal or administrative order was issued
  private Date originatedOn;
  private String originatedBy;

  private Date liftedOn;
  private String liftedBy;

  private Date updatedOn;
  private String updatedBy;

  private Map<String, Date> activeAIPs;
  private Map<String, Date> inactiveAIPs;

  private DisposalHoldState state;

  public DisposalHold() {
    super();
    this.state = DisposalHoldState.ACTIVE;
    this.activeAIPs = new TreeMap<>();
    this.inactiveAIPs = new TreeMap<>();
  }

  public DisposalHold(String disposalHoldId, String title, String description, String mandate, String scopeNotes) {
    this.id = disposalHoldId;
    this.title = title;
    this.description = description;
    this.mandate = mandate;
    this.scopeNotes = scopeNotes;
    this.activeAIPs = new TreeMap<>();
    this.inactiveAIPs = new TreeMap<>();
    this.state = DisposalHoldState.ACTIVE;
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

  public Map<String, Date> getActiveAIPs() {
    return activeAIPs;
  }

  public void setActiveAIPs(Map<String, Date> activeAIPs) {
    this.activeAIPs = activeAIPs;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Map<String, Date> getInactiveAIPs() {
    return inactiveAIPs;
  }

  public void setInactiveAIPs(Map<String, Date> inactiveAIPs) {
    this.inactiveAIPs = inactiveAIPs;
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

  @JsonIgnore
  public void addAIPtoActiveAIPs(String aipId) {
    getActiveAIPs().put(aipId, new Date());
  }

  @JsonIgnore
  public void removeActiveAIP(String aipId) {
    getActiveAIPs().remove(aipId);
    getInactiveAIPs().put(aipId, new Date());
  }

  @JsonIgnore
  public void liftAllAIPs() {
    getActiveAIPs().forEach((key, value) -> {
      getInactiveAIPs().put(key, new Date());
    });

    getActiveAIPs().clear();
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
      && Objects.equals(createdBy, that.createdBy) && Objects.equals(originatedOn, that.originatedOn)
      && Objects.equals(originatedBy, that.originatedBy) && Objects.equals(liftedOn, that.liftedOn)
      && Objects.equals(liftedBy, that.liftedBy) && Objects.equals(updatedOn, that.updatedOn)
      && Objects.equals(updatedBy, that.updatedBy) && Objects.equals(activeAIPs, that.activeAIPs)
      && Objects.equals(inactiveAIPs, that.inactiveAIPs) && state == that.state;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, description, mandate, scopeNotes, createdOn, createdBy, originatedOn, originatedBy,
      liftedOn, liftedBy, updatedOn, updatedBy, activeAIPs, inactiveAIPs, state);
  }

  @Override
  public String toString() {
    return "DisposalHold{" + "id='" + id + '\'' + ", title='" + title + '\'' + ", description='" + description + '\''
      + ", mandate='" + mandate + '\'' + ", scopeNotes='" + scopeNotes + '\'' + ", createdOn=" + createdOn
      + ", createdBy='" + createdBy + '\'' + ", originatedOn=" + originatedOn + ", originatedBy='" + originatedBy + '\''
      + ", liftedOn=" + liftedOn + ", liftedBy='" + liftedBy + '\'' + ", updatedOn=" + updatedOn + ", updatedBy='"
      + updatedBy + '\'' + ", activeAIPs=" + activeAIPs + ", inactiveAIPs=" + inactiveAIPs + ", state=" + state + '}';
  }
}
