/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;

public class PreservationEventViewBundle implements Bundle {

  private static final long serialVersionUID = -8540304346599230183L;
  private IndexedPreservationEvent event;
  private Map<String, IndexedPreservationAgent> agents;
  private Map<String, IndexedAIP> aips;
  private Map<String, IndexedRepresentation> representations;
  private Map<String, IndexedFile> files;
  private Map<String, TransferredResource> transferredResources;
  private List<String> uris;
  private List<LinkingIdentifier> outcomeObjectIds = new ArrayList<>();
  private List<LinkingIdentifier> sourcesObjectIds = new ArrayList<>();

  public PreservationEventViewBundle() {
    super();
  }

  public PreservationEventViewBundle(IndexedPreservationEvent event, Map<String, IndexedPreservationAgent> agents,
    Map<String, IndexedAIP> aips, Map<String, IndexedRepresentation> representations, Map<String, IndexedFile> files,
    Map<String, TransferredResource> transferredResources, List<String> uris, List<LinkingIdentifier> outcomeObjectIds,
    List<LinkingIdentifier> sourcesObjectIds) {
    super();
    this.event = event;
    this.agents = agents;
    this.aips = aips;
    this.representations = representations;
    this.files = files;
    this.transferredResources = transferredResources;
    this.uris = uris;
    this.outcomeObjectIds = outcomeObjectIds;
    this.sourcesObjectIds = sourcesObjectIds;
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

  public List<String> getUris() {
    return uris;
  }

  public void setUris(List<String> uris) {
    this.uris = uris;
  }

  public List<LinkingIdentifier> getOutcomeObjectIds() {
    return outcomeObjectIds;
  }

  public void setOutcomeObjectIds(List<LinkingIdentifier> outcomeObjectIds) {
    this.outcomeObjectIds = outcomeObjectIds;
  }

  public void addOutcomeObjectIds(LinkingIdentifier outcomeObjectIds) {
    this.outcomeObjectIds.add(outcomeObjectIds);
  }

  public List<LinkingIdentifier> getSourcesObjectIds() {
    return sourcesObjectIds;
  }

  public void setSourcesObjectIds(List<LinkingIdentifier> sourcesObjectIds) {
    this.sourcesObjectIds = sourcesObjectIds;
  }

  public void addSourcesObjectIds(LinkingIdentifier outcomeObjectIds) {
    this.sourcesObjectIds.add(outcomeObjectIds);
  }

  @Override
  public String toString() {
    return "PreservationEventViewBundle{" + "event=" + event + ", agents=" + agents + ", aips=" + aips
      + ", representations=" + representations + ", files=" + files + ", transferredResources=" + transferredResources
      + ", uris=" + uris + ", outcomeObjectIds=" + outcomeObjectIds + ", sourcesObjectIds=" + sourcesObjectIds + '}';
  }
}
