package org.roda.wui.api.v2.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.TransferredResources;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.TransferredResourceService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.TransferredResourceRestService;
import org.roda.wui.common.RequestControllerAssistant;
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
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author António Lindo <alindo@keep.pt>
 */

@RestController
@RequestMapping(path = "/api/v2/transfers")
public class TransferredResourceController implements TransferredResourceRestService, Exportable {

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private TransferredResourceService transferredResourceService;

  @Autowired
  private IndexService indexService;

  @Autowired
  private RequestHandler requestHandler;

  @Override
  public TransferredResources getSelectedTransferredResources(@RequestBody SelectedItemsRequest selected) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<TransferredResources>() {
      @Override
      public TransferredResources process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
        return new TransferredResources(
          transferredResourceService.retrieveSelectedTransferredResource(requestContext.getIndexService(),
            CommonServicesUtils.convertSelectedItems(selected, TransferredResource.class)));
      }
    });
  }

  @Override
  public Job moveTransferredResources(@RequestBody SelectedItemsRequest items, String resourceId) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        TransferredResource transferredResource = null;

        if (resourceId != null) {
          transferredResource = getResource(resourceId);
        }

        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, items,
          RodaConstants.CONTROLLER_TRANSFERRED_RESOURCE_PARAM, transferredResource);

        return transferredResourceService.moveTransferredResource(requestContext.getUser(),
          CommonServicesUtils.convertSelectedItems(items, TransferredResource.class), transferredResource);
      }
    });
  }

  @Override
  public TransferredResource getResource(String resourceId) {
    return indexService.retrieve(TransferredResource.class, resourceId, new ArrayList<>());
  }

  @Override
  public Job deleteMultipleResources(@RequestBody SelectedItemsRequest transferredResourceSelectedItems) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, transferredResourceSelectedItems);
        return transferredResourceService.deleteTransferredResourcesByJob(
          CommonServicesUtils.convertSelectedItems(transferredResourceSelectedItems, TransferredResource.class),
          requestContext.getUser());
      }
    });
  }

  @Override
  public TransferredResource renameTransferredResource(String resourceId, String newName, Boolean replaceExisting) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<TransferredResource>() {
      @Override
      public TransferredResource process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_TRANSFERRED_RESOURCE_ID_PARAM, resourceId,
          RodaConstants.CONTROLLER_FILENAME_PARAM, newName);
        // delegate
        return getResource(transferredResourceService.renameTransferredResource(requestContext.getIndexService(),
          resourceId, newName, replaceExisting));
      }
    });
  }

  @Override
  public Void refreshTransferResource(String transferredResourceRelativePath) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Void>() {
      @Override
      public Void process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        Optional<String> folderRelativePath = transferredResourceRelativePath != null
          ? Optional.of(transferredResourceRelativePath)
          : Optional.empty();

        folderRelativePath
          .ifPresent(s -> controllerAssistant.setParameters(RodaConstants.CONTROLLER_FOLDER_RELATIVEPATH_PARAM, s));

        // delegate
        transferredResourceService.updateTransferredResources(folderRelativePath, true);

        return null;
      }
    });
  }

  @Override
  public TransferredResource reindexResources(String path) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<TransferredResource>() {
      @Override
      public TransferredResource process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_PATH_PARAM, path);
        // delegate
        return transferredResourceService.reindexTransferredResource(requestContext.getIndexService(), path);
      }
    });
  }

  @GetMapping(path = "{uuid}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads transferred resource", description = "Download a particular transferred resource", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadTransferredResource(
    @Parameter(description = "The resource id") @PathVariable(name = "uuid") String uuid) {

    return requestHandler.processRequest(new RequestHandler.RequestProcessor<ResponseEntity<StreamingResponseBody>>() {
      @Override
      public ResponseEntity<StreamingResponseBody> process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(uuid);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_RESOURCE_ID_PARAM, uuid);
        TransferredResource transferredResource = indexService.retrieve(TransferredResource.class, uuid,
          new ArrayList<>());

        StreamResponse streamResponse = transferredResourceService.createStreamResponse(transferredResource);

        return ApiUtils.okResponse(streamResponse, null);
      }
    });
  }

  @PostMapping(path = "/create/resource", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create transferred resource", description = "Creates a new transferred resource", responses = {
    @ApiResponse(responseCode = "201", description = "Transferred resource created", content = @Content(schema = @Schema(implementation = TransferredResource.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public TransferredResource createTransferredResource(
    @Parameter(description = "The id of the parent") @RequestParam(name = "parent-uuid", required = false) String parentUUID,
    @Parameter(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = MultipartFile.class)), description = "Multipart file") @RequestPart(value = "resource") MultipartFile resource,
    @Parameter(description = "Commit after creation", content = @Content(schema = @Schema(defaultValue = "false", implementation = Boolean.class))) @RequestParam(value = "commit", defaultValue = "false") boolean commit) {

    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<TransferredResource>() {
      @Override
      public TransferredResource process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        String fileName = resource.getOriginalFilename();
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_PATH_PARAM, parentUUID,
          RodaConstants.CONTROLLER_FILENAME_PARAM, fileName, RodaConstants.CONTROLLER_SUCCESS_PARAM, true);
        return transferredResourceService.createTransferredResourceFile(parentUUID, fileName, resource.getInputStream(),
          commit);
      }
    });
  }

  @Override
  public TransferredResource createTransferredResourcesFolder(String parentUUID, String folderName, boolean commit) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<TransferredResource>() {
      @Override
      public TransferredResource process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_PARENT_PARAM, parentUUID,
          RodaConstants.CONTROLLER_FOLDERNAME_PARAM, folderName, RodaConstants.CONTROLLER_FORCE_COMMIT_PARAM, commit);
        return transferredResourceService.createTransferredResourcesFolder(parentUUID, folderName, commit);
      }
    });
  }

  @Override
  public TransferredResource findByUuid(String uuid, String localeString) {
    return indexService.retrieve(TransferredResource.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<TransferredResource> find(@RequestBody FindRequest findRequest, String localeString) {
    return indexService.find(TransferredResource.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(),
      RodaConstants.PERMISSION_METHOD_FIND_TRANSFERRED_RESOURCE)) {
      return new LongResponse(indexService.count(TransferredResource.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(SuggestRequest suggestRequest) {
    return indexService.suggest(suggestRequest, TransferredResource.class);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    // delegate
    return ApiUtils
      .okResponse(indexService.exportToCSV(findRequestString, TransferredResource.class));
  }
}
