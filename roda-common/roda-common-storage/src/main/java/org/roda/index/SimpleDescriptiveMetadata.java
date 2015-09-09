package org.roda.index;

import java.util.Date;
import java.util.List;

public class SimpleDescriptiveMetadata {

  private String aipID;
  private String level;
  private String title;
  private String description;
  private Date dateInitial;
  private Date dateFinal;
  private String parentId;
  private long childrenCount;
  private List<String> descriptiveMetadataFileIds;

  public SimpleDescriptiveMetadata(String level, String title, String description, Date dateInitial, Date dateFinal,
    String parentId, long childrenCount, List<String> descriptiveMetadataFileIds) {
    super();
    this.level = level;
    this.title = title;
    this.description = description;
    this.dateInitial = dateInitial;
    this.dateFinal = dateFinal;
    this.parentId = parentId;
    this.childrenCount = childrenCount;
    this.descriptiveMetadataFileIds = descriptiveMetadataFileIds;
  }

  /**
   * @return the aipID
   */
  public String getAipID() {
    return aipID;
  }

  /**
   * @param aipID
   *          the aipID to set
   */
  public void setAipID(String aipID) {
    this.aipID = aipID;
  }

  /**
   * @return the level
   */
  public String getLevel() {
    return level;
  }

  /**
   * @param level
   *          the level to set
   */
  public void setLevel(String level) {
    this.level = level;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title
   *          the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the dateInitial
   */
  public Date getDateInitial() {
    return dateInitial;
  }

  /**
   * @param dateInitial
   *          the dateInitial to set
   */
  public void setDateInitial(Date dateInitial) {
    this.dateInitial = dateInitial;
  }

  /**
   * @return the dateFinal
   */
  public Date getDateFinal() {
    return dateFinal;
  }

  /**
   * @param dateFinal
   *          the dateFinal to set
   */
  public void setDateFinal(Date dateFinal) {
    this.dateFinal = dateFinal;
  }

  /**
   * @return the parentId
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * @param parentId
   *          the parentId to set
   */
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  /**
   * @return the childrenCount
   */
  public long getChildrenCount() {
    return childrenCount;
  }

  /**
   * @param childrenCount
   *          the childrenCount to set
   */
  public void setChildrenCount(long childrenCount) {
    this.childrenCount = childrenCount;
  }

  /**
   * @return the descriptiveMetadataFileIds
   */
  public List<String> getDescriptiveMetadataFileIds() {
    return descriptiveMetadataFileIds;
  }

  /**
   * @param descriptiveMetadataFileIds
   *          the descriptiveMetadataFileIds to set
   */
  public void setDescriptiveMetadataFileIds(List<String> descriptiveMetadataFileIds) {
    this.descriptiveMetadataFileIds = descriptiveMetadataFileIds;
  }

}
