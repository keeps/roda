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
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_FILES)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Files implements RODAObjectList<File> {
  private static final long serialVersionUID = 7748588920265041356L;
  private List<File> fileList;

  public Files() {
    super();
    fileList = new ArrayList<>();
  }

  public Files(List<File> files) {
    super();
    this.fileList = files;
  }

  @JsonProperty(value = RodaConstants.RODA_OBJECT_FILES)
  @XmlElement(name = RodaConstants.RODA_OBJECT_FILE)
  @Override
  public List<File> getObjects() {
    return fileList;
  }

  @Override
  public void setObjects(List<File> files) {
    this.fileList = files;
  }

  @Override
  public void addObject(File file) {
    this.fileList.add(file);
  }

}
