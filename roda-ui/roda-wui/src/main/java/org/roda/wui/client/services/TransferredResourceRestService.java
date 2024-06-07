package org.roda.wui.client.services;

import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.TransferredResources;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Tag(name = "v2 transfers")
@RequestMapping(path = "../api/v2/transfers")
public interface TransferredResourceRestService extends RODAEntityRestService<TransferredResource> {

  @RequestMapping(path = "/selected", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List selected transferred resources", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItemsRequest.class))), description = "Gets a list of transferred resources", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelectedItemsRequest.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  TransferredResources getSelectedTransferredResources(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItemsRequest selected);

  @RequestMapping(path = "/move", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Move transferred resources", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItemsRequest.class))), description = "Gets a list of transferred resources", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job moveTransferredResources(
    @Parameter(name = "moveTransferResource", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItemsRequest items,
    @Parameter(description = "The transferred resource uuid") @RequestParam(name = "uuid", required = false) String uuid);

  @RequestMapping(path = "/{uuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get transferred resource", description = "Gets a particular transferred resource", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TransferredResource.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  TransferredResource getResource(
    @Parameter(description = "The transferred resource uuid") @PathVariable(name = "uuid") String uuid);

  @RequestMapping(path = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete multiple transferred resource via search query", description = "Deletes one or more transferred resources", responses = {
    @ApiResponse(responseCode = "200", description = "Created job with the internal action to delete transferred resources", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job deleteMultipleResources(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItemsRequest selected);

  @RequestMapping(path = "/rename", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Rename transferred resource", description = "Renames a transferred resource", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TransferredResource.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  TransferredResource renameTransferredResource(
    @Parameter(description = "The transferred resource uuid") @RequestParam(name = "uuid") String resourceId,
    @Parameter(description = "The new name") @RequestParam(name = "new-name") String newName,
    @Parameter(description = "Replacing existent file") @RequestParam(name = "replace-existing") Boolean replaceExisting);

  @RequestMapping(path = "/refresh", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Refreshes transferred resource", description = "Refreshes transferred resources", responses = {
    @ApiResponse(responseCode = "204", description = "No Content")})
  Void refreshTransferResource(
    @Parameter(description = "Transfer resource relative path") @RequestParam(name = "path", required = false) String transferredResourceRelativePath);

  @RequestMapping(path = "/reindex", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Reindex transferred resource", description = "Reindex a particular transferred resource", responses = {
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "409", description = "Still updating", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  TransferredResource reindexResources(
    @Parameter(description = "The path of the resource") @RequestParam(name = "path") String path);

  @RequestMapping(path = "/create/folder", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create transferred resource folder", description = "Creates a new transferred resources folder", responses = {
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = TransferredResource.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  TransferredResource createTransferredResourcesFolder(
    @Parameter(description = "The id of the parent") @RequestParam(name = "parent-uuid", required = false) String parentUUID,
    @Parameter(description = "The name of the directory to create") @RequestParam(name = "name") String folderName,
    @Parameter(description = "Commit after creation", content = @Content(schema = @Schema(defaultValue = "false", implementation = Boolean.class))) @RequestParam(name = "commit") boolean commit);
}
