/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
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
  public static Risk createRisk(User user, Risk risk)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    Risk createdRisk = RodaCoreFactory.getModelService().createRisk(risk, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_RISK_PARAM, risk);
    return createdRisk;
  }

  public static Risk updateRisk(User user, Risk risk)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    Risk updatedRisk = RodaCoreFactory.getModelService().updateRisk(risk, null, false, 0);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_RISK_PARAM, risk);
    return updatedRisk;
  }

  public static void deleteRisk(User user, String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    RodaCoreFactory.getModelService().deleteRisk(riskId, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_RISK_ID_PARAM, riskId);
  }

  public static RiskIncidence createRiskIncidence(User user, RiskIncidence incidence)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    RiskIncidence createdIncidence = RodaCoreFactory.getModelService().createRiskIncidence(incidence, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_INCIDENCE_PARAM,
      incidence);
    return createdIncidence;
  }

  public static RiskIncidence updateRiskIncidence(User user, RiskIncidence incidence)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    RiskIncidence updatedIncidence = RodaCoreFactory.getModelService().updateRiskIncidence(incidence, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_INCIDENCE_PARAM,
      incidence);
    return updatedIncidence;
  }

  public static void deleteRiskIncidence(User user, String incidenceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    RodaCoreFactory.getModelService().deleteRiskIncidence(incidenceId, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_INCIDENCE_PARAM,
      incidenceId);
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
