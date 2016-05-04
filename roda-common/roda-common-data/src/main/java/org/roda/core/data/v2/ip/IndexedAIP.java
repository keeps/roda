/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.Date;
import java.util.List;

import org.roda.core.data.descriptionLevels.DescriptionLevel;
import org.roda.core.data.v2.index.IsIndexed;

/**
 * This class contains the indexed information about an AIP.
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 */
public class IndexedAIP implements IsIndexed {
  private static final long serialVersionUID = 38813680938917204L;

  private String id;
  private AIPState state;

  private String level = null;
  private String title = null;
  private Date dateInitial = null;
  private Date dateFinal = null;

  private String description = null;

  private String parentID = null;
  private List<String> ancestors;

  private Permissions permissions = new Permissions();

  private Long numberOfSubmissionFiles;
  private Long numberOfDocumentationFiles;
  private Long numberOfSchemaFiles;

  /**
   * Constructs an empty (<strong>invalid</strong>) {@link IndexedAIP}.
   */
  public IndexedAIP() {
    super();
  }

  /**
   * Constructs a new {@link IndexedAIP} cloning the one given by argument.
   * 
   * @param other
   *          the {@link IndexedAIP} to be cloned.
   */
  public IndexedAIP(IndexedAIP other) {
    this(other.getId(), other.getState(), other.getLevel(), other.getTitle(), other.getDateInitial(),
      other.getDateFinal(), other.getDescription(), other.getParentID(), other.getAncestors(), other.getPermissions(),
      other.getNumberOfSubmissionFiles(), other.getNumberOfDocumentationFiles(), other.getNumberOfSchemaFiles());
  }

  /**
   * Constructs a new SimpleDescriptionObject with the given arguments.
   * 
   * @param id
   * @param lastModifiedDate
   * @param createdDate
   * @param state
   * @param level
   * @param id
   * @param title
   * @param dateInitial
   * @param dateFinal
   * @param description
   * @param parentID
   * @param subElementsCount
   */
  public IndexedAIP(String id, AIPState state, String level, String title, Date dateInitial, Date dateFinal,
    String description, String parentID, List<String> ancestors, Permissions permissions, Long numberOfSubmissionFiles,
    Long numberOfDocumentationFiles, Long numberOfSchemaFiles) {
    super();
    this.id = id;
    this.state = state;
    this.level = level;
    this.title = title;
    this.dateInitial = dateInitial;
    this.dateFinal = dateFinal;
    this.description = description;
    this.parentID = parentID;
    this.ancestors = ancestors;
    this.permissions = permissions;
    this.numberOfSubmissionFiles = numberOfSubmissionFiles;
    this.numberOfDocumentationFiles = numberOfDocumentationFiles;
    this.numberOfSchemaFiles = numberOfSchemaFiles;
  }

  public Long getNumberOfSubmissionFiles() {
    return numberOfSubmissionFiles;
  }

  public void setNumberOfSubmissionFiles(Long numberOfSubmissionFiles) {
    this.numberOfSubmissionFiles = numberOfSubmissionFiles;
  }

  public Long getNumberOfDocumentationFiles() {
    return numberOfDocumentationFiles;
  }

  public void setNumberOfDocumentationFiles(Long numberOfDocumentationFiles) {
    this.numberOfDocumentationFiles = numberOfDocumentationFiles;
  }

  public Long getNumberOfSchemaFiles() {
    return numberOfSchemaFiles;
  }

  public void setNumberOfSchemaFiles(Long numberOfSchemaFiles) {
    this.numberOfSchemaFiles = numberOfSchemaFiles;
  }

  public AIPState getState() {
    return state;
  }

