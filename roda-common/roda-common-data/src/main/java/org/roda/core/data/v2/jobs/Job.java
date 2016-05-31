/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = "job")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Job implements IsIndexed, Serializable {
  private static final long serialVersionUID = 615993757726175203L;

  public static enum JOB_STATE {
    CREATED, STARTED, COMPLETED, FAILED_DURING_CREATION, FAILED_TO_COMPLETE;
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
  // job state details
  private String stateDetails = "";
  // 0-100 scale completion percentage
  private int completionPercentage = 0;

  private int sourceObjectsCount = 0;
  private int sourceObjectsWaitingToBeProcessed = 0;
  private int sourceObjectsBeingProcessed = 0;
  private int sourceObjectsProcessedWithSuccess = 0;
  private int sourceObjectsProcessedWithFailure = 0;
  private int outcomeObjectsWithManualIntervention = 0;

  // plugin full class (e.g. org.roda.core.plugins.plugins.base.FixityPlugin)
  private String plugin = null;
  // plugin type (e.g. ingest, maintenance, misc, etc.)
  private PluginType pluginType = null;
  // plugin parameters
  private Map<String, String> pluginParameters = new HashMap<String, String>();

  // objects to act upon (All, None, List, Filter, etc.)
  private SelectedItems<?> sourceObjects = null;
  private String outcomeObjectsClass = "";

  public Job() {
    super();
    startDate = new Date();
    state = JOB_STATE.CREATED;
  }

  public Job(Job job) {
    this();
    this.id = job.getId();
    this.name = job.getName();
    this.username = job.getUsername();
    this.pluginType = job.getPluginType();
    this.plugin = job.getPlugin();
    this.pluginParameters = new HashMap<String, String>(job.getPluginParameters());
    this.sourceObjects = job.getSourceObjects();
    if (sourceObjects instanceof SelectedItemsList) {
      this.sourceObjectsCount = ((SelectedItemsList<?>) sourceObjects).getIds().size();
    } else {
      this.sourceObjectsCount = 0;
    }
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

  public String getStateDetails() {
    return stateDetails;
  }

  public void setStateDetails(String stateDetails) {
    this.stateDetails = stateDetails;
  }

  public int getCompletionPercentage() {
    return completionPercentage;
  }

  public Job setCompletionPercentage(int completionPercentage) {
    this.completionPercentage = completionPercentage;
    return this;
  }

  public int getSourceObjectsCount() {
    return sourceObjectsCount;
  }

  public void setSourceObjectsCount(int sourceObjectsCount) {
    this.sourceObjectsCount = sourceObjectsCount;
  }

  public int getSourceObjectsWaitingToBeProcessed() {
    return sourceObjectsWaitingToBeProcessed;
  }

  public void setSourceObjectsWaitingToBeProcessed(int sourceObjectsWaitingToBeProcessed) {
    this.sourceObjectsWaitingToBeProcessed = sourceObjectsWaitingToBeProcessed;
  }

  public int getSourceObjectsBeingProcessed() {
    return sourceObjectsBeingProcessed;
  }

  public void setSourceObjectsBeingProcessed(int sourceObjectsBeingProcessed) {
    this.sourceObjectsBeingProcessed = sourceObjectsBeingProcessed;
  }

  public int getSourceObjectsProcessedWithSuccess() {
    return sourceObjectsProcessedWithSuccess;
  }

  public void setSourceObjectsProcessedWithSuccess(int sourceObjectsProcessedWithSuccess) {
    this.sourceObjectsProcessedWithSuccess = sourceObjectsProcessedWithSuccess;
  }

  public int getSourceObjectsProcessedWithFailure() {
    return sourceObjectsProcessedWithFailure;
  }

  public void setSourceObjectsProcessedWithFailure(int sourceObjectsProcessedWithFailure) {
    this.sourceObjectsProcessedWithFailure = sourceObjectsProcessedWithFailure;
  }

  public int getOutcomeObjectsWithManualIntervention() {
    return outcomeObjectsWithManualIntervention;
  }

  public void setOutcomeObjectsWithManualIntervention(int outcomeObjectsWithManualIntervention) {
    this.outcomeObjectsWithManualIntervention = outcomeObjectsWithManualIntervention;
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

  public PluginType getPluginType() {
    return pluginType;
  }

  public SelectedItems<?> getSourceObjects() {
    return sourceObjects;
  }

  public Job setSourceObjects(SelectedItems<?> sourceObjects) {
    this.sourceObjects = sourceObjects;
    return this;
  }

  public String getOutcomeObjectsClass() {
    return outcomeObjectsClass;
  }

  public Job setOutcomeObjectsClass(String outcomeObjectsClass) {
    this.outcomeObjectsClass = outcomeObjectsClass;
    return this;
  }

  public Job setPluginType(PluginType pluginType) {
    this.pluginType = pluginType;
    return this;
  }

  @Override
  public String toString() {
    return "Job [id=" + id + ", name=" + name + ", username=" + username + ", startDate=" + startDate + ", endDate="
      + endDate + ", state=" + state + ", stateDetails=" + stateDetails + ", completionPercentage="
      + completionPercentage + ", sourceObjectsCount=" + sourceObjectsCount + ", sourceObjectsWaitingToBeProcessed="
      + sourceObjectsWaitingToBeProcessed + ", sourceObjectsBeingProcessed=" + sourceObjectsBeingProcessed
      + ", sourceObjectsProcessedWithSuccess=" + sourceObjectsProcessedWithSuccess
      + ", sourceObjectsProcessedWithFailure=" + sourceObjectsProcessedWithFailure
      + ", outcomeObjectsWithManualIntervention=" + outcomeObjectsWithManualIntervention + ", plugin=" + plugin
      + ", pluginType=" + pluginType + ", pluginParameters=" + pluginParameters + ", sourceObjects=" + sourceObjects
      + ", outcomeObjectsClass=" + outcomeObjectsClass + "]";
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
  }

}
