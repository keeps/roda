package org.roda.core.data.v2.ip.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.TransferredResource;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PreservationEventsLinkingObjects {

  private Map<String, IndexedAIP> aips = new HashMap<>();
  private Map<String, IndexedRepresentation> representations = new HashMap<>();
  private Map<String, IndexedFile> files = new HashMap<>();
  private Map<String, TransferredResource> transferredResources = new HashMap<>();
  private List<LinkingIdentifier> sourceObjectIds;
  private List<LinkingIdentifier> outcomeObjectIds;
  private List<String> uris;

  public PreservationEventsLinkingObjects() {
    this.sourceObjectIds = new ArrayList<>();
    this.outcomeObjectIds = new ArrayList<>();
    this.uris = new ArrayList<>();
  }

  public List<LinkingIdentifier> getSourceObjectIds() {
    return sourceObjectIds;
  }

  public void setSourceObjectIds(List<LinkingIdentifier> sourceObjectIds) {
    this.sourceObjectIds = sourceObjectIds;
  }

  public List<LinkingIdentifier> getOutcomeObjectIds() {
    return outcomeObjectIds;
  }

  public void setOutcomeObjectIds(List<LinkingIdentifier> outcomeObjectIds) {
    this.outcomeObjectIds = outcomeObjectIds;
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

  public Map<String, IndexedFile> getFiles() {
    return files;
  }

  public void setFiles(Map<String, IndexedFile> files) {
    this.files = files;
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
}
