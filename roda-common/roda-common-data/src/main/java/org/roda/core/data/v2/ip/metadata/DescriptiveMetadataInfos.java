package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
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
