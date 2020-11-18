/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.HasId;
import org.roda.core.data.v2.ip.SIPInformation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_REPORT)
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Report implements IsModelObject, HasId {
  private static final long serialVersionUID = 4316398565678538090L;

  public static final String NO_SOURCE_OBJECT_ID = "NO_SOURCE_ID";
  public static final String NO_SOURCE_OBJECT_CLASS = "NO_SOURCE_CLASS";
  public static final String NO_OUTCOME_OBJECT_ID = "NO_OUTCOME_ID";
  public static final String NO_OUTCOME_OBJECT_CLASS = "NO_OUTCOME_CLASS";

  private String id = "";
  private String jobId = "";
  private String sourceObjectId = NO_SOURCE_OBJECT_ID;
  private String sourceObjectClass = NO_SOURCE_OBJECT_CLASS;
  private List<String> sourceObjectOriginalIds = new ArrayList<>();
  private String sourceObjectOriginalName = "";
  private String outcomeObjectId = NO_OUTCOME_OBJECT_ID;
  private String outcomeObjectClass = NO_OUTCOME_OBJECT_CLASS;
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
  private Boolean pluginIsMandatory = true;
  private String pluginDetails = "";
  private boolean htmlPluginDetails = false;

  @JsonIgnore
  private SIPInformation sipInformation = new SIPInformation();

  private List<Report> reports = new ArrayList<>();

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
    this.sourceObjectOriginalIds = report.getSourceObjectOriginalIds();
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
    this.pluginIsMandatory = report.getPluginIsMandatory();
    this.pluginDetails = report.getPluginDetails();
    this.htmlPluginDetails = report.isHtmlPluginDetails();
    this.reports = new ArrayList<>();
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 1;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
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

  /**
   * 20180713 hsilva: to ensure that both source & outcome object ids are set,
   * this method should only be used for PluginHelper & POJO related operations
   * like marshalling, etc...
   */
  @Deprecated
  public Report setSourceObjectId(String sourceObjectId) {
    this.sourceObjectId = sourceObjectId;
    return this;
  }

  public String getSourceObjectClass() {
    return sourceObjectClass;
  }

  /**
   * 20180713 hsilva: to ensure that both source & outcome object ids class are
   * this method should only be used for PluginHelper & POJO related operations
   * like marshalling, etc...
   */
  @Deprecated
  public Report setSourceObjectClass(String sourceObjectClass) {
    this.sourceObjectClass = sourceObjectClass;
    return this;
  }

  public List<String> getSourceObjectOriginalIds() {
    return sourceObjectOriginalIds;
  }

  public Report setSourceObjectOriginalIds(List<String> sourceObjectOriginalIds) {
    this.sourceObjectOriginalIds = sourceObjectOriginalIds;
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

  /**
   * 20180713 hsilva: to ensure that both source & outcome object ids are set,
   * this method should only be used for PluginHelper & POJO related operations
   * like marshalling, etc...
   */
  @Deprecated
  public Report setOutcomeObjectId(String outcomeObjectId) {
    this.outcomeObjectId = outcomeObjectId;
    return this;
  }

  public Report setSourceAndOutcomeObjectId(String sourceObjectId, String outcomeObjectId) {
    this.sourceObjectId = sourceObjectId;
    this.outcomeObjectId = outcomeObjectId;
    return this;
  }

  public String getOutcomeObjectClass() {
    return outcomeObjectClass;
  }

  /**
   * 20180713 hsilva: to ensure that both source & outcome object ids class are
   * this method should only be used for PluginHelper & POJO related operations
   * like marshalling, etc...
   */
  @Deprecated
  public Report setOutcomeObjectClass(String outcomeObjectClass) {
    this.outcomeObjectClass = outcomeObjectClass;
    return this;
  }

  public Report setSourceAndOutcomeObjectClass(String sourceObjectClass, String outcomeObjectClass) {
    this.sourceObjectClass = sourceObjectClass;
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

  public Boolean getPluginIsMandatory() {
    return pluginIsMandatory;
  }

  public void setPluginIsMandatory(Boolean pluginIsMandatory) {
    this.pluginIsMandatory = pluginIsMandatory;
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

  @JsonIgnore
  public SIPInformation getSipInformation() {
    return sipInformation;
  }

  @JsonIgnore
  public void setSipInformation(SIPInformation sipInformation) {
    this.sipInformation = sipInformation;
  }

  public Report addReport(Report report) {
    return addReport(report, true);
  }

  public Report addReport(Report report, boolean updateReportItemDateUpdated) {

    // FIXME not quite sure that this is the best place for this logic but it's
    // very handy
    if (updateReportItemDateUpdated) {
      report.setDateUpdated(new Date());
    }
    setDateUpdated(report.getDateUpdated());
    if (totalSteps == 0 && report.getTotalSteps() != 0) {
      setTotalSteps(report.getTotalSteps());
    }
    stepsCompleted = stepsCompleted + 1;
    if (totalSteps != 0) {
      completionPercentage = Math.round((100f / totalSteps) * stepsCompleted);
    }

    if ("".equals(getPlugin())) {
      setPlugin(report.getPlugin());
    }

    if ("".equals(getPluginName())) {
      setPluginName(report.getPluginName());
    }

    if ("".equals(getPluginVersion())) {
      setPluginVersion(report.getPluginVersion());
    }

    setPluginState(ReportUtils.calculatePluginState(getPluginState(), report.getPluginState(), report.getPluginIsMandatory()));

    if (!"".equals(report.getPluginDetails()) && !getPluginDetails().equals(report.getPluginDetails())) {
      setPluginDetails(
        (!"".equals(getPluginDetails()) ? getPluginDetails() + lineSeparator : "") + report.getPluginDetails());
    }
    setOutcomeObjectState(report.getOutcomeObjectState());

    reports.add(report);
    return this;
  }

  /*@JsonIgnore
  private PluginState calculatePluginState(PluginState currentPluginState, PluginState newPluginState,
    Boolean pluginIsMandatory) {
    if (pluginIsMandatory) {
      if (currentPluginState.equals(PluginState.PARTIAL_SUCCESS)) {
        switch (newPluginState) {
          case SKIPPED:
          case RUNNING:
          case SUCCESS:
            return PluginState.PARTIAL_SUCCESS;
          case FAILURE:
            return PluginState.FAILURE;
        }
      } else if (newPluginState.equals(PluginState.SKIPPED)){
        return currentPluginState;
      } else {
        return newPluginState;
      }
    } else {
      if (!currentPluginState.equals(PluginState.FAILURE)) {
        if (newPluginState.equals(PluginState.FAILURE)) {
          return PluginState.PARTIAL_SUCCESS;
        }
      }
    }

    return newPluginState;
  }*/

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

  public String getLineSeparator() {
    return lineSeparator;
  }

  @JsonIgnore
  public Report getLastRunPlugin() {
    int size = reports.size();
    if (size == 0) return null;
    return reports.get(size-1);
  }

  @Override
  public String toString() {
    return "Report [id=" + id + ", jobId=" + jobId + ", sourceObjectId=" + sourceObjectId + ", sourceObjectClass="
      + sourceObjectClass + ", sourceObjectOriginalIds=" + sourceObjectOriginalIds + ", outcomeObjectId="
      + outcomeObjectId + ", outcomeObjectClass=" + outcomeObjectClass + ", outcomeObjectState=" + outcomeObjectState
      + ", title=" + title + ", dateCreated=" + dateCreated + ", dateUpdated=" + dateUpdated + ", completionPercentage="
      + completionPercentage + ", stepsCompleted=" + stepsCompleted + ", totalSteps=" + totalSteps + ", plugin="
      + plugin + ", pluginName=" + pluginName + ", pluginVersion=" + pluginVersion + ", pluginState=" + pluginState
      + ", pluginIsMandatory=" + pluginIsMandatory + ", pluginDetails=" + pluginDetails + ", htmlPluginDetails="
      + htmlPluginDetails + ", reports=" + reports + "]";
  }

}
