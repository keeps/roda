package org.roda.core.data.v2.distributedInstance;

import java.util.Date;

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
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    LocalInstance that = (LocalInstance) o;

    if (id != null ? !id.equals(that.id) : that.id != null)
      return false;
    if (accessKey != null ? !accessKey.equals(that.accessKey) : that.accessKey != null)
      return false;
    if (centralInstanceURL != null ? !centralInstanceURL.equals(that.centralInstanceURL)
      : that.centralInstanceURL != null)
      return false;
    if (isRegistered != null ? !isRegistered.equals(that.isRegistered) : that.isRegistered != null)
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
    result = 31 * result + (accessKey != null ? accessKey.hashCode() : 0);
    result = 31 * result + (centralInstanceURL != null ? centralInstanceURL.hashCode() : 0);
    result = 31 * result + (isRegistered != null ? isRegistered.hashCode() : 0);
    result = 31 * result + (createdOn != null ? createdOn.hashCode() : 0);
    result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
    result = 31 * result + (updatedOn != null ? updatedOn.hashCode() : 0);
    result = 31 * result + (updatedBy != null ? updatedBy.hashCode() : 0);
    return result;
  }
}
