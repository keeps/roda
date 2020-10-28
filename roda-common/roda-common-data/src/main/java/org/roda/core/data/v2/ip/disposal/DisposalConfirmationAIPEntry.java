package org.roda.core.data.v2.ip.disposal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.roda.core.data.v2.IsModelObject;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationAIPEntry implements IsModelObject {
  private static final long serialVersionUID = 3298601603286113188L;

  private String aipId;
  private String aipTitle;
  private String aipLevel;
  private String aipCollection;
  private Date aipCreationDate;
  private Date aipOverdueDate;
  private long aipNumberOfFiles;
  private long aipSize;
  private String aipDisposalScheduleId;
  private List<String> aipDisposalHoldIds;

  @Override
  public int getClassVersion() {
    return 1;
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    DisposalConfirmationAIPEntry that = (DisposalConfirmationAIPEntry) o;
    return getAipNumberOfFiles() == that.getAipNumberOfFiles() && getAipSize() == that.getAipSize()
      && Objects.equals(getAipId(), that.getAipId()) && Objects.equals(getAipTitle(), that.getAipTitle())
      && Objects.equals(getAipLevel(), that.getAipLevel())
      && Objects.equals(getAipCollection(), that.getAipCollection())
      && Objects.equals(getAipCreationDate(), that.getAipCreationDate())
      && Objects.equals(getAipOverdueDate(), that.getAipOverdueDate())
      && Objects.equals(getAipDisposalScheduleId(), that.getAipDisposalScheduleId())
      && Objects.equals(getAipDisposalHoldIds(), that.getAipDisposalHoldIds());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getAipId(), getAipTitle(), getAipLevel(), getAipCollection(), getAipCreationDate(),
      getAipOverdueDate(), getAipNumberOfFiles(), getAipSize(), getAipDisposalScheduleId(), getAipDisposalHoldIds());
  }

  @Override
  public String toString() {
    return "DisposalConfirmationAIPEntry{" + "aipId='" + aipId + '\'' + ", aipName='" + aipTitle + '\'' + ", aipLevel='"
      + aipLevel + '\'' + ", aipCollection='" + aipCollection + '\'' + ", aipCreationDate=" + aipCreationDate
      + ", aipOverdueDate=" + aipOverdueDate + ", aipNumberOfFiles=" + aipNumberOfFiles + ", aipSize=" + aipSize
      + ", aipDisposalScheduleId='" + aipDisposalScheduleId + '\'' + ", aipDisposalHoldIds=" + aipDisposalHoldIds + '}';
  }
}
