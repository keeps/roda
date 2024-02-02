/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.synchronization.local;

import java.io.Serial;
import java.util.Objects;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.synchronization.RODAInstance;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_LOCAL_INSTANCE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocalInstance extends RODAInstance {

  @Serial
  private static final long serialVersionUID = -1056506624373739060L;

  private String accessKey;
  private String centralInstanceURL;
  private Boolean isSubscribed;

  public LocalInstance() {
    setStatus(SynchronizingStatus.INACTIVE);
    isSubscribed = false;
    cleanEntitySummaryList();
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

  public Boolean getIsSubscribed() {
    return isSubscribed;
  }

  public void setIsSubscribed(Boolean subscribed) {
    isSubscribed = subscribed;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    LocalInstance that = (LocalInstance) o;
    return Objects.equals(getId(), that.getId()) && Objects.equals(accessKey, that.accessKey)
      && Objects.equals(centralInstanceURL, that.centralInstanceURL) && Objects.equals(isSubscribed, that.isSubscribed)
      && Objects.equals(getLastSynchronizationDate(), that.getLastSynchronizationDate())
      && Objects.equals(getCreatedBy(), that.getCreatedBy()) && Objects.equals(getUpdatedOn(), that.getUpdatedOn())
      && Objects.equals(getUpdatedBy(), that.getUpdatedBy());
  }

  @Override
  public int hashCode() {
    int result = getId() != null ? getId().hashCode() : 0;
    result = 31 * result + (accessKey != null ? accessKey.hashCode() : 0);
    result = 31 * result + (centralInstanceURL != null ? centralInstanceURL.hashCode() : 0);
    result = 31 * result + (isSubscribed != null ? isSubscribed.hashCode() : 0);
    result = 31 * result + (getLastSynchronizationDate() != null ? getLastSynchronizationDate().hashCode() : 0);
    result = 31 * result + (getCreatedOn() != null ? getCreatedOn().hashCode() : 0);
    result = 31 * result + (getCreatedBy() != null ? getCreatedBy().hashCode() : 0);
    result = 31 * result + (getCreatedOn() != null ? getUpdatedOn().hashCode() : 0);
    result = 31 * result + (getUpdatedBy() != null ? getUpdatedBy().hashCode() : 0);
    return result;
  }
}
