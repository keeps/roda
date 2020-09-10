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
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
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
  // aipId > list<transferredResourceId>
  private Map<String, List<String>> aipIdToTransferredResourceIds = new HashMap<>();

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

  @Override
  public <T extends IsRODAObject> JobPluginInfo processJobPluginInformation(Plugin<T> plugin, JobInfo jobInfo) {
    int taskObjectsCount = jobInfo.getObjectsCount();
    Map<Integer, JobPluginInfo> jobInfos = jobInfo.getJobInfo();
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
    int sourceObjectsProcessedWithPartialSuccess = 0;
    int sourceObjectsProcessedWithFailure = 0;
    int sourceObjectsProcessedWithSkipped = 0;
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
        sourceObjectsProcessedWithPartialSuccess += pluginInfo.getSourceObjectsProcessedWithPartialSuccess();
        sourceObjectsProcessedWithSkipped += pluginInfo.getSourceObjectsProcessedWithSkipped();
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
    ingestInfoUpdated.setSourceObjectsProcessedWithPartialSuccess(sourceObjectsProcessedWithPartialSuccess);
    ingestInfoUpdated.setSourceObjectsProcessedWithFailure(sourceObjectsProcessedWithFailure);
    ingestInfoUpdated.setSourceObjectsProcessedWithSkipped(sourceObjectsProcessedWithSkipped);
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

  /** Ordered list with no duplicates */
  public List<String> getAipIds() {
    ArrayList<String> ret;

    if (transferredResourceToAipIds != null) {
      ret = new ArrayList<>(
        transferredResourceToAipIds.values().stream().flatMap(l -> l.stream()).distinct().collect(Collectors.toList()));
      ret.remove(Report.NO_OUTCOME_OBJECT_ID);
    } else {
      ret = new ArrayList<>();
    }

    return ret;
  }

  public List<String> getAipIds(String transferredResource) {
    ArrayList<String> ret;
    List<String> aipIds = transferredResourceToAipIds.get(transferredResource);

    if (aipIds != null) {
      ret = new ArrayList<>(aipIds);
      ret.removeIf(s -> s.equals(Report.NO_OUTCOME_OBJECT_ID));
    } else {
      ret = new ArrayList<>();
    }

    return ret;
  }

  public Map<String, List<String>> getAipIdToTransferredResourceIds() {
    HashMap<String, List<String>> ret = new HashMap<>(aipIdToTransferredResourceIds);
    ret.remove(Report.NO_OUTCOME_OBJECT_ID);
    return ret;
  }

  public int getBeingProcessedCounter() {
    return getReportsFromBeingProcessed().keySet().stream().filter(id -> !id.equals(Report.NO_OUTCOME_OBJECT_ID))
      .collect(Collectors.toList()).size();
  }

  /*
   * public PluginState getPluginState(String sourceObjectId, String
   * outcomeObjectId) { if (allReports.containsKey(sourceObjectId)) { if
   * (allReports.get(sourceObjectId).containsKey(outcomeObjectId)) { return
   * allReports.get(sourceObjectId).get(outcomeObjectId).getPluginState(); } } }
   */

  public void addReport(Report report, boolean reportIsAnReportItem) {
    if (reportIsAnReportItem) {
      reportsFromBeingProcessed.get(report.getSourceObjectId()).get(report.getOutcomeObjectId()).addReport(report,
        false);
    } else {
      String sourceObjectId = report.getSourceObjectId();
      String outcomeObjectId = report.getOutcomeObjectId();
      if (StringUtils.isBlank(sourceObjectId) || StringUtils.isBlank(outcomeObjectId)) {
        LOGGER.error("Will not add report as both source & outcome object ids are blank!");
        return;
      }

      if (Report.NO_SOURCE_OBJECT_ID.equals(sourceObjectId)) {
        LOGGER.error("Will not add report as source object id is not set!");
        return;
      }

      if (aipIdToTransferredResourceIds.containsKey(outcomeObjectId)) {
        if (!aipIdToTransferredResourceIds.get(outcomeObjectId).contains(sourceObjectId)) {
          aipIdToTransferredResourceIds.get(outcomeObjectId).add(sourceObjectId);
        }
      } else {
        aipIdToTransferredResourceIds.computeIfAbsent(outcomeObjectId, key -> new ArrayList<>()).add(sourceObjectId);
      }

      if (transferredResourceToAipIds.containsKey(sourceObjectId)) {
        if (!transferredResourceToAipIds.get(sourceObjectId).contains(outcomeObjectId)) {
          transferredResourceToAipIds.get(sourceObjectId).add(outcomeObjectId);
        }
      } else {
        transferredResourceToAipIds.computeIfAbsent(sourceObjectId, key -> new ArrayList<>()).add(outcomeObjectId);
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

  public void updateSourceObjectsProcessed() {
    int countSuccess = 0;
    int countPartialSuccess = 0;
    int countFailure = 0;
    int countSkipped = 0;

    for (Map<String, Report> map : getAllReports().values()) {
      for (Report report : map.values()) {
        if (PluginState.FAILURE.equals(report.getPluginState())) {
          countFailure++;
        } else if (PluginState.SUCCESS.equals(report.getPluginState())) {
          countSuccess++;
        } else if (PluginState.PARTIAL_SUCCESS.equals(report.getPluginState())) {
          countPartialSuccess++;
        } else if (PluginState.SKIPPED.equals(report.getPluginState())) {
          countSkipped++;
        }
      }
    }
    setSourceObjectsProcessedWithFailure(countFailure);
    setSourceObjectsProcessedWithPartialSuccess(countPartialSuccess);
    setSourceObjectsProcessedWithSuccess(countSuccess);
    setSourceObjectsProcessedWithSkipped(countSkipped);
  }

  @Override
  public void finalizeInfo() {
    super.finalizeInfo();

    /*
     * int countSuccess = 0; int countPartialSuccess = 0; int countFailure = 0;
     * 
     * if (allReports != null) { for (Map<String, Report> map : allReports.values())
     * { for (Report report : map.values()) { if
     * (PluginState.FAILURE.equals(report.getPluginState())) { countFailure++; }
     * else if (PluginState.SUCCESS.equals(report.getPluginState())) {
     * countSuccess++; } else if
     * (PluginState.PARTIAL_SUCCESS.equals(report.getPluginState())) {
     * countPartialSuccess++; } } }
     * 
     * setSourceObjectsProcessedWithFailure(countFailure);
     * setSourceObjectsProcessedWithPartialSuccess(countPartialSuccess);
     * setSourceObjectsProcessedWithSuccess(countSuccess); }
     */

    // INFO 20160601 hsilva: the following line is needed because we only mark,
    // during ingest processing, the failure and therefore in the end we have to
    // set the success counter
    //setSourceObjectsProcessedWithSuccess(getSourceObjectsCount() - getSourceObjectsProcessedWithFailure());
    setStepsCompleted(getTotalSteps());

    // 20161220 hsilva: preparing maps for garbage collection
    allReports = null;
    reportsFromBeingProcessed = null;
  }

  public void failOtherTransferredResourceAIPs(ModelService model, IndexService index, String transferredResourceId) {
    for (Entry<String, Report> aipReportEntry : allReports.get(transferredResourceId).entrySet()) {
      Report report = aipReportEntry.getValue();
      if (report.getPluginState() != PluginState.FAILURE) {
        List<Report> reportItems = report.getReports();
        Report reportItem = reportItems.get(reportItems.size() - 1);
        reportItem.setPluginState(PluginState.FAILURE)
          .setPluginDetails("This AIP processing failed because a related AIP also failed");
        reportItems.remove(reportItems.size() - 1);
        reportItems.add(reportItem);
        report.setPluginState(PluginState.FAILURE);

        try {
          Job job = model.retrieveJob(report.getJobId());
          model.createOrUpdateJobReport(report, job);
        } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
          LOGGER.error("Error updating last job report indicating other AIP failure.");
        }
      }
    }
  }

  public void updateMetaPluginInformation(Report metaReport) {
    this.getAllReports().forEach((k, v) -> {
      v.forEach((id, report) -> {
        report.setTitle(metaReport.getTitle());
        report.setPlugin(metaReport.getPlugin());
        report.setPluginName(metaReport.getPluginName());
        report.setPluginVersion(metaReport.getPluginVersion());
      });
    });
  }

  public void replaceTransferredResourceId(String oldTransferredResourceId, String newTransferredResourceId) {
    Map<String, Report> aipReports = allReports.remove(oldTransferredResourceId);
    allReports.put(newTransferredResourceId, aipReports);

    aipReports = reportsFromBeingProcessed.remove(oldTransferredResourceId);
    reportsFromBeingProcessed.put(newTransferredResourceId, aipReports);

    List<String> aipIds = transferredResourceToAipIds.remove(oldTransferredResourceId);
    transferredResourceToAipIds.put(newTransferredResourceId, aipIds);

    for (Entry<String, List<String>> aipToTranferredResourceIds : aipIdToTransferredResourceIds.entrySet()) {
      if (aipToTranferredResourceIds.getValue().contains(oldTransferredResourceId)) {
        aipToTranferredResourceIds.getValue().remove(oldTransferredResourceId);
        aipToTranferredResourceIds.getValue().add(newTransferredResourceId);
      }
    }
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

}
