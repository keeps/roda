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
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.common.RodaCoreService;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Agents extends RodaCoreService {

  private static final String AGENTS_COMPONENT = "Agents";
  private static final String INGEST_SUBMIT_ROLE = "ingest.submit";

  private Agents() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static Agent createAgent(RodaUser user, Agent agent) throws AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, GenericException {
    Date startDate = new Date();

    // FIXME check user permissions
    UserUtility.checkRoles(user, INGEST_SUBMIT_ROLE);

    RodaCoreFactory.getModelService().createAgent(agent);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, AGENTS_COMPONENT, "createAgent", null, duration, "agent", agent);

    return agent;
  }

  public static void deleteAgent(RodaUser user, String agentId) throws RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException {
    Date startDate = new Date();

    // check user permissions
    // FIXME

    // delegate
    RodaCoreFactory.getModelService().deleteAgent(agentId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, AGENTS_COMPONENT, "deleteAgent", null, duration, "agentId", agentId);
  }

  public static List<Agent> retrieveAgents(IndexResult<Agent> listAgentsIndexResult) {
    List<Agent> agents = new ArrayList<Agent>();
    for (Agent agent : listAgentsIndexResult.getResults()) {
      agents.add(agent);
    }
    return agents;
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
