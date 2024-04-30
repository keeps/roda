package org.roda.wui.api.v2.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.RiskService;
import org.roda.wui.client.services.RiskRestService;
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
@RequestMapping(path = "/api/v2/risks")
public class RiskController implements RiskRestService {

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private RiskService indexedRiskService;

  @Autowired
  private IndexService indexService;

  @Override
  public IndexedRisk findByUuid(String uuid, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_ID,
      RodaConstants.RISK_NAME, RodaConstants.RISK_DESCRIPTION, RodaConstants.RISK_IDENTIFIED_ON,
      RodaConstants.RISK_IDENTIFIED_BY, RodaConstants.RISK_CATEGORIES, RodaConstants.RISK_NOTES,
      RodaConstants.RISK_PRE_MITIGATION_PROBABILITY, RodaConstants.RISK_PRE_MITIGATION_IMPACT,
      RodaConstants.RISK_PRE_MITIGATION_SEVERITY, RodaConstants.RISK_POST_MITIGATION_PROBABILITY,
      RodaConstants.RISK_POST_MITIGATION_IMPACT, RodaConstants.RISK_POST_MITIGATION_SEVERITY,
      RodaConstants.RISK_PRE_MITIGATION_NOTES, RodaConstants.RISK_POST_MITIGATION_NOTES,
      RodaConstants.RISK_MITIGATION_STRATEGY, RodaConstants.RISK_MITIGATION_OWNER,
      RodaConstants.RISK_MITIGATION_OWNER_TYPE, RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_TYPE,
      RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_VALUE);
    IndexedRisk retrieve = indexService.retrieve(requestContext, IndexedRisk.class, uuid, fieldsToReturn);
    try {
      retrieve.setHasVersions(indexedRiskService.hasRiskVersions(uuid));
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      throw new RESTException(e);
    }
    return retrieve;
  }

  @Override
  public IndexResult<IndexedRisk> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.find(IndexedRisk.class, findRequest, localeString, requestContext);
  }

  @Override
  public LongResponse count(CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return new LongResponse(indexService.count(LogEntry.class, countRequest, requestContext));
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
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
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

      return indexedRiskService.updateRisk(risk, requestContext.getUser(), properties, true,
        Integer.parseInt(incidences));
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
