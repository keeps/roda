/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;

public class IndexedPreservationEvent implements IsIndexed {
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
  private String eventOutcomeDetailNote;
  private List<LinkingIdentifier> linkingAgentIds;
  private List<LinkingIdentifier> outcomeObjectIds;
  private List<LinkingIdentifier> sourcesObjectIds;

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

  public String getEventOutcomeDetailNote() {
    return eventOutcomeDetailNote;
  }

  public void setEventOutcomeDetailNote(String eventOutcomeDetailNote) {
    this.eventOutcomeDetailNote = eventOutcomeDetailNote;
  }

  public List<LinkingIdentifier> getLinkingAgentIds() {
    return linkingAgentIds;
  }

  public void setLinkingAgentIds(List<LinkingIdentifier> linkingAgentIds) {
    this.linkingAgentIds = linkingAgentIds;
  }

  public List<LinkingIdentifier> getOutcomeObjectIds() {
    return outcomeObjectIds;
  }

  public void setOutcomeObjectIds(List<LinkingIdentifier> outcomeObjectIds) {
    this.outcomeObjectIds = outcomeObjectIds;
  }

  public List<LinkingIdentifier> getSourcesObjectIds() {
    return sourcesObjectIds;
  }

  public void setSourcesObjectIds(List<LinkingIdentifier> sourcesObjectIds) {
    this.sourcesObjectIds = sourcesObjectIds;
  }

  @Override
  public String toString() {
    return "IndexedPreservationEvent [aipId=" + aipId + ", representationId=" + representationId + ", fileId=" + fileId
      + ", id=" + id + ", eventDateTime=" + eventDateTime + ", eventDetail=" + eventDetail + ", eventType=" + eventType
      + ", eventOutcome=" + eventOutcome + ", eventOutcomeDetailExtension=" + eventOutcomeDetailExtension
      + ", eventOutcomeDetailNote=" + eventOutcomeDetailNote + ", linkingAgentIds=" + linkingAgentIds
      + ", outcomeObjectIds=" + outcomeObjectIds + ", sourcesObjectIds=" + sourcesObjectIds + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("aipId", "representationId", "fileId", "id", "eventDateTime", "eventDetail", "eventType",
      "eventOutcome", "eventOutcomeDetailExtension", "eventOutcomeDetailNote", "linkingAgentIds", "outcomeObjectIds",
      "sourcesObjectIds");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(aipId, representationId, fileId, id, eventDateTime, eventDetail, eventType, eventOutcome,
      eventOutcomeDetailExtension, eventOutcomeDetailNote, linkingAgentIds, outcomeObjectIds, sourcesObjectIds);
  }

  @Override
  public String getUUID() {
    return getId();
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.PRESERVATION_EVENT_AIP_ID, RodaConstants.PRESERVATION_EVENT_REPRESENTATION_ID,
      RodaConstants.PRESERVATION_EVENT_FILE_ID, RodaConstants.INDEX_UUID);
  }

}
