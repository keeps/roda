/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;
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
@XmlRootElement(name = "descriptive_metadata_list")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DescriptiveMetadataList implements RODAObjectList<DescriptiveMetadata>, Serializable {
  private static final long serialVersionUID = -2795788423413555545L;

  private List<DescriptiveMetadata> descriptiveMetadataList;

  public DescriptiveMetadataList() {
    super();
    descriptiveMetadataList = new ArrayList<DescriptiveMetadata>();
  }

  public DescriptiveMetadataList(List<DescriptiveMetadata> descriptiveMetadataList) {
    super();
    this.descriptiveMetadataList = descriptiveMetadataList;
  }

  @JsonProperty(value = "descriptive_metadata_list")
  @XmlElement(name = "descriptive_metadata")
  public List<DescriptiveMetadata> getObjects() {
    return descriptiveMetadataList;
  }

  public void setObjects(List<DescriptiveMetadata> descriptiveMetadataList) {
    this.descriptiveMetadataList = descriptiveMetadataList;
  }

  @Override
  public void addObject(DescriptiveMetadata descriptiveMetadata) {
    this.descriptiveMetadataList.add(descriptiveMetadata);
  }

}
