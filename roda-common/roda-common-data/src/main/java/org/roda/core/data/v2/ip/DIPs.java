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
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DIPS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DIPs implements RODAObjectList<DIP> {
  private static final long serialVersionUID = -820307643991063686L;
  private List<DIP> dipList;

  public DIPs() {
    super();
    dipList = new ArrayList<>();
  }

  public DIPs(List<DIP> dips) {
    super();
    this.dipList = dips;
  }

  @JsonProperty(value = RodaConstants.RODA_OBJECT_DIPS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_DIP)
  @Override
  public List<DIP> getObjects() {
    return dipList;
  }

  @Override
  public void setObjects(List<DIP> dips) {
    this.dipList = dips;
  }

  @Override
  public void addObject(DIP dip) {
    this.dipList.add(dip);
  }

}
