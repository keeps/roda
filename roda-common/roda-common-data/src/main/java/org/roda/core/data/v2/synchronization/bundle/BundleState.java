package org.roda.core.data.v2.synchronization.bundle;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BundleState {
  private Date fromDate;
  private Date toDate;
  private String destinationPath;
  private String zipPath;
  private List<PackageState> packageStateList;
  private Status syncStatus;

  public BundleState() {
    packageStateList = new ArrayList<>();
    syncStatus = Status.NONE;
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

  public void setSyncState(Status status) {
    this.syncStatus = status;
  }

  public Status getSyncStatus() {
    return syncStatus;
  }

  public enum Status {
    NONE, PREPARED, FAILED, SENT
  }
}
