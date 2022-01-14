/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.util.ArrayList;
import java.util.Map;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.plugins.Plugin;

public class SimpleJobPluginInfo extends JobPluginInfo {
  private static final long serialVersionUID = 2210879753936753174L;
  private ArrayList<Report> reports = new ArrayList<>();

  public SimpleJobPluginInfo() {
    super();
  }

  public ArrayList<Report> getReports() {
    return reports;
  }

  public void update(SimpleJobPluginInfo jobPluginInfo) {
    this.setTotalSteps(jobPluginInfo.getTotalSteps());
    this.setStepsCompleted(jobPluginInfo.getStepsCompleted());
    this.setCompletionPercentage(jobPluginInfo.getCompletionPercentage());
    this.setSourceObjectsCount(jobPluginInfo.getSourceObjectsCount());
    this.setSourceObjectsBeingProcessed(jobPluginInfo.getSourceObjectsBeingProcessed());
    this.setSourceObjectsWaitingToBeProcessed(jobPluginInfo.getSourceObjectsWaitingToBeProcessed());
    this.setSourceObjectsProcessedWithSuccess(jobPluginInfo.getSourceObjectsProcessedWithSuccess());
    this.setSourceObjectsProcessedWithFailure(jobPluginInfo.getSourceObjectsProcessedWithFailure());
    this.setSourceObjectsProcessedWithSkipped(jobPluginInfo.getSourceObjectsProcessedWithSkipped());
  }

  @Override
  public <T extends IsRODAObject> JobPluginInfo processJobPluginInformation(Plugin<T> plugin, JobInfo jobInfo) {
    int taskObjectsCount = jobInfo.getObjectsCount();
    Map<Integer, JobPluginInfo> jobInfos = jobInfo.getJobInfo();
    // update information in the map<plugin, pluginInfo>
    // FIXME/INFO 20160601 hsilva: the following code would be necessary in a
    // distributed architecture
    // SimpleJobPluginInfo jobPluginInfo = (SimpleJobPluginInfo)
    // jobInfos.get(plugin);
    // jobPluginInfo.update(this);
    float percentage = 0f;
    int sourceObjectsCount = 0;
    int sourceObjectsBeingProcessed = 0;
    int sourceObjectsProcessedWithSuccess = 0;
    int sourceObjectsProcessedWithFailure = 0;
    int sourceObjectsProcessedWithSkipped = 0;
    for (JobPluginInfo jpi : jobInfos.values()) {
      SimpleJobPluginInfo pluginInfo = (SimpleJobPluginInfo) jpi;
      if (pluginInfo.getTotalSteps() > 0) {
        float pluginPercentage = pluginInfo.getCompletionPercentage() == 100 ? 1.0f : 0.0f;
        if (pluginInfo.getCompletionPercentage() != 100) {
          pluginPercentage = ((float) pluginInfo.getStepsCompleted()) / pluginInfo.getTotalSteps();
        }
        float pluginWeight = ((float) pluginInfo.getSourceObjectsCount()) / taskObjectsCount;
        percentage += (pluginPercentage * pluginWeight);

        sourceObjectsProcessedWithSuccess += pluginInfo.getSourceObjectsProcessedWithSuccess();
        sourceObjectsProcessedWithFailure += pluginInfo.getSourceObjectsProcessedWithFailure();
        sourceObjectsProcessedWithSkipped += pluginInfo.getSourceObjectsProcessedWithSkipped();
      }
      sourceObjectsBeingProcessed += pluginInfo.getSourceObjectsBeingProcessed();
      sourceObjectsCount += pluginInfo.getSourceObjectsCount();
    }

    SimpleJobPluginInfo infoUpdated = new SimpleJobPluginInfo();
    // FIXME 20160819 hsilva: divide by zero problem when # of sourceObjects is
    // unknown
    infoUpdated.setCompletionPercentage(Math.round((percentage * 100)));
    infoUpdated.setSourceObjectsCount(sourceObjectsCount);
    infoUpdated.setSourceObjectsBeingProcessed(sourceObjectsBeingProcessed);
    infoUpdated.setSourceObjectsProcessedWithSuccess(sourceObjectsProcessedWithSuccess);
    infoUpdated.setSourceObjectsProcessedWithFailure(sourceObjectsProcessedWithFailure);
    infoUpdated.setSourceObjectsProcessedWithSkipped(sourceObjectsProcessedWithSkipped);
    return infoUpdated;
  }

  public void updateSourceObjectsProcessed() {
    int countSuccess = 0;
    int countPartialSuccess = 0;
    int countFailure = 0;
    for (Report rootReport : reports) {
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
}
