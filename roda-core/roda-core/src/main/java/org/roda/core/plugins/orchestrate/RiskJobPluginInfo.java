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
    RiskJobPluginInfo newJobPluginInfo = new RiskJobPluginInfo();
    newJobPluginInfo.setRisks(riskInfo.getRisks());
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
