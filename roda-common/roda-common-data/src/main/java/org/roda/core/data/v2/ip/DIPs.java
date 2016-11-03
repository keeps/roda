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
@XmlRootElement(name = "dips")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DIPs implements RODAObjectList<DIP> {
  private List<DIP> dips;

  public DIPs() {
    super();
    dips = new ArrayList<DIP>();
  }

  public DIPs(List<DIP> dips) {
    super();
    this.dips = dips;
  }

  @JsonProperty(value = "dips")
  @XmlElement(name = "dip")
  public List<DIP> getObjects() {
    return dips;
  }

  public void setObjects(List<DIP> dips) {
    this.dips = dips;
  }

  @Override
  public void addObject(DIP dip) {
    this.dips.add(dip);
  }

}
