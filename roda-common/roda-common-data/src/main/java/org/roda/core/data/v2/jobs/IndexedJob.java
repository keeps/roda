package org.roda.core.data.v2.jobs;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.HasId;
import org.roda.core.data.v2.ip.HasInstanceID;
import org.roda.core.data.v2.ip.HasInstanceName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * This class contains the indexed information about a Job.
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_JOB)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndexedJob implements IsIndexed, HasId, HasInstanceID, HasInstanceName {
  @Serial
  private static final long serialVersionUID = 2293292697809808605L;

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
  private Job.JOB_STATE state = null;
  // job state details
  private String stateDetails = "";

  // job instance id
  private String instanceId = null;

  private List<JobUserDetails> jobUsersDetails = new ArrayList<>();
  private String instanceName = null;

  // job statistics (total source objects, etc.)
  JobStats jobStats = new JobStats();

  // plugin full class (e.g. org.roda.core.plugins.plugins.base.FixityPlugin)
  private String plugin = null;
  // plugin type (e.g. ingest, maintenance, misc, etc.)
  private PluginType pluginType = null;
  // plugin parameters
  private Map<String, String> pluginParameters = new HashMap<>();

  // objects to act upon (All, None, List, Filter, etc.)
  private String outcomeObjectsClass = "";

  private List<String> attachmentsList = new ArrayList<>();

  private Map<String, Object> fields;

  private JobPriority priority;

  private JobParallelism parallelism;

  public IndexedJob() {
    super();
    startDate = new Date();
    state = Job.JOB_STATE.CREATED;
    priority = JobPriority.MEDIUM;
    parallelism = JobParallelism.NORMAL;
  }

  public IndexedJob(IndexedJob indexedJob) {
    this();
    this.id = indexedJob.getId();
    this.name = indexedJob.getName();
    this.username = indexedJob.getUsername();
    this.pluginType = indexedJob.getPluginType();
    this.priority = indexedJob.getPriority();
    this.parallelism = indexedJob.getParallelism();
    this.plugin = indexedJob.getPlugin();
    this.pluginParameters = new HashMap<>(indexedJob.getPluginParameters());
    this.instanceId = indexedJob.getInstanceId();
    this.instanceName = indexedJob.getInstanceName();
    this.attachmentsList = indexedJob.getAttachmentsList();
    this.jobUsersDetails = indexedJob.getJobUsersDetails();
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "name", "username", "startDate", "endDate", "state", "stateDetails", "priority", "type",
      "jobStats", "plugin", "pluginType", "pluginParameters", "sourceObjects", "outcomeObjectsClass", "instanceId",
      "attachmentsList");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(id, name, username, startDate, endDate, state, stateDetails, priority, parallelism, jobStats,
      plugin, pluginType, pluginParameters, outcomeObjectsClass, instanceId, attachmentsList);
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
  }

  @Override
  public List<String> liteFields() {
    return Collections.singletonList(RodaConstants.INDEX_UUID);
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public IndexedJob setName(String name) {
    this.name = name;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public IndexedJob setUsername(String username) {
    this.username = username;
    return this;
  }

  public List<JobUserDetails> getJobUsersDetails() {
    return jobUsersDetails;
  }

  public void setJobUsersDetails(List<JobUserDetails> jobUserDetails) {
    this.jobUsersDetails = jobUserDetails;
  }

  public Date getStartDate() {
    return startDate;
  }

  public IndexedJob setStartDate(Date startDate) {
    this.startDate = startDate;
    return this;
  }

  @jakarta.xml.bind.annotation.XmlElement(nillable = true)
  public Date getEndDate() {
    return endDate;
  }

  public IndexedJob setEndDate(Date endDate) {
    this.endDate = endDate;
    return this;
  }

  public Job.JOB_STATE getState() {
    return state;
  }

  public void setState(Job.JOB_STATE state) {
    this.state = state;
  }

  public JobPriority getPriority() {
    return priority;
  }

  public void setPriority(JobPriority priority) {
    this.priority = priority;
  }

  public JobParallelism getParallelism() {
    return parallelism;
  }

  public void setParallelism(JobParallelism parallelism) {
    this.parallelism = parallelism;
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

  public void setJobStats(JobStats jobStats) {
    this.jobStats = jobStats;
  }

  public String getPlugin() {
    return plugin;
  }

  public IndexedJob setPlugin(String plugin) {
    this.plugin = plugin;
    return this;
  }

  @jakarta.xml.bind.annotation.XmlElement(nillable = true)
  public Map<String, String> getPluginParameters() {
    return pluginParameters;
  }

  public IndexedJob setPluginParameters(Map<String, String> pluginParameters) {
    this.pluginParameters = pluginParameters;
    return this;
  }

  public PluginType getPluginType() {
    return pluginType;
  }

  public String getOutcomeObjectsClass() {
    return outcomeObjectsClass;
  }

  public IndexedJob setOutcomeObjectsClass(String outcomeObjectsClass) {
    this.outcomeObjectsClass = outcomeObjectsClass;
    return this;
  }

  public IndexedJob setPluginType(PluginType pluginType) {
    this.pluginType = pluginType;
    return this;
  }

  public boolean isInFinalState() {
    return Job.isFinalState(state);
  }

  public boolean isStopping() {
    return Job.JOB_STATE.STOPPING == state;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  public List<String> getAttachmentsList() {
    return attachmentsList;
  }

  public void addAttachment(String attachment) {
    this.attachmentsList.add(attachment);
  }

  public void setAttachmentsList(List<String> attachmentsList) {
    this.attachmentsList = attachmentsList;
  }

  /**
   * @return the fields
   */
  public Map<String, Object> getFields() {
    return fields;
  }

  /**
   * @param fields
   *          the fields to set
   */
  public void setFields(Map<String, Object> fields) {
    this.fields = fields;
  }
}
