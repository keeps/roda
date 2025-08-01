/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.controller;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.api.incidences.UpdateRiskIncidences;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.RiskIncidenceService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.RiskIncidenceRestService;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/incidences")
public class RiskIncidenceController implements RiskIncidenceRestService, Exportable {

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private RiskIncidenceService riskIncidenceService;

  @Autowired
  private IndexService indexService;

  @Autowired
  private RequestHandler requestHandler;

  @Override
  public RiskIncidence findByUuid(String uuid, String localeString) {
    final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_INCIDENCE_ID,
      RodaConstants.RISK_INCIDENCE_RISK_ID, RodaConstants.RISK_INCIDENCE_DESCRIPTION,
      RodaConstants.RISK_INCIDENCE_STATUS, RodaConstants.RISK_INCIDENCE_SEVERITY,
      RodaConstants.RISK_INCIDENCE_DETECTED_BY, RodaConstants.RISK_INCIDENCE_DETECTED_ON,
      RodaConstants.RISK_INCIDENCE_MITIGATED_ON, RodaConstants.RISK_INCIDENCE_MITIGATED_BY,
      RodaConstants.RISK_INCIDENCE_MITIGATED_DESCRIPTION);
    return indexService.retrieve(RiskIncidence.class, uuid, fieldsToReturn);
  }

  @Override
  public IndexResult<RiskIncidence> find(@RequestBody FindRequest findRequest, String localeString) {
    return indexService.find(RiskIncidence.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_RISK_INCIDENCE)) {
      return new LongResponse(indexService.count(RiskIncidence.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(SuggestRequest suggestRequest) {
    return indexService.suggest(suggestRequest, RiskIncidence.class);
  }

  @Override
  public Job deleteRiskIncidences(@RequestBody DeleteRequest deleteRequest) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, deleteRequest.getItemsToDelete(),
          RodaConstants.CONTROLLER_DETAILS_PARAM, deleteRequest.getDetails());
        return riskIncidenceService.deleteRiskIncidences(requestContext.getUser(), deleteRequest);
      }
    });
  }

  @Override
  public Job updateMultipleIncidences(@RequestBody UpdateRiskIncidences selected) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
        return riskIncidenceService.updateMultipleIncidences(requestContext.getUser(),
          CommonServicesUtils.convertSelectedItems(selected.getSelectedItems(), RiskIncidence.class),
          selected.getStatus(), selected.getSeverity(), selected.getMitigatedDescription());
      }
    });
  }

  @Override
  public RiskIncidence updateRiskIncidence(@RequestBody RiskIncidence incidence) {

    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<RiskIncidence>() {
      @Override
      public RiskIncidence process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(incidence.getId());
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_INCIDENCE_PARAM, incidence);
        return riskIncidenceService.updateRiskIncidence(requestContext.getModelService(), incidence);
      }
    });
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    // delegate
    return ApiUtils.okResponse(
      indexService.exportToCSV(findRequestString, RiskIncidence.class));
  }
}
