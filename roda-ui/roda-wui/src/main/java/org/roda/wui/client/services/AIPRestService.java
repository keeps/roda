package org.roda.wui.client.services;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.*;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.model.GenericOkResponse;
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

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Tag(name = "v2 aips")
@RequestMapping(path = "../api/v2/aips")
public interface AIPRestService extends RODAEntityRestService<IndexedAIP> {

  @RequestMapping(path = "/delete", method = RequestMethod.DELETE)
  @Operation(summary = "Delete one or multiple AIPs", description = "Deletes one or more AIPs", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job deleteAIPs(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<IndexedAIP> selected,
    @RequestParam(name = RodaConstants.API_QUERY_JOB_DETAILS) String details);

  @RequestMapping(path = "/representation/information", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get representation information", description = "Get Representation information fields", responses = {
    @ApiResponse(responseCode = "200", description = "Representation information", content = @Content(schema = @Schema(implementation = GenericOkResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  List<String> getRepresentationInformationFields();

  @RequestMapping(path = "/{aipId}/instance/information", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get instance information", description = "Get AIP instance information", responses = {
    @ApiResponse(responseCode = "200", description = "Instance information", content = @Content(schema = @Schema(implementation = InstanceState.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  InstanceState getInstanceName(@PathVariable(name = "aipId") String aipId,
    @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/{aipId}/metadata/descriptive", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get descriptive metadata", description = "Get descriptive metadata related to the aip", responses = {
    @ApiResponse(responseCode = "200", description = "Instance information", content = @Content(schema = @Schema(implementation = DescriptiveMetadataInfos.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DescriptiveMetadataInfos getDescriptiveMetadata(@PathVariable(name = "aipId") String aipId,
    @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/{aipId}/metadata/descriptive", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create Descriptive metadata", description = "Create AIP descriptive metadata", responses = {
    @ApiResponse(responseCode = "200", description = "Instance information", content = @Content(schema = @Schema(implementation = DescriptiveMetadataInfos.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void createAIPDescriptiveMetadata(@PathVariable(name = "aipId") String aipId,
    @Parameter(name = "body", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) CreateDescriptiveMetadataRequest selected);

  @RequestMapping(path = "/metadata/preview", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get descriptive metadata preview", description = "Get AIP descriptive metadata preview", responses = {
    @ApiResponse(responseCode = "200", description = "Instance information", content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DescriptiveMetadataPreview retrieveDescriptiveMetadataPreview(
    @Parameter(name = "previewRequest", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) DescriptiveMetadataPreviewRequest previewRequest);

  @RequestMapping(path = "/{id}/retrieve/metadata", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get Supported metadata value", description = "Get AIP supported metadata value", responses = {
    @ApiResponse(responseCode = "200", description = "Supported Metadata", content = @Content(schema = @Schema(implementation = SupportedMetadataValue.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  SupportedMetadataValue retrieveAIPSupportedMetadata(@PathVariable(name = "id") String aipId,
    @RequestParam(name = "metadataType") String metadataType,
    @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/retrieve/metadata", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get Supported metadata", description = "Get AIP supported metadata", responses = {
    @ApiResponse(responseCode = "200", description = "Supported Metadata", content = @Content(schema = @Schema(implementation = SupportedMetadata.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  List<SupportedMetadata> retrieveSupportedMetadataTypes(
    @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/create", method = RequestMethod.POST)
  @Operation(summary = "Create AIP", description = "Create a new AIP", responses = {
    @ApiResponse(responseCode = "201", description = "Newly created AIP", content = @Content(schema = @Schema(implementation = AIP.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  AIP createAIP(
    @Parameter(description = "The id of the parent") @RequestParam(name = "parentAipId", required = false) String parentAipId,
    @Parameter(description = "Type of AIP") @RequestParam(name = "aipType") String aipType);

  @RequestMapping(path = "/{aipId}/documentation", method = RequestMethod.GET)
  @Operation(summary = "Get AIP documentation", description = "Gets AIP documentation", responses = {
    @ApiResponse(responseCode = "200", description = "No content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void getDocumentation(@PathVariable(name = "aipId") String aipId);

  @RequestMapping(path = "/{aipId}/submission", method = RequestMethod.GET)
  @Operation(summary = "Get AIP submission", description = "Gets AIP submission", responses = {
    @ApiResponse(responseCode = "200", description = "No content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void getSubmissions(@PathVariable(name = "aipId") String aipId);

  @RequestMapping(path = "/move", method = RequestMethod.DELETE)
  @Operation(summary = "Move AIP in the hierarchy", description = "Moves an AIP in the hierarchy", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job moveAIPInHierarchy(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<IndexedAIP> selected,
    @RequestParam(name = "parentId", required = false) String parentID,
    @RequestParam(name = RodaConstants.API_QUERY_JOB_DETAILS, required = false) String details);

  @RequestMapping(path = "/retrieve/options", method = RequestMethod.GET)
  @Operation(summary = "Get AIP type options", description = "Get the aip type options", responses = {
    @ApiResponse(responseCode = "200", description = "AIP type options", content = @Content(schema = @Schema(implementation = TypeOptionsInfo.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  TypeOptionsInfo getTypeOptions(
    @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/changetype", method = RequestMethod.PUT)
  @Operation(summary = "Changes AIP type", description = "Changes the AIP type", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job changeAIPType(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<IndexedAIP> selected,
    @RequestParam(name = "newType", required = false) String newType,
    @RequestParam(name = RodaConstants.API_QUERY_JOB_DETAILS, required = false) String details);

  @RequestMapping(path = "/appraisal", method = RequestMethod.PUT)
  @Operation(summary = "Appraisal of transferred resource", description = "Accepts or rejects assessment", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job appraisal(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<IndexedAIP> selected,
    @RequestParam(name = "accept", required = true) boolean accept,
    @RequestParam(name = "rejectReason", required = false) String rejectReason);

  @RequestMapping(path = "/{aipId}/lock", method = RequestMethod.GET)
  @Operation(summary = "Get AIP lock", description = "Get the aip lock", responses = {
    @ApiResponse(responseCode = "200", description = "Aip lock availability", content = @Content(schema = @Schema(implementation = TypeOptionsInfo.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  boolean requestAIPLock(@PathVariable(name = "aipId") String aipId);

  @RequestMapping(path = "/{id}/ancestors", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  List<IndexedAIP> getAncestors(
    @Parameter(description = "AIP id", required = true) @PathVariable(name = "id") String id);

  @RequestMapping(path = "/{id}/representations/{representation-id}/metadata/descriptive", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves descriptive metadata information for a specific representation", responses = {
    @ApiResponse(responseCode = "200", description = "Returns an object with all the representation descriptive metadata", content = @Content(schema = @Schema(implementation = DescriptiveMetadataInfos.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Representation not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),})
  DescriptiveMetadataInfos retrieveRepresentationDescriptiveMetadata(
    @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
    @Parameter(description = "The representation identifier", required = true) @PathVariable(name = "representation-id") String representationId,
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(method = RequestMethod.GET, path = "/configuration/rules", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves from the configuration the rules for the Representation information", responses = {
      @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = List.class))),
      @ApiResponse(responseCode = "401", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  List<String> retrieveAIPRuleProperties();

  @RequestMapping(path = "/{id}/metadata/{descriptiveMetadataId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves descriptive metadata information for a specific representation", responses = {
    @ApiResponse(responseCode = "200", description = "Returns an object with all the descriptive metadata", content = @Content(schema = @Schema(implementation = CreateDescriptiveMetadataRequest.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  CreateDescriptiveMetadataRequest retrieveSpecificDescriptiveMetadata(
    @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
    @Parameter(description = "Type of descriptive metadata") @PathVariable(name = "descriptiveMetadataId") String descriptiveMetadataId,
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/{id}/metadata/{descriptiveMetadataId}", method = RequestMethod.DELETE)
  @Operation(summary = "Delete metadata file", description = "Delete metadata file related to AIP", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteDescriptiveMetadataFile(
    @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String aipId,
    @Parameter(description = "Type of descriptive metadata") @PathVariable(name = "descriptiveMetadataId") String metadataId);

  @RequestMapping(path = "/{id}/metadata", method = RequestMethod.PUT)
  @Operation(summary = "Update AIP metadata", description = "Update aip metadata", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void updateDescriptiveMetadataFile(
    @Parameter(description = "The AIP identifier", required = true) @PathVariable(name = "id") String id,
    @Parameter(name = "body", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) CreateDescriptiveMetadataRequest content);

}