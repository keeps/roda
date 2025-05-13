package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.aip.AssessmentRequest;
import org.roda.core.data.v2.aip.MoveRequest;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.UpdatePermissionsRequest;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.metadata.ConfiguredDescriptiveMetadataList;
import org.roda.core.data.v2.ip.metadata.CreateDescriptiveMetadataRequest;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataPreview;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataPreviewRequest;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataRequestForm;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataRequestXML;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataVersions;
import org.roda.core.data.v2.ip.metadata.SelectedType;
import org.roda.core.data.v2.ip.metadata.SupportedMetadataValue;
import org.roda.core.data.v2.ip.metadata.TypeOptionsInfo;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.representation.ChangeTypeRequest;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.model.GenericOkResponse;
import org.roda.wui.api.v2.services.AIPService;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.RepresentationService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.AIPRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.I18nUtility;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.server.ServerTools;
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
public class AIPController implements AIPRestService, Exportable {

  @Autowired
  HttpServletRequest request;

  @Autowired
  IndexService indexService;

  @Autowired
  AIPService aipService;

  @Autowired
  RepresentationService representationService;

  @Autowired
  private RequestHandler requestHandler;

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

  @GetMapping(path = "/{id}/representations/{representation-id}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads representation", description = "Download a particular representation", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> getRepresentationBinary(
    @Parameter(description = "The ID of the existing aip", required = true) @PathVariable(name = "id") String aipId,
    @Parameter(description = "The ID of the existing representation", required = true) @PathVariable(name = "representation-id") String representationId,
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      String indexedRepresentationId = IdUtils.getRepresentationId(aipId, representationId);
      IndexedRepresentation representation = RodaCoreFactory.getIndexService().retrieve(IndexedRepresentation.class,
        indexedRepresentationId, new ArrayList<>());

      controllerAssistant.checkObjectPermissions(requestContext.getUser(), representation);
      StreamResponse streamResponse = representationService.retrieveAIPRepresentationBinary(representation);

      return ApiUtils.okResponse(streamResponse);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId);
    }
  }

  @GetMapping(path = "/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID
    + "}/other-metadata/binary", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads representation", description = "Download a particular representation", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
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
      IndexedRepresentation representation = RodaCoreFactory.getIndexService().retrieve(IndexedRepresentation.class,
        indexedRepresentationId, new ArrayList<>());

      controllerAssistant.checkObjectPermissions(requestContext.getUser(), representation);

      StreamResponse streamResponse = representationService.retrieveAIPRepresentationOtherMetadata(representation);

      return ApiUtils.okResponse(streamResponse, null);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId);
    }
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

  @GetMapping(path = "/{id}/metadata/descriptive/{descriptive-metadata-id}/download", produces = MediaType.APPLICATION_XML_VALUE)
  @Operation(summary = "Retrieves descriptive metadata", description = "Retrieves the XML descriptive metadata", responses = {
    @ApiResponse(responseCode = "200", description = "Returns an object ", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),})
  ResponseEntity<StreamingResponseBody> downloadAIPDescriptiveMetadata(
    @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
    @Parameter(description = "The descriptive metadata identifier", required = true) @PathVariable(name = "descriptive-metadata-id") String descriptiveMetadataId,
    @Parameter(description = "The version identifier") @RequestParam(name = "version-id", required = false) String versionId) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP indexedAIP = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId,
        RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedAIP);

      // delegate
      StreamResponse streamResponse = aipService.downloadAIPDescriptiveMetadata(aipId, descriptiveMetadataId,
        versionId);
      return ApiUtils.okResponse(streamResponse);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, descriptiveMetadataId);
    }
  }

  @GetMapping(path = "/{id}/metadata/descriptive/{descriptive-metadata-id}/html", produces = MediaType.TEXT_HTML_VALUE)
  @Operation(summary = "Retrieves descriptive metadata", description = "Retrieves the descriptive metadata with the visualization template applied and internationalized", responses = {
    @ApiResponse(responseCode = "200", description = "Returns an object ", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),})
  ResponseEntity<StreamingResponseBody> retrieveAIPDescriptiveMetadataHTML(
    @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
    @Parameter(description = "The descriptive metadata identifier", required = true) @PathVariable(name = "descriptive-metadata-id") String descriptiveMetadataId,
    @Parameter(description = "The version identifier") @RequestParam(name = "version-id", required = false) String versionId,
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP indexedAIP = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId,
        RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedAIP);

      // delegate
      StreamResponse streamResponse = aipService.retrieveAIPDescriptiveMetadata(aipId, descriptiveMetadataId, versionId,
        localeString);
      return ApiUtils.okResponse(streamResponse);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, descriptiveMetadataId);
    }
  }

  @GetMapping(path = "{id}/download/documentation", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads documentation", description = "Download AIP documentation", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadAipDocuments(
    @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId, new ArrayList<>());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      return ApiUtils.okResponse(aipService.retrieveAIPPart(aipId, RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION));
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId);
    }
  }

  @GetMapping(path = "{id}/download/submission", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads submission", description = "Downloads AIP submission", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadAipSubmission(
    @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId, new ArrayList<>());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      return ApiUtils.okResponse(aipService.retrieveAIPPart(aipId, RodaConstants.STORAGE_DIRECTORY_SUBMISSION));
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId);
    }
  }

  @GetMapping(path = "/{id}/download/schemas", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads schemas", description = "Download AIP schemas", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadAipSchema(
    @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // Check object permissions
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId, new ArrayList<>());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      return ApiUtils.okResponse(aipService.retrieveAIPPart(aipId, RodaConstants.STORAGE_DIRECTORY_SCHEMAS));
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId);
    }
  }

  @GetMapping(path = "/{id}/download/preservation", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads preservation metadata", description = "Download AIP preservation metadata", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadAipPreservationMetadata(
    @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId, new ArrayList<>());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      return ApiUtils.okResponse(aipService.retrievePreservationMetadata(aipId));
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId);
    }
  }

  @GetMapping(path = "/{id}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Download AIP", description = "Download intellectual entity", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadAIP(
    @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId, new ArrayList<>());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      return ApiUtils.okResponse(aipService.retrieveAIP(aipId));
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId);
    }
  }

  @GetMapping(path = "/{id}/representations/{representation-id}/metadata/descriptive/{descriptive-metadata-id}/download", produces = MediaType.APPLICATION_XML_VALUE)
  @Operation(summary = "Retrieves representation's descriptive metadata", description = "Retrieves the representation's XML descriptive metadata", responses = {
    @ApiResponse(responseCode = "200", description = "Returns an object ", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),})
  ResponseEntity<StreamingResponseBody> downloadRepresentationDescriptiveMetadata(
    @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
    @Parameter(description = "The AIP's representation identifier", required = true) @PathVariable(name = "representation-id") String representationId,
    @Parameter(description = "The descriptive metadata identifier", required = true) @PathVariable(name = "descriptive-metadata-id") String descriptiveMetadataId,
    @Parameter(description = "The version identifier") @RequestParam(name = "version-id", required = false) String versionId) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedRepresentation indexedRepresentation = RodaCoreFactory.getIndexService().retrieve(
        IndexedRepresentation.class, IdUtils.getRepresentationId(aipId, representationId),
        RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedRepresentation);

      // delegate
      StreamResponse streamResponse = aipService.downloadRepresentationDescriptiveMetadata(aipId, representationId,
        descriptiveMetadataId, versionId);
      return ApiUtils.okResponse(streamResponse);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, descriptiveMetadataId);
    }
  }

  @GetMapping(path = "/{id}/representations/{representation-id}/metadata/descriptive/{descriptive-metadata-id}/html", produces = MediaType.TEXT_HTML_VALUE)
  @Operation(summary = "Retrieves representation's descriptive metadata", description = "Retrieves the representation's descriptive metadata with the visualization template applied and internationalized", responses = {
    @ApiResponse(responseCode = "200", description = "Returns an object ", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),})
  ResponseEntity<StreamingResponseBody> retrieveRepresentationDescriptiveMetadataHTML(
    @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
    @Parameter(description = "The AIP's representation identifier", required = true) @PathVariable(name = "representation-id") String representationId,
    @Parameter(description = "The representation's descriptive metadata identifier", required = true) @PathVariable(name = "descriptive-metadata-id") String descriptiveMetadataId,
    @Parameter(description = "The version identifier") @RequestParam(name = "version-id", required = false) String versionId,
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedRepresentation indexedRepresentation = RodaCoreFactory.getIndexService().retrieve(
        IndexedRepresentation.class, IdUtils.getRepresentationId(aipId, representationId),
        RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedRepresentation);

      // delegate
      StreamResponse streamResponse = aipService.retrieveRepresentationDescriptiveMetadata(aipId, representationId,
        descriptiveMetadataId, versionId, localeString);
      return ApiUtils.okResponse(streamResponse);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, descriptiveMetadataId);
    }
  }

  @Override
  public DescriptiveMetadata createRepresentationDescriptiveMetadata(String aipId, String representationId,
    @RequestBody CreateDescriptiveMetadataRequest createDescriptiveMetadataRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    String filename = createDescriptiveMetadataRequest.getFilename();
    String metadataId = createDescriptiveMetadataRequest.getId();
    String descriptiveMetadataType = createDescriptiveMetadataRequest.getType();
    String descriptiveMetadataVersion = createDescriptiveMetadataRequest.getVersion();
    ContentPayload payload;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());

      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      // check object permissions
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // check state
      controllerAssistant.checkAIPState(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPIsUnderADisposalPolicy(aip);

      if (createDescriptiveMetadataRequest instanceof DescriptiveMetadataRequestForm descriptiveMetadataRequestForm) {
        payload = new StringContentPayload(aipService.retrieveDescriptiveMetadataPreview(aipId, representationId,
          metadataId, descriptiveMetadataRequestForm.getValues()));
      } else {
        payload = new StringContentPayload(((DescriptiveMetadataRequestXML) createDescriptiveMetadataRequest).getXml());
      }

      // delegate
      return aipService.createDescriptiveMetadataFile(aipId, representationId, filename, descriptiveMetadataType,
        descriptiveMetadataVersion, payload, requestContext.getUser().getId(),
        requestContext.getTransactionalContext().transactionalModelService());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | RequestNotValidException | ValidationException | AlreadyExistsException
      | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId, RodaConstants.CONTROLLER_TYPE_PARAM, createDescriptiveMetadataRequest.getType(),
        RodaConstants.CONTROLLER_VERSION_ID_PARAM, descriptiveMetadataVersion);
    }
  }

  @Override
  public Job deleteAIPs(@RequestBody DeleteRequest deleteRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return aipService.deleteAIP(requestContext.getUser(),
        CommonServicesUtils.convertSelectedItems(deleteRequest.getItemsToDelete(), IndexedAIP.class),
        deleteRequest.getDetails());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        deleteRequest.getItemsToDelete(), RodaConstants.CONTROLLER_DETAILS_PARAM, deleteRequest.getDetails());
    }
  }

  @Override
  public DescriptiveMetadataInfos getDescriptiveMetadata(String aipId, String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    Locale locale = ServerTools.parseLocale(localeString);

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return aipService.retrieveDescriptiveMetadataList(requestContext.getUser(), aipId, locale);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId);
    }
  }

  @Override
  public DescriptiveMetadata createAIPDescriptiveMetadata(String aipId,
    @RequestBody CreateDescriptiveMetadataRequest descriptiveMetadataRequest) {
    return requestHandler.execute(new RequestHandler.RequestProcess<DescriptiveMetadata>() {
      @Override
      public DescriptiveMetadata execute(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException {

        String filename = descriptiveMetadataRequest.getFilename();
        String metadataId = descriptiveMetadataRequest.getId();
        String descriptiveMetadataType = descriptiveMetadataRequest.getType();
        String descriptiveMetadataVersion = descriptiveMetadataRequest.getVersion();
        ContentPayload payload;

        // If the bundle has values from the form, we need to update the XML by
        // applying the values of the form to the raw template
        IndexedAIP aip = indexService.retrieve(requestContext, IndexedAIP.class, aipId,
          RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
        // check object permissions
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

        // check state
        controllerAssistant.checkAIPState(aip);

        // check if AIP is in a disposal confirmation
        controllerAssistant.checkIfAIPIsUnderADisposalPolicy(aip);

        if (descriptiveMetadataRequest instanceof DescriptiveMetadataRequestForm descriptiveMetadataRequestForm) {
          payload = new StringContentPayload(aipService.retrieveDescriptiveMetadataPreview(aipId, null, metadataId,
            descriptiveMetadataRequestForm.getValues()));
        } else {
          payload = new StringContentPayload(((DescriptiveMetadataRequestXML) descriptiveMetadataRequest).getXml());
        }

        controllerAssistant.setRelatedObjectId(aipId);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
          RodaConstants.CONTROLLER_METADATA_ID_PARAM, metadataId, RodaConstants.CONTROLLER_TYPE_PARAM,
          descriptiveMetadataRequest.getType(), RodaConstants.CONTROLLER_VERSION_ID_PARAM, descriptiveMetadataVersion);

        // delegate
        return aipService.createDescriptiveMetadataFile(aipId, null, filename, descriptiveMetadataType,
          descriptiveMetadataVersion, payload, requestContext.getUser().getId(),
          requestContext.getTransactionalContext().transactionalModelService());
      }
    });
  }

  @Override
  public ConfiguredDescriptiveMetadataList retrieveSupportedMetadataTypes(String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    Locale locale = ServerTools.parseLocale(localeString);

    try {
      // check permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return aipService.retrieveSupportedMetadataTypes(locale);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.LOCALE, locale);
    }
  }

  @Override
  public SupportedMetadataValue retrieveAIPSupportedMetadata(String aipId, String metadataType, String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    Locale locale = ServerTools.parseLocale(localeString);

    try {
      // check permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      List<String> aipFields = new ArrayList<>(
        Arrays.asList(RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL, RodaConstants.AIP_PARENT_ID));
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId, aipFields);

      // check object permissions
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      return aipService.retrieveSupportedMetadata(requestContext.getUser(), aip, null, metadataType, locale);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, metadataType);
    }
  }

  @Override
  public SupportedMetadataValue retrieveRepresentationSupportedMetadata(String aipId, String representationId,
    String metadataType, String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    Locale locale = ServerTools.parseLocale(localeString);

    try {
      // check permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      List<String> aipFields = new ArrayList<>(
        Arrays.asList(RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL, RodaConstants.AIP_PARENT_ID));
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId, aipFields);
      IndexedRepresentation representation = RodaCoreFactory.getIndexService().retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aipId, representationId), new ArrayList<>());

      // check object permissions
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      return aipService.retrieveSupportedMetadata(requestContext.getUser(), aip, representation, metadataType, locale);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | RequestNotValidException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataType);
    }
  }

  @Override
  public AIP createAIP(String parentId, String type) {
    if (parentId == null) {
      return createAIPTop(type);
    } else {
      return createAIPBelow(parentId, type);
    }
  }

  public AIP createAIPTop(String type) {
    return requestHandler.execute(new RequestHandler.RequestProcess<AIP>() {
      @Override
      public AIP execute(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException {

        controllerAssistant.setParameters(RodaConstants.CONTROLLER_TYPE_PARAM, type);
        Permissions permissions = new Permissions();

        // delegate
        return aipService.createAIP(requestContext, null, type, permissions);
      }
    });
  }

  public AIP createAIPBelow(String parentId, String type) {
    return requestHandler.execute(new RequestHandler.RequestProcess<AIP>() {
      @Override
      public AIP execute(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException {

        controllerAssistant.setParameters(RodaConstants.CONTROLLER_PARENT_ID_PARAM, parentId,
          RodaConstants.CONTROLLER_TYPE_PARAM, type);
        Permissions permissions = new Permissions();

        IndexedAIP parentSDO = indexService.retrieve(requestContext, IndexedAIP.class, parentId,
          RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), parentSDO);

        // check state
        controllerAssistant.checkAIPState(parentSDO);

        // check if AIP is in a disposal confirmation
        controllerAssistant.checkIfAIPIsUnderADisposalPolicy(parentSDO);

        Permissions parentPermissions = parentSDO.getPermissions();

        for (String name : parentPermissions.getUsernames()) {
          permissions.setUserPermissions(name, parentPermissions.getUserPermissions(name));
        }

        for (String name : parentPermissions.getGroupnames()) {
          permissions.setGroupPermissions(name, parentPermissions.getGroupPermissions(name));
        }

        // delegate
        return aipService.createAIP(requestContext, parentId, type, permissions);
      }
    });
  }

  @Override
  public boolean getDocumentation(String aipId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP retrieve = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId, new ArrayList<>());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), retrieve);

      // delegate
      return aipService.hasDocumentation(aipId);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId);
    }
  }

  @Override
  public boolean getSubmissions(String aipId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP retrieve = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId, new ArrayList<>());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), retrieve);

      // delegate
      return aipService.hasSubmissions(aipId);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId);
    }
  }

  @Override
  public Job moveAIPInHierarchy(@RequestBody MoveRequest moveRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      SelectedItems<IndexedAIP> selectedItems = CommonServicesUtils.convertSelectedItems(moveRequest.getItemsToMove(),
        IndexedAIP.class);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), selectedItems);

      if (moveRequest.getParentId() != null) {
        IndexedAIP parentAip = indexService.retrieve(requestContext, IndexedAIP.class, moveRequest.getParentId(),
          RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);

        // check state
        controllerAssistant.checkAIPState(parentAip);

        // check if AIP is in a disposal confirmation
        controllerAssistant.checkIfAIPIsUnderADisposalPolicy(parentAip);
      }

      // delegate
      return aipService.moveAIPInHierarchy(requestContext.getUser(), selectedItems, moveRequest.getParentId(),
        moveRequest.getDetails());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        moveRequest.getItemsToMove(), RodaConstants.CONTROLLER_TO_PARENT_PARAM, moveRequest.getParentId(),
        RodaConstants.CONTROLLER_DETAILS_PARAM, moveRequest.getDetails());
    }
  }

  @Override
  public TypeOptionsInfo getTypeOptions(String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      List<String> types = new ArrayList<>();
      boolean isControlled = RodaCoreFactory.getRodaConfiguration().getBoolean("core.aip_type.controlled_vocabulary",
        false);

      if (isControlled) {
        types = RodaCoreFactory.getRodaConfigurationAsList("core.aip_type.value");
      } else {
        Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_TYPE));
        FindRequest findRequest = FindRequest.getBuilder(Filter.ALL, true).withSublist(new Sublist(0, 1))
          .withFacets(facets).build();

        IndexResult<IndexedAIP> result = I18nUtility.translate(
          RodaCoreFactory.getIndexService().find(IndexedAIP.class, findRequest, requestContext.getUser()),
          IndexedAIP.class, localeString);

        List<FacetFieldResult> facetResults = result.getFacetResults();
        for (FacetValue facetValue : facetResults.getFirst().getValues()) {
          types.add(facetValue.getValue());
        }
      }

      return new TypeOptionsInfo(isControlled, types);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state);
    }
  }

  @Override
  public Job changeAIPType(@RequestBody ChangeTypeRequest changeTypeRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      SelectedItems<IndexedAIP> indexedAIPSelectedItems = CommonServicesUtils
        .convertSelectedItems(changeTypeRequest.getItems(), IndexedAIP.class);

      // check object permissions
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedAIPSelectedItems);

      return aipService.changeAIPType(requestContext.getUser(), indexedAIPSelectedItems, changeTypeRequest.getType(),
        changeTypeRequest.getDetails());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        changeTypeRequest.getItems(), RodaConstants.CONTROLLER_TYPE_PARAM, changeTypeRequest.getType(),
        RodaConstants.CONTROLLER_DETAILS_PARAM, changeTypeRequest.getDetails());
    }
  }

  @Override
  public Job appraisal(@RequestBody AssessmentRequest assessmentRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permission
      SelectedItems<IndexedAIP> selected = CommonServicesUtils.convertSelectedItems(assessmentRequest.getItems(),
        IndexedAIP.class);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), selected);

      // delegate
      return aipService.appraisal(requestContext.getUser(), selected, assessmentRequest.isAccept(),
        assessmentRequest.getRejectReason());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        assessmentRequest.getItems(), RodaConstants.CONTROLLER_ACCEPT_PARAM, assessmentRequest.isAccept(),
        RodaConstants.CONTROLLER_REJECT_REASON_PARAM, assessmentRequest.getRejectReason());
    }
  }

  @Override
  public DescriptiveMetadataPreview retrieveDescriptiveMetadataPreview(String aipId,
    @RequestBody DescriptiveMetadataPreviewRequest previewRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      return new DescriptiveMetadataPreview(
        aipService.retrieveDescriptiveMetadataPreview(aipId, null, previewRequest.getId(), previewRequest.getValue()));
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_TEMPLATE_PARAM,
        previewRequest.getId());
    }
  }

  @Override
  public boolean isRepresentationMetadataSimilar(String aipId, String representationId, String metadataId,
    @RequestBody SelectedType selectedType) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      return aipService.isMetadataSimilar(aip, representationId, metadataId, selectedType);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_TEMPLATE_PARAM, selectedType);
    }

  }

  @Override
  public boolean isAIPMetadataSimilar(String aipId, String metadataId, @RequestBody SelectedType selectedType) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      return aipService.isMetadataSimilar(aip, null, metadataId, selectedType);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_TEMPLATE_PARAM, selectedType);
    }
  }

  @Override
  public boolean requestAIPLock(String aipId) {
    boolean lockEnabled = RodaCoreFactory.getRodaConfiguration().getBoolean("core.aip.lockToEdit", false);
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    if (!lockEnabled) {
      return true;
    }

    try {
      PluginHelper.tryLock(Collections.singletonList(aipId), requestContext.getUser().getUUID());
    } catch (LockingException e) {
      return false;
    }
    return true;
  }

  @Override
  public String releaseAIPLock(String aipId) {
    boolean lockEnabled = RodaCoreFactory.getRodaConfiguration().getBoolean("core.aip.lockToEdit", false);
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    if (lockEnabled) {
      User user = requestContext.getUser();
      PluginHelper.releaseObjectLock(aipId, user.getUUID());
    }

    return JsonUtils.getJsonFromObject(new GenericOkResponse("Lock for AIP " + aipId + " released"),
      GenericOkResponse.class);
  }

  @Override
  public Void deleteDescriptiveMetadataFile(String aipId, String metadataId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // Check object permissions
      IndexedAIP aip = indexService.retrieve(requestContext, IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // check state
      controllerAssistant.checkAIPState(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPIsUnderADisposalPolicy(aip);

      // delegate
      aipService.deleteDescriptiveMetadataFile(aipId, null, metadataId, requestContext.getUser().getId());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, metadataId);
    }
    return null;
  }

  @Override
  public Void deleteRepresentationDescriptiveMetadataFile(String aipId, String representationId, String metadataId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // Check object permissions
      IndexedAIP aip = indexService.retrieve(requestContext, IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // check state
      controllerAssistant.checkAIPState(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPIsUnderADisposalPolicy(aip);

      // delegate
      aipService.deleteDescriptiveMetadataFile(aipId, representationId, metadataId, requestContext.getUser().getId());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, metadataId);
    }
    return null;
  }

  @Override
  public DescriptiveMetadata updateAIPDescriptiveMetadataFile(String aipId,
    @RequestBody CreateDescriptiveMetadataRequest content) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP aip = indexService.retrieve(requestContext, IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // check state
      controllerAssistant.checkAIPState(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPIsUnderADisposalPolicy(aip);

      // delegate
      return aipService.updateDescriptiveMetadataFile(requestContext.getUser(), aipId, content);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException | ValidationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, content.getId());
    }
  }

  @Override
  public DescriptiveMetadata updateRepresentationDescriptiveMetadataFile(String aipId, String representationId,
    @RequestBody CreateDescriptiveMetadataRequest content) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP aip = indexService.retrieve(requestContext, IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // check state
      controllerAssistant.checkAIPState(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPIsUnderADisposalPolicy(aip);

      // delegate
      return aipService.updateDescriptiveMetadataFile(requestContext.getUser(), aipId, representationId, content);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException | ValidationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        content.getId());
    }
  }

  @Override
  public Job updatePermissions(@RequestBody UpdatePermissionsRequest updateRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      controllerAssistant.checkObjectPermissions(requestContext.getUser(),
        CommonServicesUtils.convertSelectedItems(updateRequest.getItemsToUpdate(), IndexedAIP.class));

      return aipService.updateAIPPermissions(requestContext.getUser(), updateRequest);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_AIPS_PARAM,
        updateRequest.getItemsToUpdate(), RodaConstants.CONTROLLER_PERMISSIONS_PARAM, updateRequest.getPermissions(),
        RodaConstants.CONTROLLER_DETAILS_PARAM, updateRequest.getDetails());
    }
  }

  @Override
  public DescriptiveMetadataVersions retrieveAIPDescriptiveMetadataVersions(String aipId, String metadataId,
    String locale) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {

      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      return aipService.retrieveDescriptiveMetadataVersions(aip, metadataId, locale);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, metadataId);
    }
  }

  @Override
  public DescriptiveMetadataVersions retrieveRepresentationDescriptiveMetadataVersions(String aipId,
    String representationId, String metadataId, String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {

      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      return aipService.retrieveDescriptiveMetadataVersions(aip, representationId, metadataId, localeString);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId);
    }
  }

  @Override
  public Void deleteDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      aipService.deleteDescriptiveMetadataVersion(aipId, descriptiveMetadataId, versionId);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, descriptiveMetadataId, RodaConstants.CONTROLLER_VERSION_ID_PARAM,
        versionId);
    }

    return null;
  }

  @Override
  public Void deleteRepresentationDescriptiveMetadataVersion(String aipId, String representationId,
    String descriptiveMetadataId, String versionId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      aipService.deleteDescriptiveMetadataVersion(aipId, representationId, descriptiveMetadataId, versionId);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        descriptiveMetadataId, RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId);
    }

    return null;
  }

  @Override
  public DescriptiveMetadata revertAIPDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId,
    String versionId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // check state
      controllerAssistant.checkAIPState(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPIsUnderADisposalPolicy(aip);

      // delegate
      return aipService.revertDescriptiveMetadataVersion(requestContext.getUser(), aipId, descriptiveMetadataId,
        versionId);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, descriptiveMetadataId, RodaConstants.CONTROLLER_VERSION_ID_PARAM,
        versionId);
    }
  }

  @Override
  public DescriptiveMetadata revertRepresentationDescriptiveMetadataVersion(String aipId, String representationId,
    String descriptiveMetadataId, String versionId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // check state
      controllerAssistant.checkAIPState(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPIsUnderADisposalPolicy(aip);

      // delegate
      return aipService.revertDescriptiveMetadataVersion(requestContext.getUser(), aipId, representationId,
        descriptiveMetadataId, versionId);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        descriptiveMetadataId, RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId);
    }
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    // delegate
    return ApiUtils.okResponse(
      indexService.exportToCSV(requestContext.getUser(), findRequestString, IndexedAIP.class, requestContext));
  }

  @Override
  public AIP getModelAIP(String aipId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // check object permissions
      IndexedAIP aip = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // check state
      controllerAssistant.checkAIPState(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPIsUnderADisposalPolicy(aip);

      // delegate
      return RodaCoreFactory.getModelService().retrieveAIP(aipId);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId);
    }
  }

}
