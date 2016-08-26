/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.agents;

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
@XmlRootElement(name = "agents")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Agents implements RODAObjectList<Agent> {
  private List<Agent> agents;

  public Agents() {
    super();
    agents = new ArrayList<Agent>();
  }

  public Agents(List<Agent> agents) {
    super();
    this.agents = agents;
  }

  @JsonProperty(value = "agents")
  @XmlElement(name = "agent")
  public List<Agent> getObjects() {
    return agents;
  }

  public void setObjects(List<Agent> agents) {
    this.agents = agents;
  }

  public void addObject(Agent agent) {
    this.agents.add(agent);
  }

}
