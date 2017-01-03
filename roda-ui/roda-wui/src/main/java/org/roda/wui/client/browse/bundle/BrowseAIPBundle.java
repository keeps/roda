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

import org.roda.core.data.v2.ip.IndexedAIP;

public class BrowseAIPBundle implements Serializable {

  private static final long serialVersionUID = 7901536603462531124L;

  private IndexedAIP aip;
  private List<IndexedAIP> aipAncestors;
  private List<DescriptiveMetadataViewBundle> descriptiveMetadata;
  private Long childAIPCount;
  private Long representationCount;
  private Long dipCount;

  public BrowseAIPBundle() {
    super();
  }

  public BrowseAIPBundle(IndexedAIP aip, List<IndexedAIP> aipAncestors,
    List<DescriptiveMetadataViewBundle> descriptiveMetadata, Long childAIPCount, Long representationCount,
    Long dipCount) {
    super();
    this.aip = aip;
    this.setAIPAncestors(aipAncestors);
    this.descriptiveMetadata = descriptiveMetadata;
    this.childAIPCount = childAIPCount;
    this.representationCount = representationCount;
    this.dipCount = dipCount;
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

  public List<IndexedAIP> getAIPAncestors() {
    return aipAncestors;
  }

  public void setAIPAncestors(List<IndexedAIP> aipAncestors) {
    this.aipAncestors = aipAncestors;
  }

  public Long getChildAIPCount() {
    return childAIPCount;
  }

  public void setChildAIPCount(Long childAIPCount) {
    this.childAIPCount = childAIPCount;
  }

  public Long getRepresentationCount() {
    return representationCount;
  }

  public void setRepresentationCount(Long representationCount) {
    this.representationCount = representationCount;
  }

  public Long getDipCount() {
    return dipCount;
  }

  public void setDipCount(Long dipCount) {
    this.dipCount = dipCount;
  }

}
