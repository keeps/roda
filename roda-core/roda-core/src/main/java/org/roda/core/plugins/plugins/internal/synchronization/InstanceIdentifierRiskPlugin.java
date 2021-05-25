package org.roda.core.plugins.plugins.internal.synchronization;

import org.roda.core.data.v2.risks.Risk;
import org.roda.core.plugins.Plugin;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class InstanceIdentifierRiskPlugin extends InstanceIdentifierRodaEntityPlugin<Risk> {
  @Override
  public String getName() {
    return "Risk instance identifier";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<Risk> cloneMe() {
    return new InstanceIdentifierRiskPlugin();
  }

  @Override
  public List<Class<Risk>> getObjectClasses() {
    return Arrays.asList(Risk.class);
  }
}
