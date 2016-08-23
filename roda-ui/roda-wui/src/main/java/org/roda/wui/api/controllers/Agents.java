/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Agents extends RodaWuiController {

  private Agents() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static Agent createAgent(RodaUser user, Agent agent)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    RodaCoreFactory.getModelService().createAgent(agent, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "agent", agent);

    return agent;
  }

  public static void deleteAgent(RodaUser user, String agentId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    RodaCoreFactory.getModelService().deleteAgent(agentId, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "agentId", agentId);
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
