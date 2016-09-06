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
import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = "risk_incidences")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskIncidences implements RODAObjectList<RiskIncidence> {
  private List<RiskIncidence> incidences;

  public RiskIncidences() {
    super();
    incidences = new ArrayList<RiskIncidence>();
  }

  public RiskIncidences(List<RiskIncidence> incidences) {
    super();
    this.incidences = incidences;
  }

  @JsonProperty(value = "risk_incidences")
  @XmlElement(name = "risk_incidence")
  public List<RiskIncidence> getObjects() {
    return incidences;
  }

  public void setObjects(List<RiskIncidence> incidences) {
    this.incidences = incidences;
  }

  public void addObject(RiskIncidence incidence) {
    this.incidences.add(incidence);
  }

}
