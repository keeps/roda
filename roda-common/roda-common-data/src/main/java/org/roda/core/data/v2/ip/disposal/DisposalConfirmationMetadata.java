package org.roda.core.data.v2.ip.disposal;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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

  private Date createdOn;
  private String createdBy;
  private Date updatedOn;
  private String updatedBy;

  private String approver;
  private Long numberOfAIPs;
  private Long numberOfCollections;

  private DisposalConfirmationState state;

  private Map<String, Object> fields;

  public DisposalConfirmationMetadata() {
    super();
    state = DisposalConfirmationState.PENDING;
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
    DisposalConfirmationMetadata that = (DisposalConfirmationMetadata) o;
    return Objects.equals(getId(), that.getId()) && Objects.equals(getCreatedOn(), that.getCreatedOn())
      && Objects.equals(getCreatedBy(), that.getCreatedBy()) && Objects.equals(getUpdatedOn(), that.getUpdatedOn())
      && Objects.equals(getUpdatedBy(), that.getUpdatedBy()) && Objects.equals(getApprover(), that.getApprover())
      && Objects.equals(getNumberOfAIPs(), that.getNumberOfAIPs())
      && Objects.equals(getNumberOfCollections(), that.getNumberOfCollections()) && getState() == that.getState();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getCreatedOn(), getCreatedBy(), getUpdatedOn(), getUpdatedBy(), getApprover(),
      getNumberOfAIPs(), getNumberOfCollections(), getState());
  }

  @Override
  public String toString() {
    return "DisposalConfirmation{" + "id='" + id + '\'' + ", createdOn=" + createdOn + ", createdBy='" + createdBy
      + '\'' + ", updatedOn=" + updatedOn + ", updatedBy='" + updatedBy + '\'' + ", approver='" + approver + '\''
      + ", numberOfAIPs=" + numberOfAIPs + ", numberOfCollections=" + numberOfCollections + ", state=" + state + '}';
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
    return Arrays.asList("id", "createdOn", "createdBy", "approver", "numberOfAIPs", "numberOfCollections");
  }

  /**
   * Return CSV values for this object.
   *
   * @return a {@link List} of {@link Object} with the CSV values.
   */
  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(id, createdBy, createdOn, approver, numberOfAIPs, numberOfCollections);
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

  /**
   * Return fields as they are indexed
   *
   * @return
   */
  @Override
  public Map<String, Object> getFields() {
    return fields;
  }

  /**
   * Set fields as they are indexed
   *
   * @param fields
   * @return
   */
  @Override
  public void setFields(Map<String, Object> fields) {
    this.fields = fields;
  }
}
