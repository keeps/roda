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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = RodaConstants.RODA_OBJECT_FORMATS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Formats implements RODAObjectList<Format> {
  private static final long serialVersionUID = -1500757245278990237L;
  private List<Format> formatList;

  public Formats() {
    super();
    formatList = new ArrayList<>();
  }

  public Formats(List<Format> formats) {
    super();
    this.formatList = formats;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_FORMATS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_FORMAT)
  public List<Format> getObjects() {
    return formatList;
  }

  @Override
  public void setObjects(List<Format> formats) {
    this.formatList = formats;
  }

  @Override
  public void addObject(Format format) {
    this.formatList.add(format);
  }

}
