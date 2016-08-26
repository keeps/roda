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
@XmlRootElement(name = "files")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Files implements RODAObjectList<File> {
  private List<File> files;

  public Files() {
    super();
    files = new ArrayList<File>();
  }

  public Files(List<File> files) {
    super();
    this.files = files;
  }

  @JsonProperty(value = "files")
  @XmlElement(name = "file")
  public List<File> getObjects() {
    return files;
  }

  public void setObjects(List<File> files) {
    this.files = files;
  }

  public void addObject(File file) {
    this.files.add(file);
  }

}
