package org.roda.wui.api.v2.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.model.GenericOkResponse;
import org.roda.wui.api.v2.services.AuditLogService;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.client.services.AuditLogRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
public class AuditLogController implements AuditLogRestService {
  @Autowired
  HttpServletRequest request;

  @Autowired
  IndexService indexService;

  @Autowired
  AuditLogService auditLogService;

  @Override
  public LogEntry findByUuid(String uuid, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.LOG_ID,
      RodaConstants.LOG_ACTION_COMPONENT, RodaConstants.LOG_ACTION_METHOD, RodaConstants.LOG_ADDRESS,
      RodaConstants.LOG_DATETIME, RodaConstants.LOG_RELATED_OBJECT_ID, RodaConstants.LOG_USERNAME,
      RodaConstants.LOG_PARAMETERS, RodaConstants.LOG_STATE);
    return indexService.retrieve(requestContext, LogEntry.class, uuid, fieldsToReturn);
  }

  @Override
  public IndexResult<LogEntry> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.find(LogEntry.class, findRequest, localeString, requestContext);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_LOG_ENTRY)) {
      return new LongResponse(indexService.count(LogEntry.class, countRequest, requestContext));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(SuggestRequest suggestRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.suggest(suggestRequest, LogEntry.class, requestContext);
  }

  @PostMapping(path = "/import", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Import audit logs from replica instances to the primary", description = "In order to synchronized the audit logs between all RODA instances it is necessary that read-only RODA's send their logs to the read-write RODA.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = MultipartFile.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GenericOkResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public String importLogEntry(
    @Parameter(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = MultipartFile.class)), description = "Multipart file") @RequestPart(value = "file") MultipartFile resource) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    String filename = resource.getOriginalFilename();

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      auditLogService.importLogEntries(resource.getInputStream(), filename);

      return JsonUtils.getJsonFromObject(new GenericOkResponse("Audit logs successfully imported"),
        GenericOkResponse.class);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | AlreadyExistsException | RequestNotValidException | NotFoundException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_FILENAME_PARAM, filename);
    }
  }
}
