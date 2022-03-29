package org.roda.core.plugins.orchestrate;

import java.util.ArrayList;
import java.util.Map;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.plugins.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class MutipleJobPluginInfo extends JobPluginInfo {

  private static final Logger LOGGER = LoggerFactory.getLogger(MutipleJobPluginInfo.class);
  private ArrayList<Report> allReports = new ArrayList<>();

  public MutipleJobPluginInfo() {
    super();
  }

  public void update(final MutipleJobPluginInfo mutipleJobPluginInfo) {
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
      final MutipleJobPluginInfo pluginInfo = (MutipleJobPluginInfo) jpi;
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

    final MutipleJobPluginInfo mutipleJobPluginInfo = new MutipleJobPluginInfo();
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

  public ArrayList<Report> getAllReports() {
    return this.allReports;
  }

  public void addReport(final Report report) {
    allReports.add(report);
  }

  public void updateSourceObjectsProcessed() {
    int countSuccess = 0;
    int countPartialSuccess = 0;
    int countFailure = 0;
    for (Report rootReport : getAllReports()) {
      PluginState pluginState = PluginState.SUCCESS;
      for (Report innerReport : rootReport.getReports()) {
        switch (innerReport.getPluginState()) {
          case FAILURE:
            pluginState = innerReport.getPluginState();
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
    setStepsCompleted(getTotalSteps());
    allReports = null;
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
