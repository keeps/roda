/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.user.client.rpc.StatusCodeException;

public class AsyncRequestUtils {

  public static final boolean treatCommonFailures(Throwable caught) {
    boolean treatedError = false;
    if (caught instanceof StatusCodeException && ((StatusCodeException) caught).getStatusCode() == 0) {
      // check if browser is offline
      if (!JavascriptUtils.isOnline()) {
        Toast.showError("Your browser is offline, please check your connetivity");
      } else {
        Toast.showError("Cannot reach the server, please check your connectivity");
      }
      treatedError = true;
    } else if (caught instanceof AuthorizationDeniedException) {
      UserLogin.getInstance().login();
      treatedError = true;
    }
    return treatedError;
  }

  public static final void defaultFailureTreatment(Throwable caught) {
    if (!treatCommonFailures(caught)) {
      Toast.showError(caught.getClass().getSimpleName(), caught.getMessage());
    }
  }

}
