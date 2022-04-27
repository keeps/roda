package org.roda.core.plugins.plugins.internal.synchronization.instanceIdentifier;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.plugins.Plugin;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class InstanceIdentifierNotificationPlugin extends InstanceIdentifierRodaEntityPlugin<Notification> {
  @Override
  public String getName() {
    return "Notification instance identifier";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<Notification> cloneMe() {
    return new InstanceIdentifierNotificationPlugin();
  }

  @Override
  public List<Class<Notification>> getObjectClasses() {
    return Arrays.asList(Notification.class);
  }
}
