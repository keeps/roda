package org.roda.core.data.v2.ip.metadata;

import org.roda.core.data.v2.ip.Permissions;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@JsonTypeName("DescriptiveMetadataRequestXML")
public class DescriptiveMetadataRequestXML extends CreateDescriptiveMetadataRequest {
  private String xml;

  public DescriptiveMetadataRequestXML() {
    super();
  }

  public DescriptiveMetadataRequestXML(String id, String filename, String type, String version, String rawTemplate,
    boolean similar, Permissions permissions, String xml) {
    super(id, filename, type, version, rawTemplate, similar, permissions);
    this.xml = xml;
  }

  @Override
  public String getXml() {
    return xml;
  }

  @Override
  public void setXml(String xml) {
    this.xml = xml;
  }

}
