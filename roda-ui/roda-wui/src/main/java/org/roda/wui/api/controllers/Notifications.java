/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
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
  public static Notification createNotification(User user, Notification notification)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    RodaCoreFactory.getModelService().createNotification(notification, "test-email-template",
      new HashMap<String, Object>());

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

  public static List<Notification> retrieveNotifications(User user,
    IndexResult<Notification> listNotificationsIndexResult) throws AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // TODO: The loop bellow could be replaced by the following line, right?
    // List<Notification> notification = new
    // ArrayList<>(listNotificationsIndexResult.getResults());
    List<Notification> notifications = new ArrayList<Notification>();
    for (Notification notification : listNotificationsIndexResult.getResults()) {
      notifications.add(notification);
    }

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS);

    return notifications;
  }

  public static void acknowledgeNotification(User user, String notificationId, String token, String email)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    RodaCoreFactory.getModelService().acknowledgeNotification(notificationId, token, email);

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
