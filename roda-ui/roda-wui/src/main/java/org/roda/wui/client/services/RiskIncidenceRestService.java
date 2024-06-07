package org.roda.wui.client.services;

import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.api.incidences.UpdateRiskIncidences;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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

@Tag(name = "v2 incidences")
@RequestMapping(path = "../api/v2/incidences")
public interface RiskIncidenceRestService extends RODAEntityRestService<RiskIncidence> {

  @RequestMapping(path = "/delete", method = RequestMethod.POST)
  @Operation(summary = "Deletes multiple incidences via search query", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DeleteRequest.class))), description = "Deletes one or more incidence", responses = {
    @ApiResponse(responseCode = "200", description = "Job created"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job deleteRiskIncidences(DeleteRequest request);

  @RequestMapping(path = "/update", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Updates multiple risk incidences via search query", description = "Updates multiple risk incidences", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UpdateRiskIncidences.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job updateMultipleIncidences(UpdateRiskIncidences selected);

  @RequestMapping(path = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Updates a single risk incidence", description = "Updates an existing incidence", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RiskIncidence.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RiskIncidence.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  RiskIncidence updateRiskIncidence(
    @Parameter(name = "selected", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) RiskIncidence selected);

}
