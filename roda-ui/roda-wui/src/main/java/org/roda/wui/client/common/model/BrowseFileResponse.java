/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.model;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.TechnicalMetadataInfos;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class BrowseFileResponse {

  private IndexedAIP indexedAIP;
  private IndexedRepresentation indexedRepresentation;
  private LongResponse dipCounterResponse;
  private LongResponse riskCounterResponse;
  private LongResponse preservationCounterResponse;
  private List<String> representationInformationFields;
  private TechnicalMetadataInfos technicalMetadataInfos;

  public BrowseFileResponse() {
    representationInformationFields = new ArrayList<>();
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

  public LongResponse getDipCounterResponse() {
    return dipCounterResponse;
  }

  public void setDipCounterResponse(LongResponse dipCounterResponse) {
    this.dipCounterResponse = dipCounterResponse;
  }

  public LongResponse getRiskCounterResponse() {
    return riskCounterResponse;
  }

  public void setRiskCounterResponse(LongResponse riskCounterResponse) {
    this.riskCounterResponse = riskCounterResponse;
  }

  public LongResponse getPreservationCounterResponse() {
    return preservationCounterResponse;
  }

  public void setPreservationCounterResponse(LongResponse preservationCounterResponse) {
    this.preservationCounterResponse = preservationCounterResponse;
  }

  public List<String> getRepresentationInformationFields() {
    return representationInformationFields;
  }

  public void setRepresentationInformationFields(List<String> representationInformationFields) {
    this.representationInformationFields = representationInformationFields;
  }

  public TechnicalMetadataInfos getTechnicalMetadataInfos() {
    return technicalMetadataInfos;
  }

  public void setTechnicalMetadataInfos(TechnicalMetadataInfos technicalMetadataInfos) {
    this.technicalMetadataInfos = technicalMetadataInfos;
  }
}
