/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.dissemination.browse.client;

import java.io.Serializable;
import java.util.List;

/**
 * @author sleroux
 *
 */
public class RepresentationPreservationMetadataBundle implements Serializable {

  private static final long serialVersionUID = 515251862250083594L;

  private String representationID;
  private List<String> fileIds;
  private List<String> eventIds;
  private List<String> agentIds;
  

  public RepresentationPreservationMetadataBundle() {
    super();
  }


  public String getRepresentationID() {
    return representationID;
  }


  public void setRepresentationID(String representationID) {
    this.representationID = representationID;
  }


  public List<String> getFileIds() {
    return fileIds;
  }


  public void setFileIds(List<String> fileIds) {
    this.fileIds = fileIds;
  }


  public List<String> getEventIds() {
    return eventIds;
  }


  public void setEventIds(List<String> eventIds) {
    this.eventIds = eventIds;
  }


  public List<String> getAgentIds() {
    return agentIds;
  }


  public void setAgentIds(List<String> agentIds) {
    this.agentIds = agentIds;
  }

  
  
}
