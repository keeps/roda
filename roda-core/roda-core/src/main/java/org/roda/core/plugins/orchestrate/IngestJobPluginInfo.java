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

  public IngestJobPluginInfo(int completionPercentage) {
    super(completionPercentage);
  }

  public IngestJobPluginInfo(int objectsCount, int totalSteps) {
    setObjectsCount(objectsCount);
    setObjectsBeingProcessed(objectsCount);
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
    jobPluginInfo.setObjectsBeingProcessed(pluginIsDone ? 0 : this.getObjectsBeingProcessed());
    jobPluginInfo.setObjectsWaitingToBeProcessed(pluginIsDone ? 0 : this.getObjectsWaitingToBeProcessed());
    jobPluginInfo.setObjectsProcessedWithSuccess(this.getObjectsProcessedWithSuccess());
    jobPluginInfo.setObjectsProcessedWithFailure(this.getObjectsProcessedWithFailure());
    jobPluginInfo.setOutcomeObjectsWithManualIntervention(this.getOutcomeObjectsWithManualIntervention());

    // calculate general counters
    float percentage = 0f;
    int beingProcessed = 0;
    int processedWithSuccess = 0;
    int processedWithFailure = 0;
    int outcomeObjectsWithManualIntervention = 0;
    for (JobPluginInfo jpi : jobInfos.values()) {
      IngestJobPluginInfo pluginInfo = (IngestJobPluginInfo) jpi;
      if (pluginInfo.getTotalSteps() > 0) {
        float pluginPercentage = ((float) pluginInfo.getStepsCompleted()) / pluginInfo.getTotalSteps();
        float pluginWeight = ((float) pluginInfo.getObjectsCount()) / taskObjectsCount;
        percentage += (pluginPercentage * pluginWeight);

        processedWithSuccess += pluginInfo.getObjectsProcessedWithSuccess();
        processedWithFailure += pluginInfo.getObjectsProcessedWithFailure();
        outcomeObjectsWithManualIntervention += pluginInfo.getOutcomeObjectsWithManualIntervention();
      }
      beingProcessed += pluginInfo.getObjectsBeingProcessed();
    }

    IngestJobPluginInfo ingestInfoUpdated = new IngestJobPluginInfo();
    ingestInfoUpdated.setCompletionPercentage(Math.round((percentage * 100)));
    ingestInfoUpdated.setObjectsBeingProcessed(beingProcessed);
    ingestInfoUpdated.setObjectsProcessedWithSuccess(processedWithSuccess);
    ingestInfoUpdated.setObjectsProcessedWithFailure(processedWithFailure);
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
    setObjectsBeingProcessed(beingProcessed);
    setObjectsProcessedWithFailure(getObjectsCount() - beingProcessed);
  }

  public void finalizeCounters() {
    setObjectsProcessedWithSuccess(getObjectsCount() - getObjectsProcessedWithFailure());
    setObjectsBeingProcessed(0);
    setObjectsWaitingToBeProcessed(0);
  }

}
