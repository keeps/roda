package org.roda.core.data.v2.synchronization.bundle;

import java.util.Date;
import java.util.HashMap;
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
  private Map<String, PackageState> packageStateMap;
  private Status syncStatus;

  public BundleState() {
    packageStateMap = new HashMap<>();
    syncStatus = Status.NONE;
  }

  @JsonProperty(value = "packages")
  public Map<String, PackageState> getPackageStateMap() {
    return packageStateMap;
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
  public PackageState getPackageState(String key) {
    PackageState packageState = packageStateMap.get(key);
    if (packageState == null) {
      packageState = new PackageState();
    }
    return packageState;
  }

  @JsonIgnore
  public void setPackageState(String key, PackageState packageState) {
    this.packageStateMap.put(key, packageState);
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
