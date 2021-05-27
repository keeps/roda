package org.roda.core.data.v2.institution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import java.util.Date;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_INSTITUTION)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Institution implements IsModelObject {
  private static final int VERSION = 1;
  private static final long serialVersionUID = 7125122191135652087L;

  private String id;
  private String name;
  private String nameIdentifier;
  private String description;
  private String accessKey;
  private Date lastSyncDate;
  private InstitutionStatus status;

  // automatically generated upon disposal hold creation action
  private Date createdOn;
  private String createdBy;
  private Date updatedOn;
  private String updatedBy;

  public Institution(){
    status = InstitutionStatus.CREATED;
    lastSyncDate = null;
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

  public InstitutionStatus getStatus() {
    return this.status;
  }

  public void setStatus(InstitutionStatus status) {
    this.status = status;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public Date getLastSyncDate() {
    return lastSyncDate;
  }

  public void setLastSyncDate(Date lastSyncDate) {
    this.lastSyncDate = lastSyncDate;
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return VERSION;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Institution that = (Institution) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (nameIdentifier != null ? !nameIdentifier.equals(that.nameIdentifier) : that.nameIdentifier != null)
      return false;
    if (description != null ? !description.equals(that.description) : that.description != null) return false;
    if (accessKey != null ? !accessKey.equals(that.accessKey) : that.accessKey != null) return false;
    if (lastSyncDate != null ? !lastSyncDate.equals(that.lastSyncDate) : that.lastSyncDate != null) return false;
    if (status != that.status) return false;
    if (createdOn != null ? !createdOn.equals(that.createdOn) : that.createdOn != null) return false;
    if (createdBy != null ? !createdBy.equals(that.createdBy) : that.createdBy != null) return false;
    if (updatedOn != null ? !updatedOn.equals(that.updatedOn) : that.updatedOn != null) return false;
    return updatedBy != null ? updatedBy.equals(that.updatedBy) : that.updatedBy == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (nameIdentifier != null ? nameIdentifier.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (accessKey != null ? accessKey.hashCode() : 0);
    result = 31 * result + (lastSyncDate != null ? lastSyncDate.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (createdOn != null ? createdOn.hashCode() : 0);
    result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
    result = 31 * result + (updatedOn != null ? updatedOn.hashCode() : 0);
    result = 31 * result + (updatedBy != null ? updatedBy.hashCode() : 0);
    return result;
  }
}
