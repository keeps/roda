package org.roda.wui.client.common.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class BrowseAIPResponse implements Serializable {

  @Serial
  private static final long serialVersionUID = 7702752004859207610L;

  private IndexedAIP indexedAIP;
  private List<String> representationInformationFields;
  private List<IndexedAIP> ancestors;
  private DescriptiveMetadataInfos descriptiveMetadataInfos;
  private LongResponse childAipsCount;
  private LongResponse dipCount;

  public BrowseAIPResponse() {
    // empty constructor
  }

  public IndexedAIP getIndexedAIP() {
    return indexedAIP;
  }

  public void setIndexedAIP(IndexedAIP indexedAIP) {
    this.indexedAIP = indexedAIP;
  }

  public List<String> getRepresentationInformationFields() {
    return representationInformationFields;
  }

  public void setRepresentationInformationFields(List<String> representationInformationFields) {
    this.representationInformationFields = representationInformationFields;
  }

  public List<IndexedAIP> getAncestors() {
    return ancestors;
  }

  public void setAncestors(List<IndexedAIP> ancestors) {
    this.ancestors = ancestors;
  }

  public DescriptiveMetadataInfos getDescriptiveMetadataInfos() {
    return descriptiveMetadataInfos;
  }

  public void setDescriptiveMetadataInfos(DescriptiveMetadataInfos descriptiveMetadataInfos) {
    this.descriptiveMetadataInfos = descriptiveMetadataInfos;
  }

  public LongResponse getChildAipsCount() {
    return childAipsCount;
  }

  public void setChildAipsCount(LongResponse childAipsCount) {
    this.childAipsCount = childAipsCount;
  }

  public LongResponse getDipCount() {
    return dipCount;
  }

  public void setDipCount(LongResponse dipCount) {
    this.dipCount = dipCount;
  }
}
