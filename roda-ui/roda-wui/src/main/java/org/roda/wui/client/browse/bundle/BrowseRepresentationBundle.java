/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;

public class BrowseRepresentationBundle implements Serializable {

  private static final long serialVersionUID = 7901536603462531124L;

  private List<IndexedAIP> aipAncestors = new ArrayList<>();
  private IndexedAIP aip;
  private IndexedRepresentation representation;
  private List<DescriptiveMetadataViewBundle> representationDescriptiveMetadata = new ArrayList<>();
  private Long dipCount;
  private List<String> representationInformationFields;
  private Long riskIncidenceCount;
  private Long preservationEventCount;

  public BrowseRepresentationBundle() {
    super();
  }

  public BrowseRepresentationBundle(List<IndexedAIP> aipAncestors, IndexedAIP aip, IndexedRepresentation representation,
    List<DescriptiveMetadataViewBundle> representationDescriptiveMetadata, Long dipCount,
    List<String> representationInformationFields) {
    super();
    this.aipAncestors = aipAncestors;
    this.aip = aip;
    this.representation = representation;
    this.representationDescriptiveMetadata = representationDescriptiveMetadata;
    this.dipCount = dipCount;
    this.representationInformationFields = representationInformationFields;
  }

  public List<IndexedAIP> getAipAncestors() {
    return aipAncestors;
  }

  public void setAipAncestors(List<IndexedAIP> aipAncestors) {
    this.aipAncestors = aipAncestors;
  }

  public IndexedAIP getAip() {
    return aip;
  }

  public void setAip(IndexedAIP aip) {
    this.aip = aip;
  }

  public IndexedRepresentation getRepresentation() {
    return representation;
  }

  public void setRepresentation(IndexedRepresentation representation) {
    this.representation = representation;
  }

  public List<DescriptiveMetadataViewBundle> getRepresentationDescriptiveMetadata() {
    return representationDescriptiveMetadata;
  }

  public void setRepresentationDescriptiveMetadata(
    List<DescriptiveMetadataViewBundle> representationDescriptiveMetadata) {
    this.representationDescriptiveMetadata = representationDescriptiveMetadata;
  }

  public Long getDipCount() {
    return dipCount;
  }

  public void setDipCount(Long dipCount) {
    this.dipCount = dipCount;
  }

  public List<String> getRepresentationInformationFields() {
    return representationInformationFields;
  }

  public void setRepresentationInformationFields(List<String> representationInformationFields) {
    this.representationInformationFields = representationInformationFields;
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
}
