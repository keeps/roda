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

import org.roda.core.data.common.RodaConstants.JOB_STATE;
import org.roda.core.data.common.RodaConstants.PLUGIN_TYPE;
import org.roda.core.data.common.RodaConstants.RESOURCE_TYPE;

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
  private String username;
  // job start date
  private Date startDate;
  // job end date
  private Date endDate;
  // job state
  private JOB_STATE state;
  // 0-100 scale completion percentage
  private int completionPercentage;

  // plugin full class (e.g. org.roda.core.plugins.plugins.base.FixityPlugin)
  private String plugin;
  // plugin parameters
  private Map<String, String> pluginParameters;
  // plugin type (e.g. ingest, maintenance, misc, etc.)
  private PLUGIN_TYPE pluginType;

  // resource type (e.g. bagit, e-ark sip, etc.)
  private RESOURCE_TYPE resourceType;

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
    startDate = new Date();
    endDate = null;
    completionPercentage = 0;

    objectIds = new ArrayList<String>();
    pluginParameters = new HashMap<String, String>();
  }

  public Job(Job job) {
    super();
    this.id = job.getId();
    this.username = job.getUsername();
    this.startDate = job.getStartDate();
    this.endDate = job.getEndDate();
    this.completionPercentage = job.getCompletionPercentage();
    this.plugin = job.getPlugin();
    this.pluginParameters = new HashMap<String, String>(job.getPluginParameters());
    this.pluginType = job.getPluginType();
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

  public Job setId(String id) {
    this.id = id;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public Job setUsername(String username) {
    this.username = username;
    return this;
  }

  public Date getStartDate() {
    return startDate;
  }

  public Job setStartDate(Date startDate) {
    this.startDate = startDate;
    return this;
  }

  @XmlElement(nillable = true)
  public Date getEndDate() {
    return endDate;
  }

  public Job setEndDate(Date endDate) {
    this.endDate = endDate;
    return this;
  }

  public int getCompletionPercentage() {
    return completionPercentage;
  }

  public Job setCompletionPercentage(int completionPercentage) {
    this.completionPercentage = completionPercentage;
    return this;
  }

  public String getPlugin() {
    return plugin;
  }

  public Job setPlugin(String plugin) {
    this.plugin = plugin;
    return this;
  }

  @XmlElement(nillable = true)
  public Map<String, String> getPluginParameters() {
    return pluginParameters;
  }

  public Job setPluginParameters(Map<String, String> pluginParameters) {
    this.pluginParameters = pluginParameters;
    return this;
  }

  public String getOrchestratorMethod() {
    return orchestratorMethod;
  }

  public Job setOrchestratorMethod(String orchestratorMethod) {
    this.orchestratorMethod = orchestratorMethod;
    return this;
  }

  @XmlElement(nillable = true)
  public List<String> getObjectIds() {
    return objectIds;
  }

  public Job setObjectIds(List<String> objectIds) {
    this.objectIds = objectIds;
    return this;
  }

  public PLUGIN_TYPE getPluginType() {
    return pluginType;
  }

  public Job setPluginType(PLUGIN_TYPE pluginType) {
    this.pluginType = pluginType;
    return this;
  }

  public RESOURCE_TYPE getResourceType() {
    return resourceType;
  }

  public Job setResourceType(RESOURCE_TYPE resourceType) {
    this.resourceType = resourceType;
    return this;
  }

  @Override
  public String toString() {
    return "Job [id=" + id + ", username=" + username + ", startDate=" + startDate + ", endDate=" + endDate + ", state="
      + state + ", completionPercentage=" + completionPercentage + ", plugin=" + plugin + ", pluginParameters="
      + pluginParameters + ", pluginType=" + pluginType + ", resourceType=" + resourceType + ", orchestratorMethod="
      + orchestratorMethod + ", objectIds=" + objectIds + "]";
  }

}
