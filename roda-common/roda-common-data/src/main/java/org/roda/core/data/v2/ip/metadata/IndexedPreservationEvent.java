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

public class IndexedPreservationEvent implements Serializable {
  private static final long serialVersionUID = 7328069950706217131L;
  private String aipId;
  private String representationId;
  private String fileId;
  private String id;
  private Date eventDateTime;
  private String eventDetail;
  private String eventType;
  private String eventOutcome;
  private String eventOutcomeDetailExtension;
  private String agentIdentifierType;
  private String agentIdentifierValue;
  private String agentRole;
  private String objectIdentifierType;
  private String objectIdentifierValue;
  private String objectRole;

  public String getAipId() {
    return aipId;
  }

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  public String getRepresentationId() {
    return representationId;
  }

  public void setRepresentationId(String representationId) {
    this.representationId = representationId;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getEventDateTime() {
    return eventDateTime;
  }

  public void setEventDateTime(Date eventDateTime) {
    this.eventDateTime = eventDateTime;
  }

  public String getEventDetail() {
    return eventDetail;
  }

  public void setEventDetail(String eventDetail) {
    this.eventDetail = eventDetail;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getEventOutcome() {
    return eventOutcome;
  }

  public void setEventOutcome(String eventOutcome) {
    this.eventOutcome = eventOutcome;
  }

  public String getEventOutcomeDetailExtension() {
    return eventOutcomeDetailExtension;
  }

  public void setEventOutcomeDetailExtension(String eventOutcomeDetailExtension) {
    this.eventOutcomeDetailExtension = eventOutcomeDetailExtension;
  }

  public String getAgentIdentifierType() {
    return agentIdentifierType;
  }

  public void setAgentIdentifierType(String agentIdentifierType) {
    this.agentIdentifierType = agentIdentifierType;
  }

  public String getAgentIdentifierValue() {
    return agentIdentifierValue;
  }

  public void setAgentIdentifierValue(String agentIdentifierValue) {
    this.agentIdentifierValue = agentIdentifierValue;
  }

  public String getAgentRole() {
    return agentRole;
  }

  public void setAgentRole(String agentRole) {
    this.agentRole = agentRole;
  }

  public String getObjectIdentifierType() {
    return objectIdentifierType;
  }

  public void setObjectIdentifierType(String objectIdentifierType) {
    this.objectIdentifierType = objectIdentifierType;
  }

  public String getObjectIdentifierValue() {
    return objectIdentifierValue;
  }

  public void setObjectIdentifierValue(String objectIdentifierValue) {
    this.objectIdentifierValue = objectIdentifierValue;
  }

  public String getObjectRole() {
    return objectRole;
  }

  public void setObjectRole(String objectRole) {
    this.objectRole = objectRole;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("{");
    sb.append("id:" + (id != null ? id : "") + ";");
    sb.append("aipId:" + (aipId != null ? aipId : "") + ";");
    sb.append("representationId:" + (representationId != null ? representationId : "") + ";");
    sb.append("fileId:" + (fileId != null ? fileId : "") + ";");
    sb.append("eventDateTime:" + (eventDateTime != null ? eventDateTime : "") + ";");
    sb.append("eventDetail:" + (eventDetail != null ? eventDetail : "") + ";");
    sb.append("eventType:" + (eventType != null ? eventType : "") + ";");
    sb.append("eventOutcome:" + (eventOutcome != null ? eventOutcome : "") + ";");
    sb.append(
      "eventOutcomeDetailExtension:" + (eventOutcomeDetailExtension != null ? eventOutcomeDetailExtension : "") + ";");
    sb.append("}");
    return sb.toString();
  }
}
