/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.synchronization.bundle.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.data.v2.synchronization.bundle.AttachmentState;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BundleManifest implements Serializable {
  private static final long serialVersionUID = 1433946164270654358L;
  private String id;
  private Date fromDate;
  private Date toDate;
  private List<PackageState> packageStateList;
  private List<PackageState> validationEntityList;
  private List<AttachmentState> attachmentStateList;

  public BundleManifest() {
    packageStateList = new ArrayList<>();
    attachmentStateList = new ArrayList<>();
    validationEntityList = new ArrayList<>();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
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

  @JsonProperty(value = "packagesList")
  public List<PackageState> getPackageStateList() {
    return packageStateList;
  }

  public void setPackageStateList(List<PackageState> packageStateList) {
    this.packageStateList = packageStateList;
  }

  @JsonProperty(value = "validationEntities")
  public List<PackageState> getValidationEntityList() {
    return validationEntityList;
  }

  public void setValidationEntityList(List<PackageState> validationEntityList) {
    this.validationEntityList = validationEntityList;
  }

  @JsonProperty(value = "attachmentsList")
  public List<AttachmentState> getAttachmentStateList() {
    return attachmentStateList;
  }

  public void setAttachmentStateList(List<AttachmentState> attachmentStateList) {
    this.attachmentStateList = attachmentStateList;
  }
}
