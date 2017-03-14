/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = RodaConstants.RODA_OBJECT_TRANSFERRED_RESOURCES)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferredResources implements RODAObjectList<TransferredResource> {
  private static final long serialVersionUID = -3774061860493505113L;
  private List<TransferredResource> resources;

  public TransferredResources() {
    super();
    resources = new ArrayList<>();
  }

  public TransferredResources(List<TransferredResource> resources) {
    super();
    this.resources = resources;
  }

  @JsonProperty(value = RodaConstants.RODA_OBJECT_TRANSFERRED_RESOURCES)
  @XmlElement(name = RodaConstants.RODA_OBJECT_TRANSFERRED_RESOURCE)
  public List<TransferredResource> getObjects() {
    return resources;
  }

  public void setObjects(List<TransferredResource> resources) {
    this.resources = resources;
  }

  public void addObject(TransferredResource resource) {
    this.resources.add(resource);
  }

}
