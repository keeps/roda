/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.accessKey;

import java.util.Date;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_ACCESS_KEY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessKey implements IsModelObject {
  private static final int VERSION = 1;
  private static final long serialVersionUID = 2279579053027903305L;

  private String id;
  private String name;
  private String key;
  private Date lastUsageDate;
  private Date expirationDate;
  private String userName;
  private AccessKeyStatus status;
  private Map<String, Object> claims;

  private Date createdOn;
  private String createdBy;
  private Date updatedOn;
  private String updatedBy;

  public AccessKey() {
    this.status = AccessKeyStatus.CREATED;
  }

  public AccessKey(String accessKey) {
    this.key = accessKey;
    this.status = AccessKeyStatus.CREATED;
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

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
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

  public AccessKeyStatus getStatus() {
    return status;
  }

  public void setStatus(AccessKeyStatus status) {
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

  public Map<String, Object> getClaims() {
    return claims;
  }

  public void setClaims(Map<String, Object> claims) {
    this.claims = claims;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    AccessKey that = (AccessKey) o;

    if (id != null ? !id.equals(that.id) : that.id != null)
      return false;
    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;
    if (key != null ? !key.equals(that.key) : that.key != null)
      return false;
    if (lastUsageDate != null ? !lastUsageDate.equals(that.lastUsageDate) : that.lastUsageDate != null)
      return false;
    if (expirationDate != null ? !expirationDate.equals(that.expirationDate) : that.expirationDate != null)
      return false;
    if (userName != null ? !userName.equals(that.userName) : that.userName != null)
      return false;
    if (status != that.status)
      return false;
    if (claims != null ? !claims.equals(that.claims) : that.claims != null)
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
    result = 31 * result + (key != null ? key.hashCode() : 0);
    result = 31 * result + (lastUsageDate != null ? lastUsageDate.hashCode() : 0);
    result = 31 * result + (expirationDate != null ? expirationDate.hashCode() : 0);
    result = 31 * result + (userName != null ? userName.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (claims != null ? claims.hashCode() : 0);
    result = 31 * result + (createdOn != null ? createdOn.hashCode() : 0);
    result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
    result = 31 * result + (updatedOn != null ? updatedOn.hashCode() : 0);
    result = 31 * result + (updatedBy != null ? updatedBy.hashCode() : 0);
    return result;
  }
}
