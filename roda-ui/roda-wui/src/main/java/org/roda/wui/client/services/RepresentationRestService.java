package org.roda.wui.client.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.generics.ChangeRepresentationStatesRequest;
import org.roda.core.data.v2.generics.RepresentationTypeOptions;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Tag(name = "v2 representations")
@RequestMapping(path = "../api/v2/representations")
public interface RepresentationRestService extends RODAEntityRestService<IndexedRepresentation> {

  @RequestMapping(path = "/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get representation", description = "Gets a representation", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Representation.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Representation getRepresentation(
    @PathVariable(name = RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @PathVariable(name = RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId);

  @RequestMapping(path = "/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create representation", description = "Creates a new representation on an AIP", responses = {
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = Representation.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Representation createRepresentation(
    @PathVariable(name = RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The type of the new representation") @RequestParam(name = RodaConstants.API_QUERY_PARAM_TYPE, defaultValue = "MIXED") String type,
    @Parameter(description = "Reason to create representation") @RequestParam(name = RodaConstants.API_QUERY_PARAM_DETAILS) String details);

  @RequestMapping(path = "/type-options", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get representation type options", description = "Gets the representation type options", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RepresentationTypeOptions.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  RepresentationTypeOptions getRepresentationTypeOptions(
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/change-type", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create representation", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), description = "Creates a new representation on an AIP", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job changeRepresentationType(
    @Parameter(name = "selectedRepresentations", description = "Selected representations", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<IndexedRepresentation> items,
    @Parameter(description = "The new type of the new representation") @RequestParam(name = RodaConstants.API_QUERY_PARAM_TYPE) String newType,
    @Parameter(description = "Reason to change representation type") @RequestParam(name = RodaConstants.API_QUERY_PARAM_DETAILS) String details);

  @RequestMapping(path = "", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete representation", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), description = "Deletes a new representation on an AIP", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job deleteRepresentation(
    @Parameter(name = "selectedRepresentations", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<IndexedRepresentation> items,
    @Parameter(description = "Reason to delete representation") @RequestParam(name = RodaConstants.API_QUERY_PARAM_DETAILS) String details);

  @RequestMapping(path = "/change-status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Change representation status", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = IndexedRepresentation.class))), description = "Changes representation status", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void changeRepresentationStatus(
    @Parameter(name = "Change representation status request", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) ChangeRepresentationStatesRequest changeRepresentationStatesRequest,
    @Parameter(description = "Reason to change representation status") @RequestParam(name = RodaConstants.API_QUERY_PARAM_DETAILS) String details);

  @RequestMapping(path = "/format-identification", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create format identification job", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), description = "Create format identification job", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job createFormatIdentificationJob(
    @Parameter(name = "selectedRepresentations", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<IndexedRepresentation> items);

}
