package org.roda.wui.client.services;

import java.util.List;

import org.roda.core.data.v2.file.CreateFolderRequest;
import org.roda.core.data.v2.file.MoveFilesRequest;
import org.roda.core.data.v2.file.RenameFolderRequest;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.index.IndexedFileRequest;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Tag(name = "v2 files")
@RequestMapping(path = "../api/v2/files")
public interface FileRestService extends RODAEntityRestService<IndexedFile> {

  @RequestMapping(method = RequestMethod.POST, path = "/find-via-request", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves a file using a dedicated request", requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = IndexedFileRequest.class))), responses = {
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = IndexedFile.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "File not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  IndexedFile retrieveIndexedFileViaRequest(IndexedFileRequest request);

  @RequestMapping(path = "/move", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create an internal action to move file(s) to another folder within the representation", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MoveFilesRequest.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job moveFileToFolder(MoveFilesRequest moveFilesRequest);

  @RequestMapping(path = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Creates an internal action to delete file(s) from the representation", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DeleteRequest.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job deleteFiles(DeleteRequest<IndexedFile> deleteRequest);

  @RequestMapping(path = "/rename", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Rename folder", description = "Renames a folder", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RenameFolderRequest.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Folder renamed", content = @Content(schema = @Schema(implementation = IndexedFile.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  IndexedFile renameFolder(RenameFolderRequest renameFolderRequest);

  @RequestMapping(path = "/identify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Creates a preservation action to identify the file formats via search query", description = "Identifies the file format for a set of Files", responses = {
    @ApiResponse(responseCode = "200", description = "Created job to identify the format associated to the files", content = @Content(schema = @Schema(implementation = Job.class))),})
  Job identifyFileFormat(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<IndexedFile> selected);

  @RequestMapping(method = RequestMethod.POST, path = "/create/folder", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Creates a new folder under a representation", requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CreateFolderRequest.class))), responses = {
    @ApiResponse(responseCode = "201", description = "Folder created", content = @Content(schema = @Schema(implementation = IndexedFile.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "AIP not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "409", description = "Resource already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  IndexedFile createFolderUnderRepresentation(CreateFolderRequest createFolderRequest);

  @RequestMapping(method = RequestMethod.GET, path = "/configuration/rules", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves from the configuration the rules for the Representation information", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = List.class))),
    @ApiResponse(responseCode = "401", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  List<String> retrieveFileRuleProperties();
}