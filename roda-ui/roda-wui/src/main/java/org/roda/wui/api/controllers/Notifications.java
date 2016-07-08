/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.common.RodaCoreService;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Notifications extends RodaCoreService {

  private static final String NOTIFICATIONS_COMPONENT = "Notifications";

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
    Date startDate = new Date();

    // FIXME check user permissions
    UserUtility.checkRoles(user, new Object(){}.getClass().getEnclosingMethod());

    RodaCoreFactory.getModelService().createNotification(notification, "test-email-template",
      new HashMap<String, Object>());

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, NOTIFICATIONS_COMPONENT, "createNotification", null, duration, "notification", notification);

    return notification;
  }

  public static void deleteNotification(RodaUser user, String notificationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Date startDate = new Date();

    // FIXME check user permissions
    UserUtility.checkRoles(user, new Object(){}.getClass().getEnclosingMethod());

    // delegate
    RodaCoreFactory.getModelService().deleteNotification(notificationId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, NOTIFICATIONS_COMPONENT, "deleteNotification", null, duration, "notificationId",
      notificationId);
  }

  public static List<Notification> retrieveNotifications(IndexResult<Notification> listNotificationsIndexResult) {
    // TODO: this method should also checkRoles? If so, a RodaUser is needed.
    List<Notification> notifications = new ArrayList<Notification>();
    for (Notification notification : listNotificationsIndexResult.getResults()) {
      notifications.add(notification);
    }
    return notifications;
  }

  public static void acknowledgeNotification(RodaUser user, String notificationId, String token, String email)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Date startDate = new Date();

    // FIXME check user permissions
    UserUtility.checkRoles(user, new Object(){}.getClass().getEnclosingMethod());

    // delegate
    RodaCoreFactory.getModelService().acknowledgeNotification(notificationId, token, email);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, NOTIFICATIONS_COMPONENT, "acknowledgeNotification", null, duration, "notificationId",
      notificationId, "token", token);
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
