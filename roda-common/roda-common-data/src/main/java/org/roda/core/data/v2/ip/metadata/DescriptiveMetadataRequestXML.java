package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.ip.Permissions;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@JsonTypeName("DescriptiveMetadataRequestXML")
public class DescriptiveMetadataRequestXML extends CreateDescriptiveMetadataRequest {
  @Serial
  private static final long serialVersionUID = 3769927921435978905L;
  private String xml;

  public DescriptiveMetadataRequestXML() {
    super();
  }

  public DescriptiveMetadataRequestXML(String id, String filename, String type, String version,
    boolean similar, Permissions permissions, String xml) {
    super(id, filename, type, version, similar, permissions);
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

  @Override
  public Set<MetadataValue> getValues() {
    return Set.of();
  }
}
