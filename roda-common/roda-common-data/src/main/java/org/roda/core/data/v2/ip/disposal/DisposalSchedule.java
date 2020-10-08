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
  private String mandate;
  private String scopeNotes;

  private DisposalActionCode actionCode;
  private RetentionTriggerCode retentionTriggerCode;
  private String retentionTriggerElementId;
  private RetentionPeriodIntervalCode retentionPeriodIntervalCode;
  private Integer retentionPeriodDuration;

  // If any AIP was destroyed by this disposal schedule the date should be set to
  // prevent this disposal from being deleted
  private Date destroyedTimestamp = null;

  private Date createdOn = null;
  private String createdBy = null;
  private Date updatedOn = null;
  private String updatedBy = null;

  public DisposalSchedule() {
    super();
  }

  public DisposalSchedule(String id, String title, String description, String mandate, String scopeNotes,
    DisposalActionCode actionCode, RetentionTriggerCode retentionTriggerCode, String retentionTriggerElementId,
    RetentionPeriodIntervalCode retentionPeriodIntervalCode, Integer retentionPeriodDuration, String createdBy) {
    super();
    this.id = id;
    this.title = title;
    this.description = description;
    this.mandate = mandate;
    this.scopeNotes = scopeNotes;
    this.actionCode = actionCode;
    this.retentionTriggerCode = retentionTriggerCode;
    this.retentionTriggerElementId = retentionTriggerElementId;
    this.retentionPeriodIntervalCode = retentionPeriodIntervalCode;
    this.retentionPeriodDuration = retentionPeriodDuration;

    this.createdOn = new Date();
    this.createdBy = createdBy;
    this.updatedOn = new Date();
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

  public RetentionTriggerCode getRetentionTriggerCode() {
    return retentionTriggerCode;
  }

  public void setRetentionTriggerCode(RetentionTriggerCode retentionTriggerCode) {
    this.retentionTriggerCode = retentionTriggerCode;
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

  public Date getDestroyedTimestamp() {
    return destroyedTimestamp;
  }

  public void setDestroyedTimestamp(Date destroyedTimestamp) {
    this.destroyedTimestamp = destroyedTimestamp;
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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    DisposalSchedule that = (DisposalSchedule) o;
    return Objects.equals(getId(), that.getId()) && Objects.equals(getTitle(), that.getTitle())
      && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getMandate(), that.getMandate())
      && Objects.equals(getScopeNotes(), that.getScopeNotes()) && getActionCode() == that.getActionCode()
      && getRetentionTriggerCode() == that.getRetentionTriggerCode()
      && Objects.equals(getRetentionTriggerElementId(), that.getRetentionTriggerElementId())
      && getRetentionPeriodIntervalCode() == that.getRetentionPeriodIntervalCode()
      && Objects.equals(getRetentionPeriodDuration(), that.getRetentionPeriodDuration())
      && Objects.equals(getDestroyedTimestamp(), that.getDestroyedTimestamp())
      && Objects.equals(getCreatedOn(), that.getCreatedOn()) && Objects.equals(getCreatedBy(), that.getCreatedBy())
      && Objects.equals(getUpdatedOn(), that.getUpdatedOn()) && Objects.equals(getUpdatedBy(), that.getUpdatedBy());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getTitle(), getDescription(), getMandate(), getScopeNotes(), getActionCode(),
      getRetentionTriggerCode(), getRetentionTriggerElementId(), getRetentionPeriodIntervalCode(),
      getRetentionPeriodDuration(), getDestroyedTimestamp(), getCreatedOn(), getCreatedBy(), getUpdatedOn(),
      getUpdatedBy());
  }

  @Override
  public String toString() {
    return "DisposalSchedule{" + "id='" + id + '\'' + ", title='" + title + '\'' + ", description='" + description
      + '\'' + ", mandate='" + mandate + '\'' + ", scopeNotes='" + scopeNotes + '\'' + ", actionCode=" + actionCode
      + ", retentionTriggerCode=" + retentionTriggerCode + ", retentionTriggerElementId='" + retentionTriggerElementId
      + '\'' + ", retentionPeriodIntervalCode=" + retentionPeriodIntervalCode + ", retentionPeriodDuration='"
      + retentionPeriodDuration + '\'' + ", destroyedTimestamp=" + destroyedTimestamp + ", createdOn=" + createdOn
      + ", createdBy='" + createdBy + '\'' + ", updatedOn=" + updatedOn + ", updatedBy='" + updatedBy + '\'' + '}';
  }
}
