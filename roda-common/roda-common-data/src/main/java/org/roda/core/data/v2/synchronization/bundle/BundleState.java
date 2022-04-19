package org.roda.core.data.v2.synchronization.bundle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BundleState {
  private String id;
  private Date fromDate;
  private Date toDate;
  private String destinationPath;
  private String zipPath;
  private List<PackageState> packageStateList;
  private List<AttachmentState> attachmentStateList;
  private Status syncStatus;
  private List<PackageState> validationEntityList;

  public BundleState() {
    packageStateList = new ArrayList<>();
    attachmentStateList = new ArrayList<>();
    syncStatus = Status.NONE;
    validationEntityList = new ArrayList<>();
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public Date getFromDate() {
    return fromDate;
  }

  public void setFromDate(Date fromDate) {
    this.fromDate = fromDate;
  }

  public Date getToDate() {
    return toDate;
  }

  public void setToDate(Date toDate) {
    this.toDate = toDate;
  }

  public String getDestinationPath() {
    return destinationPath;
  }

  public void setDestinationPath(String destinationPath) {
    this.destinationPath = destinationPath;
  }

  public String getZipPath() {
    return zipPath;
  }

  public void setZipFile(String path) {
    this.zipPath = path;
  }

  @JsonIgnore
  public void setPackageStateList(List<PackageState> packageStateList) {
    this.packageStateList = packageStateList;
  }

  @JsonProperty(value = "packagesList")
  public List<PackageState> getPackageStateList() {
    return packageStateList;
  }

  @JsonProperty(value = "attachmentsList")
  public List<AttachmentState> getAttachmentStateList() {
    return attachmentStateList;
  }

  @JsonIgnore
  public void setAttachmentStateList(List<AttachmentState> attachmentStateList) {
    this.attachmentStateList = attachmentStateList;
  }

  public void setSyncState(Status status) {
    this.syncStatus = status;
  }

  public Status getSyncStatus() {
    return syncStatus;
  }

  @JsonProperty(value = "validationEntities")
  public List<PackageState> getValidationEntityList() {
    return validationEntityList;
  }

  public void setValidationEntityList(List<PackageState> validationEntityList) {
    this.validationEntityList = validationEntityList;
  }

  public enum Status {
    NONE, PREPARED, FAILED, SENT
  }
}
