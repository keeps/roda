package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class TechnicalMetadataInfos implements Serializable {

  @Serial
  private static final long serialVersionUID = 2735470238554186159L;

  private List<TechnicalMetadataInfo> technicalMetadataInfoList = new ArrayList<>();

  public TechnicalMetadataInfos() {
    // empty constructor
  }

  public TechnicalMetadataInfos(List<TechnicalMetadataInfo> technicalMetadataInfoList) {
    this.technicalMetadataInfoList = technicalMetadataInfoList;
  }

  public List<TechnicalMetadataInfo> getTechnicalMetadataInfoList() {
    return technicalMetadataInfoList;
  }

  public void setTechnicalMetadataInfoList(List<TechnicalMetadataInfo> technicalMetadataInfoList) {
    this.technicalMetadataInfoList = technicalMetadataInfoList;
  }

  public void addObject(TechnicalMetadataInfo descriptiveMetadataInfo) {
    this.technicalMetadataInfoList.add(descriptiveMetadataInfo);
  }
}
