/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.io.Serializable;
import java.util.Map;

import org.roda.core.plugins.Plugin;

public class IngestJobPluginInfo extends JobPluginInfo {
  private int stepsCompleted = 0;
  private int totalSteps = 0;

  public IngestJobPluginInfo() {
    super();
  }

  public IngestJobPluginInfo(int completionPercentage) {
    super(completionPercentage);
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
    IngestJobPluginInfo ingestInfo = this;
    boolean pluginIsDone = (ingestInfo.getStepsCompleted() == ingestInfo.getTotalSteps());
    IngestJobPluginInfo jobPluginInfo = (IngestJobPluginInfo) jobInfos.get(plugin);
    jobPluginInfo.setTotalSteps(ingestInfo.getTotalSteps());
    jobPluginInfo.setStepsCompleted(ingestInfo.getStepsCompleted());
    jobPluginInfo.setCompletionPercentage(ingestInfo.getCompletionPercentage());
    jobPluginInfo.setObjectsBeingProcessed(pluginIsDone ? 0 : ingestInfo.getObjectsBeingProcessed());
    jobPluginInfo.setObjectsWaitingToBeProcessed(pluginIsDone ? 0 : ingestInfo.getObjectsWaitingToBeProcessed());
    jobPluginInfo.setObjectsProcessedWithSuccess(ingestInfo.getObjectsProcessedWithSuccess());
    jobPluginInfo.setObjectsProcessedWithFailure(ingestInfo.getObjectsProcessedWithFailure());

    float percentage = 0f;
    int beingProcessed = 0;
    int processedWithSuccess = 0;
    int processedWithFailure = 0;
    for (JobPluginInfo jpi : jobInfos.values()) {
      IngestJobPluginInfo pluginInfo = (IngestJobPluginInfo) jpi;
      if (pluginInfo.getTotalSteps() > 0) {
        float pluginPercentage = ((float) pluginInfo.getStepsCompleted()) / pluginInfo.getTotalSteps();
        float pluginWeight = ((float) pluginInfo.getObjectsCount()) / taskObjectsCount;
        percentage += (pluginPercentage * pluginWeight);

        processedWithSuccess += pluginInfo.getObjectsProcessedWithSuccess();
        processedWithFailure += pluginInfo.getObjectsProcessedWithFailure();
      }
      beingProcessed += pluginInfo.getObjectsBeingProcessed();
    }

    IngestJobPluginInfo ingestInfoUpdated = new IngestJobPluginInfo();
    ingestInfoUpdated.setCompletionPercentage(Math.round((percentage * 100)));
    ingestInfoUpdated.setObjectsBeingProcessed(beingProcessed);
    ingestInfoUpdated.setObjectsProcessedWithSuccess(processedWithSuccess);
    ingestInfoUpdated.setObjectsProcessedWithFailure(processedWithFailure);
    return ingestInfoUpdated;
  }

}
