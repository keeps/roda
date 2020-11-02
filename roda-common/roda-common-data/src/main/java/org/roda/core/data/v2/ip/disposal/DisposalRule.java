package org.roda.core.data.v2.ip.disposal;

import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.HasId;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_RULE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalRule implements IsModelObject, HasId {

  private static final long serialVersionUID = 2641907966434061053L;
  private static final int VERSION = 1;

  private String id;
  private String title;

  private Pair<String, String> ruleKey;

  private String disposalScheduleId;

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

  public Pair<String, String> getRuleKey() {
    return ruleKey;
  }

  public void setRuleKey(Pair<String, String> ruleKey) {
    this.ruleKey = ruleKey;
  }

  public String getDisposalScheduleId() {
    return disposalScheduleId;
  }

  public void setDisposalScheduleId(String disposalScheduleId) {
    this.disposalScheduleId = disposalScheduleId;
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

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return VERSION;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    DisposalRule that = (DisposalRule) o;
    return Objects.equals(id, that.id) && Objects.equals(title, that.title) && Objects.equals(ruleKey, that.ruleKey)
      && Objects.equals(disposalScheduleId, that.disposalScheduleId) && Objects.equals(order, that.order)
      && Objects.equals(createdOn, that.createdOn) && Objects.equals(createdBy, that.createdBy)
      && Objects.equals(updatedOn, that.updatedOn) && Objects.equals(updatedBy, that.updatedBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, ruleKey, disposalScheduleId, order, createdOn, createdBy, updatedOn, updatedBy);
  }

  @Override
  public String toString() {
    return "DisposalRule{" + "id='" + id + '\'' + ", title='" + title + '\'' + ", ruleKey=" + ruleKey
      + ", disposalScheduleId='" + disposalScheduleId + '\'' + ", order=" + order + ", createdOn=" + createdOn
      + ", createdBy='" + createdBy + '\'' + ", updatedOn=" + updatedOn + ", updatedBy='" + updatedBy + '\'' + '}';
  }
}
