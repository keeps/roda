package org.roda.core.plugins.orchestrate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.plugins.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class MultipleJobPluginInfo extends JobPluginInfo {

  private static final long serialVersionUID = 7384501181657834153L;
  private static final Logger LOGGER = LoggerFactory.getLogger(MultipleJobPluginInfo.class);
  private Map<String, List<Report>> allReports = new HashMap<>();

  public MultipleJobPluginInfo() {
    super();
  }

  public void update(final MultipleJobPluginInfo mutipleJobPluginInfo) {
    this.setTotalSteps(mutipleJobPluginInfo.getTotalSteps());
    this.setStepsCompleted(mutipleJobPluginInfo.getStepsCompleted());
    this.setCompletionPercentage(mutipleJobPluginInfo.getCompletionPercentage());
    this.setSourceObjectsCount(mutipleJobPluginInfo.getSourceObjectsCount());
    this.setSourceObjectsBeingProcessed(mutipleJobPluginInfo.getSourceObjectsBeingProcessed());
    this.setSourceObjectsWaitingToBeProcessed(mutipleJobPluginInfo.getSourceObjectsWaitingToBeProcessed());
    this.setSourceObjectsProcessedWithSuccess(mutipleJobPluginInfo.getSourceObjectsProcessedWithSuccess());
    this.setSourceObjectsProcessedWithFailure(mutipleJobPluginInfo.getSourceObjectsProcessedWithFailure());
    this.setOutcomeObjectsWithManualIntervention(mutipleJobPluginInfo.getOutcomeObjectsWithManualIntervention());
  }

  @Override
  public <T extends IsRODAObject> JobPluginInfo processJobPluginInformation(final Plugin<T> plugin,
    final JobInfo jobInfo) {
    final int taskObjectsCount = jobInfo.getObjectsCount();
    final Map<Integer, JobPluginInfo> jobInfos = jobInfo.getJobInfo();

    float percentage = 0f;
    int sourceObjectsCount = 0;
    int sourceObjectsBeingProcessed = 0;
    int sourceObjectsProcessedWithSuccess = 0;
    int sourceObjectsProcessedWithPartialSuccess = 0;
    int sourceObjectsProcessedWithFailure = 0;
    int sourceObjectsProcessedWithSkipped = 0;
    int outcomeObjectsWithManualIntervention = 0;
    for (JobPluginInfo jpi : jobInfos.values()) {
      final MultipleJobPluginInfo pluginInfo = (MultipleJobPluginInfo) jpi;
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

    final MultipleJobPluginInfo mutipleJobPluginInfo = new MultipleJobPluginInfo();
    mutipleJobPluginInfo.setCompletionPercentage(Math.round((percentage * 100)));
    mutipleJobPluginInfo.setSourceObjectsCount(sourceObjectsCount);
    mutipleJobPluginInfo.setSourceObjectsBeingProcessed(sourceObjectsBeingProcessed);
    mutipleJobPluginInfo.setSourceObjectsProcessedWithSuccess(sourceObjectsProcessedWithSuccess);
    mutipleJobPluginInfo.setSourceObjectsProcessedWithPartialSuccess(sourceObjectsProcessedWithPartialSuccess);
    mutipleJobPluginInfo.setSourceObjectsProcessedWithFailure(sourceObjectsProcessedWithFailure);
    mutipleJobPluginInfo.setSourceObjectsProcessedWithSkipped(sourceObjectsProcessedWithSkipped);
    mutipleJobPluginInfo.setOutcomeObjectsWithManualIntervention(outcomeObjectsWithManualIntervention);
    return mutipleJobPluginInfo;
  }

  public Map<String, List<Report>> getAllReports() {
    return this.allReports;
  }

  public void addReport(final Report report) {
    if (allReports.get(report.getSourceObjectId()) != null) {
      allReports.get(report.getSourceObjectId()).add(report);
    } else {
      List<Report> reports = new ArrayList<>();
      reports.add(report);
      allReports.put(report.getSourceObjectId(), reports);
    }
  }

  public void updateSourceObjectsProcessed() {
    int countSuccess = 0;
    int countPartialSuccess = 0;
    int countFailure = 0;
    for (List<Report> reports : getAllReports().values()) {
      PluginState pluginState = PluginState.SUCCESS;
      for (Report report : reports) {
        switch (report.getPluginState()) {
          case FAILURE:
            pluginState = report.getPluginState();
            break;
          case PARTIAL_SUCCESS:
            if (!PluginState.FAILURE.equals(pluginState))
              pluginState = PluginState.PARTIAL_SUCCESS;
            break;
          default:
            break;
        }
      }
      switch (pluginState) {
        case SUCCESS:
          countSuccess++;
          break;
        case PARTIAL_SUCCESS:
          countPartialSuccess++;
          break;
        case FAILURE:
          countFailure++;
          break;
        default:
          break;
      }
    }

    setSourceObjectsProcessedWithFailure(countFailure);
    setSourceObjectsProcessedWithPartialSuccess(countPartialSuccess);
    setSourceObjectsProcessedWithSuccess(countSuccess);
    setSourceObjectsProcessedWithSkipped(0);
  }

  @Override
  public void finalizeInfo() {
    super.finalizeInfo();
    setStepsCompleted(getTotalSteps());
    allReports = null;
  }

  public void updateMetaPluginInformation(Report metaReport, Job cachedJob) {
    this.getAllReports().forEach((k, v) -> {
      v.forEach(report -> {
        report.setTitle(cachedJob.getName());
        report.setPlugin(metaReport.getPlugin());
        report.setPluginName(metaReport.getPluginName());
        report.setPluginVersion(metaReport.getPluginVersion());
      });
    });
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }
}
