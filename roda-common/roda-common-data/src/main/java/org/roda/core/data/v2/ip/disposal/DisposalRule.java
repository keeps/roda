package org.roda.core.data.v2.ip.disposal;

import java.util.Date;
import java.util.Objects;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.ip.HasId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_RULE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalRule implements IsModelObject, HasId, Comparable<DisposalRule> {

  private static final int VERSION = 0;
  private static final long serialVersionUID = 247288959307666429L;

  private String id;
  private String title;

  private String description;

  private String key;
  private String value;

  private String disposalScheduleId;
  private String disposalScheduleName;

  private Integer order;

  private Date createdOn = null;
  private String createdBy = null;
  private Date updatedOn = null;
  private String updatedBy = null;

  public DisposalRule() {
    super();
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

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDisposalScheduleId() {
    return disposalScheduleId;
  }

  public void setDisposalScheduleId(String disposalScheduleId) {
    this.disposalScheduleId = disposalScheduleId;
  }

  public String getDisposalScheduleName() {
    return disposalScheduleName;
  }

  public void setDisposalScheduleName(String disposalScheduleName) {
    this.disposalScheduleName = disposalScheduleName;
  }

  public Integer getOrder() {
    return order;
  }

  public void setOrder(Integer order) {
    this.order = order;
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
    DisposalRule that = (DisposalRule) o;
    return Objects.equals(id, that.id) && Objects.equals(title, that.title)
      && Objects.equals(description, that.description) && Objects.equals(key, that.key)
      && Objects.equals(value, that.value) && Objects.equals(disposalScheduleId, that.disposalScheduleId)
      && Objects.equals(disposalScheduleName, that.disposalScheduleName) && Objects.equals(order, that.order)
      && Objects.equals(createdOn, that.createdOn) && Objects.equals(createdBy, that.createdBy)
      && Objects.equals(updatedOn, that.updatedOn) && Objects.equals(updatedBy, that.updatedBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, description, key, value, disposalScheduleId, disposalScheduleName, order, createdOn,
      createdBy, updatedOn, updatedBy);
  }

  @Override
  public String toString() {
    return "DisposalRule{" + "id='" + id + '\'' + ", title='" + title + '\'' + ", description='" + description + '\''
      + ", key='" + key + '\'' + ", value='" + value + '\'' + ", disposalScheduleId='" + disposalScheduleId + '\''
      + ", disposalScheduleName='" + disposalScheduleName + '\'' + ", order=" + order + ", createdOn=" + createdOn
      + ", createdBy='" + createdBy + '\'' + ", updatedOn=" + updatedOn + ", updatedBy='" + updatedBy + '\'' + '}';
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return VERSION;
  }

  @Override
  public int compareTo(DisposalRule otherRule) {
    return Integer.compare(this.getOrder(), otherRule.getOrder());
  }
}
