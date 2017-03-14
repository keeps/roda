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
import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = RodaConstants.RODA_OBJECT_PRESERVATION_METADATA_LIST)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreservationMetadataList implements RODAObjectList<PreservationMetadata> {
  private static final long serialVersionUID = 1682622329196597355L;

  @JsonIgnore
  private String id;

  private List<PreservationMetadata> metadataList;

  public PreservationMetadataList() {
    super();
    metadataList = new ArrayList<>();
  }

  public PreservationMetadataList(List<PreservationMetadata> preservationMetadataList) {
    super();
    this.metadataList = preservationMetadataList;
  }

  @JsonProperty(value = RodaConstants.RODA_OBJECT_PRESERVATION_METADATA_LIST)
  @XmlElement(name = RodaConstants.RODA_OBJECT_PRESERVATION_METADATA)
  public List<PreservationMetadata> getObjects() {
    return metadataList;
  }

  public void setObjects(List<PreservationMetadata> preservationMetadataList) {
    this.metadataList = preservationMetadataList;
  }

  @Override
  public void addObject(PreservationMetadata preservationMetadata) {
    this.metadataList.add(preservationMetadata);
  }

}
