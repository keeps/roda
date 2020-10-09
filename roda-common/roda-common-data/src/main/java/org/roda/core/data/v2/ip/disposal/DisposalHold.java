package org.roda.core.data.v2.ip.disposal;

import java.util.Date;
import java.util.Map;

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
  private String mandate;
  private String scopeNotes;

  private Date createdOn;
  private String createdBy;

  private Date liftedOn;
  private String liftedBy;

  private Date updatedOn;
  private String updatedBy;

  private Map<Date, String> activeAIPs;
  private Map<Date, String> inactiveAIPs;

  public DisposalHold(String disposalHoldId, String title, String description, String mandate, String scopeNotes) {
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

  public Map<Date, String> getActiveAIPs() {
    return activeAIPs;
  }

  public void setActiveAIPs(Map<Date, String> activeAIPs) {
    this.activeAIPs = activeAIPs;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Map<Date, String> getInactiveAIPs() {
    return inactiveAIPs;
  }

  public void setInactiveAIPs(Map<Date, String> inactiveAIPs) {
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
}
