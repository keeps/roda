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
@XmlRootElement(name = "representations")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Representations implements RODAObjectList<Representation> {
  private List<Representation> representations;

  public Representations() {
    super();
    representations = new ArrayList<Representation>();
  }

  public Representations(List<Representation> representations) {
    super();
    this.representations = representations;
  }

  @JsonProperty(value = "representations")
  @XmlElement(name = "representation")
  @Override
  public List<Representation> getObjects() {
    return representations;
  }

  @Override
  public void setObjects(List<Representation> representations) {
    this.representations = representations;
  }

  @Override
  public void addObject(Representation representation) {
    this.representations.add(representation);
  }

}
