package org.roda.wui.client.services;

import java.util.List;

import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationEventsLinkingObjects;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Tag(name = "Preservation events")
@RequestMapping(path = "../api/v2/preservation/events")
public interface PreservationEventRestService extends RODAEntityRestService<IndexedPreservationEvent> {

  @RequestMapping(method = RequestMethod.GET, path = "/{id}/agents", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List preservation agents associated to the preservation event", description = "Gets a list of preservation agents", responses = {
    @ApiResponse(responseCode = "200", description = "List of preservation agents", content = @Content(schema = @Schema(implementation = IndexedPreservationAgent.class))),
    @ApiResponse(responseCode = "404", description = "Preservation event not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  List<IndexedPreservationAgent> getPreservationAgents(
    @Parameter(description = "The preservation event id", required = true) @PathVariable(name = "id") String id);

  @RequestMapping(method = RequestMethod.GET, path = "/{id}/objects", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Gets linking objects associated to the preservation event", description = "Gets linking objects associated to the preservation event", responses = {
    @ApiResponse(responseCode = "200", description = "Preservation events linking objects", content = @Content(schema = @Schema(implementation = PreservationEventsLinkingObjects.class))),
    @ApiResponse(responseCode = "404", description = "Preservation event not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  PreservationEventsLinkingObjects getLinkingIdentifierObjects(
    @Parameter(description = "The preservation event id", required = true) @PathVariable(name = "id") String id);
}
