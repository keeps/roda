/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.plugins.Plugin;

public class IngestJobPluginInfo extends JobPluginInfo {
  private static final long serialVersionUID = -7993848868644990995L;

  private int stepsCompleted = 0;
  private int totalSteps = 0;

  // transferredResourceId > map<aipId, report>
  private Map<String, Map<String, Report>> reports = new HashMap<>();
  // transferredResourceId > list<aipId>
  private Map<String, List<String>> transferredResourceToAipIds = new HashMap<>();
  // aipId > transferredResourceId
  private Map<String, String> aipIdToTransferredResourceId = new HashMap<>();

  public IngestJobPluginInfo() {
    super();
  }

  public IngestJobPluginInfo(int sourceObjectsCount, int totalSteps) {
    super(sourceObjectsCount);
    this.totalSteps = totalSteps;
  }

  public int getStepsCompleted() {
    return stepsCompleted;
  }

  public void setStepsCompleted(int stepsCompleted) {
    this.stepsCompleted = stepsCompleted;
  }

  public int getTotalSteps() {
    return totalSteps;
  }

  public void setTotalSteps(int totalSteps) {
    this.totalSteps = totalSteps;
  }

  public IngestJobPluginInfo incrementStepsCompletedByOne() {
    this.stepsCompleted += 1;
    return this;
  }

  public <T extends Serializable> JobPluginInfo processJobPluginInformation(Plugin<T> plugin, Integer taskObjectsCount,
    Map<Plugin<?>, JobPluginInfo> jobInfos) {
    // update information in the map<plugin, pluginInfo>
    boolean pluginIsDone = (this.getStepsCompleted() == this.getTotalSteps());
    IngestJobPluginInfo jobPluginInfo = (IngestJobPluginInfo) jobInfos.get(plugin);
    jobPluginInfo.setTotalSteps(this.getTotalSteps());
    jobPluginInfo.setStepsCompleted(this.getStepsCompleted());
    jobPluginInfo.setCompletionPercentage(this.getCompletionPercentage());
    jobPluginInfo.setSourceObjectsBeingProcessed(pluginIsDone ? 0 : this.getSourceObjectsBeingProcessed());
    jobPluginInfo.setSourceObjectsWaitingToBeProcessed(pluginIsDone ? 0 : this.getSourceObjectsWaitingToBeProcessed());
    jobPluginInfo.setSourceObjectsProcessedWithSuccess(this.getSourceObjectsProcessedWithSuccess());
    jobPluginInfo.setSourceObjectsProcessedWithFailure(this.getSourceObjectsProcessedWithFailure());
    jobPluginInfo.setOutcomeObjectsWithManualIntervention(this.getOutcomeObjectsWithManualIntervention());

    // calculate general counters
    float percentage = 0f;
    int sourceObjectsBeingProcessed = 0;
    int sourceObjectsProcessedWithSuccess = 0;
    int sourceObjectsProcessedWithFailure = 0;
    int outcomeObjectsWithManualIntervention = 0;
    for (JobPluginInfoInterface jpi : jobInfos.values()) {
      IngestJobPluginInfo pluginInfo = (IngestJobPluginInfo) jpi;
      if (pluginInfo.getTotalSteps() > 0) {
        float pluginPercentage = ((float) pluginInfo.getStepsCompleted()) / pluginInfo.getTotalSteps();
        float pluginWeight = ((float) pluginInfo.getSourceObjectsCount()) / taskObjectsCount;
        percentage += (pluginPercentage * pluginWeight);

        sourceObjectsProcessedWithSuccess += pluginInfo.getSourceObjectsProcessedWithSuccess();
        sourceObjectsProcessedWithFailure += pluginInfo.getSourceObjectsProcessedWithFailure();
        outcomeObjectsWithManualIntervention += pluginInfo.getOutcomeObjectsWithManualIntervention();
      }
      sourceObjectsBeingProcessed += pluginInfo.getSourceObjectsBeingProcessed();
    }

    IngestJobPluginInfo ingestInfoUpdated = new IngestJobPluginInfo();
    ingestInfoUpdated.setCompletionPercentage(Math.round((percentage * 100)));
    ingestInfoUpdated.setSourceObjectsBeingProcessed(sourceObjectsBeingProcessed);
    ingestInfoUpdated.setSourceObjectsProcessedWithSuccess(sourceObjectsProcessedWithSuccess);
    ingestInfoUpdated.setSourceObjectsProcessedWithFailure(sourceObjectsProcessedWithFailure);
    ingestInfoUpdated.setOutcomeObjectsWithManualIntervention(outcomeObjectsWithManualIntervention);
    return ingestInfoUpdated;
  }

  public Map<String, Map<String, Report>> getReports() {
    return reports;
  }

  public Map<String, List<String>> getTransferredResourceToAipIds() {
    return transferredResourceToAipIds;
  }

  public List<String> getAipIds() {
    return transferredResourceToAipIds.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
  }

  public List<String> getAipIds(String transferredResource) {
    return transferredResourceToAipIds.get(transferredResource);
  }

  public Map<String, String> getAipIdToTransferredResourceId() {
    return aipIdToTransferredResourceId;
  }

  public int getBeingProcessedCounter() {
    return transferredResourceToAipIds.size();
  }

  public void addReport(String sourceObjectId, String outcomeObjectId, Report report) {
    if (StringUtils.isNotBlank(sourceObjectId) && StringUtils.isNotBlank(outcomeObjectId)) {
      aipIdToTransferredResourceId.put(outcomeObjectId, sourceObjectId);
      if (transferredResourceToAipIds.get(sourceObjectId) != null) {
        transferredResourceToAipIds.get(sourceObjectId).add(outcomeObjectId);
      } else {
        List<String> aipIds = new ArrayList<>();
        aipIds.add(outcomeObjectId);
        transferredResourceToAipIds.put(sourceObjectId, aipIds);
      }
    }
    if (reports.get(sourceObjectId) != null) {
      reports.get(sourceObjectId).put(outcomeObjectId, report);
    } else {
      Map<String, Report> innerReports = new HashMap<>();
      innerReports.put(outcomeObjectId, report);
      reports.put(sourceObjectId, innerReports);
    }
  }

  public void addReport(String outcomeObjectId, Report report) {
    reports.get(aipIdToTransferredResourceId.get(outcomeObjectId)).get(outcomeObjectId).addReport(report);
  }

  public void remove(String transferredResourceId) {
    reports.remove(transferredResourceId);
    transferredResourceToAipIds.remove(transferredResourceId);
  }

  public void updateCounters() {
    int beingProcessed = getBeingProcessedCounter();
    setSourceObjectsBeingProcessed(beingProcessed);
    setSourceObjectsProcessedWithFailure(getSourceObjectsCount() - beingProcessed);
  }

  public void finalizeCounters() {
    super.finalizeCounters();
    setStepsCompleted(getTotalSteps());
  }

}
