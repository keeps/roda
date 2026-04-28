/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationEventsLinkingObjects;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.storage.Binary;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.PreservationEventService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.client.services.PreservationEventRestService;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/preservation/events")
public class PreservationEventController implements PreservationEventRestService, Exportable {

  @Autowired
  HttpServletRequest request;

  @Autowired
  IndexService indexService;

  @Autowired
  PreservationEventService preservationEventService;

  @Autowired
  RequestHandler requestHandler;

  @Override
  public IndexedPreservationEvent findByUuid(String uuid, String localeString) {
    return indexService.retrieve(IndexedPreservationEvent.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<IndexedPreservationEvent> find(@RequestBody FindRequest findRequest, String localeString) {
    return indexService.find(IndexedPreservationEvent.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_PRESERVATION_EVENT)) {
      return new LongResponse(indexService.count(IndexedPreservationEvent.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(SuggestRequest suggestRequest) {
    return indexService.suggest(suggestRequest, IndexedPreservationEvent.class);
  }

  @Override
  public List<IndexedPreservationAgent> getPreservationAgents(String id) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<List<IndexedPreservationAgent>>() {
      @Override
      public List<IndexedPreservationAgent> process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(id);
        preservationEventService.setIndexService(indexService);
        IndexedPreservationEvent preservationEvent = findByUuid(id, "en");

        Binary preservationEventBinary = preservationEventService.getPreservationEventBinary(preservationEvent,
          requestContext);
        return preservationEventService.getAgentsFromPreservationEventBinary(preservationEventBinary, requestContext);
      }
    });
  }

  @Override
  public PreservationEventsLinkingObjects getLinkingIdentifierObjects(String id) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<PreservationEventsLinkingObjects>() {
      @Override
      public PreservationEventsLinkingObjects process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(id);

        preservationEventService.setIndexService(indexService);
        IndexedPreservationEvent preservationEvent = findByUuid(id, "en");

        Binary preservationEventBinary = preservationEventService.getPreservationEventBinary(preservationEvent,
          requestContext);
        return preservationEventService.getLinkingObjectsFromPreservationEventBinary(preservationEventBinary,
          requestContext);
      }
    });
  }

  @GetMapping(path = "/{id}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads preservation event file", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StreamingResponseBody.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadPreservationEvent(
    @Parameter(description = "The id of the preservation event", required = true) @PathVariable(name = "id") String id,
    @RequestHeader HttpHeaders headers) {

    return requestHandler.processRequest(new RequestHandler.RequestProcessor<ResponseEntity<StreamingResponseBody>>() {
      @Override
      public ResponseEntity<StreamingResponseBody> process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(id);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_INDEX_PRESERVATION_EVENT_ID_PARAM, id);
        IndexedPreservationEvent preservationEvent = indexService.retrieve(IndexedPreservationEvent.class, id,
          new ArrayList<>());

        if (preservationEvent.getAipID() != null) {
          controllerAssistant.checkObjectPermissions(requestContext.getUser(),
            SelectedItemsList.create(IndexedAIP.class, preservationEvent.getAipID()));
        }

        StreamResponse response = preservationEventService.retrievePreservationEventFile(preservationEvent,
          requestContext);

        return ApiUtils.okResponse(response);
      }
    });
  }

  @GetMapping(path = "/{id}/details", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Gets preservation event details in structured JSON format", responses = {
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<Map<String, String>> getPreservationEventsDetails(
    @Parameter(description = "The id of the preservation event", required = true) @PathVariable(name = "id") String id,
    @RequestHeader HttpHeaders headers) {

    return requestHandler.processRequest(new RequestHandler.RequestProcessor<ResponseEntity<Map<String, String>>>() {
      @Override
      public ResponseEntity<Map<String, String>> process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {

        controllerAssistant.setRelatedObjectId(id);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_INDEX_PRESERVATION_EVENT_ID_PARAM, id);

        IndexedPreservationEvent preservationEvent = indexService.retrieve(IndexedPreservationEvent.class, id,
          new ArrayList<>());

        if (preservationEvent.getAipID() != null) {
          controllerAssistant.checkObjectPermissions(requestContext.getUser(),
            SelectedItemsList.create(IndexedAIP.class, preservationEvent.getAipID()));
        }
        Map<String, String> details = preservationEventService.retrievePreservationEventDetails(preservationEvent,
          requestContext);

        return ResponseEntity.ok(details);
      }
    });
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    // delegate
    return ApiUtils.okResponse(indexService.exportToCSV(findRequestString, IndexedPreservationEvent.class));
  }
}
