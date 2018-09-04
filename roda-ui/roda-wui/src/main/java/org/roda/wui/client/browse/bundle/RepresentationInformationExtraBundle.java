/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.bundle;

import java.util.Map;
import java.util.Set;

import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.browse.MetadataValue;

public class RepresentationInformationExtraBundle implements Bundle {
  private static final long serialVersionUID = 3601928680426998750L;

  private RepresentationInformation representationInformation;
  private Map<String, Set<MetadataValue>> familyValues;

  public RepresentationInformationExtraBundle() {
    super();
  }

  public RepresentationInformationExtraBundle(RepresentationInformation representationInformation,
    Map<String, Set<MetadataValue>> familyValues) {
    super();
    this.representationInformation = representationInformation;
    this.familyValues = familyValues;
  }

  public RepresentationInformation getRepresentationInformation() {
    return representationInformation;
  }

  public void setRepresentationInformation(RepresentationInformation representationInformation) {
    this.representationInformation = representationInformation;
  }

  public Map<String, Set<MetadataValue>> getFamilyValues() {
    return familyValues;
  }

  public void setFamilyValues(Map<String, Set<MetadataValue>> familyValues) {
    this.familyValues = familyValues;
  }
}
