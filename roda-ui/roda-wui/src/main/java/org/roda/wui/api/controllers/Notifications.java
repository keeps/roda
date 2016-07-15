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
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaCoreService;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Notifications extends RodaCoreService {

  private Notifications() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static Notification createNotification(RodaUser user, Notification notification)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    RodaCoreFactory.getModelService().createNotification(notification, "test-email-template",
      new HashMap<String, Object>());

    // register action
    controllerAssistant.registerAction(user, null, "notification", notification);

    return notification;
  }

  public static void deleteNotification(RodaUser user, String notificationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    RodaCoreFactory.getModelService().deleteNotification(notificationId);

    // register action
    controllerAssistant.registerAction(user, null, "notificationId", notificationId);
  }

  public static List<Notification> retrieveNotifications(RodaUser user,
    IndexResult<Notification> listNotificationsIndexResult) throws AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // TODO: The loop bellow could be replaced by the following line, right?
    // List<Notification> notification = new ArrayList<>(listNotificationsIndexResult.getResults());
    List<Notification> notifications = new ArrayList<Notification>();
    for (Notification notification : listNotificationsIndexResult.getResults()) {
      notifications.add(notification);
    }

    // register action
    controllerAssistant.registerAction(user);

    return notifications;
  }

  public static void acknowledgeNotification(RodaUser user, String notificationId, String token, String email)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    RodaCoreFactory.getModelService().acknowledgeNotification(notificationId, token, email);

    // register action
    controllerAssistant.registerAction(user, null, "notificationId", notificationId, "token", token);
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
