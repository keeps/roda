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
import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = "aips")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AIPs implements RODAObjectList<AIP> {
  private List<AIP> aips;

  public AIPs() {
    super();
    aips = new ArrayList<AIP>();
  }

  public AIPs(List<AIP> aips) {
    super();
    this.aips = aips;
  }

  @JsonProperty(value = "aips")
  @XmlElement(name = "aip")
  public List<AIP> getObjects() {
    return aips;
  }

  public void setObjects(List<AIP> aips) {
    this.aips = aips;
  }

  @Override
  public void addObject(AIP aip) {
    this.aips.add(aip);
  }

}
