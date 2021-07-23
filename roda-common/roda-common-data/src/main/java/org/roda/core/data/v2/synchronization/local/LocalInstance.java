package org.roda.core.data.v2.synchronization.local;

import java.util.Date;
import java.util.Objects;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_LOCAL_INSTANCE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocalInstance implements IsModelObject {
  private static final int VERSION = 1;
  private static final long serialVersionUID = -1056506624373739060L;

  private String id;
  private String accessKey;
  private String centralInstanceURL;
  private String bundlePath;
  private Boolean isRegistered;
  private Date lastSynchronizationDate;

  private LocalInstanceIdentifierState instanceIdentifierState = LocalInstanceIdentifierState.INACTIVE;

  private Date createdOn;
  private String createdBy;
  private Date updatedOn;
  private String updatedBy;

  public LocalInstance() {
    isRegistered = false;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getCentralInstanceURL() {
    return centralInstanceURL;
  }

  public void setCentralInstanceURL(String centralInstanceURL) {
    this.centralInstanceURL = centralInstanceURL;
  }

  public String getBundlePath() {
    return bundlePath;
  }

  public void setBundlePath(String bundlePath) {
    this.bundlePath = bundlePath;
  }

  public Boolean getIsRegistered() {
    return isRegistered;
  }

  public void setIsRegistered(Boolean registered) {
    isRegistered = registered;
  }

  public Date getLastSynchronizationDate() {
    return lastSynchronizationDate;
  }

  public void setLastSynchronizationDate(Date lastSynchronizationDate) {
    this.lastSynchronizationDate = lastSynchronizationDate;
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

  public LocalInstanceIdentifierState getInstanceIdentifierState() {
    return instanceIdentifierState;
  }

  public void setInstanceIdentifierState(LocalInstanceIdentifierState instanceIdentifierState) {
    this.instanceIdentifierState = instanceIdentifierState;
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
    LocalInstance that = (LocalInstance) o;
    return Objects.equals(id, that.id) && Objects.equals(accessKey, that.accessKey)
      && Objects.equals(centralInstanceURL, that.centralInstanceURL) && Objects.equals(bundlePath, that.bundlePath)
      && Objects.equals(isRegistered, that.isRegistered)
      && Objects.equals(lastSynchronizationDate, that.lastSynchronizationDate)
      && instanceIdentifierState == that.instanceIdentifierState && Objects.equals(createdOn, that.createdOn)
      && Objects.equals(createdBy, that.createdBy) && Objects.equals(updatedOn, that.updatedOn)
      && Objects.equals(updatedBy, that.updatedBy);
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (accessKey != null ? accessKey.hashCode() : 0);
    result = 31 * result + (centralInstanceURL != null ? centralInstanceURL.hashCode() : 0);
    result = 31 * result + (bundlePath != null ? bundlePath.hashCode() : 0);
    result = 31 * result + (isRegistered != null ? isRegistered.hashCode() : 0);
    result = 31 * result + (lastSynchronizationDate != null ? lastSynchronizationDate.hashCode() : 0);
    result = 31 * result + (createdOn != null ? createdOn.hashCode() : 0);
    result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
    result = 31 * result + (updatedOn != null ? updatedOn.hashCode() : 0);
    result = 31 * result + (updatedBy != null ? updatedBy.hashCode() : 0);
    return result;
  }
}
