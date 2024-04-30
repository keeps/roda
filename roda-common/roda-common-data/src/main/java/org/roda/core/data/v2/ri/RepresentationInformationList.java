/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ri;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepresentationInformationList implements RODAObjectList<RepresentationInformation> {

  @Serial
  private static final long serialVersionUID = -1500757245278990237L;

  private List<RepresentationInformation> representationInformation;

  public RepresentationInformationList() {
    super();
    representationInformation = new ArrayList<>();
  }

  public RepresentationInformationList(List<RepresentationInformation> representationInformation) {
    super();
    this.representationInformation = representationInformation;
  }

  @Override
  public List<RepresentationInformation> getObjects() {
    return representationInformation;
  }

  @Override
  public void setObjects(List<RepresentationInformation> representationInformation) {
    this.representationInformation = representationInformation;
  }

  @Override
  public void addObject(RepresentationInformation representationInformation) {
    this.representationInformation.add(representationInformation);
  }
}
