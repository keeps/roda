package org.roda.wui.client.services;

import java.util.List;

import org.fusesource.restygwt.client.DirectRestService;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
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

/**
 * @author António Lindo <alindo@keep.pt>
 */
public interface RODAEntityRestService<T extends IsIndexed> extends DirectRestService {

  @RequestMapping(method = RequestMethod.GET, path = "/find/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Find indexed resource using the object UUID", description = "Finds existing indexed resources", responses = {
    @ApiResponse(responseCode = "200", description = "Returns the object", content = @Content(schema = @Schema(implementation = IndexResult.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),})
  T findByUuid(@Parameter(description = "The object UUID", required = true) @PathVariable(name = "uuid") String uuid,
    @Parameter(description = "language", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/find", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Find indexed resources via search query", description = "Finds existing indexed resources", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = IndexResult.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),})
  IndexResult<T> find(
    @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FindRequest.class))) FindRequest findRequest,
    @Parameter(description = "language", content = @Content(schema = @Schema(defaultValue = "en", implementation = String.class))) @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/count", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Count indexed resources", description = "Counts indexed resources", responses = {
    @ApiResponse(responseCode = "200", description = "Returns the object counting", content = @Content(schema = @Schema(implementation = LongResponse.class)))})
  LongResponse count(
    @RequestBody(description = "Count parameters", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CountRequest.class))) CountRequest countRequest);

  @RequestMapping(path = "/suggest", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Returns a set of suggestions giving a query", description = "Returns a set of suggestions giving a query", responses = {
      @ApiResponse(responseCode = "200", description = "List of suggestions", content = @Content(schema = @Schema(implementation = List.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden access", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  List<String> suggest(
      @RequestBody(description = "Suggest parameters", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SuggestRequest.class))) SuggestRequest suggestRequest);
}
