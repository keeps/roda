package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class InstanceState implements Serializable {

  private boolean localToInstance;

  private String instanceName;

  public InstanceState() {
    // do nothing
  }

  public String getInstanceName() {
    return instanceName;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  public boolean isLocalToInstance() {
    return localToInstance;
  }

  public void setLocalToInstance(boolean localToInstance) {
    this.localToInstance = localToInstance;
  }
}
