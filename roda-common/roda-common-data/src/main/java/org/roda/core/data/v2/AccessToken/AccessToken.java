package org.roda.core.data.v2.AccessToken;

import java.util.Date;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_ACCESS_TOKEN)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessToken implements IsModelObject {
  private static final int VERSION = 1;
  private static final long serialVersionUID = 2279579053027903305L;

  private String id;
  private String name;
  private String accessKey;
  private Date lastUsageDate;
  private Date expirationDate;
  private String userName;
  private AccessTokenStatus status;

  private Date createdOn;
  private String createdBy;
  private Date updatedOn;
  private String updatedBy;

  public AccessToken() {
    status = AccessTokenStatus.CREATED;
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return VERSION;
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

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public Date getLastUsageDate() {
    return lastUsageDate;
  }

  public void setLastUsageDate(Date lastUsageDate) {
    this.lastUsageDate = lastUsageDate;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public AccessTokenStatus getStatus() {
    return status;
  }

  public void setStatus(AccessTokenStatus status) {
    this.status = status;
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
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AccessToken that = (AccessToken) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (accessKey != null ? !accessKey.equals(that.accessKey) : that.accessKey != null) return false;
    if (lastUsageDate != null ? !lastUsageDate.equals(that.lastUsageDate) : that.lastUsageDate != null) return false;
    if (expirationDate != null ? !expirationDate.equals(that.expirationDate) : that.expirationDate != null)
      return false;
    if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
    if (createdOn != null ? !createdOn.equals(that.createdOn) : that.createdOn != null) return false;
    if (createdBy != null ? !createdBy.equals(that.createdBy) : that.createdBy != null) return false;
    if (updatedOn != null ? !updatedOn.equals(that.updatedOn) : that.updatedOn != null) return false;
    return updatedBy != null ? updatedBy.equals(that.updatedBy) : that.updatedBy == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (accessKey != null ? accessKey.hashCode() : 0);
    result = 31 * result + (lastUsageDate != null ? lastUsageDate.hashCode() : 0);
    result = 31 * result + (expirationDate != null ? expirationDate.hashCode() : 0);
    result = 31 * result + (userName != null ? userName.hashCode() : 0);
    result = 31 * result + (createdOn != null ? createdOn.hashCode() : 0);
    result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
    result = 31 * result + (updatedOn != null ? updatedOn.hashCode() : 0);
    result = 31 * result + (updatedBy != null ? updatedBy.hashCode() : 0);
    return result;
  }
}
