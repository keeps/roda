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
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.controllers.BrowserHelper;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.FilesService;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.FileRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class FilesController implements FileRestService {
  @Autowired
  private HttpServletRequest request;

  @Autowired
  private FilesService filesService;

  @Autowired
  private IndexService indexService;

  @RequestMapping(path = "{uuid}/preview", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Previews a file", description = "Previews a particular file using streaming capabilities", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StreamingResponseBody.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> previewBinary(
    @Parameter(description = "The UUID of the existing file", required = true) @PathVariable(name = "uuid") String fileUUID,
    @RequestHeader HttpHeaders headers) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      List<String> fileFields = new ArrayList<>(RodaConstants.FILE_FIELDS_TO_RETURN);
      fileFields.add(RodaConstants.FILE_ISDIRECTORY);
      IndexedFile file = indexService.retrieve(requestContext, IndexedFile.class, fileUUID, fileFields);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), file);

      StreamResponse response = filesService.retrieveAIPRepresentationFile(file);

      return ApiUtils.rangeResponse(headers, response.getStream());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (NotFoundException | GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, fileUUID, state, RodaConstants.CONTROLLER_FILE_UUID_PARAM,
        fileUUID);
    }
  }

  @RequestMapping(path = "{uuid}/download", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads file", description = "Download a particular file", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StreamingResponseBody.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadBinary(
    @Parameter(description = "The UUID of the existing file", required = true) @PathVariable(name = "uuid") String fileUUID) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      List<String> fileFields = new ArrayList<>(RodaConstants.FILE_FIELDS_TO_RETURN);
      fileFields.add(RodaConstants.FILE_ISDIRECTORY);
      IndexedFile file = indexService.retrieve(requestContext, IndexedFile.class, fileUUID, fileFields);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), file);

      StreamResponse response = filesService.retrieveAIPRepresentationFile(file);

      return ApiUtils.okResponse(response);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (NotFoundException | GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, fileUUID, state, RodaConstants.CONTROLLER_FILE_UUID_PARAM,
        fileUUID);
    }
  }

  @Override
  public IndexedFile findByUuid(String uuid, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.retrieve(requestContext, IndexedFile.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<IndexedFile> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.find(IndexedFile.class, findRequest, localeString, requestContext);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_FILE)) {
      return new LongResponse(indexService.count(IndexedFile.class, countRequest, requestContext));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(@RequestBody SuggestRequest suggestRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.suggest(suggestRequest, IndexedFile.class, requestContext);
  }

  @Override
  public IndexedFile retrieveIndexedFileViaRequest(@RequestBody IndexedFileRequest indexedFileRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    String uuid = IdUtils.getFileId(indexedFileRequest.getAipId(), indexedFileRequest.getRepresentationId(),
      indexedFileRequest.getDirectoryPaths(), indexedFileRequest.getFileId());
    IndexedFile retrieve = indexService.retrieve(requestContext, IndexedFile.class, uuid, new ArrayList<>());

    if (retrieve.isReference()) {
      retrieve.setAvailable(filesService.isShallowFileAvailable(retrieve));
    } else {
      retrieve.setAvailable(true);
    }

    RodaConstants.DistributedModeType distributedModeType = RodaCoreFactory.getDistributedModeType();

    if (RODAInstanceUtils.isConfiguredAsDistributedMode()
      && RodaConstants.DistributedModeType.CENTRAL.equals(distributedModeType)) {
      boolean isLocalInstance = retrieve.getInstanceId().equals(RODAInstanceUtils.getLocalInstanceIdentifier());
      filesService.retrieveDistributedInstanceName(retrieve.getInstanceId(), isLocalInstance)
        .ifPresent(retrieve::setInstanceName);
      retrieve.setLocalInstance(isLocalInstance);
    }

    return retrieve;
  }

  @Override
  public Job moveFileToFolder(@RequestBody MoveFilesRequest moveFilesRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), moveFilesRequest.getItemsToMove());

      IndexedAIP destinationAIP = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class,
        moveFilesRequest.getAipId(), RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), destinationAIP);

      // delegate
      return filesService.moveFiles(requestContext.getUser(), moveFilesRequest);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, moveFilesRequest.getAipId(), state,
        RodaConstants.CONTROLLER_AIP_ID_PARAM, moveFilesRequest.getAipId(),
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, moveFilesRequest.getRepresentationId(),
        RodaConstants.CONTROLLER_FILES_PARAM, moveFilesRequest.getItemsToMove(), RodaConstants.CONTROLLER_FILE_PARAM,
        moveFilesRequest.getFileUUIDtoMove(), RodaConstants.CONTROLLER_DETAILS_PARAM, moveFilesRequest.getDetails());
    }
  }

  @Override
  public Job deleteFiles(@RequestBody DeleteRequest deleteRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      controllerAssistant.checkObjectPermissions(requestContext.getUser(),
        CommonServicesUtils.convertSelectedItems(deleteRequest.getItemsToDelete(), IndexedFile.class));

      return filesService.deleteFiles(requestContext.getUser(), deleteRequest);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        deleteRequest.getItemsToDelete(), RodaConstants.CONTROLLER_DETAILS_PARAM, deleteRequest.getDetails());
    }
  }

  @Override
  public IndexedFile renameFolder(@RequestBody RenameFolderRequest renameFolderRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      IndexedFile folder = RodaCoreFactory.getIndexService().retrieve(IndexedFile.class,
        renameFolderRequest.getFolderUUID(), RodaConstants.FILE_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), folder);

      // delegate
      return filesService.renameFolder(requestContext.getUser(), folder, renameFolderRequest.getRenameTo(),
        renameFolderRequest.getDetails());

    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | AlreadyExistsException | NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_FILE_UUID_PARAM,
        renameFolderRequest.getFolderUUID(), RodaConstants.CONTROLLER_FOLDERNAME_PARAM,
        renameFolderRequest.getRenameTo(), RodaConstants.CONTROLLER_DETAILS_PARAM, renameFolderRequest.getDetails());
    }
  }

  @Override
  public Job identifyFileFormat(@RequestBody SelectedItemsRequest selected) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      SelectedItems<IndexedFile> indexedFileSelectedItems = CommonServicesUtils.convertSelectedItems(selected,
        IndexedFile.class);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedFileSelectedItems);

      // delegate
      return filesService.createFormatIdentificationJob(requestContext.getUser(), indexedFileSelectedItems);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
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
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    String fileName = resource.getOriginalFilename();

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), aip);

      // check state
      controllerAssistant.checkAIPstate(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPInConfirmation(aip);

      // delegate
      Path file = Files.createTempFile("descriptive", ".tmp");
      Files.copy(resource.getInputStream(), file, StandardCopyOption.REPLACE_EXISTING);
      ContentPayload payload = new FSPathContentPayload(file);

      return filesService.createFile(requestContext.getUser(), aipId, representationId, directories, fileName, payload,
        details);
    } catch (RODAException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId,
        RodaConstants.CONTROLLER_DIRECTORY_PATH_PARAM, directories, RodaConstants.CONTROLLER_FILE_ID_PARAM, fileName,
        RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  @Override
  public IndexedFile createFolderUnderRepresentation(@RequestBody CreateFolderRequest createFolderRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      IndexedRepresentation indexedRepresentation = RodaCoreFactory.getIndexService().retrieve(
        IndexedRepresentation.class,
        IdUtils.getRepresentationId(createFolderRequest.getAipId(), createFolderRequest.getRepresentationId()),
        RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);

      controllerAssistant.checkObjectPermissions(requestContext.getUser(), indexedRepresentation);

      // delegate
      return filesService.createFolder(requestContext.getUser(), indexedRepresentation, createFolderRequest);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | GenericException | NotFoundException | AlreadyExistsException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_AIP_ID_PARAM,
        createFolderRequest.getAipId(), RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM,
        createFolderRequest.getRepresentationId(), RodaConstants.CONTROLLER_FILE_UUID_PARAM,
        createFolderRequest.getFolderUUID(), RodaConstants.CONTROLLER_FOLDERNAME_PARAM, createFolderRequest.getName(),
        RodaConstants.CONTROLLER_DETAILS_PARAM, createFolderRequest.getDetails());
    }
  }

  @Override
  public List<String> retrieveFileRuleProperties() {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    try {
      // delegate
      return filesService.getConfigurationFileRules(requestContext.getUser());
    } finally {
      controllerAssistant.registerAction(requestContext, LogEntryState.SUCCESS);
    }
  }
}
