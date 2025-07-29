/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class DescriptiveMetadataInfos implements Serializable {

  @Serial
  private static final long serialVersionUID = -4552881362783492103L;

  private List<DescriptiveMetadataInfo> descriptiveMetadataInfoList = new ArrayList<>();

  public DescriptiveMetadataInfos() {
    // empty constructor
  }
  public DescriptiveMetadataInfos(List<DescriptiveMetadataInfo> descriptiveMetadataInfoList) {
    this.descriptiveMetadataInfoList = descriptiveMetadataInfoList;
  }


  public List<DescriptiveMetadataInfo> getDescriptiveMetadataInfoList() {
    return descriptiveMetadataInfoList;
  }

  public void setDescriptiveMetadataInfoList(List<DescriptiveMetadataInfo> descriptiveMetadataInfoList) {
    this.descriptiveMetadataInfoList = descriptiveMetadataInfoList;
  }

  public void addObject(DescriptiveMetadataInfo descriptiveMetadataInfo) {
    this.descriptiveMetadataInfoList.add(descriptiveMetadataInfo);
  }
}
