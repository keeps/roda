package org.roda.core.plugins.plugins.synchronization.instanceIdentifier;

import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.plugins.Plugin;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class InstanceIdentifierRiskIncidencePlugin extends InstanceIdentifierRodaEntityPlugin<RiskIncidence> {
  @Override
  public String getName() {
    return "Risk Incidence instance identifier";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<RiskIncidence> cloneMe() {
    return new InstanceIdentifierRiskIncidencePlugin();
  }

  @Override
  public List<Class<RiskIncidence>> getObjectClasses() {
    return Arrays.asList(RiskIncidence.class);
  }

}
