package org.roda.wui.api.v2.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.StringResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.AuditLogService;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.client.services.AuditLogRestService;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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
@RequestMapping(path = "/api/v2/audit-logs")
public class AuditLogController implements AuditLogRestService, Exportable {
  @Autowired
  HttpServletRequest request;

  @Autowired
  IndexService indexService;

  @Autowired
  AuditLogService auditLogService;

  @Autowired
  RequestHandler requestHandler;

  @Override
  public LogEntry findByUuid(String uuid, String localeString) {
    final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.LOG_ID,
      RodaConstants.LOG_ACTION_COMPONENT, RodaConstants.LOG_ACTION_METHOD, RodaConstants.LOG_ADDRESS,
      RodaConstants.LOG_DATETIME, RodaConstants.LOG_RELATED_OBJECT_ID, RodaConstants.LOG_USERNAME,
      RodaConstants.LOG_PARAMETERS, RodaConstants.LOG_STATE, RodaConstants.LOG_REQUEST_HEADER_UUID,
      RodaConstants.LOG_REQUEST_HEADER_REASON, RodaConstants.LOG_REQUEST_HEADER_TYPE);
    return indexService.retrieve(LogEntry.class, uuid, fieldsToReturn);
  }

  @Override
  public IndexResult<LogEntry> find(@RequestBody FindRequest findRequest, String localeString) {
    return indexService.find(LogEntry.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_LOG_ENTRY)) {
      return new LongResponse(indexService.count(LogEntry.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(SuggestRequest suggestRequest) {
    return indexService.suggest(suggestRequest, LogEntry.class);
  }

  @PostMapping(path = "/import", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Import audit logs from replica instances to the primary", description = "In order to synchronized the audit logs between all RODA instances it is necessary that read-only RODA's send their logs to the read-write RODA.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = MultipartFile.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StringResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public StringResponse importLogEntry(
    @Parameter(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = MultipartFile.class)), description = "Multipart file") @RequestPart(value = "file") MultipartFile resource) {

    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<StringResponse>() {
      @Override
      public StringResponse process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        String filename = resource.getOriginalFilename();
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_FILENAME_PARAM, filename);
        // delegate
        auditLogService.importLogEntries(requestContext.getModelService(), resource.getInputStream(), filename);

        return new StringResponse("Audit logs successfully imported");
      }
    });
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    // delegate
    return ApiUtils.okResponse(indexService.exportToCSV(requestContext.getUser(), findRequestString, LogEntry.class));
  }
}