  public void setState(AIPState state) {
    this.state = state;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the description level of this DO.
   * 
   * @return a {@link DescriptionLevel} with the level of this DO.
   */
  public String getLevel() {
    return this.level;
  }

  /**
   * Sets the level of this DO.
   * 
   * @param level
   *          the level to set.
   */
  public void setLevel(String level) {
    this.level = level;
  }

  public String getId() {
    return id;
  }

  /**
   * Gets the title of this Description Object.
   * 
   * @return a String containing the title of this Descriptive Object.
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Return the initial date of this Descriptive Object.
   * 
   * @return a {@link String} with the initial date or <code>null</code> if it
   *         doesn't exist.
   */
  public Date getDateInitial() {
    return dateInitial;
  }

  /**
   * @param dateInitial
   */
  public void setDateInitial(Date dateInitial) {
    this.dateInitial = dateInitial;
  }

  /**
   * Return the final date of this Descriptive Object.
   * 
   * @return a {@link String} with the final date or <code>null</code> if it
   *         doesn't exist.
   */
  public Date getDateFinal() {
    return dateFinal;
  }

  /**
   * @param dateFinal
   */
  public void setDateFinal(Date dateFinal) {
    this.dateFinal = dateFinal;
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
   * @return the parentID
   */
  public String getParentID() {
    return parentID;
  }

  /**
   * @param parentID
   *          the parentID to set
   */
  public void setParentID(String parentID) {
    this.parentID = parentID;
  }

  public List<String> getAncestors() {
    return ancestors;
  }

  public void setAncestors(List<String> ancestors) {
    this.ancestors = ancestors;
  }

  public Permissions getPermissions() {
    return permissions;
  }

  public void setPermissions(Permissions permissions) {
    this.permissions = permissions;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ancestors == null) ? 0 : ancestors.hashCode());
    result = prime * result + ((dateFinal == null) ? 0 : dateFinal.hashCode());
    result = prime * result + ((dateInitial == null) ? 0 : dateInitial.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((level == null) ? 0 : level.hashCode());
    result = prime * result + ((numberOfDocumentationFiles == null) ? 0 : numberOfDocumentationFiles.hashCode());
    result = prime * result + ((numberOfSchemaFiles == null) ? 0 : numberOfSchemaFiles.hashCode());
    result = prime * result + ((numberOfSubmissionFiles == null) ? 0 : numberOfSubmissionFiles.hashCode());
    result = prime * result + ((parentID == null) ? 0 : parentID.hashCode());
    result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
    result = prime * result + ((state == null) ? 0 : state.hashCode());
    result = prime * result + ((title == null) ? 0 : title.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IndexedAIP other = (IndexedAIP) obj;
    if (ancestors == null) {
      if (other.ancestors != null)
        return false;
    } else if (!ancestors.equals(other.ancestors))
      return false;
    if (dateFinal == null) {
      if (other.dateFinal != null)
        return false;
    } else if (!dateFinal.equals(other.dateFinal))
      return false;
    if (dateInitial == null) {
      if (other.dateInitial != null)
        return false;
    } else if (!dateInitial.equals(other.dateInitial))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (level == null) {
      if (other.level != null)
        return false;
    } else if (!level.equals(other.level))
      return false;
    if (numberOfDocumentationFiles == null) {
      if (other.numberOfDocumentationFiles != null)
        return false;
    } else if (!numberOfDocumentationFiles.equals(other.numberOfDocumentationFiles))
      return false;
    if (numberOfSchemaFiles == null) {
      if (other.numberOfSchemaFiles != null)
        return false;
    } else if (!numberOfSchemaFiles.equals(other.numberOfSchemaFiles))
      return false;
    if (numberOfSubmissionFiles == null) {
      if (other.numberOfSubmissionFiles != null)
        return false;
    } else if (!numberOfSubmissionFiles.equals(other.numberOfSubmissionFiles))
      return false;
    if (parentID == null) {
      if (other.parentID != null)
        return false;
    } else if (!parentID.equals(other.parentID))
      return false;
    if (permissions == null) {
      if (other.permissions != null)
        return false;
    } else if (!permissions.equals(other.permissions))
      return false;
    if (state != other.state)
      return false;
    if (title == null) {
      if (other.title != null)
        return false;
    } else if (!title.equals(other.title))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "IndexedAIP [id=" + id + ", state=" + state + ", level=" + level + ", title=" + title + ", dateInitial="
      + dateInitial + ", dateFinal=" + dateFinal + ", description=" + description + ", parentID=" + parentID
      + ", ancestors=" + ancestors + ", permissions=" + permissions + ", numberOfSubmissionFiles="
      + numberOfSubmissionFiles + ", numberOfDocumentationFiles=" + numberOfDocumentationFiles
      + ", numberOfSchemaFiles=" + numberOfSchemaFiles + "]";
  }

  @Override
  public String getUUID() {
    return getId();
  }

}
