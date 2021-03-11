/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.disposal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationAIPEntry implements IsModelObject {
  private static final long serialVersionUID = 895170173370654544L;

  private String aipId;
  private String aipTitle;
  private String aipLevel;
  private String parentId;
  private String destroyedTransitiveSource;
  private DestroyedSelectionState destroyedSelection;
  private String aipCollection;
  private Date aipCreationDate;
  private Date aipOverdueDate;
  private long aipNumberOfFiles;
  private long aipSize;
  private String aipDisposalScheduleId;
  private List<String> aipDisposalHoldIds;
  private List<String> aipDisposalHoldTransitiveIds;

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 1;
  }

  @JsonIgnore
  @Override
  public String getId() {
    return aipId;
  }

  public DisposalConfirmationAIPEntry() {
    aipDisposalHoldIds = new ArrayList<String>();
  }

  public String getAipId() {
    return aipId;
  }

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  public String getAipTitle() {
    return aipTitle;
  }

  public void setAipTitle(String aipTitle) {
    this.aipTitle = aipTitle;
  }

  public String getAipLevel() {
    return aipLevel;
  }

  public void setAipLevel(String aipLevel) {
    this.aipLevel = aipLevel;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getDestroyedTransitiveSource() {
    return destroyedTransitiveSource;
  }

  public void setDestroyedTransitiveSource(String destroyedTransitiveSource) {
    this.destroyedTransitiveSource = destroyedTransitiveSource;
  }

  public DestroyedSelectionState getDestroyedSelection() {
    return destroyedSelection;
  }

  public void setDestroyedSelection(DestroyedSelectionState destroyedSelection) {
    this.destroyedSelection = destroyedSelection;
  }

  public String getAipCollection() {
    return aipCollection;
  }

  public void setAipCollection(String aipCollection) {
    this.aipCollection = aipCollection;
  }

  public Date getAipCreationDate() {
    return aipCreationDate;
  }

  public void setAipCreationDate(Date aipCreationDate) {
    this.aipCreationDate = aipCreationDate;
  }

  public Date getAipOverdueDate() {
    return aipOverdueDate;
  }

  public void setAipOverdueDate(Date aipOverdueDate) {
    this.aipOverdueDate = aipOverdueDate;
  }

  public long getAipNumberOfFiles() {
    return aipNumberOfFiles;
  }

  public void setAipNumberOfFiles(long aipNumberOfFiles) {
    this.aipNumberOfFiles = aipNumberOfFiles;
  }

  public long getAipSize() {
    return aipSize;
  }

  public void setAipSize(long aipSize) {
    this.aipSize = aipSize;
  }

  public String getAipDisposalScheduleId() {
    return aipDisposalScheduleId;
  }

  public void setAipDisposalScheduleId(String aipDisposalScheduleId) {
    this.aipDisposalScheduleId = aipDisposalScheduleId;
  }

  public List<String> getAipDisposalHoldIds() {
    return aipDisposalHoldIds;
  }

  public void setAipDisposalHoldIds(List<String> aipDisposalHoldIds) {
    this.aipDisposalHoldIds = aipDisposalHoldIds;
  }

  public List<String> getAipDisposalHoldTransitiveIds() {
    return aipDisposalHoldTransitiveIds;
  }

  public void setAipDisposalHoldTransitiveIds(List<String> aipDisposalHoldTransitiveIds) {
    this.aipDisposalHoldTransitiveIds = aipDisposalHoldTransitiveIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    DisposalConfirmationAIPEntry that = (DisposalConfirmationAIPEntry) o;
    return aipNumberOfFiles == that.aipNumberOfFiles && aipSize == that.aipSize && Objects.equals(aipId, that.aipId)
      && Objects.equals(aipTitle, that.aipTitle) && Objects.equals(aipLevel, that.aipLevel)
      && Objects.equals(parentId, that.parentId)
      && Objects.equals(destroyedTransitiveSource, that.destroyedTransitiveSource)
      && destroyedSelection == that.destroyedSelection && Objects.equals(aipCollection, that.aipCollection)
      && Objects.equals(aipCreationDate, that.aipCreationDate) && Objects.equals(aipOverdueDate, that.aipOverdueDate)
      && Objects.equals(aipDisposalScheduleId, that.aipDisposalScheduleId)
      && Objects.equals(aipDisposalHoldIds, that.aipDisposalHoldIds)
      && Objects.equals(aipDisposalHoldTransitiveIds, that.aipDisposalHoldTransitiveIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(aipId, aipTitle, aipLevel, parentId, destroyedTransitiveSource, destroyedSelection,
      aipCollection, aipCreationDate, aipOverdueDate, aipNumberOfFiles, aipSize, aipDisposalScheduleId,
      aipDisposalHoldIds, aipDisposalHoldTransitiveIds);
  }

  @Override
  public String toString() {
    return "DisposalConfirmationAIPEntry{" + "aipId='" + aipId + '\'' + ", aipTitle='" + aipTitle + '\''
      + ", aipLevel='" + aipLevel + '\'' + ", parentId='" + parentId + '\'' + ", destroyedTransitiveSource='"
      + destroyedTransitiveSource + '\'' + ", destroyedSelection=" + destroyedSelection + ", aipCollection='"
      + aipCollection + '\'' + ", aipCreationDate=" + aipCreationDate + ", aipOverdueDate=" + aipOverdueDate
      + ", aipNumberOfFiles=" + aipNumberOfFiles + ", aipSize=" + aipSize + ", aipDisposalScheduleId='"
      + aipDisposalScheduleId + '\'' + ", aipDisposalHoldIds=" + aipDisposalHoldIds + ", aipDisposalHoldTransitiveIds="
      + aipDisposalHoldTransitiveIds + '}';
  }
}
