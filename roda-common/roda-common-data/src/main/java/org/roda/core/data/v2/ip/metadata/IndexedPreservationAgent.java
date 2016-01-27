/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;
import java.util.Date;

public class IndexedPreservationAgent implements Serializable {
  private static final long serialVersionUID = 7864328669898523851L;
  private String id;
  private String agentType;
  private String name;
  private String version;
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getAgentType() {
    return agentType;
  }
  public void setAgentType(String agentType) {
    this.agentType = agentType;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }
  
  
  
}
