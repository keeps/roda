/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = RodaConstants.RODA_OBJECT_JOB)
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job implements IsModelObject, IsIndexed {
  private static final long serialVersionUID = 615993757726175203L;

  public enum JOB_STATE {
    CREATED, STARTED, COMPLETED, FAILED_DURING_CREATION, FAILED_TO_COMPLETE, STOPPED, STOPPING;
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

  // job statistics (total source objects, etc.)
  JobStats jobStats = new JobStats();

  // plugin full class (e.g. org.roda.core.plugins.plugins.base.FixityPlugin)
  private String plugin = null;
  // plugin type (e.g. ingest, maintenance, misc, etc.)
  private PluginType pluginType = null;
  // plugin parameters
  private Map<String, String> pluginParameters = new HashMap<>();

  // objects to act upon (All, None, List, Filter, etc.)
  private SelectedItems<? extends IsRODAObject> sourceObjects = null;
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
    this.pluginParameters = new HashMap<>(job.getPluginParameters());
    this.sourceObjects = job.getSourceObjects();
    if (sourceObjects instanceof SelectedItemsList) {
      jobStats.setSourceObjectsCount(((SelectedItemsList<?>) sourceObjects).getIds().size());
    }
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 1;
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

  public JobStats getJobStats() {
    return jobStats;
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

  public boolean isInFinalState() {
    return isFinalState(state);
  }

  public boolean isStopping() {
    return JOB_STATE.STOPPING == state;
  }

  public static boolean isFinalState(JOB_STATE state) {
    return JOB_STATE.COMPLETED == state || JOB_STATE.FAILED_TO_COMPLETE == state || JOB_STATE.STOPPED == state
      || JOB_STATE.FAILED_DURING_CREATION == state;
  }

  public static List<String> nonFinalStateList() {
    List<String> nonFinalStates = new ArrayList<>();

    for (JOB_STATE state : JOB_STATE.values()) {
      if (!isFinalState(state)) {
        nonFinalStates.add(state.toString());
      }
    }

    return nonFinalStates;
  }

  @Override
  public String toString() {
    return "Job [id=" + id + ", name=" + name + ", username=" + username + ", startDate=" + startDate + ", endDate="
      + endDate + ", state=" + state + ", stateDetails=" + stateDetails + ", jobStats=" + jobStats + ", plugin="
      + plugin + ", pluginType=" + pluginType + ", pluginParameters=" + pluginParameters + ", sourceObjects="
      + sourceObjects + ", outcomeObjectsClass=" + outcomeObjectsClass + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "name", "username", "startDate", "endDate", "state", "stateDetails", "jobStats",
      "plugin", "pluginType", "pluginParameters", "sourceObjects", "outcomeObjectsClass");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(id, name, username, startDate, endDate, state, stateDetails, jobStats, plugin, pluginType,
      pluginParameters, sourceObjects, outcomeObjectsClass);
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID);
  }

}
