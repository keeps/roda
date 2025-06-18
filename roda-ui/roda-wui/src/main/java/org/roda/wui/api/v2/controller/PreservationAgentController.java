package org.roda.wui.api.v2.controller;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.PreservationAgentService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.client.services.PreservationAgentRestService;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
@RequestMapping(path = "/api/v2/preservation/agents")
public class PreservationAgentController implements PreservationAgentRestService, Exportable {

  @Autowired
  HttpServletRequest request;

  @Autowired
  IndexService indexService;

  @Autowired
  PreservationAgentService preservationAgentService;

  @Autowired
  RequestHandler requestHandler;

  @Override
  public IndexedPreservationAgent findByUuid(String uuid, String localeString) {
    final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.PRESERVATION_AGENT_ID,
      RodaConstants.PRESERVATION_AGENT_NAME, RodaConstants.PRESERVATION_AGENT_TYPE,
      RodaConstants.PRESERVATION_AGENT_VERSION, RodaConstants.PRESERVATION_AGENT_NOTE,
      RodaConstants.PRESERVATION_AGENT_EXTENSION);
    return indexService.retrieve(IndexedPreservationAgent.class, uuid, fieldsToReturn);
  }

  @Override
  public IndexResult<IndexedPreservationAgent> find(@RequestBody FindRequest findRequest, String localeString) {
    return indexService.find(IndexedPreservationAgent.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_PRESERVATION_AGENT)) {
      return new LongResponse(indexService.count(IndexedPreservationAgent.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(SuggestRequest suggestRequest) {
    return indexService.suggest(suggestRequest, IndexedPreservationAgent.class);
  }

  @GetMapping(path = "/{id}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads preservation agent file", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StreamingResponseBody.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadPreservationAgent(
    @Parameter(description = "The id of the preservation agent", required = true) @PathVariable(name = "id") String id,
    @RequestHeader HttpHeaders headers) {

    return requestHandler.processRequest(new RequestHandler.RequestProcessor<ResponseEntity<StreamingResponseBody>>() {
      @Override
      public ResponseEntity<StreamingResponseBody> process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(id);
        StreamResponse response = preservationAgentService.retrievePreservationAgentFile(requestContext.getModelService(), id);

        return ApiUtils.okResponse(response);
      }
    });
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    // delegate
    return ApiUtils.okResponse(
      indexService.exportToCSV(findRequestString, IndexedPreservationAgent.class));
  }
}
