/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.formats;

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
@XmlRootElement(name = "formats")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Formats implements RODAObjectList<Format> {
  private List<Format> formats;

  public Formats() {
    super();
    formats = new ArrayList<Format>();
  }

  public Formats(List<Format> formats) {
    super();
    this.formats = formats;
  }

  @JsonProperty(value = "formats")
  @XmlElement(name = "format")
  public List<Format> getObjects() {
    return formats;
  }

  public void setObjects(List<Format> formats) {
    this.formats = formats;
  }

  public void addObject(Format format) {
    this.formats.add(format);
  }

}
