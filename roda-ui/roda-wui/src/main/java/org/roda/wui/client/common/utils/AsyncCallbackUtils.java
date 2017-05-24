/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

import config.i18n.client.ClientMessages;

public class AsyncCallbackUtils {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final ClientLogger LOGGER = new ClientLogger(AsyncCallbackUtils.class.getName());

  private AsyncCallbackUtils() {
    // do nothing
  }

  public static final boolean treatCommonFailures(Throwable caught) {
    return treatCommonFailures(caught, null);
  }

  public static final boolean treatCommonFailures(Throwable caught, final List<String> redirectPath) {
    boolean treatedError = false;
    if (caught instanceof StatusCodeException && ((StatusCodeException) caught).getStatusCode() == 0) {
      // check if browser is offline
      if (!JavascriptUtils.isOnline()) {
        Toast.showError(messages.browserOfflineError());
      } else {
        Toast.showError(messages.cannotReachServerError());
      }
      treatedError = true;
    } else if (caught instanceof AuthorizationDeniedException) {
      final AuthorizationDeniedException authExp = (AuthorizationDeniedException) caught;

      UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<User>() {
        @Override
        public void onFailure(Throwable caught) {
          UserLogin.getInstance().showSuggestLoginDialog();
        }

        @Override
        public void onSuccess(User user) {
          if (user.isGuest()) {
            UserLogin.getInstance().showSuggestLoginDialog();
          } else {
            String message = messages.authorizationDeniedAlertMessageExceptionSimple();
            if (!authExp.getMissingRoles().isEmpty()) {
              List<String> missingRolesTranslation = new ArrayList<>();
              for (String missingRole : authExp.getMissingRoles()) {
                missingRolesTranslation.add("- " + messages.role(missingRole));
              }
              message = messages.authorizationDeniedAlertMessageMissingRoles(missingRolesTranslation);
            }

            Dialogs.showInformationDialog(messages.authorizationDeniedAlert(), message, messages.dialogOk(), false,
              new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                  if (redirectPath != null) {
                    HistoryUtils.newHistory(redirectPath);
                  }
                }

                @Override
                public void onSuccess(Void result) {
                  if (redirectPath != null) {
                    HistoryUtils.newHistory(redirectPath);
                  }
                }
              });
          }
        }
      }, true);

      treatedError = true;
    }
    return treatedError;
  }

  public static final void defaultFailureTreatment(Throwable caught) {
    if (!treatCommonFailures(caught)) {
      Toast.showError(caught.getClass().getSimpleName(), caught.getMessage());
      LOGGER.error("Async request error", caught);
    }
  }

}
