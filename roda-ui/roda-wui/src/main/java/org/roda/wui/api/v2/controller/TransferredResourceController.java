package org.roda.wui.api.v2.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.TransferredResources;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.TransferredResourceService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.client.services.TransferredResourceRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.I18nUtility;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author António Lindo <alindo@keep.pt>
 */

@RestController
@RequestMapping(path = "/api/v2/transfers")
@Tag(name = TransferredResourceController.SWAGGER_ENDPOINT)
public class TransferredResourceController implements TransferredResourceRestService {
  public static final String SWAGGER_ENDPOINT = "v2 transfers";

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private TransferredResourceService transferredResourceService;

  @Autowired
  private IndexService indexService;

  @Override
  public TransferredResources getSelectedTransferredResources(
    @RequestBody SelectedItems<TransferredResource> selected) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      return new TransferredResources(transferredResourceService.retrieveSelectedTransferredResource(selected));
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        selected);
    }
  }

  @Override
  public Job moveTransferredResources(@RequestBody SelectedItems<TransferredResource> items, String resourceId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    TransferredResource transferredResource = null;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      if (resourceId != null) {
        transferredResource = getResource(resourceId);
      }
      return transferredResourceService.moveTransferredResource(requestContext.getUser(), items, transferredResource);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        items, RodaConstants.CONTROLLER_TRANSFERRED_RESOURCE_PARAM, transferredResource);
    }
  }

  @Override
  public TransferredResource getResource(String resourceId) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.retrieve(requestContext.getUser(), TransferredResource.class, resourceId, new ArrayList<>());
  }

  @Override
  public Job deleteMultipleResources(@RequestBody SelectedItems<TransferredResource> transferredResourceSelectedItems) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      return transferredResourceService.deleteTransferredResourcesByJob(transferredResourceSelectedItems,
        requestContext.getUser());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        transferredResourceSelectedItems);
    }
  }

  @Override
  public TransferredResource renameTransferredResource(String resourceId, String newName, Boolean replaceExisting) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return getResource(transferredResourceService.renameTransferredResource(resourceId, newName, replaceExisting));
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_TRANSFERRED_RESOURCE_ID_PARAM, resourceId, RodaConstants.CONTROLLER_FILENAME_PARAM,
        newName);
    }
  }

  @Override
  public Void refreshTransferResource(String transferredResourceRelativePath) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    Optional<String> folderRelativePath = transferredResourceRelativePath != null
      ? Optional.of(transferredResourceRelativePath)
      : Optional.empty();

    try {
      // check permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      transferredResourceService.updateTransferredResources(folderRelativePath, true);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      if (folderRelativePath.isPresent()) {
        controllerAssistant.registerAction(requestContext.getUser(), state,
          RodaConstants.CONTROLLER_FOLDER_RELATIVEPATH_PARAM, folderRelativePath.get());
      } else {
        controllerAssistant.registerAction(requestContext.getUser(), state);
      }
    }
    return null;
  }

  @Override
  public TransferredResource reindexResources(String path) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return transferredResourceService.reindexTransferredResource(path);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_PATH_PARAM, path);
    }
  }

  @GetMapping(path = "/binary/{uuid}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads transferred resource", description = "Download a particular transferred resource", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> getResourceBinary(
    @Parameter(description = "The resource id") @PathVariable(name = "uuid") String uuid) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      TransferredResource transferredResource = indexService.retrieve(requestContext.getUser(),
        TransferredResource.class, uuid, new ArrayList<>());

      StreamResponse streamResponse = transferredResourceService.createStreamResponse(transferredResource);

      return ApiUtils.okResponse(streamResponse, null);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), uuid, state,
        RodaConstants.CONTROLLER_RESOURCE_ID_PARAM, uuid);
    }
  }

  @PostMapping(path = "/create/resource", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create transferred resource", description = "Creates a new transferred resource", responses = {
    @ApiResponse(responseCode = "201", description = "OK", content = @Content(schema = @Schema(implementation = TransferredResource.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public TransferredResource createTransferredResource(
    @Parameter(description = "The id of the parent") @RequestParam(name = "parent-uuid", required = false) String parentUUID,
    @Parameter(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = MultipartFile.class)), description = "Multipart file") @RequestPart(value = "resource") MultipartFile resource,
    @Parameter(description = "Commit after creation", content = @Content(schema = @Schema(defaultValue = "false", implementation = Boolean.class))) @RequestParam(value = "commit", defaultValue = "false") boolean commit) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    String fileName = resource.getOriginalFilename();

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return transferredResourceService.createTransferredResourceFile(parentUUID, fileName, resource.getInputStream(),
        commit);
    } catch (RODAException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_PATH_PARAM,
        parentUUID, RodaConstants.CONTROLLER_FILENAME_PARAM, fileName, RodaConstants.CONTROLLER_SUCCESS_PARAM, true);
    }
  }

  @Override
  public TransferredResource createTransferredResourcesFolder(String parentUUID, String folderName, boolean commit) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return transferredResourceService.createTransferredResourcesFolder(parentUUID, folderName, commit);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_PARENT_PARAM,
        parentUUID, RodaConstants.CONTROLLER_FOLDERNAME_PARAM, folderName, RodaConstants.CONTROLLER_FORCE_COMMIT_PARAM,
        commit);
    }
  }

  @Override
  public TransferredResource findByUuid(String uuid) {
    return null;
  }

  @Override
  public IndexResult<TransferredResource> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    if (findRequest.filter == null || findRequest.filter.getParameters().isEmpty()) {
      return new IndexResult<>();
    }

    // delegate
    IndexResult<TransferredResource> result = indexService.find(TransferredResource.class, findRequest.filter,
      findRequest.sorter, findRequest.sublist, findRequest.facets, requestContext.getUser(), findRequest.onlyActive,
      findRequest.fieldsToReturn);

    return I18nUtility.translate(result, TransferredResource.class, localeString);
  }

  @Override
  public String count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    return String.valueOf(indexService.count(TransferredResource.class, countRequest.filter, countRequest.onlyActive,
      requestContext.getUser()));
  }
}
