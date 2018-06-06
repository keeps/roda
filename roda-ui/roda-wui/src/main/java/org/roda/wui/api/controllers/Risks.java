/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Risks extends RodaWuiController {

  private Risks() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static Risk createRisk(User user, Risk risk) throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.createRisk(risk, false);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_RISK_PARAM, risk);
    }
  }

  public static Risk updateRisk(User user, Risk risk) throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.updateRisk(risk, null, false, 0);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, risk.getId(), state, RodaConstants.CONTROLLER_RISK_PARAM, risk);
    }
  }

  public static void deleteRisk(User user, String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      BrowserHelper.deleteRisk(riskId, false);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, riskId, state, RodaConstants.CONTROLLER_RISK_ID_PARAM, riskId);
    }
  }

  public static RiskIncidence createRiskIncidence(User user, RiskIncidence incidence)
    throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.createRiskIncidence(incidence, false);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_INCIDENCE_PARAM, incidence);
    }
  }

  public static RiskIncidence updateRiskIncidence(User user, RiskIncidence incidence)
    throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return BrowserHelper.updateRiskIncidence(incidence, false);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, incidence.getId(), state, RodaConstants.CONTROLLER_INCIDENCE_PARAM,
        incidence);
    }
  }

  public static void deleteRiskIncidence(User user, String incidenceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      BrowserHelper.deleteRiskIncidence(incidenceId, false);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, incidenceId, state, RodaConstants.CONTROLLER_INCIDENCE_PARAM,
        incidenceId);
    }
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
