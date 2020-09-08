package org.roda.core.data.v2.ip.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.ip.HasId;

import java.util.Date;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_SCHEDULE_METADATA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalSchedule implements IsModelObject, HasId {
  private static final long serialVersionUID = -6510051884231824005L;

  public enum DisposalActionCode {
    RETAIN_PERMANENTLY, REVIEW, DESTROY;
  }

  public enum RetentionTriggerCode {
    FROM_NOW, FROM_RECORD_ORIGINATED_DATE, FROM_RECORD_METADATA_DATE;
  }

  public enum RetentionPeriodIntervalCode {
    NO_RETENTION_PERIOD, DAYS, WEEKS, MONTHS, YEARS;
  }

  private String id;

  private Date originatedOn = null;
  private String tile;
  private String description;
  private String mandate;
  private String scopeNotes;

  private DisposalActionCode disposalActionCode;
  private RetentionTriggerCode retentionTriggerCode;
  private String retentionTriggerElementIdentifier;
  private RetentionPeriodIntervalCode retentionPeriodIntervalCode;
  private String retentionPeriodDuration;

  private Date createdOn = null;
  private String createdBy = null;
  private Date updatedOn = null;
  private String updatedBy = null;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public Date getOriginatedOn() {
    return originatedOn;
  }

  public void setOriginatedOn(Date originatedOn) {
    this.originatedOn = originatedOn;
  }

  public String getTile() {
    return tile;
  }

  public void setTile(String tile) {
    this.tile = tile;
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

  public DisposalActionCode getDisposalActionCode() {
    return disposalActionCode;
  }

  public void setDisposalActionCode(DisposalActionCode disposalActionCode) {
    this.disposalActionCode = disposalActionCode;
  }

  public RetentionTriggerCode getRetentionTriggerCode() {
    return retentionTriggerCode;
  }

  public void setRetentionTriggerCode(RetentionTriggerCode retentionTriggerCode) {
    this.retentionTriggerCode = retentionTriggerCode;
  }

  public String getRetentionTriggerElementIdentifier() {
    return retentionTriggerElementIdentifier;
  }

  public void setRetentionTriggerElementIdentifier(String retentionTriggerElementIdentifier) {
    this.retentionTriggerElementIdentifier = retentionTriggerElementIdentifier;
  }

  public RetentionPeriodIntervalCode getRetentionPeriodIntervalCode() {
    return retentionPeriodIntervalCode;
  }

  public void setRetentionPeriodIntervalCode(RetentionPeriodIntervalCode retentionPeriodIntervalCode) {
    this.retentionPeriodIntervalCode = retentionPeriodIntervalCode;
  }

  public String getRetentionPeriodDuration() {
    return retentionPeriodDuration;
  }

  public void setRetentionPeriodDuration(String retentionPeriodDuration) {
    this.retentionPeriodDuration = retentionPeriodDuration;
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

  /**
   * 20161102 hsilva: a <code>@JsonIgnore</code> should be added to avoid
   * serializing
   */
  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 1;
  }
}
