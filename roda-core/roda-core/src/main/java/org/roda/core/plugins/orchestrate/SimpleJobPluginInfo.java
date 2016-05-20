package org.roda.core.plugins.orchestrate;

import java.io.Serializable;
import java.util.Map;

import org.roda.core.plugins.Plugin;

public class SimpleJobPluginInfo extends JobPluginInfo {

  private boolean pluginExecutionIsDone = false;

  public SimpleJobPluginInfo() {
    super();
  }

  public SimpleJobPluginInfo(int completionPercentage) {
    super(completionPercentage);
  }

  @Override
  <T extends Serializable> JobPluginInfo processJobPluginInformation(Plugin<T> plugin, Integer taskObjectsCount,
    Map<Plugin<?>, JobPluginInfo> jobInfos) {

    SimpleJobPluginInfo info = this;
    SimpleJobPluginInfo jobPluginInfo = (SimpleJobPluginInfo) jobInfos.get(plugin);
    jobPluginInfo.setCompletionPercentage(info.getCompletionPercentage());
    jobPluginInfo.setObjectsBeingProcessed(pluginExecutionIsDone ? 0 : info.getObjectsBeingProcessed());
    jobPluginInfo.setObjectsWaitingToBeProcessed(pluginExecutionIsDone ? 0 : info.getObjectsWaitingToBeProcessed());
    jobPluginInfo.setObjectsProcessedWithSuccess(info.getObjectsProcessedWithSuccess());
    jobPluginInfo.setObjectsProcessedWithFailure(info.getObjectsProcessedWithFailure());

    int beingProcessed = 0;
    int processedWithSuccess = 0;
    int processedWithFailure = 0;
    for (JobPluginInfo jpi : jobInfos.values()) {
      SimpleJobPluginInfo pluginInfo = (SimpleJobPluginInfo) jpi;
      processedWithSuccess += pluginInfo.getObjectsProcessedWithSuccess();
      processedWithFailure += pluginInfo.getObjectsProcessedWithFailure();
      beingProcessed += pluginInfo.getObjectsBeingProcessed();
    }

    SimpleJobPluginInfo infoUpdated = new SimpleJobPluginInfo();
    infoUpdated.setObjectsBeingProcessed(beingProcessed);
    infoUpdated.setObjectsProcessedWithSuccess(processedWithSuccess);
    infoUpdated.setObjectsProcessedWithFailure(processedWithFailure);
    return infoUpdated;
  }

  public boolean isPluginExecutionIsDone() {
    return pluginExecutionIsDone;
  }

  public void setPluginExecutionIsDone(boolean pluginExecutionIsDone) {
    this.pluginExecutionIsDone = pluginExecutionIsDone;
  }

}
