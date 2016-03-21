/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = "job")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Job implements IsIndexed, Serializable {
  private static final long serialVersionUID = 615993757726175203L;

  public static enum JOB_STATE {
    CREATED, STARTED, COMPLETED, FAILED_DURING_CREATION;
  }

  public static enum ORCHESTRATOR_METHOD {
    RUN_PLUGIN, RUN_PLUGIN_ON_OBJECTS, ON_TRANSFERRED_RESOURCES, ON_AIPS, ON_ALL_AIPS, ON_ALL_REPRESENTATIONS,
    ON_ALL_FILES;
  }

  // job identifier
  private String id = null;
  // job name
  private String name = null;
  // job creator
  private String username = null;
  // job start date
  private Date startDate = null;
  // job end date
  private Date endDate = null;
  // job state
  private JOB_STATE state = null;
  // 0-100 scale completion percentage
  private int completionPercentage = 0;

  // plugin full class (e.g. org.roda.core.plugins.plugins.base.FixityPlugin)
  private String plugin = null;
  // plugin type (e.g. ingest, maintenance, misc, etc.)
  private PluginType pluginType = null;
  // plugin parameters
  private Map<String, String> pluginParameters = new HashMap<String, String>();

  // type of method that orchestrator should execute (e.g.
  // runPluginOnTransferredResources, runPluginOnAIPs, etc.)
  private ORCHESTRATOR_METHOD orchestratorMethod = null;
  // list of object ids to act upon
  private List<String> objectIds = new ArrayList<String>();

  // report id
  private String reportId = null;

  public Job() {
    super();
    startDate = new Date();
    state = JOB_STATE.CREATED;
  }

  public Job(Job job) {
    super();
    this.id = job.getId();
    this.name = job.getName();
    this.username = job.getUsername();
    this.startDate = job.getStartDate();
    this.endDate = job.getEndDate();
    this.state = job.getState();
    this.completionPercentage = job.getCompletionPercentage();
    this.pluginType = job.getPluginType();
    this.plugin = job.getPlugin();
    this.pluginParameters = new HashMap<String, String>(job.getPluginParameters());
    this.orchestratorMethod = job.getOrchestratorMethod();
    this.objectIds = new ArrayList<String>(job.getObjectIds());
  }

  public String getId() {
    return id;
  }

  public Job setId(String id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Job setName(String name) {
    this.name = name;
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

  public JOB_STATE getState() {
    return state;
  }

  public void setState(JOB_STATE state) {
    this.state = state;
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

  public ORCHESTRATOR_METHOD getOrchestratorMethod() {
    return orchestratorMethod;
  }

  public Job setOrchestratorMethod(ORCHESTRATOR_METHOD orchestratorMethod) {
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

  public PluginType getPluginType() {
    return pluginType;
  }

  public Job setPluginType(PluginType pluginType) {
    this.pluginType = pluginType;
    return this;
  }

  @Override
  public String toString() {
    return "Job [id=" + id + ", name=" + name + ", username=" + username + ", startDate=" + startDate + ", endDate="
      + endDate + ", state=" + state + ", completionPercentage=" + completionPercentage + ", plugin=" + plugin
      + ", pluginType=" + pluginType + ", pluginParameters=" + pluginParameters + ", orchestratorMethod="
      + orchestratorMethod + ", objectIds=" + objectIds + ", reportId=" + reportId + "]";
  }

  @Override
  public String getUUID() {
    return getId();
  }

}
