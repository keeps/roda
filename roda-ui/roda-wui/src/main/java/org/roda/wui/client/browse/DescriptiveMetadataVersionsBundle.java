/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.ip.IndexedAIP;

public class DescriptiveMetadataVersionsBundle implements Serializable {

  private static final long serialVersionUID = 7901536603462531124L;

  private IndexedAIP aip;
  private List<IndexedAIP> aipAncestors;
  private DescriptiveMetadataViewBundle descriptiveMetadata;
  private Map<String, Date> versions;

  public DescriptiveMetadataVersionsBundle() {
    super();
  }

  public DescriptiveMetadataVersionsBundle(IndexedAIP aip, List<IndexedAIP> aipAncestors,
    DescriptiveMetadataViewBundle descriptiveMetadata, Map<String, Date> versions) {
    super();
    this.aip = aip;
    this.aipAncestors = aipAncestors;
    this.descriptiveMetadata = descriptiveMetadata;
    this.versions = versions;
  }

  public IndexedAIP getAip() {
    return aip;
  }

  public void setAip(IndexedAIP aip) {
    this.aip = aip;
  }

  public List<IndexedAIP> getAipAncestors() {
    return aipAncestors;
  }

  public void setAipAncestors(List<IndexedAIP> aipAncestors) {
    this.aipAncestors = aipAncestors;
  }

  public DescriptiveMetadataViewBundle getDescriptiveMetadata() {
    return descriptiveMetadata;
  }

  public void setDescriptiveMetadata(DescriptiveMetadataViewBundle descriptiveMetadata) {
    this.descriptiveMetadata = descriptiveMetadata;
  }

  public Map<String, Date> getVersions() {
    return versions;
  }

  public void setVersions(Map<String, Date> versions) {
    this.versions = versions;
  }

}
