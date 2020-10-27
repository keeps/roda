package org.roda.core.data.v2.ip.disposal;

import java.util.ArrayList;
import java.util.Arrays;
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
public class DisposalConfirmationMetadata implements IsModelObject, IsIndexed, HasId {
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

  private String approver = null;

  private Long size = null;
  private Long numberOfAIPs = null;
  private Long numberOfCollections = null;

  private List<String> disposalScheduleIds;
  private List<String> disposalHoldIds;

  private Map<String, Object> fields;

  public DisposalConfirmationMetadata() {
    super();
    state = DisposalConfirmationState.PENDING;
    disposalScheduleIds = new ArrayList<String>();
    disposalHoldIds = new ArrayList<String>();
    fields = new HashMap<>();
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

  public String getApprover() {
    return approver;
  }

  public void setApprover(String approver) {
    this.approver = approver;
  }

  public Long getNumberOfAIPs() {
    return numberOfAIPs;
  }

  public void setNumberOfAIPs(Long numberOfAIPs) {
    this.numberOfAIPs = numberOfAIPs;
  }

  public Long getNumberOfCollections() {
    return numberOfCollections;
  }

  public void setNumberOfCollections(Long numberOfCollections) {
    this.numberOfCollections = numberOfCollections;
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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    DisposalConfirmationMetadata that = (DisposalConfirmationMetadata) o;
    return Objects.equals(getId(), that.getId()) && Objects.equals(getTitle(), that.getTitle())
      && getState() == that.getState() && Objects.equals(getCreatedOn(), that.getCreatedOn())
      && Objects.equals(getCreatedBy(), that.getCreatedBy()) && Objects.equals(getExecutedOn(), that.getExecutedOn())
      && Objects.equals(getExecutedBy(), that.getExecutedBy()) && Objects.equals(getRestoredOn(), that.getRestoredOn())
      && Objects.equals(getRestoredBy(), that.getRestoredBy()) && Objects.equals(getApprover(), that.getApprover())
      && Objects.equals(getSize(), that.getSize()) && Objects.equals(getNumberOfAIPs(), that.getNumberOfAIPs())
      && Objects.equals(getNumberOfCollections(), that.getNumberOfCollections())
      && Objects.equals(getDisposalScheduleIds(), that.getDisposalScheduleIds())
      && Objects.equals(getDisposalHoldIds(), that.getDisposalHoldIds())
      && Objects.equals(getFields(), that.getFields());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getTitle(), getState(), getCreatedOn(), getCreatedBy(), getExecutedOn(),
      getExecutedBy(), getRestoredOn(), getRestoredBy(), getApprover(), getSize(), getNumberOfAIPs(),
      getNumberOfCollections(), getDisposalScheduleIds(), getDisposalHoldIds(), getFields());
  }

  @Override
  public String toString() {
    return "DisposalConfirmationMetadata{" + "id='" + id + '\'' + ", title='" + title + '\'' + ", state=" + state
      + ", createdOn=" + createdOn + ", createdBy='" + createdBy + '\'' + ", executedOn=" + executedOn
      + ", executedBy='" + executedBy + '\'' + ", restoredOn=" + restoredOn + ", restoredBy='" + restoredBy + '\''
      + ", approver='" + approver + '\'' + ", size=" + size + ", numberOfAIPs=" + numberOfAIPs
      + ", numberOfCollections=" + numberOfCollections + ", disposalScheduleIds=" + disposalScheduleIds
      + ", disposalHoldIds=" + disposalHoldIds + ", fields=" + fields + '}';
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
      "numberOfAIPs", "numberOfCollections", "size");
  }

  /**
   * Return CSV values for this object.
   *
   * @return a {@link List} of {@link Object} with the CSV values.
   */
  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(id, title, createdOn, createdBy, executedOn, executedBy, approver, numberOfAIPs,
      numberOfCollections, size);
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
