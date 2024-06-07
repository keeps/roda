package org.roda.wui.api.v2.controller;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.api.incidences.UpdateRiskIncidences;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.RiskIncidenceService;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.RiskIncidenceRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/incidences")
public class RiskIncidenceController implements RiskIncidenceRestService {

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private RiskIncidenceService riskIncidenceService;

  @Autowired
  private IndexService indexService;

  @Override
  public RiskIncidence findByUuid(String uuid, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_INCIDENCE_ID,
      RodaConstants.RISK_INCIDENCE_RISK_ID, RodaConstants.RISK_INCIDENCE_DESCRIPTION,
      RodaConstants.RISK_INCIDENCE_STATUS, RodaConstants.RISK_INCIDENCE_SEVERITY,
      RodaConstants.RISK_INCIDENCE_DETECTED_BY, RodaConstants.RISK_INCIDENCE_DETECTED_ON,
      RodaConstants.RISK_INCIDENCE_MITIGATED_ON, RodaConstants.RISK_INCIDENCE_MITIGATED_BY,
      RodaConstants.RISK_INCIDENCE_MITIGATED_DESCRIPTION);
    return indexService.retrieve(requestContext, RiskIncidence.class, uuid, fieldsToReturn);
  }

  @Override
  public IndexResult<RiskIncidence> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.find(RiskIncidence.class, findRequest, localeString, requestContext);
  }

  @Override
  public LongResponse count(CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_RISK_INCIDENCE)) {
      return new LongResponse(indexService.count(RiskIncidence.class, countRequest, requestContext));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(SuggestRequest suggestRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.suggest(suggestRequest, RiskIncidence.class, requestContext);
  }

  @Override
  public Job deleteRiskIncidences(@RequestBody DeleteRequest deleteRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      return riskIncidenceService.deleteRiskIncidences(requestContext.getUser(), deleteRequest);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        deleteRequest.getItemsToDelete(), RodaConstants.CONTROLLER_DETAILS_PARAM, deleteRequest.getDetails());
    }
  }

  @Override
  public Job updateMultipleIncidences(@RequestBody UpdateRiskIncidences selected) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      return riskIncidenceService.updateMultipleIncidences(requestContext.getUser(),
        CommonServicesUtils.convertSelectedItems(selected.getSelectedItems(), RiskIncidence.class),
        selected.getStatus(), selected.getSeverity(), selected.getMitigatedDescription());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }

  @Override
  public RiskIncidence updateRiskIncidence(@RequestBody RiskIncidence incidence) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      return riskIncidenceService.updateRiskIncidence(incidence);
    } catch (AuthorizationDeniedException | GenericException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, incidence.getId(), state,
        RodaConstants.CONTROLLER_INCIDENCE_PARAM, incidence);
    }
  }
}
