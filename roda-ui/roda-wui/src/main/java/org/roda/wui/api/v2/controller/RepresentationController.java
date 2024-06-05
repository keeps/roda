package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.generics.LongResponse;
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
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.representation.ChangeRepresentationStatesRequest;
import org.roda.core.data.v2.representation.ChangeRepresentationTypeRequest;
import org.roda.core.data.v2.representation.RepresentationTypeOptions;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.controllers.BrowserHelper;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.RepresentationService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.client.services.RepresentationRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.I18nUtility;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

/**
 * @author António Lindo <alindo@keep.pt>
 */

@RestController
@RequestMapping(path = "/api/v2/representations")
public class RepresentationController implements RepresentationRestService {
  private static final Logger LOGGER = LoggerFactory.getLogger(RepresentationController.class);
  @Autowired
  private HttpServletRequest request;
  @Autowired
  private IndexService indexService;
  @Autowired
  private RepresentationService representationsService;

  @Override
  public IndexedRepresentation findByUuid(String uuid, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.retrieve(requestContext, IndexedRepresentation.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<IndexedRepresentation> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.find(IndexedRepresentation.class, findRequest, localeString, requestContext);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION)) {
      return new LongResponse(indexService.count(IndexedRepresentation.class, countRequest, requestContext));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(@RequestBody SuggestRequest suggestRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.suggest(suggestRequest, IndexedRepresentation.class, requestContext);
  }

  @Override
  public IndexedRepresentation retrieveIndexedRepresentation(
    @RequestBody IndexedRepresentationRequest indexedRepresentationRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      List<String> representationFieldsToReturn = new ArrayList<>(Arrays.asList(RodaConstants.INDEX_UUID,
        RodaConstants.REPRESENTATION_AIP_ID, RodaConstants.REPRESENTATION_ID, RodaConstants.REPRESENTATION_TYPE));

      IndexedRepresentation representation = RodaCoreFactory.getIndexService().retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(indexedRepresentationRequest.getAipId(),
          indexedRepresentationRequest.getRepresentationId()),
        representationFieldsToReturn);

      // check object permissions
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), representation);

      RodaConstants.DistributedModeType distributedModeType = RodaCoreFactory.getDistributedModeType();

      if (RODAInstanceUtils.isConfiguredAsDistributedMode()
          && RodaConstants.DistributedModeType.CENTRAL.equals(distributedModeType)) {
        boolean isLocalInstance = representation.getInstanceId().equals(RODAInstanceUtils.getLocalInstanceIdentifier());
        representationsService.retrieveDistributedInstanceName(representation.getInstanceId(), isLocalInstance)
            .ifPresent(representation::setInstanceName);
        representation.setLocalInstance(isLocalInstance);
      }

      return representation;
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        indexedRepresentationRequest.getAipId(), RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM,
        indexedRepresentationRequest.getRepresentationId());
    }
  }

  @Override
  public Representation getRepresentation(String aipId, String representationId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      IndexedRepresentation representation = findByUuid(representationId, null);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), representation);

      return representationsService.retrieveAIPRepresentation(representation);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId, RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId);
    }
  }

  @Override
  public Representation createRepresentation(String aipId, String type, String details) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    String representationId = IdUtils.createUUID();

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // TODO CHANGE TO AIP NEW CONTROLLER
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // check state
      controllerAssistant.checkAIPstate(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPInConfirmation(aip);

      // delegate
      return representationsService.createRepresentation(requestContext.getUser(), aipId, representationId, type,
        details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId, RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_TYPE_PARAM,
        type, RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  @Override
  public RepresentationTypeOptions getRepresentationTypeOptions(String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      List<String> types = new ArrayList<>();
      boolean isControlled = RodaCoreFactory.getRodaConfiguration()
        .getBoolean("core.representation_type.controlled_vocabulary", false);

      if (isControlled) {
        types = RodaCoreFactory.getRodaConfigurationAsList("core.representation_type.value");
      } else {
        Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.REPRESENTATION_TYPE));
        FindRequest findRequest = FindRequest.getBuilder(IndexedRepresentation.class.getName(), Filter.ALL, false)
          .withSublist(Sublist.NONE).withFacets(facets).withExportFacets(false).withSorter(Sorter.NONE)
          .withFieldsToReturn(new ArrayList<>()).build();

        IndexResult<IndexedRepresentation> result = I18nUtility.translate(
          RodaCoreFactory.getIndexService().find(IndexedRepresentation.class, findRequest, requestContext.getUser()),
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
    } catch (RequestNotValidException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state);
    }
  }

  @Override
  public Job changeRepresentationType(@RequestBody ChangeRepresentationTypeRequest changeRepresentationTypeRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), changeRepresentationTypeRequest.getItems());

      return representationsService.changeRepresentationType(requestContext.getUser(),
        changeRepresentationTypeRequest.getItems(), changeRepresentationTypeRequest.getType(),
        changeRepresentationTypeRequest.getDetails());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        changeRepresentationTypeRequest.getItems(), RodaConstants.CONTROLLER_TYPE_PARAM,
        changeRepresentationTypeRequest.getType(), RodaConstants.CONTROLLER_DETAILS_PARAM,
        changeRepresentationTypeRequest.getDetails());
    }
  }

  @Override
  public Job deleteRepresentation(@RequestBody SelectedItems<IndexedRepresentation> items, String details) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), items);
      return representationsService.deleteRepresentation(requestContext.getUser(), items, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        items, RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  @Override
  public IndexedRepresentation changeRepresentationStatus(
    @RequestBody ChangeRepresentationStatesRequest changeRepresentationStatesRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      IndexedRepresentation indexedRepresentation = RodaCoreFactory.getIndexService().retrieve(
        IndexedRepresentation.class, changeRepresentationStatesRequest.getRepresentationId(), new ArrayList<>());

      controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedRepresentation);
      return representationsService.changeRepresentationStates(requestContext.getUser(), indexedRepresentation,
        changeRepresentationStatesRequest);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, changeRepresentationStatesRequest.getRepresentationId(),
        RodaConstants.CONTROLLER_STATES_PARAM, changeRepresentationStatesRequest.getNewStates(),
        RodaConstants.CONTROLLER_DETAILS_PARAM, changeRepresentationStatesRequest.getDetails());
    }
  }

  @Override
  public Job createFormatIdentificationJob(@RequestBody SelectedItems<IndexedRepresentation> items) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      return representationsService.createFormatIdentificationJob(requestContext.getUser(), items);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        items);
    }
  }

  @Override
  public List<String> retrieveRepresentationRuleProperties() {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    try {
      // delegate
      return representationsService.getConfigurationRepresentationRules(requestContext.getUser());
    } finally {
      controllerAssistant.registerAction(requestContext, LogEntryState.SUCCESS);
    }
  }

  @GetMapping(path = "/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID
    + "}/binary", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads representation", description = "Download a particular representation", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Response.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> getRepresentationBinary(
    @Parameter(description = "The ID of the existing aip", required = true) @PathVariable(name = RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The ID of the existing representation", required = true) @PathVariable(name = RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      String indexedRepresentationId = IdUtils.getRepresentationId(aipId, representationId);
      IndexedRepresentation representation = findByUuid(indexedRepresentationId, localeString);

      controllerAssistant.checkObjectPermissions(requestContext.getUser(), representation);
      StreamResponse streamResponse = representationsService.retrieveAIPRepresentationBinary(representation);

      return ApiUtils.okResponse(streamResponse, null);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId, RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId);
    }
  }

  @GetMapping(path = "/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID
    + "}/other-metadata/binary", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads representation", description = "Download a particular representation", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Response.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> getRepresentationOtherMetadataBinary(
    @Parameter(description = "The ID of the existing aip", required = true) @PathVariable(name = RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The ID of the existing representation", required = true) @PathVariable(name = RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      String indexedRepresentationId = IdUtils.getRepresentationId(aipId, representationId);
      IndexedRepresentation representation = findByUuid(indexedRepresentationId, localeString);

      controllerAssistant.checkObjectPermissions(requestContext.getUser(), representation);

      StreamResponse streamResponse = representationsService.retrieveAIPRepresentationOtherMetadata(representation);

      return ApiUtils.okResponse(streamResponse, null);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId, RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId);
    }
  }
}
