/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.HasId;
import org.roda.core.data.v2.ip.HasInstanceID;
import org.roda.core.data.v2.ip.HasInstanceName;
import org.roda.core.data.v2.jpa.JobUserDetailsListConverter;
import org.roda.core.data.v2.jpa.ObjectMapConverter;
import org.roda.core.data.v2.jpa.SelectedItemsConverter;
import org.roda.core.data.v2.jpa.StringListConverter;
import org.roda.core.data.v2.jpa.StringMapConverter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@Entity
@Table(name = "jobs")
@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_JOB)
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job implements IsModelObject, HasId, HasInstanceID, HasInstanceName {
  @Serial
  private static final long serialVersionUID = 615993757726175203L;

  public enum JOB_STATE {
    CREATED, STARTED, COMPLETED, FAILED_DURING_CREATION, FAILED_TO_COMPLETE, STOPPED, STOPPING, TO_BE_CLEANED,
    PENDING_APPROVAL, REJECTED, SCHEDULED;
  }
  // job identifier
  @Id
  @Column(name = "id")
  private String id = null;
  // job name
  @Column(name = "name")
  private String name = null;
  // job creator
  @Column(name = "username")
  private String username = null;
  // job start date
  @Column(name = "start_date")
  @Temporal(TemporalType.TIMESTAMP)
  private Date startDate = null;
  // job end date
  @Column(name = "end_date")
  @Temporal(TemporalType.TIMESTAMP)
  private Date endDate = null;
  // job state
  @Enumerated(EnumType.STRING)
  @Column(name = "state")
  private JOB_STATE state = null;
  // job state details
  @Column(name = "state_details", columnDefinition = "TEXT")
  private String stateDetails = "";

  // job instance id
  @Column(name = "instance_id")
  private String instanceId = null;

  @Column(name = "job_users_details", columnDefinition = "TEXT")
  @Convert(converter = JobUserDetailsListConverter.class)
  private List<JobUserDetails> jobUsersDetails = new ArrayList<>();
  @Column(name = "instance_name")
  private String instanceName = null;

  // job statistics - expanded into separate columns
  @Column(name = "stats_completion_percentage")
  private int statsCompletionPercentage = 0;
  @Column(name = "stats_source_objects_count")
  private int statsSourceObjectsCount = 0;
  @Column(name = "stats_source_objects_being_processed")
  private int statsSourceObjectsBeingProcessed = 0;
  @Column(name = "stats_source_objects_waiting_to_be_processed")
  private int statsSourceObjectsWaitingToBeProcessed = 0;
  @Column(name = "stats_source_objects_processed_with_success")
  private int statsSourceObjectsProcessedWithSuccess = 0;
  @Column(name = "stats_source_objects_processed_with_partial_success")
  private int statsSourceObjectsProcessedWithPartialSuccess = 0;
  @Column(name = "stats_source_objects_processed_with_failure")
  private int statsSourceObjectsProcessedWithFailure = 0;
  @Column(name = "stats_source_objects_processed_with_skipped")
  private int statsSourceObjectsProcessedWithSkipped = 0;
  @Column(name = "stats_outcome_objects_with_manual_intervention")
  private int statsOutcomeObjectsWithManualIntervention = 0;

  // Transient jobStats field for backwards compatibility with existing code
  @Transient
  private JobStats jobStats = new JobStats();

  // plugin full class (e.g. org.roda.core.plugins.plugins.base.FixityPlugin)
  @Column(name = "plugin")
  private String plugin = null;
  // plugin type (e.g. ingest, maintenance, misc, etc.)
  @Enumerated(EnumType.STRING)
  @Column(name = "plugin_type")
  private PluginType pluginType = null;
  // plugin parameters
  @Column(name = "plugin_parameters", columnDefinition = "TEXT")
  @Convert(converter = StringMapConverter.class)
  private Map<String, String> pluginParameters = new HashMap<>();

  // objects to act upon (All, None, List, Filter, etc.)
  @Column(name = "source_objects", columnDefinition = "TEXT")
  @Convert(converter = SelectedItemsConverter.class)
  private SelectedItems<? extends IsRODAObject> sourceObjects = null;
  @Column(name = "outcome_objects_class")
  private String outcomeObjectsClass = "";

  @Column(name = "attachments_list", columnDefinition = "TEXT")
  @Convert(converter = StringListConverter.class)
  private List<String> attachmentsList = new ArrayList<>();

  @Column(name = "fields", columnDefinition = "TEXT")
  @Convert(converter = ObjectMapConverter.class)
  private Map<String, Object> fields;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority")
  private JobPriority priority;

  @Enumerated(EnumType.STRING)
  @Column(name = "parallelism")
  private JobParallelism parallelism;

  public Job() {
    super();
    startDate = new Date();
    state = JOB_STATE.CREATED;
    priority = JobPriority.MEDIUM;
    parallelism = JobParallelism.NORMAL;
  }

  public Job(Job job) {
    this();
    this.id = job.getId();
    this.name = job.getName();
    this.username = job.getUsername();
    this.pluginType = job.getPluginType();
    this.priority = job.getPriority();
    this.parallelism = job.getParallelism();
    this.plugin = job.getPlugin();
    this.pluginParameters = new HashMap<>(job.getPluginParameters());
    this.sourceObjects = job.getSourceObjects();
    if (sourceObjects instanceof SelectedItemsList) {
      getJobStats().setSourceObjectsCount(((SelectedItemsList<?>) sourceObjects).getIds().size());
    }
    this.instanceId = job.getInstanceId();
    this.instanceName = job.getInstanceName();
    this.attachmentsList = job.getAttachmentsList();
    this.jobUsersDetails = job.getJobUsersDetails();
  }

  @Transient
  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 1;
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

  public List<JobUserDetails> getJobUsersDetails() {
    return jobUsersDetails;
  }

  public void setJobUsersDetails(List<JobUserDetails> jobUserDetails) {
    this.jobUsersDetails = jobUserDetails;
  }

  public Date getStartDate() {
    return startDate;
  }

  public Job setStartDate(Date startDate) {
    this.startDate = startDate;
    return this;
  }

  @jakarta.xml.bind.annotation.XmlElement(nillable = true)
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
    // Build JobStats from individual columns for backwards compatibility
    if (jobStats == null) {
      jobStats = new JobStats();
    }
    jobStats.setCompletionPercentage(statsCompletionPercentage);
    jobStats.setSourceObjectsCount(statsSourceObjectsCount);
    jobStats.setSourceObjectsBeingProcessed(statsSourceObjectsBeingProcessed);
    jobStats.setSourceObjectsWaitingToBeProcessed(statsSourceObjectsWaitingToBeProcessed);
    jobStats.setSourceObjectsProcessedWithSuccess(statsSourceObjectsProcessedWithSuccess);
    jobStats.setSourceObjectsProcessedWithPartialSuccess(statsSourceObjectsProcessedWithPartialSuccess);
    jobStats.setSourceObjectsProcessedWithFailure(statsSourceObjectsProcessedWithFailure);
    jobStats.setSourceObjectsProcessedWithSkipped(statsSourceObjectsProcessedWithSkipped);
    jobStats.setOutcomeObjectsWithManualIntervention(statsOutcomeObjectsWithManualIntervention);
    return jobStats;
  }

  public void setJobStats(JobStats jobStats) {
    this.jobStats = jobStats;
    // Sync to individual columns
    if (jobStats != null) {
      this.statsCompletionPercentage = jobStats.getCompletionPercentage();
      this.statsSourceObjectsCount = jobStats.getSourceObjectsCount();
      this.statsSourceObjectsBeingProcessed = jobStats.getSourceObjectsBeingProcessed();
      this.statsSourceObjectsWaitingToBeProcessed = jobStats.getSourceObjectsWaitingToBeProcessed();
      this.statsSourceObjectsProcessedWithSuccess = jobStats.getSourceObjectsProcessedWithSuccess();
      this.statsSourceObjectsProcessedWithPartialSuccess = jobStats.getSourceObjectsProcessedWithPartialSuccess();
      this.statsSourceObjectsProcessedWithFailure = jobStats.getSourceObjectsProcessedWithFailure();
      this.statsSourceObjectsProcessedWithSkipped = jobStats.getSourceObjectsProcessedWithSkipped();
      this.statsOutcomeObjectsWithManualIntervention = jobStats.getOutcomeObjectsWithManualIntervention();
    }
  }

  public String getPlugin() {
    return plugin;
  }

  public Job setPlugin(String plugin) {
    this.plugin = plugin;
    return this;
  }

  @jakarta.xml.bind.annotation.XmlElement(nillable = true)
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
      || JOB_STATE.FAILED_DURING_CREATION == state || JOB_STATE.REJECTED == state || JOB_STATE.PENDING_APPROVAL == state
      || JOB_STATE.SCHEDULED == state;
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

  @Override
  public String toString() {
    return "Job [id=" + id + ", name=" + name + ", username=" + username + ", startDate=" + startDate + ", endDate="
      + endDate + ", state=" + state + ", stateDetails=" + stateDetails + ", priority=" + priority + ", type="
      + parallelism + ", jobStats=" + jobStats + ", plugin=" + plugin + ", pluginType=" + pluginType
      + ", pluginParameters=" + pluginParameters + ", sourceObjects=" + sourceObjects + ", outcomeObjectsClass="
      + outcomeObjectsClass + ", instanceId=" + instanceId + ", attachmentsList=" + attachmentsList + "]";
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

  public Job clone() {
    final Job newJob = new Job();
    newJob.setName(getName());
    newJob.setUsername(getUsername());
    newJob.setInstanceName(getInstanceName());
    newJob.setStartDate(getStartDate());
    newJob.setEndDate(getEndDate());
    newJob.setJobUsersDetails(getJobUsersDetails());
    newJob.setPluginType(getPluginType());
    newJob.setPlugin(getPlugin());
    newJob.setPluginParameters(getPluginParameters());
    newJob.setPriority(getPriority());
    newJob.setParallelism(getParallelism());
    newJob.setState(getState());
    newJob.setJobUsersDetails(getJobUsersDetails());
    newJob.setStateDetails(getStateDetails());
    newJob.setSourceObjects(getSourceObjects());
    newJob.setOutcomeObjectsClass(getOutcomeObjectsClass());
    newJob.getJobStats().setCompletionPercentage(getJobStats().getCompletionPercentage());
    newJob.getJobStats().setSourceObjectsBeingProcessed(getJobStats().getSourceObjectsBeingProcessed());
    newJob.getJobStats()
      .setOutcomeObjectsWithManualIntervention(getJobStats().getOutcomeObjectsWithManualIntervention());
    newJob.getJobStats().setSourceObjectsCount(getJobStats().getSourceObjectsCount());
    newJob.getJobStats()
      .setSourceObjectsProcessedWithPartialSuccess(getJobStats().getSourceObjectsProcessedWithPartialSuccess());
    newJob.getJobStats().setSourceObjectsProcessedWithSkipped(getJobStats().getSourceObjectsProcessedWithSkipped());
    newJob.getJobStats().setSourceObjectsProcessedWithSuccess(getJobStats().getSourceObjectsProcessedWithSuccess());
    newJob.getJobStats().setSourceObjectsProcessedWithFailure(getJobStats().getSourceObjectsProcessedWithFailure());
    newJob.getJobStats().setSourceObjectsWaitingToBeProcessed(getJobStats().getSourceObjectsWaitingToBeProcessed());
    return newJob;
  }
}
