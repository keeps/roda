package org.roda.wui.client.services;

import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskMitigationProperties;
import org.roda.core.data.v2.risks.RiskMitigationTerms;
import org.roda.core.data.v2.risks.RiskVersions;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
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
 * @author Carlos Afonso <cafonso@keep.pt>
 */

@Tag(name = "v2 risks")
@RequestMapping(path = "../api/v2/risks")
public interface RiskRestService extends RODAEntityRestService<IndexedRisk> {
  @RequestMapping(path = "/delete", method = RequestMethod.POST)
  @Operation(summary = "Delete multiple risks via search query", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItemsRequest.class))), description = "Deletes one or more risks via search query", responses = {
    @ApiResponse(responseCode = "200", description = "Job created"),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),})
  Job deleteRisk(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItemsRequest selected);

  @RequestMapping(path = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update risk", description = "Update existing risk", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Risk.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Risk updated", content = @Content(schema = @Schema(implementation = Risk.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Risk updateRisk(Risk risk);

  @RequestMapping(path = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Creates a new risk", description = "Creates a new risk", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Risk.class))), responses = {
    @ApiResponse(responseCode = "201", description = "Risk created", content = @Content(schema = @Schema(implementation = Risk.class)))})
  Risk createRisk(Risk risk);

  @RequestMapping(path = "{id}/versions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves risk history", description = "Retrieves previous risk versions", responses = {
    @ApiResponse(responseCode = "200", description = "List of version of selected risk", content = @Content(schema = @Schema(implementation = RiskVersions.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  RiskVersions retrieveRiskVersions(
    @Parameter(description = "The identifier of the risk to fetch the versioning.", required = true) @PathVariable(name = "id") String id);

  @RequestMapping(path = "{id}/versions/{version-id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves a version of risk", description = "Retrieves a specific risk version", responses = {
    @ApiResponse(responseCode = "200", description = "Historical risk", content = @Content(schema = @Schema(implementation = Risk.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Risk retrieveRiskVersion(
    @Parameter(description = "The identifier of the risk to fetch the version.", required = true) @PathVariable(name = "id") String id,
    @Parameter(description = "The identifier of the version to fetch the risk.", required = true) @PathVariable(name = "version-id") String versionId);

  @RequestMapping(path = "{id}/versions/{version-id}/revert", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Reverts the risk to the an older version", description = "Reverts the risk to the an older version", responses = {
    @ApiResponse(responseCode = "200", description = "Reverted risk", content = @Content(schema = @Schema(implementation = Risk.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Risk revertRiskVersion(
    @Parameter(description = "The identifier of the risk to revert the version.", required = true) @PathVariable(name = "id") String id,
    @Parameter(description = "The identifier of the version to revert the risk.", required = true) @PathVariable(name = "version-id") String versionId);

  @RequestMapping(path = "{id}/versions/{version-id}/delete", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Deletes an older version of the risk", description = "Deletes an older version of the risk", responses = {
    @ApiResponse(responseCode = "204", description = "Historical version of risk deleted"),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteRiskVersion(
    @Parameter(description = "The identifier of the risk to revert the version.", required = true) @PathVariable(name = "id") String id,
    @Parameter(description = "The identifier of the version to revert the risk.", required = true) @PathVariable(name = "version-id") String versionId);

  @RequestMapping(path = "{id}/mitigation/terms", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves mitigation terms for the identified risk", description = "Retrieves the pre-mitigation and pos-mitigation probabilities and impact terms", responses = {
    @ApiResponse(responseCode = "200", description = "Mitigation terms", content = @Content(schema = @Schema(implementation = RiskMitigationTerms.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  RiskMitigationTerms retrieveRiskMitigationTerms(
    @Parameter(description = "The identifier of the risk to fetch the mitigation terms", required = true) @PathVariable(name = "id") String id);

  @RequestMapping(path = "/configuration/mitigation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves risk mitigation configurations", description = "Retrieves from the configuration the properties about mitigation probabilities and impact terms", responses = {
    @ApiResponse(responseCode = "200", description = "Risk mitigation properties", content = @Content(schema = @Schema(implementation = RiskMitigationProperties.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  RiskMitigationProperties retrieveRiskMitigationProperties();
}
