package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.BrowserHelper;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.IndexedRiskService;
import org.roda.wui.client.services.IndexedRiskRestService;
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
@RequestMapping(path = "/api/v2/risks")
@Tag(name = IndexedRiskController.SWAGGER_ENDPOINT)
public class IndexedRiskController implements IndexedRiskRestService {

  public static final String SWAGGER_ENDPOINT = "v2 risks";

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private IndexedRiskService indexedRiskService;

  @Autowired
  private IndexService indexService;

  @Override
  public IndexedRisk findByUuid(String uuid) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser(), User.class);

      // delegate
      final IndexedRisk ret = indexService.retrieve(requestContext.getUser(), IndexedRisk.class, uuid,
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
  public IndexResult<IndexedRisk> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (findRequest.filter == null || findRequest.filter.getParameters().isEmpty()) {
      return new IndexResult<>();
    }
    // delegate
    return indexService.find(IndexedRisk.class, findRequest, localeString, requestContext.getUser());
  }

  @Override
  public String count(CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    return String.valueOf(
      indexService.count(IndexedRisk.class, countRequest.filter, countRequest.onlyActive, requestContext.getUser()));
  }

  public Job deleteRisk(@RequestBody SelectedItems<IndexedRisk> selected) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      return indexedRiskService.deleteRisk(requestContext.getUser(), selected);
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
  public Void refreshRisk() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      indexedRiskService.updateRiskCounters();
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state);
    }
    return null;
  }

  @Override
  public Boolean hasRiskVersions(String riskId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      return indexedRiskService.hasRiskVersions(riskId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), riskId, state,
        RodaConstants.CONTROLLER_RISK_ID_PARAM, riskId);
    }
  }

  @Override
  public Risk updateRisk(@RequestBody Risk risk, String incidences) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

      return indexedRiskService.updateRisk(risk, requestContext.getUser(), properties, true, Integer.parseInt(incidences));
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), risk.getId(), state,
        RodaConstants.CONTROLLER_RISK_PARAM, risk, RodaConstants.CONTROLLER_MESSAGE_PARAM,
        RodaConstants.VersionAction.UPDATED.toString());
    }
  }

}
