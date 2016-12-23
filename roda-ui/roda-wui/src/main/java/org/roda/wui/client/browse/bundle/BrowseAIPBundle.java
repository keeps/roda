/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.bundle;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;

public class BrowseAIPBundle implements Serializable {

  private static final long serialVersionUID = 7901536603462531124L;

  private IndexedAIP aip;
  private List<IndexedAIP> aipAncestors;
  private List<DescriptiveMetadataViewBundle> descriptiveMetadata;
  private List<IndexedRepresentation> representations;
  private Map<String, List<DescriptiveMetadataViewBundle>> representationsDescriptiveMetadata;

  public BrowseAIPBundle() {
    super();
  }

  public BrowseAIPBundle(IndexedAIP aip, List<IndexedAIP> aipAncestors,
    List<DescriptiveMetadataViewBundle> descriptiveMetadata, List<IndexedRepresentation> representations,
    Map<String, List<DescriptiveMetadataViewBundle>> representationsDescriptiveMetadata) {
    super();
    this.aip = aip;
    this.setAIPAncestors(aipAncestors);
    this.descriptiveMetadata = descriptiveMetadata;
    this.representations = representations;
    this.representationsDescriptiveMetadata = representationsDescriptiveMetadata;
  }

  public IndexedAIP getAip() {
    return aip;
  }

  public void setAIP(IndexedAIP aip) {
    this.aip = aip;
  }

  public List<DescriptiveMetadataViewBundle> getDescriptiveMetadata() {
    return descriptiveMetadata;
  }

  public void setDescriptiveMetadata(List<DescriptiveMetadataViewBundle> descriptiveMetadata) {
    this.descriptiveMetadata = descriptiveMetadata;
  }

  public List<IndexedRepresentation> getRepresentations() {
    return representations;
  }

  public void setRepresentations(List<IndexedRepresentation> representations) {
    this.representations = representations;
  }

  public List<IndexedAIP> getAIPAncestors() {
    return aipAncestors;
  }

  public void setAIPAncestors(List<IndexedAIP> aipAncestors) {
    this.aipAncestors = aipAncestors;
  }
  
  public Map<String, List<DescriptiveMetadataViewBundle>> getRepresentationsDescriptiveMetadata() {
    return representationsDescriptiveMetadata;
  }

  public void setRepresentationsDescriptiveMetadata(
    Map<String, List<DescriptiveMetadataViewBundle>> representationsDescriptiveMetadata) {
    this.representationsDescriptiveMetadata = representationsDescriptiveMetadata;
  }
}
