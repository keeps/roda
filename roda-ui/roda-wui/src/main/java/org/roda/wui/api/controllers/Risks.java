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
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.planning.MitigationPropertiesBundle;
import org.roda.wui.client.planning.RiskMitigationBundle;
import org.roda.wui.client.planning.RiskVersionsBundle;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.createRisk(risk, false);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_RISK_PARAM, risk);
    }
  }

  public static Risk createRiskWithAuthor(User user, Risk risk) throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.createRisk(risk, user, true);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
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

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.updateRisk(risk, null, false, 0);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, risk.getId(), state, RodaConstants.CONTROLLER_RISK_PARAM, risk);
    }
  }

  public static void updateRisk(User user, Risk risk, int incidences)
    throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

      BrowserHelper.updateRisk(risk, user, properties, true, incidences);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, risk.getId(), state, RodaConstants.CONTROLLER_RISK_PARAM, risk,
        RodaConstants.CONTROLLER_MESSAGE_PARAM, RodaConstants.VersionAction.UPDATED.toString());
    }
  }

  public static Job deleteRisk(User user, SelectedItems<IndexedRisk> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.deleteRisk(user, selected);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }

  public static void deleteRisk(User user, String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      BrowserHelper.deleteRisk(riskId, false);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, riskId, state, RodaConstants.CONTROLLER_RISK_ID_PARAM, riskId);
    }
  }

  public static RiskIncidence createRiskIncidence(User user, RiskIncidence incidence)
    throws AuthorizationDeniedException, GenericException, AlreadyExistsException, NotFoundException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.createRiskIncidence(incidence, false);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
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

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.updateRiskIncidence(incidence, false);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, incidence.getId(), state, RodaConstants.CONTROLLER_INCIDENCE_PARAM,
        incidence);
    }
  }

  public static void updateRiskIncidenceWithCommit(User user, RiskIncidence incidence)
    throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      BrowserHelper.updateRiskIncidence(incidence);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, incidence.getId(), state, RodaConstants.CONTROLLER_INCIDENCE_PARAM,
        incidence);
    }
  }

  public static Job deleteRiskIncidences(User user, SelectedItems<RiskIncidence> selected, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return BrowserHelper.deleteRiskIncidences(user, selected, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected,
        RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  public static void deleteRiskIncidence(User user, String incidenceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      BrowserHelper.deleteRiskIncidence(incidenceId, false);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, incidenceId, state, RodaConstants.CONTROLLER_INCIDENCE_PARAM,
        incidenceId);
    }
  }

  public static Job updateMultipleIncidences(User user, SelectedItems<RiskIncidence> selected, String status,
    String severity, Date mitigatedOn, String mitigatedBy, String mitigatedDescription)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return BrowserHelper.updateMultipleIncidences(user, selected, status, severity, mitigatedOn, mitigatedBy,
        mitigatedDescription);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }

  public static void revertRiskVersion(User user, String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.REVERTED.toString());

      int incidences = 0;

      try {
        IndexedRisk indexedRisk = RodaCoreFactory.getIndexService().retrieve(IndexedRisk.class, riskId,
          Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_INCIDENCES_COUNT));
        incidences = indexedRisk.getIncidencesCount();
      } catch (NotFoundException e) {
        // do nothing
      }

      BrowserHelper.revertRiskVersion(riskId, versionId, properties, incidences);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, riskId, state, RodaConstants.CONTROLLER_RISK_ID_PARAM, riskId,
        RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId, RodaConstants.CONTROLLER_MESSAGE_PARAM,
        RodaConstants.VersionAction.REVERTED.toString());
    }
  }

  public static void deleteRiskVersion(User user, String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      BrowserHelper.deleteRiskVersion(riskId, versionId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, riskId, state, RodaConstants.CONTROLLER_RISK_ID_PARAM, riskId,
        RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId);
    }
  }

  public static RiskVersionsBundle retrieveRiskVersions(User user, String riskId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.retrieveRiskVersions(riskId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, riskId, state, RodaConstants.CONTROLLER_RISK_ID_PARAM, riskId);
    }
  }

  public static boolean hasRiskVersions(User user, String riskId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.hasRiskVersions(riskId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, riskId, state, RodaConstants.CONTROLLER_RISK_ID_PARAM, riskId);
    }
  }

  public static Risk retrieveRiskVersion(User user, String riskId, String selectedVersion)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException, IOException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.retrieveRiskVersion(riskId, selectedVersion);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, riskId, state, RodaConstants.CONTROLLER_RISK_ID_PARAM, riskId,
        RodaConstants.CONTROLLER_SELECTED_VERSION_PARAM, selectedVersion);
    }
  }

  public static RiskMitigationBundle retrieveShowMitigationTerms(User user, int preMitigationProbability,
                                                                 int preMitigationImpact, int posMitigationProbability, int posMitigationImpact)
    throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    RiskMitigationBundle ret = BrowserHelper.retrieveShowMitigationTerms(preMitigationProbability, preMitigationImpact,
      posMitigationProbability, posMitigationImpact);

    // register action
    controllerAssistant.registerAction(user, LogEntryState.SUCCESS,
      RodaConstants.CONTROLLER_PRE_MITIGATION_PROBABILITY_PARAM, preMitigationProbability,
      RodaConstants.CONTROLLER_PRE_MITIGATION_IMPACT_PARAM, preMitigationImpact,
      RodaConstants.CONTROLLER_POS_MITIGATION_PROBABILITY_PARAM, posMitigationProbability,
      RodaConstants.CONTROLLER_POS_MITIGATION_IMPACT_PARAM, posMitigationImpact);

    return ret;
  }

  public static List<String> retrieveMitigationSeverityLimits(User user) throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    List<String> ret = BrowserHelper.retrieveShowMitigationTerms();

    // register action
    controllerAssistant.registerAction(user, LogEntryState.SUCCESS);

    return ret;
  }

  public static MitigationPropertiesBundle retrieveAllMitigationProperties(User user)
    throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    MitigationPropertiesBundle ret = BrowserHelper.retrieveAllMitigationProperties();

    // register action
    controllerAssistant.registerAction(user, LogEntryState.SUCCESS);

    return ret;
  }

  public static void updateRiskCounters(User user)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      BrowserHelper.updateRiskCounters();
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
