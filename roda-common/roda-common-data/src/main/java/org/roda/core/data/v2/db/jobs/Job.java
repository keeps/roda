/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.db.jobs;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.PluginType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * A job while it runs ({@code jobs}).
 *
 * Its counters, plugin parameters, LIST source objects, users and attachments
 * live in their own tables ({@link JobStats}, {@link JobPluginParameter},
 * {@link JobSourceObject}, {@link JobUser}, {@link JobAttachment}), all keyed
 * by {@link #getId()}.
 *
 * @author RODA Development Team
 */
@Entity
@Table(name = "jobs")
public class Job implements Serializable {
  @Serial
  private static final long serialVersionUID = -2283436826411950021L;

  @Id
  @Column(name = "id", columnDefinition = "uuid")
  private String id;

  @Column(name = "name")
  private String name;

  @Column(name = "username")
  private String username;

  @Enumerated(EnumType.STRING)
  @Column(name = "state", nullable = false, length = 32)
  private JOB_STATE state = JOB_STATE.CREATED;

  @Column(name = "state_details", columnDefinition = "text")
  private String stateDetails;

  @Column(name = "plugin")
  private String plugin;

  @Enumerated(EnumType.STRING)
  @Column(name = "plugin_type", length = 32)
  private PluginType pluginType;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority", length = 16)
  private JobPriority priority;

  @Enumerated(EnumType.STRING)
  @Column(name = "parallelism", length = 16)
  private JobParallelism parallelism;

  @Column(name = "outcome_objects_class")
  private String outcomeObjectsClass;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_selection", nullable = false, length = 8)
  private JobSourceSelection sourceSelection = JobSourceSelection.NONE;

  @Column(name = "source_objects_class")
  private String sourceObjectsClass;

  // the selection filter as JSON; set exactly when sourceSelection is FILTER
  @Column(name = "source_filter", columnDefinition = "jsonb")
  private String sourceFilter;

  @Column(name = "source_just_active")
  private Boolean sourceJustActive;

  @Column(name = "instance_id")
  private String instanceId;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "start_date", nullable = false)
  private Date startDate;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "end_date")
  private Date endDate;

  // set once the job and its reports were written to storage; the row is then removed by the cleanup task
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "flushed_at")
  private Date flushedAt;

  public Job() {
    // used by JPA and serialization
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
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

  public String getPlugin() {
    return plugin;
  }

  public void setPlugin(String plugin) {
    this.plugin = plugin;
  }

  public PluginType getPluginType() {
    return pluginType;
  }

  public void setPluginType(PluginType pluginType) {
    this.pluginType = pluginType;
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

  public String getOutcomeObjectsClass() {
    return outcomeObjectsClass;
  }

  public void setOutcomeObjectsClass(String outcomeObjectsClass) {
    this.outcomeObjectsClass = outcomeObjectsClass;
  }

  public JobSourceSelection getSourceSelection() {
    return sourceSelection;
  }

  public void setSourceSelection(JobSourceSelection sourceSelection) {
    this.sourceSelection = sourceSelection;
  }

  public String getSourceObjectsClass() {
    return sourceObjectsClass;
  }

  public void setSourceObjectsClass(String sourceObjectsClass) {
    this.sourceObjectsClass = sourceObjectsClass;
  }

  public String getSourceFilter() {
    return sourceFilter;
  }

  public void setSourceFilter(String sourceFilter) {
    this.sourceFilter = sourceFilter;
  }

  public Boolean getSourceJustActive() {
    return sourceJustActive;
  }

  public void setSourceJustActive(Boolean sourceJustActive) {
    this.sourceJustActive = sourceJustActive;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Date getFlushedAt() {
    return flushedAt;
  }

  public void setFlushedAt(Date flushedAt) {
    this.flushedAt = flushedAt;
  }
}
