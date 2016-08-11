package org.roda.core.common.notifications;

import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.notifications.Notification;

public interface NotificationProcessor {
  public Notification processNotification(Notification notification) throws RODAException;
}
