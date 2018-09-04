/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.bundle;

import java.util.List;

import org.roda.core.data.v2.ip.IndexedAIP;

public class BrowseAIPBundle implements Bundle {

  private static final long serialVersionUID = 7901536603462531124L;

  private IndexedAIP aip;
  private List<IndexedAIP> aipAncestors;
  private List<DescriptiveMetadataViewBundle> descriptiveMetadata;
  private Long childAIPCount;
  private Long representationCount;
  private Long dipCount;
  private List<String> representationInformationFields;
  private Long riskIncidenceCount;
  private Long preservationEventCount;
  private Long logCount;

  public BrowseAIPBundle() {
    super();
  }

  public BrowseAIPBundle(IndexedAIP aip, List<IndexedAIP> aipAncestors,
    List<DescriptiveMetadataViewBundle> descriptiveMetadata, Long childAIPCount, Long representationCount,
    Long dipCount, List<String> representationInformationFields, Long riskIncidenceCount, Long preservationEventCount,
    Long logCount) {
    super();
    this.aip = aip;
    this.riskIncidenceCount = riskIncidenceCount;
    this.preservationEventCount = preservationEventCount;
    this.logCount = logCount;
    this.setAIPAncestors(aipAncestors);
    this.descriptiveMetadata = descriptiveMetadata;
    this.childAIPCount = childAIPCount;
    this.representationCount = representationCount;
    this.dipCount = dipCount;
    this.representationInformationFields = representationInformationFields;
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

  public Long getRiskIncidenceCount() {
    return riskIncidenceCount;
  }

  public void setRiskIncidenceCount(Long riskIncidenceCount) {
    this.riskIncidenceCount = riskIncidenceCount;
  }

  public Long getPreservationEventCount() {
    return preservationEventCount;
  }

  public void setPreservationEventCount(Long preservationEventCount) {
    this.preservationEventCount = preservationEventCount;
  }

  public Long getLogCount() {
    return logCount;
  }

  public void setLogCount(Long logCount) {
    this.logCount = logCount;
  }

  public List<String> getRepresentationInformationFields() {
    return representationInformationFields;
  }

  public void setRepresentationInformationFields(List<String> representationInformationFields) {
    this.representationInformationFields = representationInformationFields;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("BrowseAIPBundle [");
    if (aip != null) {
      builder.append("aip=");
      builder.append(aip);
      builder.append(", ");
    }
    if (aipAncestors != null) {
      builder.append("aipAncestors=");
      builder.append(aipAncestors);
      builder.append(", ");
    }
    if (descriptiveMetadata != null) {
      builder.append("descriptiveMetadata=");
      builder.append(descriptiveMetadata);
      builder.append(", ");
    }
    if (childAIPCount != null) {
      builder.append("childAIPCount=");
      builder.append(childAIPCount);
      builder.append(", ");
    }
    if (representationCount != null) {
      builder.append("representationCount=");
      builder.append(representationCount);
      builder.append(", ");
    }
    if (dipCount != null) {
      builder.append("dipCount=");
      builder.append(dipCount);
      builder.append(", ");
    }
    if (representationInformationFields != null) {
      builder.append("representationInformationFields=");
      builder.append(representationInformationFields);
    }
    builder.append("]");
    return builder.toString();
  }

}
