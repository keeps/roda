package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
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
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskMitigationProperties;
import org.roda.core.data.v2.risks.RiskMitigationTerms;
import org.roda.core.data.v2.risks.RiskVersions;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.RiskService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.RiskRestService;
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
@RequestMapping(path = "/api/v2/risks")
public class RiskController implements RiskRestService, Exportable {

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private RiskService riskService;

  @Autowired
  private IndexService indexService;

  @Autowired
  RequestHandler requestHandler;

  @Override
  public IndexedRisk findByUuid(String uuid, String localeString) {
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
    IndexedRisk retrieve = indexService.retrieve(IndexedRisk.class, uuid, fieldsToReturn);
    try {
      retrieve.setHasVersions(riskService.hasRiskVersions(uuid));
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      throw new RESTException(e);
    }
    return retrieve;
  }

  @Override
  public IndexResult<IndexedRisk> find(@RequestBody FindRequest findRequest, String localeString) {
    return indexService.find(IndexedRisk.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_RISK)) {
      return new LongResponse(indexService.count(IndexedRisk.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(@RequestBody SuggestRequest suggestRequest) {
    return indexService.suggest(suggestRequest, IndexedRisk.class);
  }

  public Job deleteRisk(@RequestBody SelectedItemsRequest selected) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
        // delegate
        return riskService.deleteRisk(requestContext.getUser(),
          CommonServicesUtils.convertSelectedItems(selected, IndexedRisk.class));
      }
    });
  }

  @Override
  public Risk updateRisk(@RequestBody Risk risk) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Risk>() {
      @Override
      public Risk process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(risk.getId());
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_RISK_PARAM, risk,
          RodaConstants.CONTROLLER_MESSAGE_PARAM, RodaConstants.VersionAction.UPDATED.toString());
        Map<String, String> properties = new HashMap<>();
        properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

        int incidences = 0;

        IndexedRisk indexedRisk = requestContext.getIndexService().retrieve(IndexedRisk.class, risk.getUUID(),
          Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_INCIDENCES_COUNT));
        incidences = indexedRisk.getIncidencesCount();

        return riskService.updateRisk(risk, requestContext, properties, true, incidences);
      }
    });
  }

  @Override
  public Risk createRisk(@RequestBody Risk risk) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Risk>() {
      @Override
      public Risk process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_RISK_PARAM, risk);
        // delegate
        return riskService.createRisk(risk, requestContext, true);
      }
    });
  }

  @Override
  public RiskVersions retrieveRiskVersions(String id) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<RiskVersions>() {
      @Override
      public RiskVersions process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(id);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_RISK_ID_PARAM, id);
        // delegate
        return riskService.retrieveRiskVersions(requestContext.getModelService(), id);
      }
    });
  }

  @Override
  public Risk retrieveRiskVersion(String id, String versionId) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<Risk>() {
      @Override
      public Risk process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(id);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_RISK_ID_PARAM, id,
          RodaConstants.CONTROLLER_SELECTED_VERSION_PARAM, versionId);
        return riskService.retrieveRiskVersion(requestContext.getModelService(), id, versionId);
      }
    });
  }

  @Override
  public Risk revertRiskVersion(String id, String versionId) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Risk>() {
      @Override
      public Risk process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(id);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_RISK_ID_PARAM, id,
          RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId, RodaConstants.CONTROLLER_MESSAGE_PARAM,
          RodaConstants.VersionAction.REVERTED.toString());
        // delegate
        Map<String, String> properties = new HashMap<>();
        properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.REVERTED.toString());

        int incidences = 0;

        IndexedRisk indexedRisk = requestContext.getIndexService().retrieve(IndexedRisk.class, id,
          Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_INCIDENCES_COUNT));
        incidences = indexedRisk.getIncidencesCount();

        return riskService.revertRiskVersion(id, versionId, properties, incidences);
      }
    });
  }

  @Override
  public Void deleteRiskVersion(String id, String versionId) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Void>() {
      @Override
      public Void process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(id);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_RISK_ID_PARAM, id,
          RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId);

        // delegate
        riskService.deleteRiskVersion(requestContext.getModelService(), id, versionId);

        return null;
      }
    });
  }

  @Override
  public RiskMitigationTerms retrieveRiskMitigationTerms(String id) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<RiskMitigationTerms>() {
      @Override
      public RiskMitigationTerms process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(id);
        IndexedRisk indexedRisk = requestContext.getIndexService().retrieve(IndexedRisk.class, id, new ArrayList<>());

        // delegate
        return riskService.retrieveFromConfigurationMitigationTerms(indexedRisk);
      }
    });
  }

  @Override
  public RiskMitigationProperties retrieveRiskMitigationProperties() {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<RiskMitigationProperties>() {
      @Override
      public RiskMitigationProperties process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        // delegate
        return riskService.retrieveMitigationProperties();
      }
    });
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    // delegate
    return ApiUtils
      .okResponse(indexService.exportToCSV(requestContext.getUser(), findRequestString, IndexedRisk.class));
  }
}
