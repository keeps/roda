/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.io.Serializable;
import java.util.Map;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;

public class PreservationEventViewBundle implements Serializable {

  private static final long serialVersionUID = -8540304346599230183L;
  private IndexedPreservationEvent event;
  private Map<String, IndexedPreservationAgent> agents;
  private Map<String, IndexedAIP> aips;
  private Map<String, IndexedRepresentation> representations;
  private Map<String, IndexedFile> files;
  private Map<String, TransferredResource> transferredResources;

  public PreservationEventViewBundle() {
    super();
  }

  public PreservationEventViewBundle(IndexedPreservationEvent event, Map<String, IndexedPreservationAgent> agents,
    Map<String, IndexedAIP> aips, Map<String, IndexedRepresentation> representations, Map<String, IndexedFile> files,
    Map<String, TransferredResource> transferredResources) {
    super();
    this.event = event;
    this.agents = agents;
    this.aips = aips;
    this.representations = representations;
    this.files = files;
    this.transferredResources = transferredResources;
  }

  public IndexedPreservationEvent getEvent() {
    return event;
  }

  public void setEvent(IndexedPreservationEvent event) {
    this.event = event;
  }

  public Map<String, IndexedPreservationAgent> getAgents() {
    return agents;
  }

  public void setAgents(Map<String, IndexedPreservationAgent> agents) {
    this.agents = agents;
  }

  public Map<String, IndexedFile> getFiles() {
    return files;
  }

  public void setFiles(Map<String, IndexedFile> files) {
    this.files = files;
  }

  public Map<String, IndexedAIP> getAips() {
    return aips;
  }

  public void setAips(Map<String, IndexedAIP> aips) {
    this.aips = aips;
  }

  public Map<String, IndexedRepresentation> getRepresentations() {
    return representations;
  }

  public void setRepresentations(Map<String, IndexedRepresentation> representations) {
    this.representations = representations;
  }

  public Map<String, TransferredResource> getTransferredResources() {
    return transferredResources;
  }

  public void setTransferredResources(Map<String, TransferredResource> transferredResources) {
    this.transferredResources = transferredResources;
  }

  @Override
  public String toString() {
    return "PreservationEventViewBundle [event=" + event + ", agents=" + agents + ", aips=" + aips
      + ", representations=" + representations + ", files=" + files + ", transferredResources=" + transferredResources
      + "]";
  }

}
