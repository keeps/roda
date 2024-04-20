package org.roda.wui.client.services;

import org.fusesource.restygwt.client.DirectRestService;
import org.roda.core.data.v2.disposal.metadata.DisposalHoldsAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalTransitiveHoldsAIPMetadata;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHolds;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
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

@Tag(name = "v2 disposal holds")
@RequestMapping(path = "../api/v2/disposal/holds")
public interface DisposalHoldRestService extends DirectRestService {

  @RequestMapping(method = RequestMethod.GET, path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List disposal holds", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalHolds.class)))})
  DisposalHolds listDisposalHolds();

  @RequestMapping(method = RequestMethod.PUT, path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update disposal hold", description = "Update existing disposal hold", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DisposalHold.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalHold.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalHold updateDisposalHold(DisposalHold hold);

  @RequestMapping(method = RequestMethod.POST, path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create disposal hold", description = "Create a new disposal hold", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DisposalHold.class))), responses = {
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = DisposalHold.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalHold createDisposalHold(DisposalHold hold);

  @RequestMapping(method = RequestMethod.GET, path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get disposal hold", description = "Get disposal hold information", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalHold.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalHold retrieveDisposalHold(
    @Parameter(description = "The ID of the disposal hold to retrieve.", required = true) @PathVariable(name = "id") String id);

  @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete disposal hold", description = "Delete disposal hold", responses = {
    @ApiResponse(responseCode = "204", description = "Resource deleted"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteDisposalHold(
    @Parameter(description = "The ID of the disposal hold to delete.", required = true) @PathVariable("id") String disposalHoldId);

  @RequestMapping(method = RequestMethod.POST, path = "/apply", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Apply disposal hold to selected AIPs", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class)))})
  Job applyDisposalHold(
    @Parameter(description = "Selected AIPs", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<IndexedAIP> selectedItems,
    @Parameter(description = "Disposal hold id", required = true) @RequestParam(name = "disposal-hold-id") String disposalHoldId,
    @Parameter(name = "override", description = "Lift all disposal holds associated and apply the selected disposal hold") @RequestParam(name = "override", required = false, defaultValue = "false") boolean override);

  @RequestMapping(method = RequestMethod.POST, path = "/lift", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Lift disposal holds from selected AIPs", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class)))})
  Job liftDisposalHoldBySelectedItems(
    @Parameter(description = "Selected AIPs", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<IndexedAIP> selectedItems,
    @Parameter(description = "disposal hold id", required = true) @RequestParam(name = "disposal-hold-id") String disposalHoldId);

  @RequestMapping(method = RequestMethod.PUT, path = "/lift/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Lift specific disposal hold", responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = DisposalHold.class))),
    @ApiResponse(responseCode = "404", description = "Disposal hold not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalHold liftDisposalHold(
    @Parameter(description = "Disposal hold id", required = true) @PathVariable(name = "id") String id);

  @RequestMapping(method = RequestMethod.POST, path = "/disassociate", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Disassociate disposal hold from selected AIPs", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class)))})
  Job disassociateDisposalHold(
    @Parameter(description = "Selected AIPs", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<IndexedAIP> selectedItems,
    @Parameter(description = "Disposal hold id") @RequestParam(name = "disposal-hold-id", required = false) String disposalHoldId,
    @Parameter(name = "clear", description = "Disassociate all disposal holds associated to AIP") @RequestParam(name = "clear", required = false, defaultValue = "false") boolean clear);

  @RequestMapping(method = RequestMethod.GET, path = "/transitive/{aip-id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List transitive holds", responses = {
    @ApiResponse(responseCode = "200", description = "List of transitive disposal holds", content = @Content(schema = @Schema(implementation = DisposalTransitiveHoldsAIPMetadata.class)))})
  DisposalTransitiveHoldsAIPMetadata listTransitiveHolds(
    @Parameter(description = "AIP identifier", required = true) @PathVariable(name = "aip-id") String aipId);

  @RequestMapping(method = RequestMethod.GET, path = "/associate/{aip-id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List holds associations", responses = {
    @ApiResponse(responseCode = "200", description = "List of disposal holds associated to an AIP", content = @Content(schema = @Schema(implementation = DisposalHoldsAIPMetadata.class)))})
  DisposalHoldsAIPMetadata listDisposalHoldsAssociation(
    @Parameter(description = "AIP identifier", required = true) @PathVariable(name = "aip-id") String aipId);
}
