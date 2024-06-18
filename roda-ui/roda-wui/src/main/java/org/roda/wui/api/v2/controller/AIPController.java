package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.metadata.CreateDescriptiveMetadataRequest;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataPreview;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataPreviewRequest;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataRequestForm;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataVersionsResponse;
import org.roda.core.data.v2.ip.metadata.InstanceState;
import org.roda.core.data.v2.ip.metadata.SupportedMetadata;
import org.roda.core.data.v2.ip.metadata.SupportedMetadataValue;
import org.roda.core.data.v2.ip.metadata.TypeOptionsInfo;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.controllers.BrowserHelper;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.AIPService;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.RepresentationService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.client.services.AIPRestService;
import org.roda.wui.common.ControllerAssistant;
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

import com.gargoylesoftware.htmlunit.javascript.host.fetch.Response;

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

  @GetMapping(path = "/binary/{" + RodaConstants.API_PATH_PARAM_AIP_ID
    + "}/documentation", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads documentation", description = "Download AIP documentation", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Response.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadAipDocuments(
    @Parameter(description = "The ID of the existing aip", required = true) @PathVariable(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      IndexedAIP aip = findByUuid(aipId, localeString);

      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      return ApiUtils.okResponse(aipService.retrieveAIPPart(aipId, RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION),
        null);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId);
    }
  }

  @GetMapping(path = "/binary/{" + RodaConstants.API_PATH_PARAM_AIP_ID
    + "}/submission", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads submission", description = "Download AIP submission", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Response.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadAipSubmission(
    @Parameter(description = "The ID of the existing aip", required = true) @PathVariable(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      IndexedAIP aip = findByUuid(aipId, localeString);

      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      return ApiUtils.okResponse(aipService.retrieveAIPPart(aipId, RodaConstants.STORAGE_DIRECTORY_SUBMISSION), null);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId);
    }
  }

  @GetMapping(path = "/binary/{" + RodaConstants.API_PATH_PARAM_AIP_ID
    + "}/schemas", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads schemas", description = "Download AIP schemas", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Response.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadAipSchema(
    @Parameter(description = "The ID of the existing aip", required = true) @PathVariable(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      IndexedAIP aip = findByUuid(aipId, localeString);

      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      return ApiUtils.okResponse(aipService.retrieveAIPPart(aipId, RodaConstants.STORAGE_DIRECTORY_SCHEMAS), null);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId);
    }
  }

  @GetMapping(path = "/binary/{" + RodaConstants.API_PATH_PARAM_AIP_ID
    + "}/preservation_metadata", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads preservation metadata", description = "Download AIP preservation metadata", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Response.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadAipPreservationMetadata(
    @Parameter(description = "The ID of the existing aip", required = true) @PathVariable(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      IndexedAIP aip = findByUuid(aipId, localeString);

      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      return ApiUtils.okResponse(aipService.retrievePreservationMetadata(aipId), null);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId);
    }
  }

  @GetMapping(path = "/binary/{" + RodaConstants.API_PATH_PARAM_AIP_ID
    + "}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Download AIP", description = "Download intelectual entity", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Response.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadAIP(
    @Parameter(description = "The ID of the existing aip", required = true) @PathVariable(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      IndexedAIP aip = findByUuid(aipId, localeString);

      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      return ApiUtils.okResponse(aipService.retrieveAIP(aipId), null);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId);
    }
  }

  @Override
  public Job deleteAIPs(@RequestBody SelectedItems<IndexedAIP> aips, String details) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      return aipService.deleteAIP(requestContext.getUser(), aips, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM, aips,
        RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  @Override
  public List<String> getRepresentationInformationFields() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    aipService.setIndexService(indexService);

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      return aipService.getRepresentationInformation(requestContext.getUser());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state);
    }
  }

  @Override
  public InstanceState getInstanceName(String aipId, String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    aipService.setIndexService(indexService);
    IndexedAIP aip = findByUuid(aipId, localeString);

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      return aipService.getInstanceInformation(aip);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state);
    }
  }

  @Override
  public DescriptiveMetadataInfos getDescriptiveMetadata(String aipId, String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    Locale locale = ServerTools.parseLocale(localeString);

    aipService.setIndexService(indexService);

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      return aipService.retrieveDescriptiveMetadataList(requestContext.getUser(), aipId, locale);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state);
    }
  }

  /*
   *
   *
   * VE METODO ABAIXO QUE ISTO PODE NAO ESTAR BEM
   *
   *
   *
   */
  @Override
  public Void createAIPDescriptiveMetadata(String aipId, @RequestBody CreateDescriptiveMetadataRequest body) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    String filename = body.getFilename();
    String metadataId = body.getId();
    String descriptiveMetadataType = body.getType();
    String descriptiveMetadataVersion = body.getVersion();
    ContentPayload payload;

    // If the bundle has values from the form, we need to update the XML by
    // applying the values of the form to the raw template
    try {
      controllerAssistant.checkRoles(requestContext.getUser());

      if (body instanceof DescriptiveMetadataRequestForm) {
        payload = new StringContentPayload(aipService.retrieveDescriptiveMetadataPreview(metadataId, body.getValues()));
      } else {
        payload = new StringContentPayload(body.getXml());
      }

      createDescriptiveMetadataFile(requestContext.getUser(), aipId, null, filename, descriptiveMetadataType,
        descriptiveMetadataVersion, payload);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_TEMPLATE_PARAM,
        body.getId());
    }
    return null;
  }

  @Override
  public List<SupportedMetadata> retrieveSupportedMetadataTypes(String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    Locale locale = ServerTools.parseLocale(localeString);

    aipService.setIndexService(indexService);

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
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.LOCALE, locale);
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
      List<String> aipFields = new ArrayList<>(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      aipFields.addAll(Arrays.asList(RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL, RodaConstants.AIP_PARENT_ID));

      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, aipFields);

      // check object permissions
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      return aipService.retrieveSupportedMetadata(requestContext.getUser(), aip, null, metadataType, locale);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId, RodaConstants.LOCALE, locale);
    }
  }

  public DescriptiveMetadata createDescriptiveMetadataFile(User user, String aipId, String representationId,
    String metadataId, String metadataType, String metadataVersion, ContentPayload metadataPayload) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    // check user permissions

    LogEntryState state = LogEntryState.SUCCESS;
    aipService.setIndexService(indexService);

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      IndexedAIP aip = aipService.retrieve(requestContext, IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // check state
      controllerAssistant.checkAIPstate(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPInConfirmation(aip);

      // delegate
      return aipService.createDescriptiveMetadataFile(aipId, representationId, metadataId, metadataType,
        metadataVersion, metadataPayload, requestContext.getUser().getId());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId, RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId,
        RodaConstants.CONTROLLER_METADATA_ID_PARAM, metadataId, RodaConstants.CONTROLLER_TYPE_PARAM, metadataType,
        RodaConstants.CONTROLLER_VERSION_ID_PARAM, metadataVersion);
    }
  }

  @Override
  public AIP createAIP(String parentId, String type) {

    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    aipService.setIndexService(indexService);

    if (parentId == null) {
      try {

        controllerAssistant.checkRoles(requestContext.getUser());
        Permissions permissions = new Permissions();

        // delegate
        return aipService.createAIP(requestContext.getUser(), null, type, permissions);

      } catch (RODAException e) {
        state = LogEntryState.FAILURE;
        throw new RESTException(e);
      } finally {
        // register action
        controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_TYPE_PARAM, type);
      }

    } else {
      try {
        Permissions permissions = new Permissions();

        if (parentId != null) {
          IndexedAIP parentSDO = aipService.retrieve(requestContext, IndexedAIP.class, parentId,
            RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
          controllerAssistant.checkObjectPermissions(requestContext.getUser(), parentSDO);

          // check state
          controllerAssistant.checkAIPstate(parentSDO);

          // check if AIP is in a disposal confirmation
          controllerAssistant.checkIfAIPInConfirmation(parentSDO);

          Permissions parentPermissions = parentSDO.getPermissions();

          for (String name : parentPermissions.getUsernames()) {
            permissions.setUserPermissions(name, parentPermissions.getUserPermissions(name));
          }

          for (String name : parentPermissions.getGroupnames()) {
            permissions.setGroupPermissions(name, parentPermissions.getGroupPermissions(name));
          }
        } else {
          throw new RequestNotValidException("Creating AIP that should be below another with a null parentId");
        }

        // delegate
        return aipService.createAIP(requestContext.getUser(), parentId, type, permissions);
      } catch (RODAException e) {
        state = LogEntryState.FAILURE;
        throw new RESTException(e);
      } finally {
        // register action
        controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_PARENT_ID_PARAM,
          parentId, RodaConstants.CONTROLLER_TYPE_PARAM, type);
      }
    }
  }

  @Override
  public Void getDocumentation(String aipId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      aipService.hasDocumentation(aipId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId);
    }
    return null;
  }

  @Override
  public Void getSubmissions(String aipId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      aipService.hasSubmissions(aipId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId);
    }
    return null;
  }

  @Override
  public Job moveAIPInHierarchy(@RequestBody SelectedItems<IndexedAIP> selected, String parentId, String details) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    aipService.setIndexService(indexService);
    try {

      // check user permissions
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), selected);

      if (parentId != null) {
        IndexedAIP parentAip = aipService.retrieve(requestContext, IndexedAIP.class, parentId,
          RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), parentAip);

        // check state
        controllerAssistant.checkAIPstate(parentAip);

        // check if AIP is in a disposal confirmation
        controllerAssistant.checkIfAIPInConfirmation(parentAip);
      }

      // delegate
      return aipService.moveAIPInHierarchy(requestContext.getUser(), selected, parentId, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), parentId, state,
        RodaConstants.CONTROLLER_SELECTED_PARAM, selected, RodaConstants.CONTROLLER_TO_PARENT_PARAM, parentId,
        RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  @Override
  public TypeOptionsInfo getTypeOptions(String locale) {

    List<String> types = new ArrayList<>();
    boolean isControlled = RodaCoreFactory.getRodaConfiguration().getBoolean("core.aip_type.controlled_vocabulary",
      false);

    if (isControlled) {
      types = RodaCoreFactory.getRodaConfigurationAsList("core.aip_type.value");
    } else {
      FindRequest.FindRequestBuilder fr = FindRequest.getBuilder(Filter.ALL, false);
      IndexResult<IndexedAIP> result = find(fr.build(), locale);

      List<FacetFieldResult> facetResults = result.getFacetResults();
      for (FacetValue facetValue : facetResults.get(0).getValues()) {
        types.add(facetValue.getValue());
      }
    }

    return new TypeOptionsInfo(isControlled, types);
  }

  @Override
  public Job changeAIPType(@RequestBody SelectedItems<IndexedAIP> selected, String newType, String details) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), selected);

      return aipService.changeAIPType(requestContext.getUser(), selected, newType, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        selected, RodaConstants.CONTROLLER_TYPE_PARAM, newType, RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  @Override
  public Job appraisal(@RequestBody SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    aipService.setIndexService(indexService);

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), selected);
      // delegate
      return aipService.appraisal(requestContext.getUser(), selected, accept, rejectReason);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        selected, RodaConstants.CONTROLLER_ACCEPT_PARAM, accept, RodaConstants.CONTROLLER_REJECT_REASON_PARAM,
        rejectReason);
    }
  }

  @Override
  public DescriptiveMetadataPreview retrieveDescriptiveMetadataPreview(
    @RequestBody DescriptiveMetadataPreviewRequest previewRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      return new DescriptiveMetadataPreview(
        aipService.retrieveDescriptiveMetadataPreview(previewRequest.getId(), previewRequest.getValue()));
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_TEMPLATE_PARAM,
        previewRequest.getId());
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
  public CreateDescriptiveMetadataRequest retrieveSpecificDescriptiveMetadata(String aipId,
    String descriptiveMetadataId, String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    Locale locale = ServerTools.parseLocale(localeString);

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      List<String> aipFields = new ArrayList<>(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      aipFields.addAll(Arrays.asList(RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL, RodaConstants.AIP_PARENT_ID));

      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, aipFields);

      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      return aipService.retrieveSpecificDescriptiveMetadata(requestContext.getUser(), aip, null, descriptiveMetadataId,
        locale);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId, RodaConstants.CONTROLLER_METADATA_ID_PARAM, descriptiveMetadataId);
    }
  }

  @Override
  public Void deleteDescriptiveMetadataFile(String aipId, String metadataId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    aipService.setIndexService(indexService);

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // check state
      controllerAssistant.checkAIPstate(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPInConfirmation(aip);

      // delegate
      aipService.deleteDescriptiveMetadataFile(aipId, null, metadataId, requestContext.getUser().getId());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId, RodaConstants.CONTROLLER_METADATA_ID_PARAM, metadataId);
    }
    return null;
  }

  @Override
  public Void updateDescriptiveMetadataFile(String aipId, @RequestBody CreateDescriptiveMetadataRequest content) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    aipService.setIndexService(indexService);

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      IndexedAIP aip = indexService.retrieve(requestContext, IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // check state
      controllerAssistant.checkAIPstate(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPInConfirmation(aip);

      // delegate
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_USER, requestContext.getUser().getId());
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

      aipService.updateDescriptiveMetadataFile(aipId, null, content.getId(), content.getType(), content.getVersion(),
        new StringContentPayload(content.getXml()), properties, requestContext.getUser().getId());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId, RodaConstants.CONTROLLER_METADATA_ID_PARAM, content.getId());
    }
    return null;
  }

  @Override
  public DescriptiveMetadataVersionsResponse retrieveDescriptiveMetadataVersionsResponse(String aipId,
    String metadataId, String locale) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    aipService.setIndexService(indexService);

    try {

      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      IndexedAIP aip = aipService.retrieve(requestContext, IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      return aipService.retrieveDescriptiveMetadataVersionsResponse(aip, null, metadataId, locale);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId, RodaConstants.CONTROLLER_METADATA_ID_PARAM, metadataId);
    }
  }

  @Override
  public Void deleteDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    aipService.setIndexService(indexService);

    try {

      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      IndexedAIP aip = aipService.retrieve(requestContext, IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      aipService.deleteDescriptiveMetadataVersion(aipId, null, descriptiveMetadataId, versionId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId, RodaConstants.CONTROLLER_METADATA_ID_PARAM, descriptiveMetadataId,
        RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId);
    }
    return null;
  }

  @Override
  public Void revertDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    aipService.setIndexService(indexService);

    try {

      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      IndexedAIP aip = aipService.retrieve(requestContext, IndexedAIP.class, aipId,
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // delegate
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_USER, requestContext.getUser().getId());
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.REVERTED.toString());

      RodaCoreFactory.getModelService().revertDescriptiveMetadataVersion(aipId, null, descriptiveMetadataId, versionId,
        properties);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        aipId, RodaConstants.CONTROLLER_METADATA_ID_PARAM, descriptiveMetadataId,
        RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId);
    }
    return null;
  }

}
