/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.notifications;

import java.io.Serial;
import java.io.Serializable;

public class NotificationAcknowledgeRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = 8871372985287041094L;

  private String notificationUUID;
  private String token;

  public NotificationAcknowledgeRequest() {
    // empty constructor
  }

  public String getNotificationUUID() {
    return notificationUUID;
  }

  public void setNotificationUUID(String notificationUUID) {
    this.notificationUUID = notificationUUID;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
