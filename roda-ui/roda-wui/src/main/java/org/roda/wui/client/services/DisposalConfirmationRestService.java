package org.roda.wui.client.services;

import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationCreateRequest;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationForm;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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

@Tag(name = "v2 disposal confirmations")
@RequestMapping(path = "../api/v2/disposal/confirmations")
public interface DisposalConfirmationRestService extends RODAEntityRestService<DisposalConfirmation> {

  @RequestMapping(method = RequestMethod.POST, path = "/destroy", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Destroys the records in the disposal confirmations", description = "Creates an internal job to destroy the records in the disposal confirmations", responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job destroyRecordsInDisposalConfirmation(
    @Parameter(description = "Selected disposal confirmations", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItemsRequest selectedItems);

  @RequestMapping(method = RequestMethod.POST, path = "/permanent", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Permanently deletes the records in the disposal confirmations", description = "Creates an internal job to permanently delete the records in the disposal confirmations", responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job permanentlyDeleteRecordsInDisposalConfirmation(SelectedItemsRequest selectedItems);

  @RequestMapping(method = RequestMethod.POST, path = "/restore", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Restores disposal confirmations", description = "Creates an internal job to restore the disposal confirmations", responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job restoreDisposalConfirmation(
    @Parameter(description = "Selected disposal confirmations", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItemsRequest selectedItems);

  @RequestMapping(method = RequestMethod.POST, path = "/recover", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Recovers disposal confirmations", description = "Creates an internal job to recover the disposal confirmations", responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job recoverDisposalConfirmation(
    @Parameter(description = "Selected disposal confirmations", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItemsRequest selectedItems);

  @RequestMapping(method = RequestMethod.POST, path = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Deletes disposal rule", description = "Creates an internal job to delete the disposal confirmations", responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job deleteDisposalConfirmation(
    @Parameter(description = "Selected disposal confirmations", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItemsRequest selectedItems,
    @Parameter(description = "Reason for deletion") @RequestParam(name = "reason", required = false) String reason);

  @RequestMapping(method = RequestMethod.POST, path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Creates a disposal confirmation", requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DisposalConfirmationCreateRequest.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job createDisposalConfirmation(DisposalConfirmationCreateRequest createRequest);

  @RequestMapping(path = "/configurations/form", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves from the configuration the custom disposal confirmation form", description = "Retrieves the metadata values from the configuration files according to configured form", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalConfirmationForm.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalConfirmationForm retrieveDisposalConfirmationForm();
}