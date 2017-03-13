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
@XmlRootElement(name = "risks")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Risks implements RODAObjectList<Risk> {
  private static final long serialVersionUID = 1430901222528933545L;
  private List<Risk> riskList;

  public Risks() {
    super();
    riskList = new ArrayList<>();
  }

  public Risks(List<Risk> risks) {
    super();
    this.riskList = risks;
  }

  @JsonProperty(value = "risks")
  @XmlElement(name = "risk")
  public List<Risk> getObjects() {
    return riskList;
  }

  public void setObjects(List<Risk> risks) {
    this.riskList = risks;
  }

  public void addObject(Risk risk) {
    this.riskList.add(risk);
  }

}
