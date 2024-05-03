package org.roda.wui.api.v2.controller;

import java.util.ArrayList;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.api.incidences.SelectedIncidences;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.RiskIncidenceService;
import org.roda.wui.client.services.RiskIncidenceRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/incidences")
@Tag(name = RiskIncidenceController.SWAGGER_ENDPOINT)
public class RiskIncidenceController implements RiskIncidenceRestService {
  public static final String SWAGGER_ENDPOINT = "v2 incidences";

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private RiskIncidenceService incidenceService;

  @Autowired
  private IndexService indexService;

  @Override
  public RiskIncidence findByUuid(String uuid) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser(), User.class);

      // delegate
      final RiskIncidence ret = indexService.retrieve(requestContext.getUser(), RiskIncidence.class, uuid,
        new ArrayList<>());

      // checking object permissions
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), ret, User.class);

      return ret;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), uuid, state, RodaConstants.CONTROLLER_CLASS_PARAM,
        User.class.getSimpleName(), RodaConstants.CONTROLLER_ID_PARAM, uuid);
    }
  }

  @Override
  public IndexResult<RiskIncidence> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (findRequest.filter == null || findRequest.filter.getParameters().isEmpty()) {
      return new IndexResult<>();
    }
    // delegate
    return indexService.find(RiskIncidence.class, findRequest, localeString, requestContext.getUser());
  }

  @Override
  public String count(CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    return String.valueOf(
      indexService.count(RiskIncidence.class, countRequest.filter, countRequest.onlyActive, requestContext.getUser()));
  }

  @Override
  public Job deleteRiskIncidences(@RequestBody SelectedItems<RiskIncidence> selected, String details) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      return incidenceService.deleteRiskIncidences(requestContext.getUser(), selected, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        selected, RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  @Override
  public Job updateMultipleIncidences(@RequestBody SelectedIncidences<RiskIncidence> selected) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      return incidenceService.updateMultipleIncidences(requestContext.getUser(), selected.getSelectedItems(),
        selected.getStatus(), selected.getSeverity(), selected.getMitigatedDescription());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        selected);
    }
  }

  @Override
  public RiskIncidence updateRiskIncidence(@RequestBody RiskIncidence incidence) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      return incidenceService.updateRiskIncidence(incidence);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), incidence.getId(), state,
        RodaConstants.CONTROLLER_INCIDENCE_PARAM, incidence);
    }
  }
}
