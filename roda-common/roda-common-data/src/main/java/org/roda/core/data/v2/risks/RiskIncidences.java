/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.risks;

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
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_INCIDENCES)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskIncidences implements RODAObjectList<RiskIncidence> {
  private static final long serialVersionUID = -5429901297485260170L;
  private List<RiskIncidence> incidences;

  public RiskIncidences() {
    super();
    incidences = new ArrayList<>();
  }

  public RiskIncidences(List<RiskIncidence> incidences) {
    super();
    this.incidences = incidences;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_INCIDENCES)
  @XmlElement(name = RodaConstants.RODA_OBJECT_INCIDENCE)
  public List<RiskIncidence> getObjects() {
    return incidences;
  }

  @Override
  public void setObjects(List<RiskIncidence> incidences) {
    this.incidences = incidences;
  }

  @Override
  public void addObject(RiskIncidence incidence) {
    this.incidences.add(incidence);
  }

}
