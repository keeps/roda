package org.roda.core.plugins.plugins.internal.synchronization.instanceIdentifier;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.plugins.Plugin;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class InstanceIdentifierDIPPlugin extends InstanceIdentifierRodaEntityPlugin<DIP> {
  @Override
  public String getName() {
    return "DIP instance identifier";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<DIP> cloneMe() {
    return new InstanceIdentifierDIPPlugin();
  }

  @Override
  public List<Class<DIP>> getObjectClasses() {
    return Arrays.asList(DIP.class);
  }

  @Override
  public String getPreservationEventDescription() {
    return "Updated the instance identifier";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The instance identifier was updated successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Could not update the instance identifier";
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.UPDATE;
  }
}
