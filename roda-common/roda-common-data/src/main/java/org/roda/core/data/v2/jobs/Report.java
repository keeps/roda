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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIPState;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Report implements Serializable, IsIndexed {
  private static final long serialVersionUID = 4316398565678538090L;

  public enum PluginState {
    SUCCESS, PARTIAL_SUCCESS, FAILURE, RUNNING
  }

  private String id = "";
  private String jobId = "";
  private String sourceObjectId = "";
  private String sourceObjectClass = "";
  private String sourceObjectOriginalId = "";
  private String sourceObjectOriginalName = "";
  private String outcomeObjectId = "";
  private String outcomeObjectClass = "";
  private AIPState outcomeObjectState = AIPState.getDefault();

  private String title = "";
  private Date dateCreated;
  private Date dateUpdated;
  private Integer completionPercentage = 0;
  private Integer stepsCompleted = 0;
  private Integer totalSteps = 0;
  private String plugin = "";
  private String pluginName = "";
  private String pluginVersion = "";
  private PluginState pluginState = PluginState.RUNNING;
  private String pluginDetails = "";
  private boolean htmlPluginDetails = false;

  private List<Report> reports = new ArrayList<Report>();

  private String lineSeparator = "";

  public Report() {
    super();
    dateCreated = new Date();
    dateUpdated = null;
  }

  /**
   * Copy constructor (only doesn't copy the reports)
   */
  public Report(Report report) {
    super();
    this.id = report.getId();
    this.jobId = report.getJobId();
    this.sourceObjectId = report.getSourceObjectId();
    this.sourceObjectClass = report.getSourceObjectClass();
    this.sourceObjectOriginalId = report.getSourceObjectOriginalId();
    this.sourceObjectOriginalName = report.getSourceObjectOriginalName();
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
    this.pluginDetails = report.getPluginDetails();
    this.htmlPluginDetails = report.isHtmlPluginDetails();
    this.reports = new ArrayList<Report>();
  }

  public String getId() {
    return id;
  }

  public Report setId(String id) {
    this.id = id;
    return this;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getSourceObjectId() {
    return sourceObjectId;
  }

  public Report setSourceObjectId(String sourceObjectId) {
    this.sourceObjectId = sourceObjectId;
    return this;
  }

  public String getSourceObjectClass() {
    return sourceObjectClass;
  }

  public Report setSourceObjectClass(String sourceObjectClass) {
    this.sourceObjectClass = sourceObjectClass;
    return this;
  }

  public String getSourceObjectOriginalId() {
    return sourceObjectOriginalId;
  }

  public Report setSourceObjectOriginalId(String sourceObjectOriginalId) {
    this.sourceObjectOriginalId = sourceObjectOriginalId;
    return this;
  }

  public String getSourceObjectOriginalName() {
    return sourceObjectOriginalName;
  }

  public Report setSourceObjectOriginalName(String sourceObjectOriginalName) {
    this.sourceObjectOriginalName = sourceObjectOriginalName;
    return this;
  }

  public String getOutcomeObjectId() {
    return outcomeObjectId;
  }

  public Report setOutcomeObjectId(String outcomeObjectId) {
    this.outcomeObjectId = outcomeObjectId;
    return this;
  }

  public String getOutcomeObjectClass() {
    return outcomeObjectClass;
  }

  public Report setOutcomeObjectClass(String outcomeObjectClass) {
    this.outcomeObjectClass = outcomeObjectClass;
    return this;
  }

  public AIPState getOutcomeObjectState() {
    return outcomeObjectState;
  }

  public Report setOutcomeObjectState(AIPState outcomeObjectState) {
    this.outcomeObjectState = outcomeObjectState;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public Report setTitle(String title) {
    this.title = title;
    return this;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public Report setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
    return this;
  }

  public Date getDateUpdated() {
    return dateUpdated;
  }

  public Report setDateUpdated(Date dateUpdated) {
    this.dateUpdated = dateUpdated;
    return this;
  }

  public Integer getCompletionPercentage() {
    return completionPercentage;
  }

  public Report setCompletionPercentage(Integer completionPercentage) {
    this.completionPercentage = completionPercentage;
    return this;
  }

  public Integer getStepsCompleted() {
    return stepsCompleted;
  }

  public Report setStepsCompleted(Integer stepsCompleted) {
    this.stepsCompleted = stepsCompleted;
    return this;
  }

  public Integer getTotalSteps() {
    return totalSteps;
  }

  public Report setTotalSteps(Integer totalSteps) {
    this.totalSteps = totalSteps;
    return this;
  }

  public String getPlugin() {
    return plugin;
  }

  public Report setPlugin(String plugin) {
    this.plugin = plugin;
    return this;
  }

  public String getPluginName() {
    return pluginName;
  }

  public Report setPluginName(String pluginName) {
    this.pluginName = pluginName;
    return this;
  }

  public String getPluginVersion() {
    return pluginVersion;
  }

  public Report setPluginVersion(String pluginVersion) {
    this.pluginVersion = pluginVersion;
    return this;
  }

  public PluginState getPluginState() {
    return pluginState;
  }

  public Report setPluginState(PluginState pluginState) {
    this.pluginState = pluginState;
    return this;
  }

  public String getPluginDetails() {
    return pluginDetails;
  }

  public Report setPluginDetails(String pluginDetails) {
    this.pluginDetails = pluginDetails;
    return this;
  }

  public Report addPluginDetails(String pluginDetails) {
    this.pluginDetails += pluginDetails;
    return this;
  }

  public boolean isHtmlPluginDetails() {
    return htmlPluginDetails;
  }

  public Report setHtmlPluginDetails(boolean htmlPluginDetails) {
    this.htmlPluginDetails = htmlPluginDetails;
    return this;
  }

  public Report addReport(Report report) {
    // FIXME not quite sure that this is the best place for this logic but it's
    // very handy
    report.setDateUpdated(new Date());
    setDateUpdated(report.getDateUpdated());
    if (totalSteps == 0 && report.getTotalSteps() != 0) {
      setTotalSteps(report.getTotalSteps());
    }
    stepsCompleted = stepsCompleted + 1;
    if (totalSteps != 0) {
      completionPercentage = Math.round((100f / totalSteps) * stepsCompleted);
    }
    setPlugin(report.getPlugin());
    setPluginName(report.getPluginName());
    setPluginVersion(report.getPluginVersion());
    setPluginState(report.getPluginState());
    if (!"".equals(report.getPluginDetails()) && !getPluginDetails().equals(report.getPluginDetails())) {
      setPluginDetails(
        (!"".equals(getPluginDetails()) ? getPluginDetails() + lineSeparator : "") + report.getPluginDetails());
    }
    setOutcomeObjectState(report.getOutcomeObjectState());

    reports.add(report);

    return this;
  }

  public List<Report> getReports() {
    return reports;
  }

  public Report setReports(List<Report> reports) {
    this.reports = reports;
    return this;
  }

  public void injectLineSeparator(String lineSeparator) {
    this.lineSeparator = lineSeparator;
  }

  @Override
  public String toString() {
    return "Report [id=" + id + ", jobId=" + jobId + ", sourceObjectId=" + sourceObjectId + ", sourceObjectClass="
      + sourceObjectClass + ", sourceObjectOriginalId=" + sourceObjectOriginalId + ", outcomeObjectId="
      + outcomeObjectId + ", outcomeObjectClass=" + outcomeObjectClass + ", outcomeObjectState=" + outcomeObjectState
      + ", title=" + title + ", dateCreated=" + dateCreated + ", dateUpdated=" + dateUpdated + ", completionPercentage="
      + completionPercentage + ", stepsCompleted=" + stepsCompleted + ", totalSteps=" + totalSteps + ", plugin="
      + plugin + ", pluginName=" + pluginName + ", pluginVersion=" + pluginVersion + ", pluginState=" + pluginState
      + ", pluginDetails=" + pluginDetails + ", htmlPluginDetails=" + htmlPluginDetails + ", reports=" + reports + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "jobId", "sourceObjectId", "sourceObjectClass", "sourceObjectOriginalId",
      "outcomeObjectId", "outcomeObjectClass", "outcomeObjectState", "title", "dateCreated", "dateUpdated",
      "completionPercentage", "stepsCompleted", "totalSteps", "plugin", "pluginName", "pluginVersion", "pluginState",
      "pluginDetails", "htmlPluginDetails", "reports");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(id, jobId, sourceObjectId, sourceObjectClass, sourceObjectOriginalId, outcomeObjectId,
      outcomeObjectClass, outcomeObjectState, title, dateCreated, dateUpdated, completionPercentage, stepsCompleted,
      totalSteps, plugin, pluginName, pluginVersion, pluginState, pluginDetails, htmlPluginDetails, reports);
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
  }
}
