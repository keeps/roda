/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.services;

import org.fusesource.restygwt.client.DirectRestService;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHolds;
import org.roda.core.data.v2.disposal.metadata.DisposalHoldsAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalTransitiveHoldsAIPMetadata;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.ip.disposalhold.DisassociateDisposalHoldRequest;
import org.roda.core.data.v2.ip.disposalhold.UpdateDisposalHoldRequest;
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
 * @author Miguel Guimaraes <mguimarães@keep.pt>
 */

@Tag(name = "Disposal holds")
@RequestMapping(path = "../api/v2/disposal/holds")
public interface DisposalHoldRestService extends DirectRestService {

  @RequestMapping(method = RequestMethod.GET, path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List disposal holds", responses = {
    @ApiResponse(responseCode = "200", description = "Returns a list of disposal holds", content = @Content(schema = @Schema(implementation = DisposalHolds.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalHolds listDisposalHolds();

  @RequestMapping(method = RequestMethod.PUT, path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update disposal hold", description = "Update existing disposal hold", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UpdateDisposalHoldRequest.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalHold.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalHold updateDisposalHold(UpdateDisposalHoldRequest updateDisposalHoldRequest);

  @RequestMapping(method = RequestMethod.POST, path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create disposal hold", description = "Create a new disposal hold", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DisposalHold.class))), responses = {
    @ApiResponse(responseCode = "201", description = "Disposal hold created", content = @Content(schema = @Schema(implementation = DisposalHold.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalHold createDisposalHold(DisposalHold hold);

  @RequestMapping(method = RequestMethod.GET, path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get disposal hold", description = "Get disposal hold information", responses = {
    @ApiResponse(responseCode = "200", description = "Returns a disposal hold", content = @Content(schema = @Schema(implementation = DisposalHold.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalHold retrieveDisposalHold(
    @Parameter(description = "The ID of the disposal hold to retrieve.", required = true) @PathVariable(name = "id") String id);

  @RequestMapping(method = RequestMethod.POST, path = "/{id}/associate", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Apply disposal hold to selected AIPs", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItemsRequest.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class)))})
  Job applyDisposalHold(
    @Parameter(description = "Selected AIPs", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItemsRequest selectedItems,
    @Parameter(description = "Disposal hold id", required = true) @PathVariable(name = "id") String disposalHoldId,
    @Parameter(name = "override", description = "Lift all disposal holds associated and apply the selected disposal hold") @RequestParam(name = "override", required = false, defaultValue = "false") boolean override);

  @RequestMapping(method = RequestMethod.POST, path = "/{id}/lift", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Lift specific disposal hold", responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = DisposalHold.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Disposal hold not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job liftDisposalHold(
    @Parameter(description = "Disposal hold id", required = true) @PathVariable(name = "id") String id,
    @Parameter(description = "Outcome details", required = true) @RequestParam(name = "details", required = false, defaultValue = "") String details);

  @RequestMapping(method = RequestMethod.POST, path = "/{id}/disassociate", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Disassociate disposal hold from selected AIPs", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DisassociateDisposalHoldRequest.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job disassociateDisposalHold(DisassociateDisposalHoldRequest request,
    @Parameter(description = "Disposal hold id", required = false) @PathVariable(name = "id") String disposalHoldId);

  @RequestMapping(method = RequestMethod.GET, path = "/transitive/{aip-id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List transitive holds", responses = {
    @ApiResponse(responseCode = "200", description = "List of transitive disposal holds", content = @Content(schema = @Schema(implementation = DisposalTransitiveHoldsAIPMetadata.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalTransitiveHoldsAIPMetadata listTransitiveHolds(
    @Parameter(description = "AIP identifier", required = true) @PathVariable(name = "aip-id") String aipId);

  @RequestMapping(method = RequestMethod.GET, path = "/associations/{aip-id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List holds associations", responses = {
    @ApiResponse(responseCode = "200", description = "List of disposal holds associated to an AIP", content = @Content(schema = @Schema(implementation = DisposalHoldsAIPMetadata.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalHoldsAIPMetadata listDisposalHoldsAssociation(
    @Parameter(description = "AIP identifier", required = true) @PathVariable(name = "aip-id") String aipId);
}
