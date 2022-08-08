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
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;

public class BrowseFileBundle implements Bundle {

  private static final long serialVersionUID = 7901536603462531124L;

  private List<IndexedAIP> aipAncestors;
  private IndexedAIP aip;
  private IndexedRepresentation representation;
  private IndexedFile file;
  private Long totalSiblingCount;
  private Long dipCount;
  private List<String> representationInformationFields;
  private Long riskIncidenceCount;
  private Long preservationEventCount;
  private String instanceName;

  private boolean localToInstance;
  private Boolean isAvailable;

  public BrowseFileBundle() {
    super();
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

  public IndexedFile getFile() {
    return file;
  }

  public void setFile(IndexedFile file) {
    this.file = file;
  }

  public Long getTotalSiblingCount() {
    return totalSiblingCount;
  }

  public void setTotalSiblingCount(Long totalSiblingCount) {
    this.totalSiblingCount = totalSiblingCount;
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

  public String getInstanceName() {
    return instanceName;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  public boolean isLocalToInstance() {
    return localToInstance;
  }

  public void setLocalToInstance(boolean localToInstance) {
    this.localToInstance = localToInstance;
  }

  public Boolean isAvailable() {
    return isAvailable;
  }

  public void setAvailable(Boolean isAvailable) {
    this.isAvailable = isAvailable;
  }
}
