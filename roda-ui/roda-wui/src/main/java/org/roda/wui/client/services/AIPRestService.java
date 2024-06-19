package org.roda.wui.client.services;

import java.util.List;

import org.roda.core.data.v2.aip.AssessmentRequest;
import org.roda.core.data.v2.aip.MoveRequest;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.generics.UpdatePermissionsRequest;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.ConfiguredDescriptiveMetadataList;
import org.roda.core.data.v2.ip.metadata.CreateDescriptiveMetadataRequest;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataPreview;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataPreviewRequest;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataVersions;
import org.roda.core.data.v2.ip.metadata.SupportedMetadataValue;
import org.roda.core.data.v2.ip.metadata.TypeOptionsInfo;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.representation.ChangeTypeRequest;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.model.GenericOkResponse;
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
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Tag(name = "v2 aips")
@RequestMapping(path = "../api/v2/aips")
public interface AIPRestService extends RODAEntityRestService<IndexedAIP> {

  @RequestMapping(path = "/delete", method = RequestMethod.POST)
  @Operation(summary = "Delete multiple AIPs via search query", description = "Creates an internal action to delete the selected AIPs", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DeleteRequest.class))), responses = {
      @ApiResponse(responseCode = "200", description = "Job created"),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job deleteAIPs(DeleteRequest deleteRequest);

  @RequestMapping(method = RequestMethod.GET, path = "/configuration/rules", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves from the configuration the rules for the Representation information", responses = {
      @ApiResponse(responseCode = "200", description = "List of rules", content = @Content(schema = @Schema(implementation = List.class))),
      @ApiResponse(responseCode = "401", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  List<String> retrieveAIPRuleProperties();

  @RequestMapping(path = "/{id}/metadata/descriptive/information", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get descriptive metadata", description = "Get descriptive metadata related to the aip", responses = {
      @ApiResponse(responseCode = "200", description = "Information related to AIP descriptive metadata", content = @Content(schema = @Schema(implementation = DescriptiveMetadataInfos.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DescriptiveMetadataInfos getDescriptiveMetadata(
      @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
      @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/{id}/metadata/descriptive", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Creates a descriptive metadata under the AIP", description = "Create AIP descriptive metadata", requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateDescriptiveMetadataRequest.class))), responses = {
      @ApiResponse(responseCode = "201", description = "Descriptive metadata created", content = @Content(schema = @Schema(implementation = DescriptiveMetadata.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DescriptiveMetadata createAIPDescriptiveMetadata(@PathVariable(name = "id") String aipId,
                                                   CreateDescriptiveMetadataRequest selected);

  @RequestMapping(path = "/{id}/metadata/descriptive/{descriptive-metadata-id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete metadata file", description = "Delete metadata file related to AIP", responses = {
      @ApiResponse(responseCode = "204", description = "No Content"),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteDescriptiveMetadataFile(
      @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
      @Parameter(description = "The descriptive metadata identifier") @PathVariable(name = "descriptive-metadata-id") String metadataId);

  @RequestMapping(path = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Creates an AIP", description = "Creates a new AIP", responses = {
      @ApiResponse(responseCode = "201", description = "Returns the new AIP", content = @Content(schema = @Schema(implementation = AIP.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  AIP createAIP(
      @Parameter(description = "The parent AIP identifier") @RequestParam(name = "parent-aip-id", required = false) String parentAipId,
      @Parameter(description = "Type of AIP") @RequestParam(name = "type", defaultValue = "MIXED") String aipType);

  @RequestMapping(path = "/{id}/ancestors", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves AIP ancestors", description = "Returns a list of AIPs", responses = {
      @ApiResponse(responseCode = "200", description = "List of AIPs", content = @Content(schema = @Schema(implementation = List.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  List<IndexedAIP> getAncestors(
      @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String id);

  @RequestMapping(path = "/configuration/descriptive/metadata", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Gets the descriptive metadata configured", description = "Get AIP supported metadata", responses = {
      @ApiResponse(responseCode = "200", description = "Supported Metadata", content = @Content(schema = @Schema(implementation = ConfiguredDescriptiveMetadataList.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  ConfiguredDescriptiveMetadataList retrieveSupportedMetadataTypes(
      @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/configuration/types", method = RequestMethod.GET)
  @Operation(summary = "Retrieves the types associated to AIP", description = "Returns a list of controlled vocabulary from RODA configuration or list of AIP types available", responses = {
      @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TypeOptionsInfo.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  TypeOptionsInfo getTypeOptions(
      @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/type", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Changes the AIP type via search query", responses = {
      @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job changeAIPType(ChangeTypeRequest request);

  @RequestMapping(path = "/move", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Move AIP in the hierarchy", description = "Moves an AIP in the hierarchy", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MoveRequest.class))), responses = {
      @ApiResponse(responseCode = "200", description = "Job created"),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job moveAIPInHierarchy(MoveRequest moveRequest);

  @RequestMapping(path = "/{id}/representations/{representation-id}/metadata/descriptive/information", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves descriptive metadata information for a specific representation", responses = {
      @ApiResponse(responseCode = "200", description = "Returns an object with all the representation descriptive metadata", content = @Content(schema = @Schema(implementation = DescriptiveMetadataInfos.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "404", description = "Representation not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),})
  DescriptiveMetadataInfos retrieveRepresentationDescriptiveMetadata(
      @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
      @Parameter(description = "The representation identifier", required = true) @PathVariable(name = "representation-id") String representationId,
      @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/assessment", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Appraisal of transferred resource", description = "Accepts or rejects assessment", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AssessmentRequest.class))), responses = {
      @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job appraisal(AssessmentRequest request);

  @RequestMapping(path = "/{id}/metadata/descriptive/{descriptive-metadata-id}/values", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get Supported metadata value", description = "Get AIP metadata value", responses = {
      @ApiResponse(responseCode = "200", description = "Supported Metadata", content = @Content(schema = @Schema(implementation = SupportedMetadataValue.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  SupportedMetadataValue retrieveAIPSupportedMetadata(
      @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
      @Parameter(description = "The descriptive metadata identifier", required = true) @PathVariable(name = "descriptive-metadata-id") String metadataType,
      @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/{id}/lock/request", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get AIP lock", description = "Get the aip lock", responses = {
      @ApiResponse(responseCode = "200", description = "Aip lock availability", content = @Content(schema = @Schema(implementation = Boolean.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  boolean requestAIPLock(@PathVariable(name = "id") String aipId);

  @RequestMapping(path = "/{id}/lock/release", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get AIP lock", description = "Get the aip lock", responses = {
      @ApiResponse(responseCode = "200", description = "Lock released", content = @Content(schema = @Schema(implementation = GenericOkResponse.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  String releaseAIPLock(@PathVariable(name = "id") String aipId);

  @RequestMapping(path = "/{id}/documentation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get AIP documentation", description = "Queries if there are documentation associated with the AIP", responses = {
      @ApiResponse(responseCode = "200", description = "Returns true if documentation is present"),
      @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  boolean getDocumentation(@PathVariable(name = "id") String aipId);

  @RequestMapping(path = "/{id}/submission", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get AIP submission", description = "Queries if there are submissions associated with the AIP", responses = {
      @ApiResponse(responseCode = "200", description = "Returns true if submission is present"),
      @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  boolean getSubmissions(@PathVariable(name = "id") String aipId);

  @RequestMapping(path = "/{id}/metadata/descriptive/{descriptive-metadata-id}/versions/{version-id}/revert", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Reverts AIP descriptive metadata to the an older version", description = "Reverts AIP descriptive metadata to the an older version", responses = {
      @ApiResponse(responseCode = "200", description = "Reverted descriptive metadata"),
      @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DescriptiveMetadata revertAIPDescriptiveMetadataVersion(
      @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
      @Parameter(description = "The descriptive metadata identifier", required = true) @PathVariable(name = "descriptive-metadata-id") String descriptiveMetadataId,
      @Parameter(description = "The version identifier", required = true) @PathVariable(name = "version-id") String versionId);

  @RequestMapping(path = "/{id}/metadata/descriptive/{descriptive-metadata-id}/versions/{version-id}/delete", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete an AIP descriptive metadata version", description = "Deletes a descriptive metadata version related to an AIP", responses = {
      @ApiResponse(responseCode = "204", description = "No Content"),
      @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteDescriptiveMetadataVersion(
      @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
      @Parameter(description = "The descriptive metadata identifier", required = true) @PathVariable(name = "descriptive-metadata-id") String descriptiveMetadataId,
      @Parameter(description = "The version identifier", required = true) @PathVariable(name = "version-id") String versionId);

  @RequestMapping(path = "/{id}/metadata/descriptive/{descriptive-metadata-id}/versions", method = RequestMethod.GET)
  @Operation(summary = "Get descriptive metadata versions", description = "Get descriptive metadata versions", responses = {
      @ApiResponse(responseCode = "200", description = "Returns an object with all the descriptive metadata versions", content = @Content(schema = @Schema(implementation = DescriptiveMetadataVersions.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DescriptiveMetadataVersions retrieveDescriptiveMetadataVersions(
      @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
      @Parameter(description = "The descriptive metadata identifier", required = true) @PathVariable(name = "descriptive-metadata-id") String metadataId,
      @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/{id}/metadata/descriptive", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Updates an AIP descriptive metadata", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateDescriptiveMetadataRequest.class))), responses = {
      @ApiResponse(responseCode = "200", description = "Descriptive metadata updated"),
      @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DescriptiveMetadata updateDescriptiveMetadataFile(
      @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String id,
      CreateDescriptiveMetadataRequest content);

  @RequestMapping(path = "/permissions/update", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Creates an internal actions to update the permissions of AIP(s)", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UpdatePermissionsRequest.class))), responses = {
      @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job updatePermissions(UpdatePermissionsRequest updateRequest);

  // TO REVIEW

  @RequestMapping(path = "{id}/metadata/descriptive/preview", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get descriptive metadata preview", description = "Get AIP descriptive metadata preview", responses = {
      @ApiResponse(responseCode = "200", description = "Instance information", content = @Content(schema = @Schema(implementation = String.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DescriptiveMetadataPreview retrieveDescriptiveMetadataPreview(
      @Parameter(description = "The id identifier", required = true) @PathVariable(name = "id") String id,
      @Parameter(name = "previewRequest", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) DescriptiveMetadataPreviewRequest previewRequest);
}