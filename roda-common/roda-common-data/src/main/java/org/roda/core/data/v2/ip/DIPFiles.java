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

import jakarta.xml.bind.annotation.XmlElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DIPFILES)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DIPFiles implements RODAObjectList<DIPFile> {
  private static final long serialVersionUID = 1740460866608268089L;
  private List<DIPFile> files;

  public DIPFiles() {
    super();
    files = new ArrayList<>();
  }

  public DIPFiles(List<DIPFile> files) {
    super();
    this.files = files;
  }

  @JsonProperty(value = RodaConstants.RODA_OBJECT_DIPFILES)
  @XmlElement(name = RodaConstants.RODA_OBJECT_DIPFILE)
  @Override
  public List<DIPFile> getObjects() {
    return files;
  }

  @Override
  public void setObjects(List<DIPFile> files) {
    this.files = files;
  }

  @Override
  public void addObject(DIPFile file) {
    this.files.add(file);
  }

}
