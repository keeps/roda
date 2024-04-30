package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.FilesService;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/api/v2/files")
@Tag(name = FilesController.SWAGGER_ENDPOINT)
public class FilesController {
  public static final String SWAGGER_ENDPOINT = "v2 files";

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private FilesService filesService;

  @Autowired
  private IndexService indexService;

  @RequestMapping(path = "{uuid}/binary", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads file", description = "Download a particular file", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StreamingResponseBody.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadBinary(
    @Parameter(description = "The UUID of the existing file", required = true) @PathVariable(name = "uuid") String fileUUID,
    @RequestHeader HttpHeaders headers) throws RODAException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      List<String> fileFields = new ArrayList<>(RodaConstants.FILE_FIELDS_TO_RETURN);
      fileFields.add(RodaConstants.FILE_ISDIRECTORY);
      IndexedFile file = indexService.retrieve(requestContext, IndexedFile.class, fileUUID, fileFields);
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), file);

      StreamResponse response = filesService.retrieveAIPRepresentationFile(file);

      return ApiUtils.rangeResponse(headers, response.getStream());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, fileUUID, state,
        RodaConstants.CONTROLLER_FILE_UUID_PARAM, fileUUID);
    }
  }
}
