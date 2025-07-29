/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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

  public String getXml() {
    return xml;
  }

  public void setXml(String xml) {
    this.xml = xml;
  }

}
