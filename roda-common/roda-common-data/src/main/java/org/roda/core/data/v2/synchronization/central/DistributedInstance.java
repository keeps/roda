/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.synchronization.central;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.synchronization.RODAInstance;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISTRIBUTED_INSTANCE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DistributedInstance extends RODAInstance {
  private static final long serialVersionUID = 7125122191135652087L;

  private String description;
  private String accessKeyId;
  private String username;

  public DistributedInstance() {
    setStatus(SynchronizingStatus.CREATED);
    setLastSynchronizationDate(null);
    cleanEntitySummaryList();
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DistributedInstance that = (DistributedInstance) o;

    if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null)
      return false;
    if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
      return false;
    if (description != null ? !description.equals(that.description) : that.description != null)
      return false;
    if (accessKeyId != null ? !accessKeyId.equals(that.accessKeyId) : that.accessKeyId != null)
      return false;
    if (username != null ? !username.equals(that.username) : that.username != null)
      return false;
    if (getLastSynchronizationDate() != null ? !getLastSynchronizationDate().equals(that.getLastSynchronizationDate())
      : that.getLastSynchronizationDate() != null)
      return false;
    if (getStatus() != that.getStatus())
      return false;
    if (getCreatedOn() != null ? !getCreatedOn().equals(that.getCreatedOn()) : that.getCreatedOn() != null)
      return false;
    if (getCreatedBy() != null ? !getCreatedBy().equals(that.getCreatedBy()) : that.getCreatedBy() != null)
      return false;
    if (getUpdatedOn() != null ? !getUpdatedOn().equals(that.getUpdatedOn()) : that.getUpdatedOn() != null)
      return false;
    return getUpdatedBy() != null ? getUpdatedBy().equals(that.getUpdatedBy()) : that.getUpdatedBy() == null;
  }

  @Override
  public int hashCode() {
    int result = getId() != null ? getId().hashCode() : 0;
    result = 31 * result + (getName() != null ? getName().hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (accessKeyId != null ? accessKeyId.hashCode() : 0);
    result = 31 * result + (username != null ? username.hashCode() : 0);
    result = 31 * result + (getLastSynchronizationDate() != null ? getLastSynchronizationDate().hashCode() : 0);
    result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
    result = 31 * result + (getCreatedOn() != null ? getCreatedOn().hashCode() : 0);
    result = 31 * result + (getCreatedBy() != null ? getCreatedBy().hashCode() : 0);
    result = 31 * result + (getUpdatedOn() != null ? getUpdatedOn().hashCode() : 0);
    result = 31 * result + (getUpdatedBy() != null ? getUpdatedBy().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DistributedInstance{" + "id='" + getId() + '\'' + ", name='" + getName() + '\'' + ", description='"
      + description + '\'' + ", accessKeyId='" + accessKeyId + '\'' + ", username='" + username + '\''
      + ", lastSyncDate=" + getLastSynchronizationDate() + ", status=" + getStatus() + ", createdOn=" + getCreatedOn()
      + ", createdBy='" + getCreatedBy() + '\'' + ", updatedOn=" + getUpdatedOn() + ", updatedBy='" + getUpdatedBy()
      + '\'' + '}';
  }
}
