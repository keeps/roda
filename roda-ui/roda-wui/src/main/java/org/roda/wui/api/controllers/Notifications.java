/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.notifications.EmailNotificationProcessor;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Notifications extends RodaWuiController {

  private Notifications() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static Notification createNotification(User user, Notification notification, String template)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    if (template == null) {
      template = RodaConstants.API_NOTIFICATION_DEFAULT_TEMPLATE;
    }

    notification = RodaCoreFactory.getModelService().createNotification(notification,
      new EmailNotificationProcessor(template));

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "notification", notification);

    return notification;
  }

  public static Notification updateNotification(User user, Notification notification)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    notification = RodaCoreFactory.getModelService().updateNotification(notification);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "notification", notification);

    return notification;
  }

  public static void deleteNotification(User user, String notificationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    RodaCoreFactory.getModelService().deleteNotification(notificationId);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "notificationId", notificationId);
  }

  public static void acknowledgeNotification(User user, String notificationId, String token)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    RodaCoreFactory.getModelService().acknowledgeNotification(notificationId, token);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "notification_id", notificationId, "token",
      token);
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
