/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestJobPluginInfo extends JobPluginInfo {
  private static final long serialVersionUID = -7993848868644990995L;
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestJobPluginInfo.class);

  private int stepsCompleted = 0;
  private int totalSteps = 0;

  // transferredResourceId > map<aipId, report>
  private Map<String, Map<String, Report>> allReports = new HashMap<>();
  // transferredResourceId > map<aipId, report>
  private Map<String, Map<String, Report>> reportsFromBeingProcessed = new HashMap<>();
  // transferredResourceId > list<aipId>
  private Map<String, List<String>> transferredResourceToAipIds = new HashMap<>();
  // aipId > transferredResourceId
  private Map<String, String> aipIdToTransferredResourceId = new HashMap<>();

  public IngestJobPluginInfo() {
    super();
  }

  public int getStepsCompleted() {
    return stepsCompleted;
  }

  public IngestJobPluginInfo setStepsCompleted(int stepsCompleted) {
    this.stepsCompleted = stepsCompleted;
    return this;
  }

  public int getTotalSteps() {
    return totalSteps;
  }

  public IngestJobPluginInfo setTotalSteps(int totalSteps) {
    this.totalSteps = totalSteps;
    return this;
  }

  public IngestJobPluginInfo incrementStepsCompletedByOne() {
    this.stepsCompleted += 1;
    return this;
  }

  public void update(IngestJobPluginInfo ingestJobPluginInfo) {
    this.setTotalSteps(ingestJobPluginInfo.getTotalSteps());
    this.setStepsCompleted(ingestJobPluginInfo.getStepsCompleted());
    this.setCompletionPercentage(ingestJobPluginInfo.getCompletionPercentage());
    this.setSourceObjectsCount(ingestJobPluginInfo.getSourceObjectsCount());
    this.setSourceObjectsBeingProcessed(ingestJobPluginInfo.getSourceObjectsBeingProcessed());
    this.setSourceObjectsWaitingToBeProcessed(ingestJobPluginInfo.getSourceObjectsWaitingToBeProcessed());
    this.setSourceObjectsProcessedWithSuccess(ingestJobPluginInfo.getSourceObjectsProcessedWithSuccess());
    this.setSourceObjectsProcessedWithFailure(ingestJobPluginInfo.getSourceObjectsProcessedWithFailure());
    this.setOutcomeObjectsWithManualIntervention(ingestJobPluginInfo.getOutcomeObjectsWithManualIntervention());
  }

  public <T extends IsRODAObject> JobPluginInfo processJobPluginInformation(Plugin<T> plugin, JobInfo jobInfo) {
    int taskObjectsCount = jobInfo.getObjectsCount();
    Map<Plugin<?>, JobPluginInfo> jobInfos = jobInfo.getJobInfo();
    // update information in the map<plugin, pluginInfo>
    // FIXME/INFO 20160601 hsilva: the following code would be necessary in a
    // distributed architecture
    // IngestJobPluginInfo jobPluginInfo = (IngestJobPluginInfo)
    // jobInfos.get(plugin);
    // jobPluginInfo.update(this);

    // calculate general counters
    float percentage = 0f;
    int sourceObjectsCount = 0;
    int sourceObjectsBeingProcessed = 0;
    int sourceObjectsProcessedWithSuccess = 0;
    int sourceObjectsProcessedWithFailure = 0;
    int outcomeObjectsWithManualIntervention = 0;
    for (JobPluginInfo jpi : jobInfos.values()) {
      IngestJobPluginInfo pluginInfo = (IngestJobPluginInfo) jpi;
      if (pluginInfo.getTotalSteps() > 0) {
        float pluginPercentage = pluginInfo.getCompletionPercentage() == 100 ? 1.0f : 0.0f;
        if (pluginInfo.getCompletionPercentage() != 100) {
          pluginPercentage = ((float) pluginInfo.getStepsCompleted()) / pluginInfo.getTotalSteps();
        }
        float pluginWeight = ((float) pluginInfo.getSourceObjectsCount()) / taskObjectsCount;
        percentage += (pluginPercentage * pluginWeight);

        sourceObjectsProcessedWithSuccess += pluginInfo.getSourceObjectsProcessedWithSuccess();
        sourceObjectsProcessedWithFailure += pluginInfo.getSourceObjectsProcessedWithFailure();
        outcomeObjectsWithManualIntervention += pluginInfo.getOutcomeObjectsWithManualIntervention();
      }
      sourceObjectsBeingProcessed += pluginInfo.getSourceObjectsBeingProcessed();
      sourceObjectsCount += pluginInfo.getSourceObjectsCount();
    }

    IngestJobPluginInfo ingestInfoUpdated = new IngestJobPluginInfo();
    ingestInfoUpdated.setCompletionPercentage(Math.round((percentage * 100)));
    ingestInfoUpdated.setSourceObjectsCount(sourceObjectsCount);
    ingestInfoUpdated.setSourceObjectsBeingProcessed(sourceObjectsBeingProcessed);
    ingestInfoUpdated.setSourceObjectsProcessedWithSuccess(sourceObjectsProcessedWithSuccess);
    ingestInfoUpdated.setSourceObjectsProcessedWithFailure(sourceObjectsProcessedWithFailure);
    ingestInfoUpdated.setOutcomeObjectsWithManualIntervention(outcomeObjectsWithManualIntervention);
    return ingestInfoUpdated;
  }

  public Map<String, Map<String, Report>> getAllReports() {
    return allReports;
  }

  public Map<String, Map<String, Report>> getReportsFromBeingProcessed() {
    return reportsFromBeingProcessed;
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
    if (reportsFromBeingProcessed.get(sourceObjectId) != null) {
      reportsFromBeingProcessed.get(sourceObjectId).put(outcomeObjectId, report);
      allReports.get(sourceObjectId).put(outcomeObjectId, report);
    } else {
      Map<String, Report> innerReports = new HashMap<>();
      innerReports.put(outcomeObjectId, report);
      reportsFromBeingProcessed.put(sourceObjectId, innerReports);
      allReports.put(sourceObjectId, innerReports);
    }
  }

  public void addReport(String outcomeObjectId, Report report) {
    reportsFromBeingProcessed.get(aipIdToTransferredResourceId.get(outcomeObjectId)).get(outcomeObjectId)
      .addReport(report);
  }

  public void remove(String transferredResourceId) {
    reportsFromBeingProcessed.remove(transferredResourceId);
    transferredResourceToAipIds.remove(transferredResourceId);
  }

  public void updateCounters() {
    int beingProcessed = getBeingProcessedCounter();
    setSourceObjectsBeingProcessed(beingProcessed);
    setSourceObjectsProcessedWithFailure(getSourceObjectsCount() - beingProcessed);
  }

  public void finalizeInfo() {
    super.finalizeInfo();
    // INFO 20160601 hsilva: the following line is needed because we only mark,
    // during ingest processing, the failure and therefore in the end we have to
    // set the success counter
    setSourceObjectsProcessedWithSuccess(getSourceObjectsCount() - getSourceObjectsProcessedWithFailure());
    setStepsCompleted(getTotalSteps());
  }

  public <T extends IsRODAObject> void failOtherTransferredResourceAIPs(Plugin<T> plugin, ModelService model,
    IndexService index, String transferredResourceId) {
    for (Entry<String, Report> aipReportEntry : allReports.get(transferredResourceId).entrySet()) {
      Report report = aipReportEntry.getValue();
      if (report.getPluginState() != PluginState.FAILURE) {
        List<Report> reportItems = report.getReports();
        Report reportItem = reportItems.get(reportItems.size() - 1);
        reportItem.setPluginState(PluginState.FAILURE)
          .setPluginDetails("This AIP processing failed because a related AIP also failed");
        reportItems.remove(reportItems.size() - 1);
        reportItems.add(reportItem);
        // report.setReports(reportItems);
        report.setPluginState(PluginState.FAILURE);

        try {
          model.createOrUpdateJobReport(report);
        } catch (GenericException e) {
          LOGGER.error("Error updating last job report indicating other AIP failure.");
        }

        // Report reportItem = PluginHelper
        // .initPluginReportItem(plugin, aipReportEntry.getKey(),
        // AIPState.INGEST_PROCESSING)
        // .setPluginState(PluginState.FAILURE)
        // .setPluginDetails("This AIP processing failed because a related AIP
        // also failed");
        // PluginHelper.updatePartialJobReport(plugin, model, index, reportItem,
        // false);
      }
    }

  }

}
