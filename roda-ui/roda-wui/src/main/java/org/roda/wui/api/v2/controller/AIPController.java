package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.StringResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.AIPService;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.RepresentationService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.client.services.AIPRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
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

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/aips")
public class AIPController implements AIPRestService {

  @Autowired
  HttpServletRequest request;

  @Autowired
  IndexService indexService;

  @Autowired
  AIPService aipService;

  @Autowired
  RepresentationService representationService;

  @Override
  public IndexedAIP findByUuid(String uuid, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    IndexedAIP retrieve = indexService.retrieve(requestContext, IndexedAIP.class, uuid, new ArrayList<>());

    RodaConstants.DistributedModeType distributedModeType = RodaCoreFactory.getDistributedModeType();

    if (RODAInstanceUtils.isConfiguredAsDistributedMode()
        && RodaConstants.DistributedModeType.CENTRAL.equals(distributedModeType)) {
      boolean isLocalInstance = retrieve.getInstanceId().equals(RODAInstanceUtils.getLocalInstanceIdentifier());
      aipService.retrieveDistributedInstanceName(retrieve.getInstanceId(), isLocalInstance)
          .ifPresent(retrieve::setInstanceName);
      retrieve.setLocalInstance(isLocalInstance);
    }

    return retrieve;
  }

  @Override
  public IndexResult<IndexedAIP> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.find(IndexedAIP.class, findRequest, localeString, requestContext);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_AIP)) {
      return new LongResponse(indexService.count(IndexedAIP.class, countRequest, requestContext));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(@RequestBody SuggestRequest suggestRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.suggest(suggestRequest, IndexedAIP.class, requestContext);
  }

  @Override
  public List<IndexedAIP> getAncestors(String id) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      IndexedAIP indexedAIP = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, id, new ArrayList<>());

      // check object permissions
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedAIP);

      return aipService.getAncestors(indexedAIP, requestContext.getUser());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, id, state, RodaConstants.CONTROLLER_ID_PARAM, id);
    }
  }

  @Override
  public DescriptiveMetadataInfos retrieveRepresentationDescriptiveMetadata(String aipId, String representationId,
    String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      List<String> aipFieldsWithPermissions = new ArrayList<>(Arrays.asList(RodaConstants.AIP_STATE,
        RodaConstants.INDEX_UUID, RodaConstants.AIP_GHOST, RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL));
      aipFieldsWithPermissions.addAll(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      List<String> representationFieldsToReturn = new ArrayList<>(Arrays.asList(RodaConstants.INDEX_UUID,
        RodaConstants.REPRESENTATION_AIP_ID, RodaConstants.REPRESENTATION_ID, RodaConstants.REPRESENTATION_TYPE));

      IndexedRepresentation representation = RodaCoreFactory.getIndexService().retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aipId, representationId), representationFieldsToReturn);

      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, representation.getAipId(),
        aipFieldsWithPermissions);

      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      return representationService.getDescriptiveMetadata(representation, localeString);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (NotFoundException | GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId);
    }
  }

  @Override
  public List<String> retrieveAIPRuleProperties() {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    try {
      // delegate
      return aipService.getConfigurationAIPRules(requestContext.getUser());
    } finally {
      controllerAssistant.registerAction(requestContext, LogEntryState.SUCCESS);
    }
  }

  @GetMapping(path = "/{id}/representations/{representation-id}/metadata/descriptive/{descriptive-metadata-id}/html", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Retrieves descriptive metadata result", responses = {
    @ApiResponse(responseCode = "200", description = "Returns an object ", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),})
  ResponseEntity<StreamingResponseBody> retrieveRepresentationDescriptiveMetadataHTML(
    @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
    @Parameter(description = "The representation identifier", required = true) @PathVariable(name = "representation-id") String representationId,
    @Parameter(description = "The descriptive metadata identifier", required = true) @PathVariable(name = "descriptive-metadata-id") String descriptiveMetadataId,
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      IndexedRepresentation representation = RodaCoreFactory.getIndexService().retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aipId, representationId), RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), representation);

      // delegate
      StreamResponse streamResponse = representationService.retrieveRepresentationDescriptiveMetadata(aipId,
        representationId, descriptiveMetadataId, localeString);
      return ApiUtils.okResponse(streamResponse, null);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        descriptiveMetadataId);
    }
  }
}
