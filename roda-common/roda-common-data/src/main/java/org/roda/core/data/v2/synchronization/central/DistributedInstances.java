/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.synchronization.central;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISTRIBUTED_INSTANCES)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DistributedInstances implements RODAObjectList<DistributedInstance> {
  private static final long serialVersionUID = -2558439591345762182L;
  private List<DistributedInstance> distributedInstances;

  public DistributedInstances() {
    super();
    distributedInstances = new ArrayList<>();
  }

  public DistributedInstances(List<DistributedInstance> distributedInstances) {
    super();
    this.distributedInstances = distributedInstances;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_DISTRIBUTED_INSTANCES)
  @XmlElement(name = RodaConstants.RODA_OBJECT_DISTRIBUTED_INSTANCE)
  public List<DistributedInstance> getObjects() {
    return distributedInstances;
  }

  @Override
  public void setObjects(List<DistributedInstance> distributedInstances) {
    this.distributedInstances = distributedInstances;
  }

  @Override
  public void addObject(DistributedInstance distributedInstance) {
    this.distributedInstances.add(distributedInstance);
  }
}
