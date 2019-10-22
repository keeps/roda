/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

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
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DESCRIPTIVE_METADATA_LIST)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DescriptiveMetadataList implements RODAObjectList<DescriptiveMetadata> {
  private static final long serialVersionUID = -2795788423413555545L;
  private List<DescriptiveMetadata> metadataList;

  public DescriptiveMetadataList() {
    super();
    metadataList = new ArrayList<>();
  }

  public DescriptiveMetadataList(List<DescriptiveMetadata> descriptiveMetadataList) {
    super();
    this.metadataList = descriptiveMetadataList;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_DESCRIPTIVE_METADATA_LIST)
  @XmlElement(name = RodaConstants.RODA_OBJECT_DESCRIPTIVE_METADATA)
  public List<DescriptiveMetadata> getObjects() {
    return metadataList;
  }

  @Override
  public void setObjects(List<DescriptiveMetadata> descriptiveMetadataList) {
    this.metadataList = descriptiveMetadataList;
  }

  @Override
  public void addObject(DescriptiveMetadata descriptiveMetadata) {
    this.metadataList.add(descriptiveMetadata);
  }

}
