package org.roda.wui.client.services;

import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationCreateRequest;
import org.roda.core.data.v2.ri.RepresentationInformationFamily;
import org.roda.core.data.v2.ri.RepresentationInformationFamilyOptions;
import org.roda.core.data.v2.ri.RepresentationInformationFilterRequest;
import org.roda.core.data.v2.ri.RepresentationInformationRelationOptions;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
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
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Tag(name = "v2 representation information")
@RequestMapping(path = "../api/v2/representation-information")
public interface RepresentationInformationRestService extends RODAEntityRestService<RepresentationInformation> {

  @RequestMapping(path = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Creates a representation information", description = "Creates a representation information", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RepresentationInformationCreateRequest.class))), responses = {
    @ApiResponse(responseCode = "201", description = "Representation information created", content = @Content(schema = @Schema(implementation = RepresentationInformation.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  RepresentationInformation createRepresentationInformation(RepresentationInformationCreateRequest request);

  @RequestMapping(path = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Updates an existing representation information", description = "Updates an existing representation information", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RepresentationInformationCreateRequest.class))), responses = {
    @ApiResponse(responseCode = "201", description = "Representation information created", content = @Content(schema = @Schema(implementation = RepresentationInformation.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Representation information not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  RepresentationInformation updateRepresentationInformation(RepresentationInformationCreateRequest request);

  @RequestMapping(path = "/{id}/family", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves representation information family values", description = "Retrieves metadata values about the representation information type of family that can be customizable", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RepresentationInformationFamily.class))),
    @ApiResponse(responseCode = "401", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Representation information not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  RepresentationInformationFamily retrieveRepresentationInformationFamily(
    @Parameter(description = "The representation information identifier", required = true) @PathVariable(name = "id") String id,
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/configuration/families/{family-type}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves from the configuration the family metadata values", description = "Retrieves the metadata values from the configuration files according to the family type", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RepresentationInformationFamily.class))),
    @ApiResponse(responseCode = "401", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Family type not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  RepresentationInformationFamily retrieveRepresentationInformationFamilyConfigurations(
    @Parameter(description = "The type of family") @PathVariable(name = "family-type") String familyType,
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/configuration/families/options", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves from the configuration the options for the representation information family", description = "Retrieve the representation information family possible values and internationalize the results", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RepresentationInformationFamilyOptions.class))),
    @ApiResponse(responseCode = "401", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  RepresentationInformationFamilyOptions retrieveRepresentationInformationFamilyOptions(
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/configuration/relations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves from the configuration the internationalization of configurable relation types", description = "Retrieves all information about the configured relation types properly internationalized", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RepresentationInformationRelationOptions.class))),
    @ApiResponse(responseCode = "401", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  RepresentationInformationRelationOptions retrieveRepresentationInformationRelationOptions(
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete multiple representation information via search query", description = "Deletes one or more representation information", responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class)))})
  Job deleteMultipleRepresentationInformation(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<RepresentationInformation> selected);

  @RequestMapping(path = "/filter", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Add a Filter to a set of selected representation information", requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = RepresentationInformationFilterRequest.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class)))})
  Job addFilterToRepresentationInformation(
    @Parameter(name = "request", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RepresentationInformationFilterRequest.class))) RepresentationInformationFilterRequest request);
}
