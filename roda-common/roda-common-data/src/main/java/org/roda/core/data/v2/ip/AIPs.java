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
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_AIPS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AIPs implements RODAObjectList<AIP> {
  private static final long serialVersionUID = -2348317938368225021L;
  private List<AIP> aipList;

  public AIPs() {
    super();
    aipList = new ArrayList<>();
  }

  public AIPs(List<AIP> aips) {
    super();
    this.aipList = aips;
  }

  @JsonProperty(value = RodaConstants.RODA_OBJECT_AIPS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_AIP)
  @Override
  public List<AIP> getObjects() {
    return aipList;
  }

  @Override
  public void setObjects(List<AIP> aips) {
    this.aipList = aips;
  }

  @Override
  public void addObject(AIP aip) {
    this.aipList.add(aip);
  }

}
