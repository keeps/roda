/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;


import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_REPRESENTATIONS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Representations implements RODAObjectList<Representation> {
  private static final long serialVersionUID = 9206453728686675504L;
  private List<Representation> representationList;

  public Representations() {
    super();
    representationList = new ArrayList<>();
  }

  public Representations(List<Representation> representations) {
    super();
    this.representationList = representations;
  }

  @JsonProperty(value = RodaConstants.RODA_OBJECT_REPRESENTATIONS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_REPRESENTATION)
  @Override
  public List<Representation> getObjects() {
    return representationList;
  }

  @Override
  public void setObjects(List<Representation> representations) {
    this.representationList = representations;
  }

  @Override
  public void addObject(Representation representation) {
    this.representationList.add(representation);
  }

}
