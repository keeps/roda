package org.roda.core.plugins.orchestrate;

import java.io.Serializable;
import java.util.Map;

import org.roda.core.plugins.Plugin;

public class SimpleJobPluginInfo extends JobPluginInfo {
  private static final long serialVersionUID = 402391793635402897L;

  private boolean pluginExecutionIsDone = false;

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
    jobPluginInfo.setSourceObjectsBeingProcessed(pluginExecutionIsDone ? 0 : this.getSourceObjectsBeingProcessed());
    jobPluginInfo
      .setSourceObjectsWaitingToBeProcessed(pluginExecutionIsDone ? 0 : this.getSourceObjectsWaitingToBeProcessed());
    jobPluginInfo.setSourceObjectsProcessedWithSuccess(this.getSourceObjectsProcessedWithSuccess());
    jobPluginInfo.setSourceObjectsProcessedWithFailure(this.getSourceObjectsProcessedWithFailure());

    int sourceObjectsBeingProcessed = 0;
    int sourceObjectsProcessedWithSuccess = 0;
    int sourceObjectsProcessedWithFailure = 0;
    for (JobPluginInfoInterface jpi : jobInfos.values()) {
      SimpleJobPluginInfo pluginInfo = (SimpleJobPluginInfo) jpi;
      sourceObjectsProcessedWithSuccess += pluginInfo.getSourceObjectsProcessedWithSuccess();
      sourceObjectsProcessedWithFailure += pluginInfo.getSourceObjectsProcessedWithFailure();
      sourceObjectsBeingProcessed += pluginInfo.getSourceObjectsBeingProcessed();
    }

    SimpleJobPluginInfo infoUpdated = new SimpleJobPluginInfo();
    infoUpdated.setSourceObjectsBeingProcessed(sourceObjectsBeingProcessed);
    infoUpdated.setSourceObjectsProcessedWithSuccess(sourceObjectsProcessedWithSuccess);
    infoUpdated.setSourceObjectsProcessedWithFailure(sourceObjectsProcessedWithFailure);
    return infoUpdated;
  }

  public boolean isPluginExecutionIsDone() {
    return pluginExecutionIsDone;
  }

  public void setPluginExecutionIsDone(boolean pluginExecutionIsDone) {
    this.pluginExecutionIsDone = pluginExecutionIsDone;
  }

  public void done() {
    this.pluginExecutionIsDone = false;
  }

}
