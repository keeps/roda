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
@XmlRootElement(name = RodaConstants.RODA_OBJECT_OTHER_METADATA_LIST)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtherMetadataList implements RODAObjectList<OtherMetadata> {
  private static final long serialVersionUID = 3622093303834285254L;

  @JsonIgnore
  private String id;

  private List<OtherMetadata> metadataList;

  public OtherMetadataList() {
    super();
    metadataList = new ArrayList<>();
  }

  public OtherMetadataList(List<OtherMetadata> otherMetadataList) {
    super();
    this.metadataList = otherMetadataList;
  }

  @JsonProperty(value = RodaConstants.RODA_OBJECT_OTHER_METADATA_LIST)
  @XmlElement(name = RodaConstants.RODA_OBJECT_OTHER_METADATA)
  public List<OtherMetadata> getObjects() {
    return metadataList;
  }

  public void setObjects(List<OtherMetadata> otherMetadataList) {
    this.metadataList = otherMetadataList;
  }

  @Override
  public void addObject(OtherMetadata otherMetadata) {
    this.metadataList.add(otherMetadata);
  }

}
