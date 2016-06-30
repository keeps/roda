/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.util.Map;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.plugins.Plugin;

public class SimpleJobPluginInfo extends JobPluginInfo {
  private static final long serialVersionUID = 2210879753936753174L;

  public SimpleJobPluginInfo() {
    super();
  }

  public void update(SimpleJobPluginInfo jobPluginInfo) {
    this.setCompletionPercentage(jobPluginInfo.getCompletionPercentage());
    this.setSourceObjectsCount(jobPluginInfo.getSourceObjectsCount());
    this.setSourceObjectsBeingProcessed(jobPluginInfo.getSourceObjectsBeingProcessed());
    this.setSourceObjectsWaitingToBeProcessed(jobPluginInfo.getSourceObjectsWaitingToBeProcessed());
    this.setSourceObjectsProcessedWithSuccess(jobPluginInfo.getSourceObjectsProcessedWithSuccess());
    this.setSourceObjectsProcessedWithFailure(jobPluginInfo.getSourceObjectsProcessedWithFailure());
  }

  @Override
  public <T extends IsRODAObject> JobPluginInfo processJobPluginInformation(Plugin<T> plugin, JobInfo jobInfo) {
    int taskObjectsCount = jobInfo.getObjectsCount();
    Map<Plugin<?>, JobPluginInfo> jobInfos = jobInfo.getJobInfo();
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
    for (JobPluginInfo jpi : jobInfos.values()) {
      SimpleJobPluginInfo pluginInfo = (SimpleJobPluginInfo) jpi;

      // float pluginPercentage = pluginInfo.isDone() ? 1 : 0;
      // float pluginWeight = ((float) pluginInfo.getSourceObjectsCount()) /
      // taskObjectsCount;
      // percentage += (pluginPercentage * pluginWeight);

      sourceObjectsProcessedWithSuccess += pluginInfo.getSourceObjectsProcessedWithSuccess();
      sourceObjectsProcessedWithFailure += pluginInfo.getSourceObjectsProcessedWithFailure();
      sourceObjectsBeingProcessed += pluginInfo.getSourceObjectsBeingProcessed();
      sourceObjectsCount += pluginInfo.getSourceObjectsCount();
    }

    SimpleJobPluginInfo infoUpdated = new SimpleJobPluginInfo();
    int newPercentage = Math
      .round((((sourceObjectsProcessedWithSuccess + sourceObjectsProcessedWithFailure) * 100) / sourceObjectsCount));
    infoUpdated.setCompletionPercentage(newPercentage);
    infoUpdated.setSourceObjectsCount(sourceObjectsCount);
    infoUpdated.setSourceObjectsBeingProcessed(sourceObjectsBeingProcessed);
    infoUpdated.setSourceObjectsProcessedWithSuccess(sourceObjectsProcessedWithSuccess);
    infoUpdated.setSourceObjectsProcessedWithFailure(sourceObjectsProcessedWithFailure);
    return infoUpdated;
  }

}
