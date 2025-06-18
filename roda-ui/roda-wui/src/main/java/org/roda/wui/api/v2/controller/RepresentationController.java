package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexedRepresentationRequest;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.representation.ChangeRepresentationStatesRequest;
import org.roda.core.data.v2.representation.ChangeTypeRequest;
import org.roda.core.data.v2.representation.RepresentationTypeOptions;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.RepresentationService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.RepresentationRestService;
import org.roda.wui.common.I18nUtility;
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
 * @author António Lindo <alindo@keep.pt>
 */

@RestController
@RequestMapping(path = "/api/v2/representations")
public class RepresentationController implements RepresentationRestService, Exportable {

  @Autowired
  private HttpServletRequest request;
  @Autowired
  private IndexService indexService;
  @Autowired
  private RepresentationService representationService;
  @Autowired
  private RequestHandler requestHandler;

  @Override
  public IndexedRepresentation findByUuid(String uuid, String localeString) {
    return indexService.retrieve(IndexedRepresentation.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<IndexedRepresentation> find(@RequestBody FindRequest findRequest, String localeString) {
    return indexService.find(IndexedRepresentation.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION)) {
      return new LongResponse(indexService.count(IndexedRepresentation.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(@RequestBody SuggestRequest suggestRequest) {
    return indexService.suggest(suggestRequest, IndexedRepresentation.class);
  }

  @Override
  public IndexedRepresentation retrieveIndexedRepresentationViaRequest(
    @RequestBody IndexedRepresentationRequest indexedRepresentationRequest) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<IndexedRepresentation>() {
      @Override
      public IndexedRepresentation process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_AIP_ID_PARAM,
          indexedRepresentationRequest.getAipId(), RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM,
          indexedRepresentationRequest.getRepresentationId());
        List<String> representationFieldsToReturn = new ArrayList<>(Arrays.asList(RodaConstants.INDEX_UUID,
          RodaConstants.REPRESENTATION_AIP_ID, RodaConstants.REPRESENTATION_ID, RodaConstants.REPRESENTATION_TYPE));

        IndexedRepresentation representation = requestContext.getIndexService().retrieve(IndexedRepresentation.class,
          IdUtils.getRepresentationId(indexedRepresentationRequest.getAipId(),
            indexedRepresentationRequest.getRepresentationId()),
          representationFieldsToReturn);

        // check object permissions
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), representation);

        RodaConstants.DistributedModeType distributedModeType = RodaCoreFactory.getDistributedModeType();

        if (RODAInstanceUtils.isConfiguredAsDistributedMode()
          && RodaConstants.DistributedModeType.CENTRAL.equals(distributedModeType)) {
          boolean isLocalInstance = representation.getInstanceId()
            .equals(RODAInstanceUtils.getLocalInstanceIdentifier());
          representationService
            .retrieveDistributedInstanceName(requestContext, representation.getInstanceId(), isLocalInstance)
            .ifPresent(representation::setInstanceName);
          representation.setLocalInstance(isLocalInstance);
        }

        return representation;
      }
    });
  }

  @Override
  public Representation getRepresentation(String aipId, String representationId) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<Representation>() {
      @Override
      public Representation process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        IndexedRepresentation representation = findByUuid(representationId, null);
        controllerAssistant.setRelatedObjectId(aipId);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
          RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId);
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), representation);

        return representationService.retrieveAIPRepresentation(requestContext, representation);
      }
    });
  }

  @Override
  public Representation createRepresentation(String aipId, String type, String details) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Representation>() {
      @Override
      public Representation process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        String representationId = IdUtils.createUUID();
        controllerAssistant.setRelatedObjectId(aipId);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
          RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_TYPE_PARAM, type,
          RodaConstants.CONTROLLER_DETAILS_PARAM, details);

        IndexedAIP aip = requestContext.getIndexService().retrieve(IndexedAIP.class, aipId,
          RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

        // check state
        controllerAssistant.checkAIPState(aip);

        // check if AIP is in a disposal confirmation
        controllerAssistant.checkIfAIPIsUnderADisposalPolicy(aip);

        // delegate
        return representationService.createRepresentation(requestContext, aipId, representationId, type, details);
      }
    });
  }

  @Override
  public RepresentationTypeOptions getRepresentationTypeOptions(String localeString) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<RepresentationTypeOptions>() {

      @Override
      public RepresentationTypeOptions process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        List<String> types = new ArrayList<>();
        boolean isControlled = RodaCoreFactory.getRodaConfiguration()
          .getBoolean("core.representation_type.controlled_vocabulary", false);

        if (isControlled) {
          types = RodaCoreFactory.getRodaConfigurationAsList("core.representation_type.value");
        } else {
          Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.REPRESENTATION_TYPE));
          FindRequest findRequest = FindRequest.getBuilder(Filter.ALL, false).withSublist(Sublist.NONE)
            .withFacets(facets).withExportFacets(false).withSorter(Sorter.NONE).withFieldsToReturn(new ArrayList<>())
            .build();

          IndexResult<IndexedRepresentation> result = I18nUtility.translate(
            requestContext.getIndexService().find(IndexedRepresentation.class, findRequest, requestContext.getUser()),
            IndexedRepresentation.class, localeString);

          List<FacetFieldResult> facetResults = result.getFacetResults();
          for (FacetValue facetValue : facetResults.getFirst().getValues()) {
            types.add(facetValue.getValue());
          }

          if (!types.contains("MIXED")) {
            types.add("MIXED");
          }
        }

        return new RepresentationTypeOptions(isControlled, types);
      }
    });
  }

  @Override
  public Job changeRepresentationType(@RequestBody ChangeTypeRequest changeRepresentationTypeRequest) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        SelectedItems<IndexedRepresentation> indexedRepresentationSelectedItems = CommonServicesUtils
          .convertSelectedItems(changeRepresentationTypeRequest.getItems(), IndexedRepresentation.class);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM,
          changeRepresentationTypeRequest.getItems(), RodaConstants.CONTROLLER_TYPE_PARAM,
          changeRepresentationTypeRequest.getType(), RodaConstants.CONTROLLER_DETAILS_PARAM,
          changeRepresentationTypeRequest.getDetails());
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedRepresentationSelectedItems);
        return representationService.changeRepresentationType(requestContext.getUser(),
          indexedRepresentationSelectedItems, changeRepresentationTypeRequest.getType(),
          changeRepresentationTypeRequest.getDetails());
      }
    });
  }

  @Override
  public Job deleteRepresentation(@RequestBody SelectedItemsRequest items, String details) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        SelectedItems<IndexedRepresentation> indexedRepresentationSelectedItems = CommonServicesUtils
          .convertSelectedItems(items, IndexedRepresentation.class);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, items,
          RodaConstants.CONTROLLER_DETAILS_PARAM, details);
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedRepresentationSelectedItems);
        return representationService.deleteRepresentation(requestContext.getUser(), indexedRepresentationSelectedItems,
          details);
      }
    });
  }

  @Override
  public Job changeRepresentationStatus(
    @RequestBody ChangeRepresentationStatesRequest changeRepresentationStatesRequest) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM,
          RodaConstants.CONTROLLER_SELECTED_PARAM, changeRepresentationStatesRequest.getItems(),
          RodaConstants.CONTROLLER_STATES_PARAM, changeRepresentationStatesRequest.getNewStates(),
          RodaConstants.CONTROLLER_DETAILS_PARAM, changeRepresentationStatesRequest.getDetails());

        SelectedItems<IndexedRepresentation> indexedRepresentationSelectedItems = CommonServicesUtils
          .convertSelectedItems(changeRepresentationStatesRequest.getItems(), IndexedRepresentation.class);

        controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedRepresentationSelectedItems);

        return representationService.changeRepresentationStatus(requestContext.getUser(),
          indexedRepresentationSelectedItems, changeRepresentationStatesRequest.getNewStates(),
          changeRepresentationStatesRequest.getDetails());
      }
    });
  }

  @Override
  public Job createFormatIdentificationJob(@RequestBody SelectedItemsRequest items) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, items);
        SelectedItems<IndexedRepresentation> indexedRepresentationSelectedItems = CommonServicesUtils
          .convertSelectedItems(items, IndexedRepresentation.class);
        return representationService.createFormatIdentificationJob(requestContext.getUser(),
          indexedRepresentationSelectedItems);
      }
    });
  }

  @Override
  public List<String> retrieveRepresentationRuleProperties() {
    return requestHandler.processRequestWithoutCheckRoles(new RequestHandler.RequestProcessor<List<String>>() {
      @Override
      public List<String> process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        return representationService.getConfigurationRepresentationRules(requestContext.getUser());
      }
    });
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    // delegate
    return ApiUtils
      .okResponse(indexService.exportToCSV(requestContext.getUser(), findRequestString, IndexedRepresentation.class));
  }
}