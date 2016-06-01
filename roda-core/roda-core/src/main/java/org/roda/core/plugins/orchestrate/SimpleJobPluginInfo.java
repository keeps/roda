package org.roda.core.plugins.orchestrate;

import java.io.Serializable;
import java.util.Map;

import org.roda.core.plugins.Plugin;

public class SimpleJobPluginInfo extends JobPluginInfo {
  private static final long serialVersionUID = 2210879753936753174L;

  private boolean pluginExecutionDone = false;

  public SimpleJobPluginInfo() {
    super();
  }

  public SimpleJobPluginInfo(int sourceObjectsCount) {
    super(sourceObjectsCount);
  }

  @Override
  public <T extends Serializable> JobPluginInfo processJobPluginInformation(Plugin<T> plugin, Integer taskObjectsCount,
    Map<Plugin<?>, JobPluginInfo> jobInfos) {

    SimpleJobPluginInfo jobPluginInfo = (SimpleJobPluginInfo) jobInfos.get(plugin);
    jobPluginInfo.setCompletionPercentage(this.getCompletionPercentage());
    jobPluginInfo.setSourceObjectsCount(this.getSourceObjectsCount());
    jobPluginInfo.setSourceObjectsBeingProcessed(pluginExecutionDone ? 0 : this.getSourceObjectsBeingProcessed());
    jobPluginInfo
      .setSourceObjectsWaitingToBeProcessed(pluginExecutionDone ? 0 : this.getSourceObjectsWaitingToBeProcessed());
    jobPluginInfo.setSourceObjectsProcessedWithSuccess(this.getSourceObjectsProcessedWithSuccess());
    jobPluginInfo.setSourceObjectsProcessedWithFailure(this.getSourceObjectsProcessedWithFailure());

    int objectsCount = 0;
    int beingProcessed = 0;
    int processedWithSuccess = 0;
    int processedWithFailure = 0;
    for (JobPluginInfo jpi : jobInfos.values()) {
      SimpleJobPluginInfo pluginInfo = (SimpleJobPluginInfo) jpi;
      objectsCount += pluginInfo.getSourceObjectsCount();
      processedWithSuccess += pluginInfo.getSourceObjectsProcessedWithSuccess();
      processedWithFailure += pluginInfo.getSourceObjectsProcessedWithFailure();
      beingProcessed += pluginInfo.getSourceObjectsBeingProcessed();
    }

    SimpleJobPluginInfo infoUpdated = new SimpleJobPluginInfo();
    infoUpdated.setSourceObjectsCount(objectsCount);
    infoUpdated.setSourceObjectsBeingProcessed(beingProcessed);
    infoUpdated.setSourceObjectsProcessedWithSuccess(processedWithSuccess);
    infoUpdated.setSourceObjectsProcessedWithFailure(processedWithFailure);
    return infoUpdated;
  }

  public boolean isPluginExecutionDone() {
    return pluginExecutionDone;
  }

  public void setPluginExecutionDone(boolean pluginExecutionIsDone) {
    this.pluginExecutionDone = pluginExecutionIsDone;
  }

  public void done() {
    this.pluginExecutionDone = false;
  }

}
