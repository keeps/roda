package org.roda.wui.client.services;

import java.util.List;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Tag(name = "v2 aips")
@RequestMapping(path = "../api/v2/aips")
public interface AIPRestService extends RODAEntityRestService<IndexedAIP> {

  @RequestMapping(path = "/{id}/ancestors", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  List<IndexedAIP> getAncestors(@Parameter(description = "AIP id", required = true) @PathVariable(name = "id") String id);

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
}