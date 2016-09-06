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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = "preservation_metadata_list")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreservationMetadataList implements RODAObjectList<PreservationMetadata>, Serializable {
  private static final long serialVersionUID = 1682622329196597355L;

  @JsonIgnore
  private String id;

  private List<PreservationMetadata> preservationMetadataList;

  public PreservationMetadataList() {
    super();
    preservationMetadataList = new ArrayList<PreservationMetadata>();
  }

  public PreservationMetadataList(List<PreservationMetadata> preservationMetadataList) {
    super();
    this.preservationMetadataList = preservationMetadataList;
  }

  @JsonProperty(value = "preservation_metadata_list")
  @XmlElement(name = "preservation_metadata")
  public List<PreservationMetadata> getObjects() {
    return preservationMetadataList;
  }

  public void setObjects(List<PreservationMetadata> preservationMetadataList) {
    this.preservationMetadataList = preservationMetadataList;
  }

  @Override
  public void addObject(PreservationMetadata preservationMetadata) {
    this.preservationMetadataList.add(preservationMetadata);
  }

}
