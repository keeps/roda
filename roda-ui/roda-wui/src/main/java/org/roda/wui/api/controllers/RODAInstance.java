package org.roda.wui.api.controllers;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

/**
 * @author Shahzod Yusupov <syusupov@keep.pt>
 */
public class RODAInstance extends RodaWuiController {
  private RODAInstance() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */

  public static Long synchronizeIfUpdated(User user) throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, JobAlreadyStartedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;
    Long totalupdates = 0L;
    try {
      // delegate
      totalupdates += RODAInstanceHelper.synchronizeIfUpdated(user);
      return totalupdates;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }
}
