/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.io.Serializable;
import java.util.List;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Representation;

public class BrowseItemBundle implements Serializable {

  private static final long serialVersionUID = 7901536603462531124L;

  private IndexedAIP aip;
  private List<IndexedAIP> aipAncestors;
  private List<DescriptiveMetadataViewBundle> descriptiveMetadata;
  private PreservationMetadataBundle preservationMetadata;
  private List<Representation> representations;

  public BrowseItemBundle() {
    super();

  }

  public BrowseItemBundle(IndexedAIP aip, List<IndexedAIP> aipAncestors,
    List<DescriptiveMetadataViewBundle> descriptiveMetadata, PreservationMetadataBundle preservationMetadata,
    List<Representation> representations) {
    super();
    this.aip = aip;
    this.setAIPAncestors(aipAncestors);
    this.descriptiveMetadata = descriptiveMetadata;
    this.preservationMetadata = preservationMetadata;
    this.representations = representations;
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

  public PreservationMetadataBundle getPreservationMetadata() {
    return preservationMetadata;
  }

  public void setPreservationMetadata(PreservationMetadataBundle preservationMetadata) {
    this.preservationMetadata = preservationMetadata;
  }

  public List<Representation> getRepresentations() {
    return representations;
  }

  public void setRepresentations(List<Representation> representations) {
    this.representations = representations;
  }

  public List<IndexedAIP> getAIPAncestors() {
    return aipAncestors;
  }

  public void setAIPAncestors(List<IndexedAIP> aipAncestors) {
    this.aipAncestors = aipAncestors;
  }

}
