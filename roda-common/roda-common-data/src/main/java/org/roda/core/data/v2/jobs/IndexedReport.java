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
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = RodaConstants.RODA_OBJECT_REPORT)
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndexedReport extends Report implements IsIndexed {
  private static final long serialVersionUID = 3735723897367711258L;

  private String jobName = null;
  private String sourceObjectLabel = null;
  private String outcomeObjectLabel = null;
  private PluginType jobPluginType = null;

  private List<String> successfulPlugins = new ArrayList<>();
  private List<String> unsuccessfulPlugins = new ArrayList<>();
  private int unsuccessfulPluginsCounter = 0;
  
  private Map<String, Object> fields;

  public IndexedReport() {
    super();
  }

  public IndexedReport(IndexedReport report) {
    super(report);
    this.jobName = report.getJobName();
    this.sourceObjectLabel = report.getSourceObjectLabel();
    this.outcomeObjectLabel = report.getOutcomeObjectLabel();
    this.jobPluginType = report.getJobPluginType();
    this.successfulPlugins = report.getSuccessfulPlugins();
    this.unsuccessfulPlugins = report.getUnsuccessfulPlugins();
    this.unsuccessfulPluginsCounter = report.getUnsuccessfulPluginsCounter();
  }

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public String getSourceObjectLabel() {
    return sourceObjectLabel;
  }

  public void setSourceObjectLabel(String sourceObjectLabel) {
    this.sourceObjectLabel = sourceObjectLabel;
  }

  public String getOutcomeObjectLabel() {
    return outcomeObjectLabel;
  }

  public void setOutcomeObjectLabel(String outcomeObjectLabel) {
    this.outcomeObjectLabel = outcomeObjectLabel;
  }

  public PluginType getJobPluginType() {
    return jobPluginType;
  }

  public void setJobPluginType(PluginType jobPluginType) {
    this.jobPluginType = jobPluginType;
  }

  public List<String> getSuccessfulPlugins() {
    return successfulPlugins;
  }

  public void setSuccessfulPlugins(List<String> successfulPlugins) {
    this.successfulPlugins = successfulPlugins;
  }

  public List<String> getUnsuccessfulPlugins() {
    return unsuccessfulPlugins;
  }

  public void setUnsuccessfulPlugins(List<String> unsuccessfulPlugins) {
    this.unsuccessfulPlugins = unsuccessfulPlugins;
    if (unsuccessfulPlugins != null) {
      this.unsuccessfulPluginsCounter = unsuccessfulPlugins.size();
    }
  }

  public int getUnsuccessfulPluginsCounter() {
    return unsuccessfulPluginsCounter;
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "jobId", "jobName", "sourceObjectId", "sourceObjectOriginalName", "sourceObjectLabel",
      "sourceObjectClass", "sourceObjectOriginalIds", "outcomeObjectId", "outcomeObjectLabel", "outcomeObjectClass",
      "outcomeObjectState", "title", "dateCreated", "dateUpdated", "completionPercentage", "stepsCompleted",
      "totalSteps", "plugin", "pluginName", "pluginVersion", "pluginState", "pluginDetails", "htmlPluginDetails",
      "successfulPlugins", "unsuccessfulPlugins", "reports");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(super.getId(), super.getJobId(), getJobName(), super.getSourceObjectId(),
      super.getSourceObjectOriginalName(), getSourceObjectLabel(), super.getSourceObjectClass(),
      super.getSourceObjectOriginalIds(), super.getOutcomeObjectId(), getOutcomeObjectLabel(),
      super.getOutcomeObjectClass(), super.getOutcomeObjectState(), super.getTitle(), super.getDateCreated(),
      super.getDateUpdated(), super.getCompletionPercentage(), super.getStepsCompleted(), super.getTotalSteps(),
      super.getPlugin(), super.getPluginName(), super.getPluginVersion(), super.getPluginState(),
      super.getPluginDetails(), super.isHtmlPluginDetails(), getSuccessfulPlugins(), getUnsuccessfulPlugins(),
      super.getReports());
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.JOB_REPORT_JOB_ID, RodaConstants.INDEX_UUID);
  }

  /**
   * @return the fields
   */
  public Map<String, Object> getFields() {
    return fields;
  }

  /**
   * @param fields the fields to set
   */
  public void setFields(Map<String, Object> fields) {
    this.fields = fields;
  }
}
