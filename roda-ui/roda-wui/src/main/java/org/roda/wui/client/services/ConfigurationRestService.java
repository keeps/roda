package org.roda.wui.client.services;

import java.util.List;

import org.fusesource.restygwt.client.DirectRestService;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.StringResponse;
import org.roda.core.data.v2.jobs.PluginInfoList;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.properties.ConversionProfileOutcomeType;
import org.roda.core.data.v2.properties.ConversionProfiles;
import org.roda.core.data.v2.properties.DropdownPluginParameterItems;
import org.roda.core.data.v2.properties.ObjectClassFields;
import org.roda.core.data.v2.properties.ReindexPluginObjects;
import org.roda.core.data.v2.properties.SharedProperties;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.client.browse.Viewers;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Tag(name = "v2 configurations")
@RequestMapping(path = "../api/v2/configurations")
public interface ConfigurationRestService extends DirectRestService {

  @RequestMapping(method = RequestMethod.GET, path = "/cron", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the cron value", responses = {
    @ApiResponse(responseCode = "200", description = "Returns the cron value", content = @Content(schema = @Schema(implementation = StringResponse.class)))})
  StringResponse retrieveCronValue(
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(method = RequestMethod.GET, path = "/exports/limit", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the export limit value", responses = {
    @ApiResponse(responseCode = "200", description = "Returns the export limit value", content = @Content(schema = @Schema(implementation = LongResponse.class)))})
  LongResponse retrieveExportLimit();

  @RequestMapping(method = RequestMethod.GET, path = "/viewers", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves configuration properties for viewers", responses = {
    @ApiResponse(responseCode = "200", description = "Returns viewers properties", content = @Content(schema = @Schema(implementation = Viewers.class)))})
  Viewers retrieveViewersProperties();

  @RequestMapping(method = RequestMethod.GET, path = "/shared-properties", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve shared properties from configuration files", responses = {
    @ApiResponse(responseCode = "200", description = "Returns shared properties (key-value pairs)", content = @Content(schema = @Schema(implementation = SharedProperties.class)))})
  SharedProperties retrieveSharedProperties(
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(method = RequestMethod.GET, path = "/object-fields", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve configuration properties for draw the object class fields", responses = {
    @ApiResponse(responseCode = "200", description = "Returns a set of object class fields")})
  ObjectClassFields retrieveObjectClassFields(
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(method = RequestMethod.GET, path = "/plugins", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves a list of plugins that matches a certain type", responses = {
    @ApiResponse(responseCode = "200", description = "Returns a set of plugin information", content = @Content(schema = @Schema(implementation = PluginInfoList.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Access forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  PluginInfoList retrievePluginsInfo(@RequestParam(name = "type") List<PluginType> types);

  @RequestMapping(method = RequestMethod.GET, path = "/plugins/reindex", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves a list of resources that are used by reindex plugins", responses = {
    @ApiResponse(responseCode = "200", description = "Returns a set of objects classes", content = @Content(schema = @Schema(implementation = ReindexPluginObjects.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Access forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  ReindexPluginObjects retrieveReindexPluginObjectClasses();

  @RequestMapping(method = RequestMethod.GET, path = "/embedded-dip", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the configuration value associated to the property of show DIP embedded in system or externally", responses = {
    @ApiResponse(responseCode = "200", description = "Returns true or false", content = @Content(schema = @Schema(implementation = Boolean.class))),})
  Boolean retrieveShowEmbeddedDIP();

  @RequestMapping(method = RequestMethod.GET, path = "/plugins/dropdown/{parameter-id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves a list of items from the configurations according to the parameter identifier", responses = {
    @ApiResponse(responseCode = "200", description = "Returns a set of dropdown items", content = @Content(schema = @Schema(implementation = DropdownPluginParameterItems.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Access forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DropdownPluginParameterItems retrieveDropdownPluginItems(
    @Parameter(description = "The plugin parameter identifier", required = true) @PathVariable(name = "parameter-id") String parameterId,
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(method = RequestMethod.GET, path = "/plugins/{id}/conversion/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves a list of conversion profiles from the configurations according to the plugin identifier", responses = {
    @ApiResponse(responseCode = "200", description = "Returns a set of conversion profiles for a specific plugin", content = @Content(schema = @Schema(implementation = DropdownPluginParameterItems.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Access forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  ConversionProfiles retrieveConversionProfiles(
    @Parameter(description = "The plugin identifier", required = true) @PathVariable(name = "id") String pluginId,
    @Parameter(description = "The type of outcome", required = true, content = @Content(schema = @Schema(implementation = ConversionProfileOutcomeType.class))) @RequestParam(name = "outcome") ConversionProfileOutcomeType outcomeType,
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);
}
