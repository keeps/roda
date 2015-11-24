/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = "job")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Job implements Serializable {
  private static final long serialVersionUID = 615993757726175203L;

  // job identifier
  private String id;
  // job creator
  private String user;
  // job start date
  private Date start;
  // job end date
  private Date end;
  // 0-100 scale completion status
  private int completionStatus;

  // plugin full class (e.g. org.roda.core.plugins.plugins.base.FixityPlugin)
  private String plugin;
  // plugin parameters
  private Map<String, String> pluginParameters;

  // resource type (e.g. bagit, e-ark sip, etc.)
  private String resourceType;

  // type of method that orchestrator should execute (e.g.
  // runPluginOnTransferredResources, runPluginOnAIPs, etc.)
  private String orchestratorMethod;
  // list of object ids to act upon
  private List<String> objectIds;
  // // object full class (e.g. org.roda.core.model.AIP, etc.)
  // private String objectType;

  public Job() {
    super();
    id = UUID.randomUUID().toString();
    start = new Date();
    end = null;
    completionStatus = 0;
    objectIds = new ArrayList<String>();
    pluginParameters = new HashMap<String, String>();
  }

  public Job(Job job) {
    super();
    this.id = job.getId();
    this.user = job.getUser();
    this.start = job.getStart();
    this.end = job.getEnd();
    this.completionStatus = job.getCompletionStatus();
    this.plugin = job.getPlugin();
    this.pluginParameters = new HashMap<String, String>(job.getPluginParameters());
    this.resourceType = job.getResourceType();
    this.orchestratorMethod = job.getOrchestratorMethod();
    this.objectIds = new ArrayList<String>(job.getObjectIds());
  }

  public Job(String id) {
    this();
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  @XmlElement(nillable = true)
  public Date getEnd() {
    return end;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  public int getCompletionStatus() {
    return completionStatus;
  }

  public void setCompletionStatus(int completionStatus) {
    this.completionStatus = completionStatus;
  }

  public String getPlugin() {
    return plugin;
  }

  public void setPlugin(String plugin) {
    this.plugin = plugin;
  }

  @XmlElement(nillable = true)
  public Map<String, String> getPluginParameters() {
    return pluginParameters;
  }

  public void setPluginParameters(Map<String, String> pluginParameters) {
    this.pluginParameters = pluginParameters;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getOrchestratorMethod() {
    return orchestratorMethod;
  }

  public void setOrchestratorMethod(String orchestratorMethod) {
    this.orchestratorMethod = orchestratorMethod;
  }

  @XmlElement(nillable = true)
  public List<String> getObjectIds() {
    return objectIds;
  }

  public void setObjectIds(List<String> objectIds) {
    this.objectIds = objectIds;
  }

  @Override
  public String toString() {
    return "Job [id=" + id + ", user=" + user + ", start=" + start + ", end=" + end + ", completionStatus="
      + completionStatus + ", plugin=" + plugin + ", pluginParameters=" + pluginParameters + ", resourceType="
      + resourceType + ", orchestratorMethod=" + orchestratorMethod + ", objectIds=" + objectIds + "]";
  }

}
