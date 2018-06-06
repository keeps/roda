/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import org.roda.core.common.notifications.EmailNotificationProcessor;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
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
    throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    String tmpl = template;

    // check user permissions
    controllerAssistant.checkRoles(user);

    if (tmpl == null) {
      tmpl = RodaConstants.API_NOTIFICATION_DEFAULT_TEMPLATE;
    }

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.createNotification(notification, new EmailNotificationProcessor(tmpl));
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.RODA_OBJECT_NOTIFICATION, notification);
    }
  }

  public static Notification updateNotification(User user, Notification notification)
    throws AuthorizationDeniedException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.updateNotification(notification);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, notification.getId(), state, RodaConstants.RODA_OBJECT_NOTIFICATION,
        notification);
    }
  }

  public static void deleteNotification(User user, String notificationId)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      BrowserHelper.deleteNotification(notificationId);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, notificationId, state, RodaConstants.CONTROLLER_NOTIFICATION_ID_PARAM,
        notificationId);
    }
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
