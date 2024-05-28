package org.roda.wui.client.services;

import io.swagger.v3.oas.annotations.Parameter;
import org.fusesource.restygwt.client.DirectRestService;
import org.roda.core.data.v2.properties.SharedProperties;
import org.roda.wui.client.browse.Viewers;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Tag(name = "v2 configurations")
@RequestMapping(path = "../api/v2/configurations")
public interface ConfigurationRestService extends DirectRestService {

  @RequestMapping(method = RequestMethod.GET, path = "/viewers", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves configuration properties for viewers", responses = {
    @ApiResponse(responseCode = "200", description = "Returns viewers properties", content = @Content(schema = @Schema(implementation = Viewers.class)))})
  Viewers retrieveViewersProperties();

  @RequestMapping(method = RequestMethod.GET, path = "/shared-properties", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve shared properties from configuration files", responses = {
    @ApiResponse(responseCode = "200", description = "Returns shared properties (key-value pairs)", content = @Content(schema = @Schema(implementation = SharedProperties.class)))})
  SharedProperties retrieveSharedProperties(
    @Parameter(description = "The language to be used for internationalization", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);
}
