package org.roda.wui.client.services;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.IndexedRepresentationRequest;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.representation.ChangeRepresentationStatesRequest;
import org.roda.core.data.v2.representation.ChangeTypeRequest;
import org.roda.core.data.v2.representation.RepresentationTypeOptions;
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
@Tag(name = "Representations")
@RequestMapping(path = "../api/v2/representations")
public interface RepresentationRestService extends RODAEntityRestService<IndexedRepresentation> {

  @RequestMapping(method = RequestMethod.POST, path = "/find-via-request", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves a representation using a dedicated request", requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = IndexedRepresentationRequest.class))), responses = {
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = IndexedFile.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "File not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  IndexedRepresentation retrieveIndexedRepresentationViaRequest(IndexedRepresentationRequest request);

  @RequestMapping(path = "/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{"
    + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID
    + "}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get representation", description = "Gets a representation", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Representation.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Representation getRepresentation(@PathVariable(name = RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @PathVariable(name = RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId);

  @RequestMapping(path = "/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create representation", description = "Creates a new representation on an AIP", responses = {
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = Representation.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Representation createRepresentation(@PathVariable(name = "id") String aipId,
    @Parameter(description = "The type of the new representation") @RequestParam(name = RodaConstants.API_QUERY_PARAM_TYPE, defaultValue = "MIXED") String type,
    @Parameter(description = "Reason to create representation") @RequestParam(name = RodaConstants.API_QUERY_PARAM_DETAILS) String details);

  @RequestMapping(path = "/configuration/types", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the types associated to representations", description = "Returns a list of controlled vocabulary from RODA configuration or list of representation types available", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RepresentationTypeOptions.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  RepresentationTypeOptions getRepresentationTypeOptions(
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/type", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Changes the representation type via search query", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangeTypeRequest.class))), description = "", responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job changeRepresentationType(ChangeTypeRequest request);

  @RequestMapping(path = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete representation", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItemsRequest.class))), description = "Deletes a new representation on an AIP", responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job deleteRepresentation(
    @Parameter(name = "selectedRepresentations", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItemsRequest items,
    @Parameter(description = "Reason to delete representation") @RequestParam(name = RodaConstants.API_QUERY_PARAM_DETAILS) String details);

  @RequestMapping(path = "status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Changes the representation status", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangeRepresentationStatesRequest.class))), description = "Changes representation status", responses = {
    @ApiResponse(responseCode = "200", description = "Representation status changed"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job changeRepresentationStatus(ChangeRepresentationStatesRequest changeRepresentationStatesRequest);

  @RequestMapping(path = "/identify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Creates a preservation action to identify the file formats via search query", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItemsRequest.class))), description = "Identifies the file format for a set of files within the representation", responses = {
    @ApiResponse(responseCode = "200", description = "Created job to identify the format associated to the files within the representation", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job createFormatIdentificationJob(
    @Parameter(name = "selectedRepresentations", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItemsRequest items);

  @RequestMapping(method = RequestMethod.GET, path = "/configuration/rules", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves from the configuration the rules for the Representation information", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = List.class))),
    @ApiResponse(responseCode = "401", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  List<String> retrieveRepresentationRuleProperties();
}
