package org.roda.wui.client.common.model;

import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.InstanceState;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class BrowseAIPResponse implements Serializable {

  @Serial
  private static final long serialVersionUID = 7702752004859207610L;

  private IndexedAIP indexedAIP;
  private List<String> representationInformationFields;
  private List<IndexedAIP> ancestors;
  private InstanceState instance;
  private DescriptiveMetadataInfos descriptiveMetadataInfos;
  private LongResponse childAipsCount;
  private LongResponse representationCount;
  private LongResponse dipCount;
  private LongResponse incidenceCount;
  private LongResponse eventCount;
  private LongResponse logCount;



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

  public InstanceState getInstance() {
    return instance;
  }

  public void setInstance(InstanceState instance) {
    this.instance = instance;
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

  public LongResponse getRepresentationCount() {
    return representationCount;
  }

  public void setRepresentationCount(LongResponse representationCount) {
    this.representationCount = representationCount;
  }

  public LongResponse getDipCount() {
    return dipCount;
  }

  public void setDipCount(LongResponse dipCount) {
    this.dipCount = dipCount;
  }

  public LongResponse getIncidenceCount() {
    return incidenceCount;
  }

  public void setIncidenceCount(LongResponse incidenceCount) {
    this.incidenceCount = incidenceCount;
  }

  public LongResponse getEventCount() {
    return eventCount;
  }

  public void setEventCount(LongResponse eventCount) {
    this.eventCount = eventCount;
  }

  public LongResponse getLogCount() {
    return logCount;
  }

  public void setLogCount(LongResponse logCount) {
    this.logCount = logCount;
  }


}
