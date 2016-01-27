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
import java.util.List;

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
  private List<IndexedPreservationAgent> agents;
  private List<IndexedObject> outcome;
  private List<IndexedObject> source;

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

  public List<IndexedPreservationAgent> getAgents() {
    return agents;
  }

  public void setAgents(List<IndexedPreservationAgent> agents) {
    this.agents = agents;
  }

  public List<IndexedObject> getOutcome() {
    return outcome;
  }

  public void setOutcome(List<IndexedObject> outcome) {
    this.outcome = outcome;
  }

  public List<IndexedObject> getSource() {
    return source;
  }

  public void setSource(List<IndexedObject> source) {
    this.source = source;
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
