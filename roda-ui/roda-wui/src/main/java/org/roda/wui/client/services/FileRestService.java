/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.services;

import java.util.List;

import org.roda.core.data.v2.file.CreateFolderRequest;
import org.roda.core.data.v2.file.MoveFilesRequest;
import org.roda.core.data.v2.file.RenameFolderRequest;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.IndexedFileRequest;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.TechnicalMetadataInfo;
import org.roda.core.data.v2.ip.metadata.TechnicalMetadataInfos;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
@Tag(name = "Files")
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
  @Operation(summary = "Creates an internal action to delete file(s) from the representation", responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job deleteFiles(
    @Parameter(description = "Selected disposal confirmations", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) DeleteRequest deleteRequest);

  @RequestMapping(path = "/rename", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Rename folder", description = "Renames a folder", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RenameFolderRequest.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Folder renamed", content = @Content(schema = @Schema(implementation = IndexedFile.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  IndexedFile renameFolder(RenameFolderRequest renameFolderRequest);

  @RequestMapping(path = "/format-identification", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Creates a preservation action to identify the file formats via search query", description = "Identifies the file format for a set of Files", responses = {
    @ApiResponse(responseCode = "200", description = "Created job to identify the format associated to the files", content = @Content(schema = @Schema(implementation = Job.class))),})
  Job identifyFileFormat(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItemsRequest selected);

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

  @RequestMapping(method = RequestMethod.GET, path = "/{fileUUID}/metadata/technical/information", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the file's technical metadata ids and labels", responses = {
    @ApiResponse(responseCode = "200", description = "Information related to file's technical metadata", content = @Content(schema = @Schema(implementation = TechnicalMetadataInfo.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  TechnicalMetadataInfos retrieveTechnicalMetadataInfos(
    @Parameter(description = "The file identifier", required = true) @PathVariable(name = "fileUUID") String fileUUID,
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(method = RequestMethod.GET, path = "/{fileUUID}/other_metadata/{metadata_type}/{metadata_file_suffix}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Gets other metadata (JSON info or ZIP file).\nOptional query params of **start** and **limit** defined the returned query", responses = {
    @ApiResponse(responseCode = "200", description = "Other metadata file", content = @Content(schema = @Schema(implementation = OtherMetadata.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  ResponseEntity<StreamingResponseBody> getOtherMetadata(
    @Parameter(description = "The UUID of the existing File", required = true) @PathVariable(name = "fileUUID") String fileUUID,
    @Parameter(description = "The type of the other metadata", required = true) @PathVariable(name = "metadata_type") String metadataType,
    @Parameter(description = "The file suffix of the other metadata", required = true) @PathVariable(name = "metadata_file_suffix") String metadataFileSuffix);
}