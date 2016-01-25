/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;
import java.util.Date;

/**
 * This is an event preservation object
 * 
 * @author Rui Castro
 * 
 */
public class EventPreservationObject extends PreservationObject implements Serializable {
  private Date date;
  private String name;
  private String description;
  private String outcomeResult;
  private String outcomeDetails;
  private String targetID;
  private static final long serialVersionUID = 1555211337883930542L;
  private String targetPID = null;
  private String agentPID = null;

  /*
   * accession adding emulation information appraisal capture compression
   * creation data carrier migration deaccession decompression decryption
   * deletion deselection digital signature validation dissemination file
   * extension change file system analysis file system extraction filename
   * change
   */
  public static final String PRESERVATION_EVENT_TYPE_FIXITY_CHECK = "fixity check";
  /*
   * forensic feature analysis
   */
  public static final String PRESERVATION_EVENT_TYPE_FORMAT_IDENTIFICATION = "format identification";

  public static final String PRESERVATION_EVENT_TYPE_FORMAT_VALIDATION = "format validation";
  /*
   * identifier assignment imaging Information Package merging Information
   * Package splitting ingest end ingest start
   */
  public static final String PRESERVATION_EVENT_TYPE_INGESTION = "ingestion";
  /*
   * message digest calculation metadata extraction (propertyExtraction)
   * metadata modification
   */
  public static final String PRESERVATION_EVENT_TYPE_MIGRATION = "migration";
  public static final String PRESERVATION_EVENT_TYPE_NORMALIZATION = "normalization";
  /*
   * object modification object validation quality review quarantine recovery
   * redaction replication SIP creation storage migration unpacking unquarantine
   */
  public static final String PRESERVATION_EVENT_TYPE_ANTIVIRUS_CHECK = "virus check";
  /*
   * wellformedness check
   */

  public static final String PRESERVATION_EVENT_AGENT_ROLE_INGEST_TASK = "ingest task";
  public static final String PRESERVATION_EVENT_AGENT_ROLE_PRESERVATION_TASK = "preservation task";
  public static final String PRESERVATION_EVENT_AGENT_ROLE_VALIDATION_TASK = "validation task";
  public static final String PRESERVATION_EVENT_AGENT_ROLE_EXECUTING_PROGRAM_TASK = "executing program task";

  public static final String PRESERVATION_EVENT_OBJECT_ROLE_TARGET = "target";

  // ID is already set in PreservationObject
  // getType() is already set in PreservationObject

  private Date datetime = null;
  private String eventType = null;
  private String eventDetail = null;

  private String outcome = null;
  private String outcomeDetailNote = null;
  private String outcomeDetailExtension = null;

  private String agentID = null;
  private String agentRole = null;

  private String[] objectIDs = null;

  /**
   * Construct an empty {@link EventPreservationObject}.
   */
  public EventPreservationObject() {
    super();
  }

  /**
   * @see PreservationObject#toString()
   */
  @Override
  public String toString() {

    int objectCount = (getObjectIDs() != null) ? getObjectIDs().length : 0;

    return "EventPreservationObject(" + super.toString() + ", datetime=" //$NON-NLS-1$ //$NON-NLS-2$
      + getDatetime() + ", eventType=" + getEventType() //$NON-NLS-1$
      + ", eventDetail=" + getEventDetail() + ", outcome=" //$NON-NLS-1$ //$NON-NLS-2$
      + getOutcome() + ", agentID=" + getAgentID() + ", agentRole=" //$NON-NLS-1$ //$NON-NLS-2$
      + getAgentRole() + ", objectIDs=" + objectCount + ")"; //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @return the datetime
   */
  public Date getDatetime() {
    return datetime;
  }

  /**
   * @param datetime
   *          the datetime to set
   */
  public void setDatetime(Date datetime) {
    this.datetime = datetime;
  }

  /**
   * @return the eventType
   */
  public String getEventType() {
    return eventType;
  }

  /**
   * @param eventType
   *          the eventType to set
   */
  public void setEventType(String eventType) {
    this.eventType = eventType;
    setLabel(eventType);
  }

  /**
   * @return the eventDetail
   */
  public String getEventDetail() {
    return eventDetail;
  }

  /**
   * @param eventDetail
   *          the eventDetail to set
   */
  public void setEventDetail(String eventDetail) {
    this.eventDetail = eventDetail;
  }

  /**
   * @return the outcome
   */
  public String getOutcome() {
    return outcome;
  }

  /**
   * @param outcome
   *          the outcome to set
   */
  public void setOutcome(String outcome) {
    this.outcome = outcome;
  }

  /**
   * @return the outcomeDetailNote
   */
  public String getOutcomeDetailNote() {
    return outcomeDetailNote;
  }

  /**
   * @param outcomeDetailNote
   *          the outcomeDetailNote to set
   */
  public void setOutcomeDetailNote(String outcomeDetailNote) {
    this.outcomeDetailNote = outcomeDetailNote;
  }

  /**
   * @return the outcomeDetailExtension
   */
  public String getOutcomeDetailExtension() {
    return outcomeDetailExtension;
  }

  /**
   * @param outcomeDetailExtension
   *          the outcomeDetailExtension to set
   */
  public void setOutcomeDetailExtension(String outcomeDetailExtension) {
    this.outcomeDetailExtension = outcomeDetailExtension;
  }

  /**
   * @return the agentID
   */
  public String getAgentID() {
    return agentID;
  }

  /**
   * @param agentID
   *          the agentID to set
   */
  public void setAgentID(String agentID) {
    this.agentID = agentID;
  }

  /**
   * @return the agentRole
   */
  public String getAgentRole() {
    return agentRole;
  }

  /**
   * @param agentRole
   *          the agentRole to set
   */
  public void setAgentRole(String agentRole) {
    this.agentRole = agentRole;
  }

  /**
   * @return the objectIDs
   */
  public String[] getObjectIDs() {
    return objectIDs;
  }

  /**
   * @param objectIDs
   *          the objectIDs to set
   */
  public void setObjectIDs(String[] objectIDs) {
    this.objectIDs = objectIDs;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getOutcomeResult() {
    return outcomeResult;
  }

  public void setOutcomeResult(String outcomeResult) {
    this.outcomeResult = outcomeResult;
  }

  public String getOutcomeDetails() {
    return outcomeDetails;
  }

  public void setOutcomeDetails(String outcomeDetails) {
    this.outcomeDetails = outcomeDetails;
  }

  public String getTargetID() {
    return targetID;
  }

  public void setTargetID(String targetID) {
    this.targetID = targetID;
  }

  public String getTargetPID() {
    return targetPID;
  }

  public void setTargetPID(String targetPID) {
    this.targetPID = targetPID;
  }

  public String getAgentPID() {
    return agentPID;
  }

  public void setAgentPID(String agentPID) {
    this.agentPID = agentPID;
  }

}
