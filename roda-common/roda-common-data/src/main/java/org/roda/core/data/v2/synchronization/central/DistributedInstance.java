package org.roda.core.data.v2.synchronization.central;

import java.util.Date;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISTRIBUTED_INSTANCE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DistributedInstance implements IsModelObject {
  private static final int VERSION = 1;
  private static final long serialVersionUID = 7125122191135652087L;

  private String id;
  private String name;
  private String nameIdentifier;
  private String description;
  private String accessKeyId;
  private String username;
  private Date lastSyncDate;
  private DistributedInstanceStatus status;

  private Date createdOn;
  private String createdBy;
  private Date updatedOn;
  private String updatedBy;

  private int syncErrors;

  public DistributedInstance() {
    status = DistributedInstanceStatus.CREATED;
    lastSyncDate = null;
    syncErrors = 0;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNameIdentifier() {
    return nameIdentifier;
  }

  public void setNameIdentifier(String nameIdentifier) {
    this.nameIdentifier = nameIdentifier;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public DistributedInstanceStatus getStatus() {
    return this.status;
  }

  public void setStatus(DistributedInstanceStatus status) {
    this.status = status;
  }

  public String getAccessKeyId() {
    return accessKeyId;
  }

  public void setAccessKeyId(String accessKeyId) {
    this.accessKeyId = accessKeyId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Date getLastSyncDate() {
    return lastSyncDate;
  }

  public void setLastSyncDate(Date lastSyncDate) {
    this.lastSyncDate = lastSyncDate;
  }

  public int getSyncErrors() {
    return this.syncErrors;
  }

  public void setSyncErrors(int syncErrors) {
    this.syncErrors = syncErrors;
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

    DistributedInstance that = (DistributedInstance) o;

    if (id != null ? !id.equals(that.id) : that.id != null)
      return false;
    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;
    if (nameIdentifier != null ? !nameIdentifier.equals(that.nameIdentifier) : that.nameIdentifier != null)
      return false;
    if (description != null ? !description.equals(that.description) : that.description != null)
      return false;
    if (accessKeyId != null ? !accessKeyId.equals(that.accessKeyId) : that.accessKeyId != null)
      return false;
    if (username != null ? !username.equals(that.username) : that.username != null)
      return false;
    if (lastSyncDate != null ? !lastSyncDate.equals(that.lastSyncDate) : that.lastSyncDate != null)
      return false;
    if (status != that.status)
      return false;
    if (createdOn != null ? !createdOn.equals(that.createdOn) : that.createdOn != null)
      return false;
    if (createdBy != null ? !createdBy.equals(that.createdBy) : that.createdBy != null)
      return false;
    if (updatedOn != null ? !updatedOn.equals(that.updatedOn) : that.updatedOn != null)
      return false;
    return updatedBy != null ? updatedBy.equals(that.updatedBy) : that.updatedBy == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (nameIdentifier != null ? nameIdentifier.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (accessKeyId != null ? accessKeyId.hashCode() : 0);
    result = 31 * result + (username != null ? username.hashCode() : 0);
    result = 31 * result + (lastSyncDate != null ? lastSyncDate.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (createdOn != null ? createdOn.hashCode() : 0);
    result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
    result = 31 * result + (updatedOn != null ? updatedOn.hashCode() : 0);
    result = 31 * result + (updatedBy != null ? updatedBy.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DistributedInstance{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", nameIdentifier='"
      + nameIdentifier + '\'' + ", description='" + description + '\'' + ", accessKeyId='" + accessKeyId + '\''
      + ", username='" + username + '\'' + ", lastSyncDate=" + lastSyncDate + ", status=" + status + ", createdOn="
      + createdOn + ", createdBy='" + createdBy + '\'' + ", updatedOn=" + updatedOn + ", updatedBy='" + updatedBy + '\''
      + ", syncErrors='" + syncErrors + '\'' + '}';
  }
}
