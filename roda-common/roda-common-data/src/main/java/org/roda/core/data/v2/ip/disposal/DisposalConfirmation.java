package org.roda.core.data.v2.ip.disposal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.HasId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_CONFIRMATION_METADATA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalConfirmation implements IsModelObject, IsIndexed, HasId {
  private static final long serialVersionUID = 1429757961220532837L;

  private String id;
  private String title;
  private DisposalConfirmationState state;

  private Date createdOn;
  private String createdBy;

  private Date executedOn = null;
  private String executedBy = null;

  private Date restoredOn = null;
  private String restoredBy = null;

  private Map<String, String> extraFields;

  private Long size = null;
  private Long numberOfAIPs = null;

  private List<String> disposalScheduleIds;
  private List<String> disposalHoldIds;

  private Map<String, Object> fields;

  public DisposalConfirmation() {
    super();
    state = DisposalConfirmationState.PENDING;
    disposalScheduleIds = new ArrayList<>();
    disposalHoldIds = new ArrayList<>();
    fields = new HashMap<>();
    extraFields = new HashMap<>();
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

  public Map<String, String> getExtraFields() {
    return extraFields;
  }

  public void setExtraFields(Map<String, String> extraFields) {
    this.extraFields = extraFields;
  }

  public Long getNumberOfAIPs() {
    return numberOfAIPs;
  }

  public void setNumberOfAIPs(Long numberOfAIPs) {
    this.numberOfAIPs = numberOfAIPs;
  }

  public DisposalConfirmationState getState() {
    return state;
  }

  public void setState(DisposalConfirmationState state) {
    this.state = state;
  }

  public Date getExecutedOn() {
    return executedOn;
  }

  public void setExecutedOn(Date executedOn) {
    this.executedOn = executedOn;
  }

  public String getExecutedBy() {
    return executedBy;
  }

  public void setExecutedBy(String executedBy) {
    this.executedBy = executedBy;
  }

  public Date getRestoredOn() {
    return restoredOn;
  }

  public void setRestoredOn(Date restoredOn) {
    this.restoredOn = restoredOn;
  }

  public String getRestoredBy() {
    return restoredBy;
  }

  public void setRestoredBy(String restoredBy) {
    this.restoredBy = restoredBy;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public List<String> getDisposalScheduleIds() {
    return disposalScheduleIds;
  }

  public void setDisposalScheduleIds(List<String> disposalScheduleIds) {
    this.disposalScheduleIds = disposalScheduleIds;
  }

  public List<String> getDisposalHoldIds() {
    return disposalHoldIds;
  }

  public void setDisposalHoldIds(List<String> disposalHoldIds) {
    this.disposalHoldIds = disposalHoldIds;
  }

  @JsonIgnore
  public void addDisposalHoldIds(Collection<String> disposalHoldIds) {
    this.disposalHoldIds.addAll(disposalHoldIds);
  }

  @JsonIgnore
  public void addDisposalScheduleIds(Collection<String> disposalScheduleIds) {
    this.disposalScheduleIds.addAll(disposalScheduleIds);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    DisposalConfirmation that = (DisposalConfirmation) o;
    return Objects.equals(id, that.id) && Objects.equals(title, that.title) && state == that.state
      && Objects.equals(createdOn, that.createdOn) && Objects.equals(createdBy, that.createdBy)
      && Objects.equals(executedOn, that.executedOn) && Objects.equals(executedBy, that.executedBy)
      && Objects.equals(restoredOn, that.restoredOn) && Objects.equals(restoredBy, that.restoredBy)
      && Objects.equals(extraFields, that.extraFields) && Objects.equals(size, that.size)
      && Objects.equals(numberOfAIPs, that.numberOfAIPs)
      && Objects.equals(disposalScheduleIds, that.disposalScheduleIds)
      && Objects.equals(disposalHoldIds, that.disposalHoldIds) && Objects.equals(fields, that.fields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, state, createdOn, createdBy, executedOn, executedBy, restoredOn, restoredBy,
      extraFields, size, numberOfAIPs, disposalScheduleIds, disposalHoldIds, fields);
  }

  @Override
  public String toString() {
    return "DisposalConfirmation{" + "id='" + id + '\'' + ", title='" + title + '\'' + ", state=" + state
      + ", createdOn=" + createdOn + ", createdBy='" + createdBy + '\'' + ", executedOn=" + executedOn
      + ", executedBy='" + executedBy + '\'' + ", restoredOn=" + restoredOn + ", restoredBy='" + restoredBy + '\''
      + ", extraFields=" + extraFields + ", size=" + size + ", numberOfAIPs=" + numberOfAIPs + ", numberOfCollections="
      + disposalScheduleIds + ", disposalHoldIds=" + disposalHoldIds + ", fields=" + fields + '}';
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
  }

  /**
   * Return CSV header names for this object.
   *
   * @return a {@link List} of {@link String} with the header names.
   */
  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "title", "createdOn", "createdBy", "executedOn", "executedBy", "approver",
      "numberOfAIPs", "size");
  }

  /**
   * Return CSV values for this object.
   *
   * @return a {@link List} of {@link Object} with the CSV values.
   */
  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(id, title, createdOn, createdBy, executedOn, executedBy, numberOfAIPs, size);
  }

  /**
   * Return the fields to create lite
   *
   * @return a {@link List} of {@link String} with the fields.
   */
  @Override
  public List<String> liteFields() {
    return Collections.singletonList(RodaConstants.INDEX_UUID);
  }

  @JsonIgnore
  @Override
  public Map<String, Object> getFields() {
    return fields;
  }

  @JsonIgnore
  @Override
  public void setFields(Map<String, Object> fields) {
    this.fields = fields;
  }
}
