package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.UpdatePermissionsRequest;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.DIPService;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.DIPRestService;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
 * @author António Lindo <alindo@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/dips")
public class DIPController implements DIPRestService, Exportable {

  @Autowired
  HttpServletRequest request;

  @Autowired
  IndexService indexService;

  @Autowired
  DIPService dipService;

  @Autowired
  RequestHandler requestHandler;

  @Override
  public IndexedDIP findByUuid(String uuid, String localeString) {
    return indexService.retrieve(IndexedDIP.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<IndexedDIP> find(@RequestBody FindRequest findRequest, String localeString) {
    return indexService.find(IndexedDIP.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_DIP)) {
      return new LongResponse(indexService.count(IndexedDIP.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(SuggestRequest suggestRequest) {
    return indexService.suggest(suggestRequest, IndexedDIP.class);
  }

  @GetMapping(path = "{uuid}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads file", description = "Downloads a DIP", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StreamingResponseBody.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadBinary(
    @Parameter(description = "The UUID of the existing DIP", required = true) @PathVariable(name = "uuid") String dipUUID) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<ResponseEntity<StreamingResponseBody>>() {
      @Override
      public ResponseEntity<StreamingResponseBody> process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {

        IndexedDIP dip = indexService.retrieve(IndexedDIP.class, dipUUID, new ArrayList<>());

        controllerAssistant.checkObjectPermissions(requestContext.getUser(), dip);

        controllerAssistant.setParameters(RodaConstants.CONTROLLER_DIP_UUID_PARAM, dipUUID);

        return ApiUtils.okResponse(dipService.createStreamResponse(requestContext, dip.getUUID()));
      }
    });
  }

  @Override
  public Job deleteIndexedDIPs(@RequestBody DeleteRequest deleteRequest) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, deleteRequest.getItemsToDelete(),
          RodaConstants.CONTROLLER_DETAILS_PARAM, deleteRequest.getDetails());

        controllerAssistant.checkObjectPermissions(requestContext.getUser(),
          CommonServicesUtils.convertSelectedItems(deleteRequest.getItemsToDelete(), IndexedDIP.class));

        return dipService.deleteDIPsJob(deleteRequest, requestContext.getUser());
      }
    });
  }

  @Override
  public Job updatePermissions(@RequestBody UpdatePermissionsRequest updateRequest) {

    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {

        controllerAssistant.setParameters(RodaConstants.CONTROLLER_DIPS_PARAM, updateRequest.getItemsToUpdate(),
          RodaConstants.CONTROLLER_PERMISSIONS_PARAM, updateRequest.getPermissions(),
          RodaConstants.CONTROLLER_DETAILS_PARAM, updateRequest.getDetails());

        controllerAssistant.checkObjectPermissions(requestContext.getUser(),
          CommonServicesUtils.convertSelectedItems(updateRequest.getItemsToUpdate(), IndexedDIP.class));

        return dipService.updateDIPPermissions(requestContext.getUser(), updateRequest);
      }
    });
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    // delegate
    return ApiUtils.okResponse(indexService.exportToCSV(findRequestString, IndexedDIP.class));
  }
}
