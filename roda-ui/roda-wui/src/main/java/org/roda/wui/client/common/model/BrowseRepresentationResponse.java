package org.roda.wui.client.common.model;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.wui.client.services.Services;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class BrowseRepresentationResponse {

  private List<IndexedAIP> ancestors;
  private IndexedAIP indexedAIP;
  private IndexedRepresentation indexedRepresentation;
  private DescriptiveMetadataInfos descriptiveMetadataInfos;
  private List<String> riRules;
  private Services services;

  public BrowseRepresentationResponse() {
    ancestors = new ArrayList<>();
    descriptiveMetadataInfos = new DescriptiveMetadataInfos();
    riRules = new ArrayList<>();
  }

  public List<IndexedAIP> getAncestors() {
    return ancestors;
  }

  public void setAncestors(List<IndexedAIP> ancestors) {
    this.ancestors = ancestors;
  }

  public IndexedAIP getIndexedAIP() {
    return indexedAIP;
  }

  public void setIndexedAIP(IndexedAIP indexedAIP) {
    this.indexedAIP = indexedAIP;
  }

  public IndexedRepresentation getIndexedRepresentation() {
    return indexedRepresentation;
  }

  public void setIndexedRepresentation(IndexedRepresentation indexedRepresentation) {
    this.indexedRepresentation = indexedRepresentation;
  }

  public DescriptiveMetadataInfos getDescriptiveMetadataInfos() {
    return descriptiveMetadataInfos;
  }

  public void setDescriptiveMetadataInfos(DescriptiveMetadataInfos descriptiveMetadataInfos) {
    this.descriptiveMetadataInfos = descriptiveMetadataInfos;
  }

  public List<String> getRiRules() {
    return riRules;
  }

  public void setRiRules(List<String> riRules) {
    this.riRules = riRules;
  }

  public Services getServices() {
    return services;
  }

  public void setServices(Services services) {
    this.services = services;
  }
}
