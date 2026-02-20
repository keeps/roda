/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.roda.core.data.v2.ip.AIPState;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * StepReport represents a single step/plugin execution within a job report.
 * This is a separate JPA entity to properly store step reports in their own table
 * with a foreign key relationship to the parent Report.
 * 
 * @author RODA Development Team
 */
@Entity
@Table(name = "job_step_reports", indexes = {@Index(name = "idx_step_report_parent_id", columnList = "parent_report_id")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StepReport implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "parent_report_id")
  private String parentReportId;

  @Column(name = "source_object_id")
  private String sourceObjectId = Report.NO_SOURCE_OBJECT_ID;

  @Column(name = "source_object_class")
  private String sourceObjectClass = Report.NO_SOURCE_OBJECT_CLASS;

  @Column(name = "outcome_object_id")
  private String outcomeObjectId = Report.NO_OUTCOME_OBJECT_ID;

  @Column(name = "outcome_object_class")
  private String outcomeObjectClass = Report.NO_OUTCOME_OBJECT_CLASS;

  @Enumerated(EnumType.STRING)
  @Column(name = "outcome_object_state")
  private AIPState outcomeObjectState = AIPState.getDefault();

  @Column(name = "title")
  private String title = "";

  @Column(name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(name = "date_updated")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateUpdated;

  @Column(name = "completion_percentage")
  private Integer completionPercentage = 0;

  @Column(name = "steps_completed")
  private Integer stepsCompleted = 0;

  @Column(name = "total_steps")
  private Integer totalSteps = 0;

  @Column(name = "plugin")
  private String plugin = "";

  @Column(name = "plugin_name")
  private String pluginName = "";

  @Column(name = "plugin_version")
  private String pluginVersion = "";

  @Enumerated(EnumType.STRING)
  @Column(name = "plugin_state")
  private PluginState pluginState = PluginState.RUNNING;

  @Column(name = "plugin_is_mandatory")
  private Boolean pluginIsMandatory = true;

  @Column(name = "plugin_details", columnDefinition = "TEXT")
  private String pluginDetails = "";

  @Column(name = "html_plugin_details")
  private boolean htmlPluginDetails = false;

  @Column(name = "step_order")
  private Integer stepOrder = 0;

  public StepReport() {
    super();
    this.id = UUID.randomUUID().toString();
    this.dateCreated = new Date();
  }

  /**
   * Create a StepReport from a Report (for backwards compatibility during migration)
   */
  public StepReport(Report report, String parentReportId, int stepOrder) {
    this();
    this.parentReportId = parentReportId;
    this.stepOrder = stepOrder;
    this.sourceObjectId = report.getSourceObjectId();
    this.sourceObjectClass = report.getSourceObjectClass();
    this.outcomeObjectId = report.getOutcomeObjectId();
    this.outcomeObjectClass = report.getOutcomeObjectClass();
    this.outcomeObjectState = report.getOutcomeObjectState();
    this.title = report.getTitle();
    this.dateCreated = report.getDateCreated();
    this.dateUpdated = report.getDateUpdated();
    this.completionPercentage = report.getCompletionPercentage();
    this.stepsCompleted = report.getStepsCompleted();
    this.totalSteps = report.getTotalSteps();
    this.plugin = report.getPlugin();
    this.pluginName = report.getPluginName();
    this.pluginVersion = report.getPluginVersion();
    this.pluginState = report.getPluginState();
    this.pluginIsMandatory = report.getPluginIsMandatory();
    this.pluginDetails = report.getPluginDetails();
    this.htmlPluginDetails = report.isHtmlPluginDetails();
  }

  /**
   * Convert this StepReport to a Report (for backwards compatibility)
   */
  @JsonIgnore
  public Report toReport() {
    Report report = new Report();
    report.setId(this.id);
    report.setSourceObjectId(this.sourceObjectId);
    report.setSourceObjectClass(this.sourceObjectClass);
    report.setOutcomeObjectId(this.outcomeObjectId);
    report.setOutcomeObjectClass(this.outcomeObjectClass);
    report.setOutcomeObjectState(this.outcomeObjectState);
    report.setTitle(this.title);
    report.setDateCreated(this.dateCreated);
    report.setDateUpdated(this.dateUpdated);
    report.setCompletionPercentage(this.completionPercentage);
    report.setStepsCompleted(this.stepsCompleted);
    report.setTotalSteps(this.totalSteps);
    report.setPlugin(this.plugin);
    report.setPluginName(this.pluginName);
    report.setPluginVersion(this.pluginVersion);
    report.setPluginState(this.pluginState);
    report.setPluginIsMandatory(this.pluginIsMandatory);
    report.setPluginDetails(this.pluginDetails);
    report.setHtmlPluginDetails(this.htmlPluginDetails);
    return report;
  }

  // Getters and setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getParentReportId() {
    return parentReportId;
  }

  public void setParentReportId(String parentReportId) {
    this.parentReportId = parentReportId;
  }

  public String getSourceObjectId() {
    return sourceObjectId;
  }

  public void setSourceObjectId(String sourceObjectId) {
    this.sourceObjectId = sourceObjectId;
  }

  public String getSourceObjectClass() {
    return sourceObjectClass;
  }

  public void setSourceObjectClass(String sourceObjectClass) {
    this.sourceObjectClass = sourceObjectClass;
  }

  public String getOutcomeObjectId() {
    return outcomeObjectId;
  }

  public void setOutcomeObjectId(String outcomeObjectId) {
    this.outcomeObjectId = outcomeObjectId;
  }

  public String getOutcomeObjectClass() {
    return outcomeObjectClass;
  }

  public void setOutcomeObjectClass(String outcomeObjectClass) {
    this.outcomeObjectClass = outcomeObjectClass;
  }

  public AIPState getOutcomeObjectState() {
    return outcomeObjectState;
  }

  public void setOutcomeObjectState(AIPState outcomeObjectState) {
    this.outcomeObjectState = outcomeObjectState;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }

  public Date getDateUpdated() {
    return dateUpdated;
  }

  public void setDateUpdated(Date dateUpdated) {
    this.dateUpdated = dateUpdated;
  }

  public Integer getCompletionPercentage() {
    return completionPercentage;
  }

  public void setCompletionPercentage(Integer completionPercentage) {
    this.completionPercentage = completionPercentage;
  }

  public Integer getStepsCompleted() {
    return stepsCompleted;
  }

  public void setStepsCompleted(Integer stepsCompleted) {
    this.stepsCompleted = stepsCompleted;
  }

  public Integer getTotalSteps() {
    return totalSteps;
  }

  public void setTotalSteps(Integer totalSteps) {
    this.totalSteps = totalSteps;
  }

  public String getPlugin() {
    return plugin;
  }

  public void setPlugin(String plugin) {
    this.plugin = plugin;
  }

  public String getPluginName() {
    return pluginName;
  }

  public void setPluginName(String pluginName) {
    this.pluginName = pluginName;
  }

  public String getPluginVersion() {
    return pluginVersion;
  }

  public void setPluginVersion(String pluginVersion) {
    this.pluginVersion = pluginVersion;
  }

  public PluginState getPluginState() {
    return pluginState;
  }

  public void setPluginState(PluginState pluginState) {
    this.pluginState = pluginState;
  }

  public Boolean getPluginIsMandatory() {
    return pluginIsMandatory;
  }

  public void setPluginIsMandatory(Boolean pluginIsMandatory) {
    this.pluginIsMandatory = pluginIsMandatory;
  }

  public String getPluginDetails() {
    return pluginDetails;
  }

  public void setPluginDetails(String pluginDetails) {
    this.pluginDetails = pluginDetails;
  }

  public boolean isHtmlPluginDetails() {
    return htmlPluginDetails;
  }

  public void setHtmlPluginDetails(boolean htmlPluginDetails) {
    this.htmlPluginDetails = htmlPluginDetails;
  }

  public Integer getStepOrder() {
    return stepOrder;
  }

  public void setStepOrder(Integer stepOrder) {
    this.stepOrder = stepOrder;
  }

  @Override
  public String toString() {
    return "StepReport [id=" + id + ", parentReportId=" + parentReportId + ", sourceObjectId=" + sourceObjectId
      + ", plugin=" + plugin + ", pluginState=" + pluginState + ", stepOrder=" + stepOrder + "]";
  }
}
