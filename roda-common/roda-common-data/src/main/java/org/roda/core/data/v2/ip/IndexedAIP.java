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
  private Boolean hasRepresentations;
  private Boolean ghost;

  private String ingestSIPId;
  private String ingestJobId;

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
      other.getNumberOfSubmissionFiles(), other.getNumberOfDocumentationFiles(), other.getNumberOfSchemaFiles(),
      other.getHasRepresentations(), other.getGhost());
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
    Long numberOfDocumentationFiles, Long numberOfSchemaFiles, Boolean hasRepresentations, Boolean ghost) {
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
    this.hasRepresentations = hasRepresentations;
    this.ghost = ghost;
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

  public String getIngestSIPId() {
    return ingestSIPId;
  }

  public IndexedAIP setIngestSIPId(String ingestSIPId) {
    this.ingestSIPId = ingestSIPId;
    return this;
  }

  public String getIngestJobId() {
    return ingestJobId;
  }

  public IndexedAIP setIngestJobId(String ingestJobId) {
    this.ingestJobId = ingestJobId;
    return this;
  }

  public Boolean getHasRepresentations() {
    return hasRepresentations;
  }

  public void setHasRepresentations(Boolean hasRepresentations) {
    this.hasRepresentations = hasRepresentations;
  }

  public Boolean getGhost() {
    return ghost;
  }

  public void setGhost(Boolean ghost) {
    this.ghost = ghost;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    IndexedAIP that = (IndexedAIP) o;

    if (id != null ? !id.equals(that.id) : that.id != null)
      return false;
    if (state != that.state)
      return false;
    if (level != null ? !level.equals(that.level) : that.level != null)
      return false;
    if (title != null ? !title.equals(that.title) : that.title != null)
      return false;
    if (dateInitial != null ? !dateInitial.equals(that.dateInitial) : that.dateInitial != null)
      return false;
    if (dateFinal != null ? !dateFinal.equals(that.dateFinal) : that.dateFinal != null)
      return false;
    if (description != null ? !description.equals(that.description) : that.description != null)
      return false;
    if (parentID != null ? !parentID.equals(that.parentID) : that.parentID != null)
      return false;
    if (ancestors != null ? !ancestors.equals(that.ancestors) : that.ancestors != null)
      return false;
    if (permissions != null ? !permissions.equals(that.permissions) : that.permissions != null)
      return false;
    if (numberOfSubmissionFiles != null ? !numberOfSubmissionFiles.equals(that.numberOfSubmissionFiles)
      : that.numberOfSubmissionFiles != null)
      return false;
    if (numberOfDocumentationFiles != null ? !numberOfDocumentationFiles.equals(that.numberOfDocumentationFiles)
      : that.numberOfDocumentationFiles != null)
      return false;
    if (numberOfSchemaFiles != null ? !numberOfSchemaFiles.equals(that.numberOfSchemaFiles)
      : that.numberOfSchemaFiles != null)
      return false;
    if (hasRepresentations != null ? !hasRepresentations.equals(that.hasRepresentations)
      : that.hasRepresentations != null)
      return false;
    if (ghost != null ? !ghost.equals(that.ghost) : that.ghost != null)
      return false;
    if (ingestSIPId != null ? !ingestSIPId.equals(that.ingestSIPId) : that.ingestSIPId != null)
      return false;
    return ingestJobId != null ? ingestJobId.equals(that.ingestJobId) : that.ingestJobId == null;

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (state != null ? state.hashCode() : 0);
    result = 31 * result + (level != null ? level.hashCode() : 0);
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (dateInitial != null ? dateInitial.hashCode() : 0);
    result = 31 * result + (dateFinal != null ? dateFinal.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (parentID != null ? parentID.hashCode() : 0);
    result = 31 * result + (ancestors != null ? ancestors.hashCode() : 0);
    result = 31 * result + (permissions != null ? permissions.hashCode() : 0);
    result = 31 * result + (numberOfSubmissionFiles != null ? numberOfSubmissionFiles.hashCode() : 0);
    result = 31 * result + (numberOfDocumentationFiles != null ? numberOfDocumentationFiles.hashCode() : 0);
    result = 31 * result + (numberOfSchemaFiles != null ? numberOfSchemaFiles.hashCode() : 0);
    result = 31 * result + (hasRepresentations != null ? hasRepresentations.hashCode() : 0);
    result = 31 * result + (ghost != null ? ghost.hashCode() : 0);
    result = 31 * result + (ingestSIPId != null ? ingestSIPId.hashCode() : 0);
    result = 31 * result + (ingestJobId != null ? ingestJobId.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "IndexedAIP{" + "id='" + id + '\'' + ", state=" + state + ", level='" + level + '\'' + ", title='" + title
      + '\'' + ", dateInitial=" + dateInitial + ", dateFinal=" + dateFinal + ", description='" + description + '\''
      + ", parentID='" + parentID + '\'' + ", ancestors=" + ancestors + ", permissions=" + permissions
      + ", numberOfSubmissionFiles=" + numberOfSubmissionFiles + ", numberOfDocumentationFiles="
      + numberOfDocumentationFiles + ", numberOfSchemaFiles=" + numberOfSchemaFiles + ", hasRepresentations="
      + hasRepresentations + ", ghost=" + ghost + ", ingestSIPId='" + ingestSIPId + '\'' + ", ingestJobId='"
      + ingestJobId + '\'' + '}';
  }

  @Override
  public String[] toCsvHeaders() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toCsvValues() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getUUID() {
    return getId();
  }

}
