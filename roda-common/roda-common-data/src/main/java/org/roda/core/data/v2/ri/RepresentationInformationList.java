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

import jakarta.xml.bind.annotation.XmlElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_REPRESENTATION_INFORMATION_LIST)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepresentationInformationList implements RODAObjectList<RepresentationInformation> {
  @Serial
  private static final long serialVersionUID = -1500757245278990237L;
  private List<RepresentationInformation> representationInformationList;

  public RepresentationInformationList() {
    super();
    representationInformationList = new ArrayList<>();
  }

  public RepresentationInformationList(List<RepresentationInformation> representationInformation) {
    super();
    this.representationInformationList = representationInformation;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_REPRESENTATION_INFORMATION_LIST)
  @XmlElement(name = RodaConstants.RODA_OBJECT_REPRESENTATION_INFORMATION)
  public List<RepresentationInformation> getObjects() {
    return representationInformationList;
  }

  @Override
  public void setObjects(List<RepresentationInformation> representationInformation) {
    this.representationInformationList = representationInformation;
  }

  @Override
  public void addObject(RepresentationInformation representationInformation) {
    this.representationInformationList.add(representationInformation);
  }

}
