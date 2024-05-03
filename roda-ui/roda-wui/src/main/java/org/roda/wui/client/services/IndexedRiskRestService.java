package org.roda.wui.client.services;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
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
 * @author Carlos Afonso <cafonso@keep.pt>
 */

@Tag(name = "v2 risks")
@RequestMapping(path = "../api/v2/risks")
public interface IndexedRiskRestService extends RODAEntityRestService<IndexedRisk> {
  @RequestMapping(path = "/delete", method = RequestMethod.POST)
  @Operation(summary = "Delete multiple risks via search query", requestBody = @RequestBody(required = true, content = @Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), description = "Deletes one or more risks", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job deleteRisk(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<IndexedRisk> selected);

  @RequestMapping(path = "/refresh", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Refreshes transferred resource", description = "Refreshes transferred resources", responses = {
    @ApiResponse(responseCode = "204", description = "No Content")})
  Void refreshRisk();

  @RequestMapping(method = RequestMethod.GET, path = "/{id}/has-version", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Check if risk has version", responses = {
    @ApiResponse(responseCode = "200", description = "Returns if risk has version", content = @Content(schema = @Schema(implementation = Boolean.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Boolean hasRiskVersions(
    @Parameter(description = "The ID of the disposal confirmation", required = true) @PathVariable("id") String disposalConfirmationId);

  @RequestMapping(path = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update risk", description = "Update existing risk", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Risk.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Risk.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Risk updateRisk(
    @Parameter(name = "selected", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) Risk selected,
    @RequestParam(name = RodaConstants.RISK_INCIDENCES_COUNT) String incidencesCount);

}
