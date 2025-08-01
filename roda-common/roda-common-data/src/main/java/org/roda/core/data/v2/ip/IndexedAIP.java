/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.descriptionlevels.DescriptionLevel;
import org.roda.core.data.v2.disposal.schedule.DisposalActionCode;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodCalculation;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodIntervalCode;
import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * This class contains the indexed information about an AIP.
 *
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 */
@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_AIP)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndexedAIP
  implements IsIndexed, HasId, HasPermissions, HasState, HasDisposal, HasInstanceID, HasInstanceName {

  @Serial
  private static final long serialVersionUID = 38813680938917204L;

  private String id;
  private AIPState state;

  private String type = null;

  private String instanceId = null;
  private String instanceName = null;
  private boolean isLocalInstance = false;

  private String level = null;
  private String title = null;
  private Date dateInitial = null;
  private Date dateFinal = null;

  private Date createdOn = null;
  private String createdBy = null;
  private Date updatedOn = null;
  private String updatedBy = null;

  private String description = null;

  private String parentID = null;
  private List<String> ancestors;

  private Permissions permissions;

  private Long numberOfSubmissionFiles;
  private Long numberOfDocumentationFiles;
  private Long numberOfSchemaFiles;
  private Boolean hasRepresentations;
  private Boolean ghost;
  private Boolean hasShallowFiles;

  private List<String> ingestSIPIds;
  private String ingestJobId;
  private List<String> ingestUpdateJobIds = new ArrayList<>();

  @JsonIgnore
  private List<String> allIngestJobIds = new ArrayList<>();

  private Map<String, Object> fields = new HashMap<>();

  private String disposalScheduleId = null;
  private String disposalScheduleName = null;
  private String disposalRetentionPeriod = null;
  private List<String> disposalHoldsId = new ArrayList<>();
  private List<String> transitiveDisposalHoldsId = new ArrayList<>();
  private Date destroyedOn = null;
  private String destroyedBy = null;
  private DisposalActionCode disposalAction = null;
  private AIPDisposalScheduleAssociationType scheduleAssociationType = null;

  private Integer retentionPeriodDuration = null;
  private RetentionPeriodIntervalCode retentionPeriodInterval = null;
  private Date overdueDate = null;
  private Date retentionPeriodStartDate = null;
  private String retentionPeriodDetails = null;
  private RetentionPeriodCalculation retentionPeriodCalculation = null;

  private boolean onHold = false;

  private String disposalConfirmationId = null;

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
    this(other.getId(), other.getState(), other.getType(), other.getInstanceId(), other.getInstanceName(),
      other.getLevel(), other.getTitle(), other.getDateInitial(), other.getDateFinal(), other.getDescription(),
      other.getParentID(), other.getAncestors(), other.getPermissions(), other.getNumberOfSubmissionFiles(),
      other.getNumberOfDocumentationFiles(), other.getNumberOfSchemaFiles(), other.getHasRepresentations(),
      other.getGhost(), other.hasShallowFiles(), other.getDisposalScheduleId(), other.getDisposalScheduleName(),
      other.getRetentionPeriodDuration(), other.getRetentionPeriodInterval(), other.getRetentionPeriodStartDate(),
      other.getRetentionPeriodDetails(), other.getRetentionPeriodState(), other.getDisposalHoldsId(),
      other.getTransitiveDisposalHoldsId(), other.getDestroyedOn(), other.getDestroyedBy(),
      other.getDisposalConfirmationId(), other.getScheduleAssociationType());
  }

  /**
   * Constructs a new SimpleDescriptionObject with the given arguments.
   *
   * @param id
   * @param state
   * @param level
   * @param title
   * @param dateInitial
   * @param dateFinal
   * @param description
   * @param parentID
   */
  public IndexedAIP(String id, AIPState state, String type, String instanceId, String instanceName, String level,
    String title, Date dateInitial, Date dateFinal, String description, String parentID, List<String> ancestors,
    Permissions permissions, Long numberOfSubmissionFiles, Long numberOfDocumentationFiles, Long numberOfSchemaFiles,
    Boolean hasRepresentations, Boolean ghost, Boolean hasShallowFiles, String disposalScheduleId,
    String disposalScheduleName, Integer retentionPeriodDuration, RetentionPeriodIntervalCode retentionPeriodInterval,
    Date retentionPeriodStartDate, String retentionPeriodDetails, RetentionPeriodCalculation retentionPeriodCalculation,
    List<String> disposalHoldsId, List<String> transitiveDisposalHoldsId, Date destroyedOn, String destroyedBy,
    String disposalConfirmationId, AIPDisposalScheduleAssociationType scheduleAssociationType) {
    super();
    this.id = id;
    this.state = state;
    this.type = type;
    this.instanceId = instanceId;
    this.instanceName = instanceName;
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
    this.hasShallowFiles = hasShallowFiles;
    this.disposalScheduleId = disposalScheduleId;
    this.disposalScheduleName = disposalScheduleName;
    this.retentionPeriodDuration = retentionPeriodDuration;
    this.retentionPeriodInterval = retentionPeriodInterval;
    this.retentionPeriodStartDate = retentionPeriodStartDate;
    this.retentionPeriodDetails = retentionPeriodDetails;
    this.retentionPeriodCalculation = retentionPeriodCalculation;
    this.disposalHoldsId = disposalHoldsId;
    this.transitiveDisposalHoldsId = transitiveDisposalHoldsId;
    this.destroyedOn = destroyedOn;
    this.destroyedBy = destroyedBy;
    this.disposalConfirmationId = disposalConfirmationId;
    this.scheduleAssociationType = scheduleAssociationType;
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

  @Override
  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
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

  @Override
  public Permissions getPermissions() {
    return permissions;
  }

  @Override
  public void setPermissions(Permissions permissions) {
    this.permissions = permissions;
  }

  public List<String> getIngestSIPIds() {
    return ingestSIPIds;
  }

  public IndexedAIP setIngestSIPIds(List<String> ingestSIPIds) {
    this.ingestSIPIds = ingestSIPIds;
    return this;
  }

  public String getIngestJobId() {
    return ingestJobId;
  }

  public IndexedAIP setIngestJobId(String ingestJobId) {
    this.ingestJobId = ingestJobId;
    return this;
  }

  public List<String> getIngestUpdateJobIds() {
    return ingestUpdateJobIds;
  }

  public IndexedAIP setIngestUpdateJobIds(List<String> ingestUpdateJobIds) {
    this.ingestUpdateJobIds = ingestUpdateJobIds;
    return this;
  }

  public IndexedAIP addIngestUpdateJobId(String ingestUpdateJobId) {
    this.ingestUpdateJobIds.add(ingestUpdateJobId);
    return this;
  }

  public List<String> getAllIngestJobIds() {
    return allIngestJobIds;
  }

  public IndexedAIP setAllUpdateJobIds(List<String> allIngestJobIds) {
    this.allIngestJobIds = allIngestJobIds;
    return this;
  }

  public IndexedAIP addAllUpdateJobId(String ingestJobId) {
    this.allIngestJobIds.add(ingestJobId);
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

  public Boolean hasShallowFiles() {
    return hasShallowFiles;
  }

  public void setHasShallowFiles(Boolean hasShallowFiles) {
    this.hasShallowFiles = hasShallowFiles;
  }

  public String getDisposalScheduleId() {
    return disposalScheduleId;
  }

  public IndexedAIP setDisposalScheduleId(String disposalScheduleId) {
    this.disposalScheduleId = disposalScheduleId;
    return this;
  }

  public String getDisposalScheduleName() {
    return disposalScheduleName;
  }

  public IndexedAIP setDisposalScheduleName(String disposalScheduleName) {
    this.disposalScheduleName = disposalScheduleName;
    return this;
  }

  public Integer getRetentionPeriodDuration() {
    return retentionPeriodDuration;
  }

  public IndexedAIP setRetentionPeriodDuration(Integer retentionPeriodDuration) {
    this.retentionPeriodDuration = retentionPeriodDuration;
    return this;
  }

  public RetentionPeriodIntervalCode getRetentionPeriodInterval() {
    return retentionPeriodInterval;
  }

  public void setRetentionPeriodInterval(RetentionPeriodIntervalCode retentionPeriodInterval) {
    this.retentionPeriodInterval = retentionPeriodInterval;
  }

  public List<String> getDisposalHoldsId() {
    return disposalHoldsId;
  }

  public IndexedAIP setDisposalHoldsId(List<String> disposalHoldsId) {
    this.disposalHoldsId = disposalHoldsId;
    return this;
  }

  public List<String> getTransitiveDisposalHoldsId() {
    return transitiveDisposalHoldsId;
  }

  public IndexedAIP setTransitiveDisposalHoldsId(List<String> transitiveDisposalHoldsId) {
    this.transitiveDisposalHoldsId = transitiveDisposalHoldsId;
    return this;
  }

  public Date getDestroyedOn() {
    return destroyedOn;
  }

  public IndexedAIP setDestroyedOn(Date destroyedOn) {
    this.destroyedOn = destroyedOn;
    return this;
  }

  public String getDestroyedBy() {
    return destroyedBy;
  }

  public IndexedAIP setDestroyedBy(String destroyedBy) {
    this.destroyedBy = destroyedBy;
    return this;
  }

  public DisposalActionCode getDisposalAction() {
    return disposalAction;
  }

  public IndexedAIP setDisposalAction(DisposalActionCode disposalAction) {
    this.disposalAction = disposalAction;
    return this;
  }

  public Date getOverdueDate() {
    return overdueDate;
  }

  public IndexedAIP setOverdueDate(Date overdueDate) {
    this.overdueDate = overdueDate;
    return this;
  }

  public boolean isOnHold() {
    return onHold;
  }

  public IndexedAIP setOnHold(boolean onHold) {
    this.onHold = onHold;
    return this;
  }

  public String getDisposalConfirmationId() {
    return disposalConfirmationId;
  }

  public IndexedAIP setDisposalConfirmationId(String disposalConfirmationId) {
    this.disposalConfirmationId = disposalConfirmationId;
    return this;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public IndexedAIP setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
    return this;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public IndexedAIP setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  public Date getUpdatedOn() {
    return updatedOn;
  }

  public IndexedAIP setUpdatedOn(Date updatedOn) {
    this.updatedOn = updatedOn;
    return this;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public IndexedAIP setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }

  public AIPDisposalScheduleAssociationType getScheduleAssociationType() {
    return scheduleAssociationType;
  }

  public IndexedAIP setScheduleAssociationType(AIPDisposalScheduleAssociationType scheduleAssociationType) {
    this.scheduleAssociationType = scheduleAssociationType;
    return this;
  }

  public Map<String, Object> getFields() {
    return fields;
  }

  public void setFields(Map<String, Object> fields) {
    fields.entrySet().stream().filter(p -> p.getValue() != null)
      .forEach(e -> this.fields.put(e.getKey(), e.getValue()));
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
    if (instanceId != that.instanceId)
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
    if (ingestSIPIds != null ? !ingestSIPIds.equals(that.ingestSIPIds) : that.ingestSIPIds != null)
      return false;
    if (ingestJobId != null ? !ingestJobId.equals(that.ingestJobId) : that.ingestJobId != null)
      return false;
    if (disposalScheduleId != null ? !disposalScheduleId.equals(that.disposalScheduleId)
      : that.disposalScheduleId != null)
      return false;
    if (disposalScheduleName != null ? !disposalScheduleName.equals(that.disposalScheduleName)
      : that.disposalScheduleName != null)
      return false;
    if (disposalHoldsId != null ? !disposalHoldsId.equals(that.disposalHoldsId) : that.disposalHoldsId != null)
      return false;
    if (retentionPeriodDuration != null ? !retentionPeriodDuration.equals(that.retentionPeriodDuration)
      : that.retentionPeriodDuration != null)
      return false;
    if (retentionPeriodInterval != null ? !retentionPeriodInterval.equals(that.retentionPeriodInterval)
      : that.retentionPeriodInterval != null)
      return false;
    if (retentionPeriodStartDate != null ? !retentionPeriodStartDate.equals(that.retentionPeriodStartDate)
      : that.retentionPeriodStartDate != null)
      return false;
    if (destroyedOn != null ? !destroyedOn.equals(that.destroyedOn) : that.destroyedOn != null)
      return false;
    if (scheduleAssociationType != null ? !scheduleAssociationType.equals(that.scheduleAssociationType)
      : that.scheduleAssociationType != null)
      return false;
    return destroyedBy != null ? destroyedBy.equals(that.destroyedBy) : that.destroyedBy == null;

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (state != null ? state.hashCode() : 0);
    result = 31 * result + (instanceId != null ? instanceId.hashCode() : 0);
    result = 31 * result + (instanceName != null ? instanceName.hashCode() : 0);
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
    result = 31 * result + (ingestSIPIds != null ? ingestSIPIds.hashCode() : 0);
    result = 31 * result + (ingestJobId != null ? ingestJobId.hashCode() : 0);
    result = 31 * result + (disposalScheduleId != null ? disposalScheduleId.hashCode() : 0);
    result = 31 * result + (disposalScheduleName != null ? disposalScheduleName.hashCode() : 0);
    result = 31 * result + (disposalHoldsId != null ? disposalHoldsId.hashCode() : 0);
    result = 31 * result + (retentionPeriodInterval != null ? retentionPeriodInterval.hashCode() : 0);
    result = 31 * result + (retentionPeriodDuration != null ? retentionPeriodDuration.hashCode() : 0);
    result = 31 * result + (retentionPeriodStartDate != null ? retentionPeriodStartDate.hashCode() : 0);
    result = 31 * result + (destroyedOn != null ? destroyedOn.hashCode() : 0);
    result = 31 * result + (destroyedBy != null ? destroyedBy.hashCode() : 0);
    result = 31 * result + (scheduleAssociationType != null ? scheduleAssociationType.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "IndexedAIP{" + "id='" + id + '\'' + ", state=" + state + ", type='" + type + '\'' + ", instanceId='"
      + instanceId + '\'' + instanceName + '\'' + ", level='" + level + '\'' + ", title='" + title + '\''
      + ", dateInitial=" + dateInitial + ", dateFinal=" + dateFinal + ", createdOn=" + createdOn + ", createdBy='"
      + createdBy + '\'' + ", updatedOn=" + updatedOn + ", updatedBy='" + updatedBy + '\'' + ", description='"
      + description + '\'' + ", parentID='" + parentID + '\'' + ", ancestors=" + ancestors + ", permissions="
      + permissions + ", numberOfSubmissionFiles=" + numberOfSubmissionFiles + ", numberOfDocumentationFiles="
      + numberOfDocumentationFiles + ", numberOfSchemaFiles=" + numberOfSchemaFiles + ", hasRepresentations="
      + hasRepresentations + ", ghost=" + ghost + ", ingestSIPIds=" + ingestSIPIds + ", ingestJobId='" + ingestJobId
      + '\'' + ", ingestUpdateJobIds=" + ingestUpdateJobIds + ", allIngestJobIds=" + allIngestJobIds + ", fields="
      + fields + ", disposalScheduleId='" + disposalScheduleId + '\'' + ", disposalScheduleName='"
      + disposalScheduleName + '\'' + ", retentionPeriodDuration='" + retentionPeriodDuration + '\''
      + ", retentionPeriodInterval='" + retentionPeriodInterval + '\'' + '\'' + ", retentionPeriodStartDate='"
      + retentionPeriodStartDate + ", disposalHoldsId=" + disposalHoldsId + ", destroyedOn=" + destroyedOn
      + ", destroyedBy='" + destroyedBy + '\'' + ", disposalAction='" + disposalAction + '\'' + ", overdueDate="
      + overdueDate + ", disposalHoldStatus=" + onHold + ", disposalConfirmationId='" + disposalConfirmationId + '\''
      + ", scheduleAssociationType=" + scheduleAssociationType + '}';
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "state", "type", "instanceId", "instanceName", "level", "title", "dateInitial",
      "dateFinal", "description", "parentID", "ancestors", "permissions", "numberOfSubmissionFiles",
      "numberOfDocumentationFiles", "numberOfSchemaFiles", "hasRepresentations", "ghost", "ingestSIPId", "ingestJobId",
      "ingestUpdateJobIds", "allIngestJobIds", "createdOn", "createdBy", "updatedOn", "updatedBy", "disposalScheduleId",
      "disposalScheduleName", "disposalHoldsId", "destroyedOn", "destroyedBy", "scheduleAssociationType");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(id, state, type, instanceId, instanceName, level, title, dateInitial, dateFinal, description,
      parentID, ancestors, permissions, numberOfSubmissionFiles, numberOfDocumentationFiles, numberOfSchemaFiles,
      hasRepresentations, ghost, ingestSIPIds, ingestJobId, ingestUpdateJobIds, allIngestJobIds, createdOn, createdBy,
      updatedOn, updatedBy, disposalScheduleId, disposalScheduleName, disposalHoldsId, destroyedOn, destroyedBy,
      scheduleAssociationType);
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID);
  }

  public Date getRetentionPeriodStartDate() {
    return retentionPeriodStartDate;
  }

  public void setRetentionPeriodStartDate(Date retentionPeriodStartDate) {
    this.retentionPeriodStartDate = retentionPeriodStartDate;
  }

  public String getRetentionPeriodDetails() {
    return retentionPeriodDetails;
  }

  public void setRetentionPeriodDetails(String retentionPeriodDetails) {
    this.retentionPeriodDetails = retentionPeriodDetails;
  }

  public RetentionPeriodCalculation getRetentionPeriodState() {
    return retentionPeriodCalculation;
  }

  public void setRetentionPeriodState(RetentionPeriodCalculation retentionPeriodCalculation) {
    this.retentionPeriodCalculation = retentionPeriodCalculation;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  public boolean isLocalInstance() {
    return isLocalInstance;
  }

  public void setLocalInstance(boolean localInstance) {
    isLocalInstance = localInstance;
  }
}
