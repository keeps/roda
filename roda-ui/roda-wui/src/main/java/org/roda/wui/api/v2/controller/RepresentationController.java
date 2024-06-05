package org.roda.wui.api.v2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.generics.ChangeRepresentationStatesRequest;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.RepresentationTypeOptions;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
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
import org.roda.core.util.IdUtils;
import org.roda.wui.api.controllers.BrowserHelper;
import org.roda.wui.api.v2.services.TransferredResourceService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.RepresentationsService;
import org.roda.wui.client.services.RepresentationRestService;
import org.roda.wui.common.ControllerAssistant;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author António Lindo <alindo@keep.pt>
 */

@RestController
@RequestMapping(path = "/api/v2/representations")
public class RepresentationController implements RepresentationRestService {
  @Autowired
  private HttpServletRequest request;

  @Autowired
  private IndexService indexService;

  @Autowired
  private RepresentationsService representationsService;

  private static final Logger LOGGER = LoggerFactory.getLogger(RepresentationController.class);


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
    return new LongResponse(indexService.count(IndexedRepresentation.class, countRequest, requestContext));
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
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId);
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
      //TODO CHANGE TO AIP NEW CONTROLLER
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // check state
      controllerAssistant.checkAIPstate(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPInConfirmation(aip);

      // delegate
      return representationsService.createRepresentation(requestContext.getUser(), aipId, representationId, type, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_TYPE_PARAM, type,
        RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  @Override
  public RepresentationTypeOptions getRepresentationTypeOptions(String localeString) {
    List<String> types = new ArrayList<>();
    boolean isControlled = RodaCoreFactory.getRodaConfiguration()
      .getBoolean("core.representation_type.controlled_vocabulary", false);

    if (isControlled) {
      types = RodaCoreFactory.getRodaConfigurationAsList("core.representation_type.value");
    } else {
      Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.REPRESENTATION_TYPE));
      FindRequest findRequest = FindRequest.getBuilder(IndexedRepresentation.class.getName(), Filter.ALL,
          false).withSublist(Sublist.NONE).withFacets(facets).withExportFacets(false).withSorter(Sorter.NONE)
        .withFieldsToReturn(new ArrayList<>()).build();

      IndexResult<IndexedRepresentation> result = find(findRequest, localeString);

      List<FacetFieldResult> facetResults = result.getFacetResults();
      for (FacetValue facetValue : facetResults.get(0).getValues()) {
        types.add(facetValue.getValue());
      }

      Boolean flag = false;

      for (String word : types) {
        if (word.equals("MIXED")) {
          flag = true;
          break;
        }
      }

      if (!flag)
        types.add("MIXED");
    }

    return new RepresentationTypeOptions(Pair.of(isControlled, types));
  }

  @Override
  public Job changeRepresentationType(@RequestBody  SelectedItems<IndexedRepresentation> items, String newType, String details) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), items);
      return representationsService.changeRepresentationType(requestContext.getUser(), items, newType, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM, items,
        RodaConstants.CONTROLLER_TYPE_PARAM, newType, RodaConstants.CONTROLLER_DETAILS_PARAM, details);
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
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM, items,
        RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  @Override
  public Void changeRepresentationStatus(@RequestBody ChangeRepresentationStatesRequest changeRepresentationStatesRequest, String details) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    IndexedRepresentation indexedRepresentation = changeRepresentationStatesRequest.getIndexedRepresentation();
    List<String> newStates = changeRepresentationStatesRequest.getNewStates();
    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedRepresentation);
      representationsService.changeRepresentationStates(requestContext.getUser(), indexedRepresentation, newStates, details);
      return null;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_REPRESENTATION_PARAM, indexedRepresentation,
        RodaConstants.CONTROLLER_STATES_PARAM, newStates, RodaConstants.CONTROLLER_DETAILS_PARAM, details);
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
      return BrowserHelper.createFormatIdentificationJob(requestContext.getUser(), items);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM, items);
    }
  }

  @GetMapping(path = "/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/binary", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
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
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId);
    }
  }

  @GetMapping(path = "/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/other-metadata/binary", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
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

      StreamResponse streamResponse = representationsService.retrieveAIPRepresentationOthermetadata(representation);

      return ApiUtils.okResponse(streamResponse, null);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId);
    }
  }



}
