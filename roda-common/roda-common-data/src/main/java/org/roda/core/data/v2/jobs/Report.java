package org.roda.core.data.v2.jobs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Report implements Serializable {
  private static final long serialVersionUID = 4316398565678538090L;

  public enum PluginState {
    SUCCESS, PARTIAL_SUCCESS, FAILURE
  }

  private String id = null;
  private String jobId = null;
  private String itemId = null;
  private String otherId = null;

  private String title = null;
  private Date dateCreated = null;
  private Date dateUpdated = null;
  private Integer completionPercentage = 0;
  private Integer stepsCompleted = 0;
  private Integer totalSteps = 0;
  private String plugin = null;
  private PluginState pluginState = PluginState.FAILURE;
  private String pluginDetails = "";

  private List<Report> reports = new ArrayList<Report>();

  public Report() {
    super();
  }

  public Report(Report report) {
    super();
    this.id = report.getId();
    this.jobId = report.getJobId();
    this.itemId = report.getItemId();
    this.otherId = report.getOtherId();
    this.title = report.getTitle();
    this.dateCreated = report.getDateCreated();
    this.dateUpdated = report.getDateUpdated();
    this.completionPercentage = report.getCompletionPercentage();
    this.stepsCompleted = report.getStepsCompleted();
    this.totalSteps = report.getTotalSteps();
    this.pluginState = report.getPluginState();
    this.pluginDetails = report.getPluginDetails();
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

  public String getItemId() {
    return itemId;
  }

  public Report setItemId(String itemId) {
    this.itemId = itemId;
    return this;
  }

  public String getOtherId() {
    return otherId;
  }

  public Report setOtherId(String otherId) {
    this.otherId = otherId;
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

  public Report addReport(Report report) {
    // FIXME not quite sure that this is the best place for this logic but it's
    // very handy
    if (report.getDateUpdated() == null) {
      report.setDateUpdated(new Date());
    }
    setDateUpdated(report.getDateUpdated());
    if (totalSteps == 0 && report.getTotalSteps() != 0) {
      setTotalSteps(report.getTotalSteps());
    }
    stepsCompleted = stepsCompleted + 1;
    if (totalSteps != 0) {
      completionPercentage = (int) ((100f / totalSteps) * stepsCompleted);
    }
    setPlugin(report.getPlugin());
    setPluginState(report.getPluginState());

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

  @Override
  public String toString() {
    return "Report [id=" + id + ", jobId=" + jobId + ", itemId=" + itemId + ", otherId=" + otherId + ", title=" + title
      + ", dateCreated=" + dateCreated + ", dateUpdated=" + dateUpdated + ", completionPercentage="
      + completionPercentage + ", stepsCompleted=" + stepsCompleted + ", totalSteps=" + totalSteps + ", plugin="
      + plugin + ", pluginState=" + pluginState + ", pluginDetails=" + pluginDetails + ", reports=" + reports + "]";
  }

}
