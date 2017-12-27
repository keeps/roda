/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.bundle;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.browse.MetadataValue;

public class RepresentationInformationExtraBundle implements Serializable {
  private static final long serialVersionUID = 3601928680426998750L;

  private RepresentationInformation representationInformation;
  private String family;
  private Set<MetadataValue> values;

  public RepresentationInformationExtraBundle() {
    super();
  }

  public RepresentationInformationExtraBundle(RepresentationInformation representationInformation, String family) {
    super();
    this.setRepresentationInformation(representationInformation);
    this.setFamily(family);
    this.values = new HashSet<>();
  }

  public RepresentationInformationExtraBundle(RepresentationInformation representationInformation, String family,
    Set<MetadataValue> values) {
    super();
    this.setRepresentationInformation(representationInformation);
    this.setFamily(family);
    this.values = values;
  }

  public Set<MetadataValue> getValues() {
    return values;
  }

  public void setValues(Set<MetadataValue> values) {
    this.values = values;
  }

  public RepresentationInformation getRepresentationInformation() {
    return representationInformation;
  }

  public void setRepresentationInformation(RepresentationInformation representationInformation) {
    this.representationInformation = representationInformation;
  }

  public String getFamily() {
    return family;
  }

  public void setFamily(String family) {
    this.family = family;
  }
}
