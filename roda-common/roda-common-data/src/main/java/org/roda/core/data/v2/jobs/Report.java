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
import java.util.List;

import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.HasId;
import org.roda.core.data.v2.ip.HasInstanceID;
import org.roda.core.data.v2.ip.SIPInformation;
import org.roda.core.data.v2.jpa.StringListConverter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

@Entity
@Table(name = "job_reports", indexes = {@Index(name = "idx_report_job_id", columnList = "jobId")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Report implements IsModelObject, HasId, HasInstanceID {
  @Serial
  private static final long serialVersionUID = 4316398565678538090L;

  public static final String NO_SOURCE_OBJECT_ID = "NO_SOURCE_ID";
  public static final String NO_SOURCE_OBJECT_CLASS = "NO_SOURCE_CLASS";
  public static final String NO_OUTCOME_OBJECT_ID = "NO_OUTCOME_ID";
  public static final String NO_OUTCOME_OBJECT_CLASS = "NO_OUTCOME_CLASS";

  @Id
  @Column(name = "id")
  private String id = "";
  @Column(name = "job_id")
  private String jobId = "";
  @Column(name = "source_object_id")
  private String sourceObjectId = NO_SOURCE_OBJECT_ID;
  @Column(name = "source_object_class")
  private String sourceObjectClass = NO_SOURCE_OBJECT_CLASS;
  @Column(name = "source_object_original_ids", columnDefinition = "TEXT")
  @Convert(converter = StringListConverter.class)
  private List<String> sourceObjectOriginalIds = new ArrayList<>();
  @Column(name = "source_object_original_name")
  private String sourceObjectOriginalName = "";
  @Column(name = "outcome_object_id")
  private String outcomeObjectId = NO_OUTCOME_OBJECT_ID;
  @Column(name = "outcome_object_class")
  private String outcomeObjectClass = NO_OUTCOME_OBJECT_CLASS;
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
  @Column(name = "ingest_type")
  private String ingestType = "";
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

  @Column(name = "instance_id")
  private String instanceId = null;
  @Column(name = "transaction_id")
  private String transactionId = null;

  @Transient
  @JsonIgnore
  private SIPInformation sipInformation = new SIPInformation();

  @OneToMany(mappedBy = "parentReportId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @OrderBy("stepOrder ASC")
  private List<StepReport> stepReports = new ArrayList<>();

  // Transient field for backwards compatibility with existing code that uses List<Report>
  @Transient
  private List<Report> reports = null;

  @Column(name = "line_separator")
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
    this.ingestType = report.getIngestType();
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
    this.reports = null;
    this.stepReports = new ArrayList<>();
    this.instanceId = report.getInstanceId();
    this.transactionId = report.getTransactionId();
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

  public String getIngestType() {
    return ingestType;
  }

  public void setIngestType(String status) {
    this.ingestType = status;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
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

    setPluginState(
      ReportUtils.calculatePluginState(getPluginState(), report.getPluginState(), report.getPluginIsMandatory()));

    if (!"".equals(report.getPluginDetails()) && !getPluginDetails().equals(report.getPluginDetails())) {
      // Fix: avoid adding repeated line separators
      String separator = (lineSeparator != null && !lineSeparator.isEmpty()) ? lineSeparator : "\n";
      setPluginDetails(
        (!"".equals(getPluginDetails()) ? getPluginDetails() + separator : "") + report.getPluginDetails());
    }
    setOutcomeObjectState(report.getOutcomeObjectState());

    // Add to both stepReports (JPA entity) and reports (backwards compatibility)
    if (reports == null) {
      reports = new ArrayList<>();
    }
    reports.add(report);
    
    // Also add as StepReport
    if (stepReports == null) {
      stepReports = new ArrayList<>();
    }
    stepReports.add(new StepReport(report, this.id, stepReports.size()));
    
    return this;
  }

  /*
   * @JsonIgnore private PluginState calculatePluginState(PluginState
   * currentPluginState, PluginState newPluginState, Boolean pluginIsMandatory) {
   * if (pluginIsMandatory) { if
   * (currentPluginState.equals(PluginState.PARTIAL_SUCCESS)) { switch
   * (newPluginState) { case SKIPPED: case RUNNING: case SUCCESS: return
   * PluginState.PARTIAL_SUCCESS; case FAILURE: return PluginState.FAILURE; } }
   * else if (newPluginState.equals(PluginState.SKIPPED)){ return
   * currentPluginState; } else { return newPluginState; } } else { if
   * (!currentPluginState.equals(PluginState.FAILURE)) { if
   * (newPluginState.equals(PluginState.FAILURE)) { return
   * PluginState.PARTIAL_SUCCESS; } } }
   *
   * return newPluginState; }
   */

  /**
   * Get the step reports as a list of Report objects for backwards compatibility.
   * If reports is null, builds it from stepReports.
   */
  public List<Report> getReports() {
    if (reports == null && stepReports != null) {
      reports = new ArrayList<>();
      for (StepReport stepReport : stepReports) {
        reports.add(stepReport.toReport());
      }
    }
    return reports != null ? reports : new ArrayList<>();
  }

  public Report setReports(List<Report> reports) {
    this.reports = reports;
    // Sync to stepReports
    if (reports != null) {
      this.stepReports = new ArrayList<>();
      int order = 0;
      for (Report report : reports) {
        this.stepReports.add(new StepReport(report, this.id, order++));
      }
    }
    return this;
  }

  /**
   * Get the underlying StepReport entities.
   */
  @JsonIgnore
  public List<StepReport> getStepReports() {
    return stepReports;
  }

  /**
   * Set the underlying StepReport entities.
   */
  @JsonIgnore
  public void setStepReports(List<StepReport> stepReports) {
    this.stepReports = stepReports;
    this.reports = null; // Reset cached reports
  }

  @JsonProperty("lineSeparator")
  public void injectLineSeparator(String lineSeparator) {
    this.lineSeparator = lineSeparator;
  }

  public String getLineSeparator() {
    return lineSeparator;
  }

  @JsonIgnore
  public Report getLastRunPlugin() {
    List<Report> reportsList = getReports();
    int size = reportsList.size();
    if (size == 0)
      return null;
    return reportsList.get(size - 1);
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
      + htmlPluginDetails + ", reports=" + reports + ", instanceId=" + instanceId + ", transactionId=" + transactionId
      + "]";
  }

}
