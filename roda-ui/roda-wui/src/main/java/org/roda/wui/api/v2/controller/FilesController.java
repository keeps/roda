package org.roda.wui.api.v2.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.TechnicalMetadataNotFoundException;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.file.CreateFolderRequest;
import org.roda.core.data.v2.file.MoveFilesRequest;
import org.roda.core.data.v2.file.RenameFolderRequest;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexedFileRequest;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.TechnicalMetadataInfos;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.RangeConsumesOutputStream;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.FilesService;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.FileRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

@RestController
@RequestMapping(path = "/api/v2/files")
public class FilesController implements FileRestService, Exportable {
  @Autowired
  private HttpServletRequest request;

  @Autowired
  private FilesService filesService;

  @Autowired
  private IndexService indexService;

  @Autowired
  private RequestHandler requestHandler;

  @RequestMapping(path = "{uuid}/preview", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Previews a file", description = "Previews a particular file using streaming capabilities", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StreamingResponseBody.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> previewBinary(
    @Parameter(description = "The UUID of the existing file", required = true) @PathVariable(name = "uuid") String fileUUID,
    @RequestHeader HttpHeaders headers) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<ResponseEntity<StreamingResponseBody>>() {
      @Override
      public ResponseEntity<StreamingResponseBody> process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(fileUUID);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_FILE_UUID_PARAM, fileUUID);
        List<String> fileFields = new ArrayList<>(RodaConstants.FILE_FIELDS_TO_RETURN);
        fileFields.add(RodaConstants.FILE_ISDIRECTORY);
        IndexedFile file = indexService.retrieve(IndexedFile.class, fileUUID, fileFields);
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), file);

        RangeConsumesOutputStream stream = filesService.retrieveAIPRepresentationRangeStream(requestContext, file);

        return ApiUtils.rangeResponse(headers, stream);
      }
    });
  }

  @RequestMapping(path = "{uuid}/download", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads file", description = "Download a particular file", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StreamingResponseBody.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadBinary(
    @Parameter(description = "The UUID of the existing file", required = true) @PathVariable(name = "uuid") String fileUUID) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<ResponseEntity<StreamingResponseBody>>() {
      @Override
      public ResponseEntity<StreamingResponseBody> process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(fileUUID);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_FILE_UUID_PARAM, fileUUID);
        List<String> fileFields = new ArrayList<>(RodaConstants.FILE_FIELDS_TO_RETURN);
        fileFields.add(RodaConstants.FILE_ISDIRECTORY);
        IndexedFile file = indexService.retrieve(IndexedFile.class, fileUUID, fileFields);
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), file);

        StreamResponse response = filesService.retrieveAIPRepresentationFile(requestContext, file);

        return ApiUtils.okResponse(response);
      }
    });
  }

  @GetMapping(path = "/{id}/metadata/preservation/html", produces = MediaType.TEXT_HTML_VALUE)
  @Operation(summary = "Retrieves file technical metadata", description = "Retrieves the technical metadata with the visualization template applied and internationalized", responses = {
    @ApiResponse(responseCode = "200", description = "Returns an object ", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),})
  ResponseEntity<StreamingResponseBody> retrievePreservationMetadataHTML(
    @Parameter(description = "The File identifier", required = true) @PathVariable(name = "id") String fileId,
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<ResponseEntity<StreamingResponseBody>>() {
      @Override
      public ResponseEntity<StreamingResponseBody> process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_FILE_ID_PARAM, fileId);
        // check object permissions
        IndexedFile indexedFile = requestContext.getIndexService().retrieve(IndexedFile.class, fileId,
          RodaConstants.FILE_FIELDS_TO_RETURN);
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedFile);
        // delegate
        StreamResponse streamResponse = filesService.retrieveFilePreservationHTML(requestContext, indexedFile,
          localeString);
        return ApiUtils.okResponse(streamResponse);
      }
    });
  }

  @GetMapping(path = "/{id}/metadata/preservation/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Download technical metadata file", description = "Download the technical metadata file", responses = {
    @ApiResponse(responseCode = "200", description = "Returns an object ", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),})
  ResponseEntity<StreamingResponseBody> retrievePreservationMetadataFile(
    @Parameter(description = "The File identifier", required = true) @PathVariable(name = "id") String fileId) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<ResponseEntity<StreamingResponseBody>>() {
      @Override
      public ResponseEntity<StreamingResponseBody> process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_FILE_ID_PARAM, fileId);
        // check object permissions
        IndexedFile indexedFile = requestContext.getIndexService().retrieve(IndexedFile.class, fileId,
          RodaConstants.FILE_FIELDS_TO_RETURN);

        controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedFile);

        // delegate
        StreamResponse streamResponse = filesService.retrieveFilePreservationFile(requestContext, indexedFile);
        return ApiUtils.okResponse(streamResponse);
      }
    });
  }

  @Override
  public IndexedFile findByUuid(String uuid, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.retrieve(IndexedFile.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<IndexedFile> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.find(IndexedFile.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_FILE)) {
      return new LongResponse(indexService.count(IndexedFile.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(@RequestBody SuggestRequest suggestRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.suggest(suggestRequest, IndexedFile.class);
  }

  @Override
  public IndexedFile retrieveIndexedFileViaRequest(@RequestBody IndexedFileRequest indexedFileRequest) {
    String uuid = IdUtils.getFileId(indexedFileRequest.getAipId(), indexedFileRequest.getRepresentationId(),
      indexedFileRequest.getDirectoryPaths(), indexedFileRequest.getFileId());
    // retrieve checks permissions
    IndexedFile retrieve = indexService.retrieve(IndexedFile.class, uuid, new ArrayList<>());
    // TODO Review if a check role is needed
    return requestHandler.processRequestWithoutCheckRoles(new RequestHandler.RequestProcessor<IndexedFile>() {
      @Override
      public IndexedFile process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        if (retrieve.isReference()) {
          retrieve.setAvailable(filesService.isShallowFileAvailable(retrieve));
        } else {
          retrieve.setAvailable(true);
        }

        RodaConstants.DistributedModeType distributedModeType = RodaCoreFactory.getDistributedModeType();

        if (RODAInstanceUtils.isConfiguredAsDistributedMode()
          && RodaConstants.DistributedModeType.CENTRAL.equals(distributedModeType)) {
          boolean isLocalInstance = retrieve.getInstanceId().equals(RODAInstanceUtils.getLocalInstanceIdentifier());
          filesService.retrieveDistributedInstanceName(requestContext, retrieve.getInstanceId(), isLocalInstance)
            .ifPresent(retrieve::setInstanceName);
          retrieve.setLocalInstance(isLocalInstance);
        }

        return retrieve;
      }
    });
  }

  @Override
  public Job moveFileToFolder(@RequestBody MoveFilesRequest moveFilesRequest) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(moveFilesRequest.getAipId());
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_AIP_ID_PARAM, moveFilesRequest.getAipId(),
          RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, moveFilesRequest.getRepresentationId(),
          RodaConstants.CONTROLLER_FILES_PARAM, moveFilesRequest.getItemsToMove(), RodaConstants.CONTROLLER_FILE_PARAM,
          moveFilesRequest.getFileUUIDtoMove(), RodaConstants.CONTROLLER_DETAILS_PARAM, moveFilesRequest.getDetails());
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), moveFilesRequest.getItemsToMove());

        IndexedAIP destinationAIP = requestContext.getIndexService().retrieve(IndexedAIP.class,
          moveFilesRequest.getAipId(), RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), destinationAIP);

        // delegate
        return filesService.moveFiles(requestContext, moveFilesRequest);
      }
    });
  }

  @Override
  public Job deleteFiles(@RequestBody DeleteRequest deleteRequest) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, deleteRequest.getItemsToDelete(),
          RodaConstants.CONTROLLER_DETAILS_PARAM, deleteRequest.getDetails());
        controllerAssistant.checkObjectPermissions(requestContext.getUser(),
          CommonServicesUtils.convertSelectedItems(deleteRequest.getItemsToDelete(), IndexedFile.class));
        return filesService.deleteFiles(requestContext.getUser(), deleteRequest);
      }
    });
  }

  @Override
  public IndexedFile renameFolder(@RequestBody RenameFolderRequest renameFolderRequest) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<IndexedFile>() {
      @Override
      public IndexedFile process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_FILE_UUID_PARAM, renameFolderRequest.getFolderUUID(),
          RodaConstants.CONTROLLER_FOLDERNAME_PARAM, renameFolderRequest.getRenameTo(),
          RodaConstants.CONTROLLER_DETAILS_PARAM, renameFolderRequest.getDetails());
        IndexedFile folder = requestContext.getIndexService().retrieve(IndexedFile.class,
          renameFolderRequest.getFolderUUID(), RodaConstants.FILE_FIELDS_TO_RETURN);
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), folder);

        // delegate
        return filesService.renameFolder(requestContext, folder, renameFolderRequest.getRenameTo(),
          renameFolderRequest.getDetails());
      }
    });
  }

  @Override
  public Job identifyFileFormat(@RequestBody SelectedItemsRequest selected) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
        SelectedItems<IndexedFile> indexedFileSelectedItems = CommonServicesUtils.convertSelectedItems(selected,
          IndexedFile.class);
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedFileSelectedItems);

        // delegate
        return filesService.createFormatIdentificationJob(requestContext.getUser(), indexedFileSelectedItems);
      }
    });
  }

  @PostMapping(path = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Uploads a file under a representation", description = "Creates a new file resource under a representation", responses = {
    @ApiResponse(responseCode = "201", description = "File resource created", content = @Content(schema = @Schema(implementation = IndexedFile.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public File uploadFileResource(
    @Parameter(description = "The AIP identifier") @RequestParam(name = "aip-id") String aipId,
    @Parameter(description = "The Representation identifier") @RequestParam(name = "representation-id") String representationId,
    @Parameter(description = "The parent directory where the File will be placed") @RequestParam(name = "folder", required = false) List<String> directories,
    @Parameter(description = "Reason why upload the file") @RequestParam(name = "details", required = false) String details,
    @Parameter(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = MultipartFile.class)), description = "Multipart file") @RequestPart(value = "resource") MultipartFile resource) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<File>() {
      @Override
      public File process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        String fileName = resource.getOriginalFilename();
        controllerAssistant.setRelatedObjectId(aipId);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
          RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId,
          RodaConstants.CONTROLLER_DIRECTORY_PATH_PARAM, directories, RodaConstants.CONTROLLER_FILE_ID_PARAM, fileName,
          RodaConstants.CONTROLLER_DETAILS_PARAM, details);
        // delegate
        IndexedAIP aip = requestContext.getIndexService().retrieve(IndexedAIP.class, aipId,
          RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

        // check state
        controllerAssistant.checkAIPState(aip);

        // check if AIP is in a disposal confirmation
        controllerAssistant.checkIfAIPIsUnderADisposalPolicy(aip);

        // delegate
        try {
          Path file = Files.createTempFile("descriptive", ".tmp");
          Files.copy(resource.getInputStream(), file, StandardCopyOption.REPLACE_EXISTING);
          ContentPayload payload = new FSPathContentPayload(file);
          return filesService.createFile(requestContext, aipId, representationId, directories, fileName, payload,
            details);
        } catch (IOException e) {
          throw new RESTException(e);
        }
      }
    });
  }

  @Override
  public IndexedFile createFolderUnderRepresentation(@RequestBody CreateFolderRequest createFolderRequest) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<IndexedFile>() {
      @Override
      public IndexedFile process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_AIP_ID_PARAM, createFolderRequest.getAipId(),
          RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, createFolderRequest.getRepresentationId(),
          RodaConstants.CONTROLLER_FILE_UUID_PARAM, createFolderRequest.getFolderUUID(),
          RodaConstants.CONTROLLER_FOLDERNAME_PARAM, createFolderRequest.getName(),
          RodaConstants.CONTROLLER_DETAILS_PARAM, createFolderRequest.getDetails());
        IndexedRepresentation indexedRepresentation = requestContext.getIndexService().retrieve(
          IndexedRepresentation.class,
          IdUtils.getRepresentationId(createFolderRequest.getAipId(), createFolderRequest.getRepresentationId()),
          RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);

        controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedRepresentation);

        // delegate
        return filesService.createFolder(requestContext, indexedRepresentation, createFolderRequest);
      }
    });
  }

  @Override
  public List<String> retrieveFileRuleProperties() {
    return requestHandler.processRequestWithoutCheckRoles(new RequestHandler.RequestProcessor<List<String>>() {
      @Override
      public List<String> process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        return filesService.getConfigurationFileRules(requestContext.getUser());
      }
    });
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    // delegate
    return ApiUtils
      .okResponse(indexService.exportToCSV(requestContext.getUser(), findRequestString, IndexedFile.class));
  }

  @GetMapping(path = "/{uuid}/metadata/technical/{typeId}/html", produces = MediaType.TEXT_HTML_VALUE)
  @Operation(summary = "Retrieves technical metadata", description = "Retrieves the technical metadata with the visualization template applied and internationalized", responses = {
    @ApiResponse(responseCode = "200", description = "Returns an object ", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),})
  ResponseEntity<StreamingResponseBody> retrieveFileTechnicalMetadataHTML(
    @Parameter(description = "The file identifier", required = true) @PathVariable(name = "uuid") String fileUUID,
    @Parameter(description = "The technical metadata type identifier", required = true) @PathVariable(name = "typeId") String typeId,
    @Parameter(description = "The version identifier") @RequestParam(name = "versionId", required = false) String versionId,
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<ResponseEntity<StreamingResponseBody>>() {
      @Override
      public ResponseEntity<StreamingResponseBody> process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_FILE_ID_PARAM, fileUUID);
        IndexedFile file = requestContext.getIndexService().retrieve(IndexedFile.class, fileUUID, List.of());
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), file);
        return ApiUtils.okResponse(
          filesService.retrieveFileTechnicalMetadataHTML(requestContext, file, typeId, versionId, localeString));
      }
    });
  }

  @Override
  public TechnicalMetadataInfos retrieveTechnicalMetadataInfos(String fileUUID, String localeString) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<TechnicalMetadataInfos>() {
      @Override
      public TechnicalMetadataInfos process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_FILE_ID_PARAM, fileUUID);
        IndexedFile file = requestContext.getIndexService().retrieve(IndexedFile.class, fileUUID, List.of());
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), file);
        return filesService.retrieveFileTechnicalMetadataInfos(requestContext, file, localeString);
      }
    });
  }

  @GetMapping(path = "/{uuid}/metadata/technical/{typeId}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Download technical metadata file", description = "Download the techinal metadata file", responses = {
    @ApiResponse(responseCode = "200", description = "Returns an object ", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),})
  ResponseEntity<StreamingResponseBody> retrieveTechnicalMetadataFile(
    @Parameter(description = "The File identifier", required = true) @PathVariable(name = "uuid") String fileUUID,
    @Parameter(description = "The technical metadata type identifier", required = true) @PathVariable(name = "typeId") String typeId,
    @Parameter(description = "The version identifier") @RequestParam(name = "versionId", required = false) String versionId) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<ResponseEntity<StreamingResponseBody>>() {
      @Override
      public ResponseEntity<StreamingResponseBody> process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_FILE_ID_PARAM, fileUUID);
        // check object permissions
        IndexedFile indexedFile = requestContext.getIndexService().retrieve(IndexedFile.class, fileUUID,
          RodaConstants.FILE_FIELDS_TO_RETURN);

        controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedFile);

        // delegate
        StreamResponse streamResponse = filesService.retrieveFileTechnicalMetadata(requestContext, indexedFile, typeId,
          versionId);
        return ApiUtils.okResponse(streamResponse);
      }
    });
  }
}
