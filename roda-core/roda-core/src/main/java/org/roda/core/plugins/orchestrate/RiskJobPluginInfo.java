package org.roda.core.plugins.orchestrate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.roda.core.plugins.Plugin;

public class RiskJobPluginInfo extends JobPluginInfo {

  private Map<String, Integer> risks = new HashMap<String, Integer>();

  @Override
  <T extends Serializable> JobPluginInfo processJobPluginInformation(Plugin<T> plugin, Integer taskObjectsCount,
    Map<Plugin<?>, JobPluginInfo> jobInfos) {
    RiskJobPluginInfo riskInfo = this;
    IngestJobPluginInfo jobPluginInfo = (IngestJobPluginInfo) jobInfos.get(plugin);
    jobPluginInfo.setCompletionPercentage(riskInfo.getCompletionPercentage());
    jobPluginInfo.setObjectsBeingProcessed(riskInfo.getObjectsBeingProcessed());
    jobPluginInfo.setObjectsWaitingToBeProcessed(riskInfo.getObjectsWaitingToBeProcessed());
    jobPluginInfo.setObjectsProcessedWithSuccess(riskInfo.getObjectsProcessedWithSuccess());
    jobPluginInfo.setObjectsProcessedWithFailure(riskInfo.getObjectsProcessedWithFailure());

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

    RiskJobPluginInfo newJobPluginInfo = new RiskJobPluginInfo();
    newJobPluginInfo.setRisks(this.getRisks());
    newJobPluginInfo.setCompletionPercentage(Math.round((percentage * 100)));
    newJobPluginInfo.setObjectsBeingProcessed(beingProcessed);
    newJobPluginInfo.setObjectsProcessedWithSuccess(processedWithSuccess);
    newJobPluginInfo.setObjectsProcessedWithFailure(processedWithFailure);
    return newJobPluginInfo;
  }

  public Map<String, Integer> getRisks() {
    return risks;
  }

  public void setRisks(Map<String, Integer> risks) {
    this.risks = risks;
  }

  public RiskJobPluginInfo putRisk(String riskId, int counter) {
    if (risks.containsKey(riskId)) {
      risks.put(riskId, risks.get(riskId) + counter);
    } else {
      risks.put(riskId, counter);
    }

    return this;
  }

}
