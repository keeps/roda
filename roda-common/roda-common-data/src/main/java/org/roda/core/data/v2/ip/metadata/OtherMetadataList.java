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
@XmlRootElement(name = "other_metadata_list")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtherMetadataList implements RODAObjectList<OtherMetadata>, Serializable {
  private static final long serialVersionUID = 3622093303834285254L;

  @JsonIgnore
  private String id;

  private List<OtherMetadata> otherMetadataList;

  public OtherMetadataList() {
    super();
    otherMetadataList = new ArrayList<OtherMetadata>();
  }

  public OtherMetadataList(List<OtherMetadata> otherMetadataList) {
    super();
    this.otherMetadataList = otherMetadataList;
  }

  @JsonProperty(value = "other_metadata_list")
  @XmlElement(name = "other_metadata")
  public List<OtherMetadata> getObjects() {
    return otherMetadataList;
  }

  public void setObjects(List<OtherMetadata> otherMetadataList) {
    this.otherMetadataList = otherMetadataList;
  }

  @Override
  public void addObject(OtherMetadata otherMetadata) {
    this.otherMetadataList.add(otherMetadata);
  }

}
