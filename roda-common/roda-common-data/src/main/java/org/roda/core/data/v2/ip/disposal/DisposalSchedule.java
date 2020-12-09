package org.roda.core.data.v2.ip.disposal;

import java.util.Date;
import java.util.Objects;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.ip.HasId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_SCHEDULE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalSchedule implements IsModelObject, HasId {
  private static final long serialVersionUID = -2870778207871507847L;
  private static final int VERSION = 1;

  private String id;
  private String title;
  private String description;
  // describes the authority and jurisdiction under which the disposal schedule
  // operates
  private String mandate;
  // provide additional information to users on how the disposal schedule should
  // be interpreted and applied
  private String scopeNotes;

  private DisposalActionCode actionCode;
  private String retentionTriggerElementId;
  private RetentionPeriodIntervalCode retentionPeriodIntervalCode;
  private Integer retentionPeriodDuration;

  // If any AIP was associated by this disposal schedule the date should be set to
  // prevent this disposal from being deleted
  private Date firstTimeUsed = null;

  private Long apiCounter;

  private Date createdOn = null;
  private String createdBy = null;
  private Date updatedOn = null;
  private String updatedBy = null;

  private DisposalScheduleState state;

  public DisposalSchedule() {
    super();
    this.state = DisposalScheduleState.ACTIVE;
    this.apiCounter = 0L;
  }

  public DisposalSchedule(String id, String title, String description, String mandate, String scopeNotes,
    DisposalActionCode actionCode, String retentionTriggerElementId,
    RetentionPeriodIntervalCode retentionPeriodIntervalCode, Integer retentionPeriodDuration, Date createdOn,
    String createdBy) {
    super();
    this.id = id;
    this.title = title;
    this.description = description;
    this.mandate = mandate;
    this.scopeNotes = scopeNotes;
    this.actionCode = actionCode;
    this.retentionTriggerElementId = retentionTriggerElementId;
    this.retentionPeriodIntervalCode = retentionPeriodIntervalCode;
    this.retentionPeriodDuration = retentionPeriodDuration;
    this.state = DisposalScheduleState.ACTIVE;
    this.apiCounter = 0L;

    this.createdOn = createdOn;
    this.createdBy = createdBy;
    this.updatedOn = createdOn;
    this.updatedBy = createdBy;
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return VERSION;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
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

  public DisposalActionCode getActionCode() {
    return actionCode;
  }

  public void setActionCode(DisposalActionCode actionCode) {
    this.actionCode = actionCode;
  }

  public String getRetentionTriggerElementId() {
    return retentionTriggerElementId;
  }

  public void setRetentionTriggerElementId(String retentionTriggerElementId) {
    this.retentionTriggerElementId = retentionTriggerElementId;
  }

  public RetentionPeriodIntervalCode getRetentionPeriodIntervalCode() {
    return retentionPeriodIntervalCode;
  }

  public void setRetentionPeriodIntervalCode(RetentionPeriodIntervalCode retentionPeriodIntervalCode) {
    this.retentionPeriodIntervalCode = retentionPeriodIntervalCode;
  }

  public Integer getRetentionPeriodDuration() {
    return retentionPeriodDuration;
  }

  public void setRetentionPeriodDuration(Integer retentionPeriodDuration) {
    this.retentionPeriodDuration = retentionPeriodDuration;
  }

  public Date getFirstTimeUsed() {
    return firstTimeUsed;
  }

  public void setFirstTimeUsed(Date firstTimeUsed) {
    if (this.firstTimeUsed == null) {
      this.firstTimeUsed = firstTimeUsed;
    }
  }

  public Long getApiCounter() {
    return apiCounter;
  }

  public void setApiCounter(Long apiCounter) {
    this.apiCounter = apiCounter;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
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

  public DisposalScheduleState getState() {
    return state;
  }

  public void setState(DisposalScheduleState state) {
    this.state = state;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    DisposalSchedule that = (DisposalSchedule) o;
    return Objects.equals(id, that.id) && Objects.equals(title, that.title)
      && Objects.equals(description, that.description) && Objects.equals(mandate, that.mandate)
      && Objects.equals(scopeNotes, that.scopeNotes) && actionCode == that.actionCode
      && Objects.equals(retentionTriggerElementId, that.retentionTriggerElementId)
      && retentionPeriodIntervalCode == that.retentionPeriodIntervalCode
      && Objects.equals(retentionPeriodDuration, that.retentionPeriodDuration)
      && Objects.equals(firstTimeUsed, that.firstTimeUsed) && Objects.equals(apiCounter, that.apiCounter)
      && Objects.equals(createdOn, that.createdOn) && Objects.equals(createdBy, that.createdBy)
      && Objects.equals(updatedOn, that.updatedOn) && Objects.equals(updatedBy, that.updatedBy) && state == that.state;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, description, mandate, scopeNotes, actionCode, retentionTriggerElementId,
      retentionPeriodIntervalCode, retentionPeriodDuration, firstTimeUsed, apiCounter, createdOn, createdBy, updatedOn,
      updatedBy, state);
  }

  @Override
  public String toString() {
    return "DisposalSchedule{" + "id='" + id + '\'' + ", title='" + title + '\'' + ", description='" + description
      + '\'' + ", mandate='" + mandate + '\'' + ", scopeNotes='" + scopeNotes + '\'' + ", actionCode=" + actionCode
      + ", retentionTriggerElementId='" + retentionTriggerElementId + '\'' + ", retentionPeriodIntervalCode="
      + retentionPeriodIntervalCode + ", retentionPeriodDuration=" + retentionPeriodDuration + ", destroyedTimestamp="
      + firstTimeUsed + ", numberOfAIPUnder=" + apiCounter + ", createdOn=" + createdOn + ", createdBy='" + createdBy
      + '\'' + ", updatedOn=" + updatedOn + ", updatedBy='" + updatedBy + '\'' + ", state=" + state + '}';
  }
}
