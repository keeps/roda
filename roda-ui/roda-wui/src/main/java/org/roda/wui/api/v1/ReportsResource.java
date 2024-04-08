/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.server.JSONP;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Reports;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(ReportsResource.ENDPOINT)
@Tag(name = ReportsResource.SWAGGER_ENDPOINT)
public class ReportsResource {
  public static final String ENDPOINT = "/reports";
  public static final String SWAGGER_ENDPOINT = "v1 reports";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "List transferred resources reports", description = "Gets a list of transferred resources reports", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Reports.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response getTransferredResourceReports(
    @Parameter(description = "The ID of the existing transferred resource or SIP") @QueryParam(RodaConstants.API_QUERY_PARAM_ID) String id,
    @Parameter(description = "Choose the ID related object", schema = @Schema(allowableValues = {
      "transferred_resource_uuid", "transferred_resource_path", "sip",
      "transferred_resource_original_name"}, defaultValue = RodaConstants.CONTROLLER_ID_OBJECT_RESOURCE_PATH)) @QueryParam(RodaConstants.API_QUERY_PARAM_TYPE) String resourceOrSip,
    @Parameter(description = "Index of the first element to return", schema = @Schema(defaultValue = "0")) @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @Parameter(description = "Maximum number of elements to return", schema = @Schema(defaultValue = "100")) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @Parameter(description = "Choose format in which to get the reports", schema = @Schema(implementation = RodaConstants.ListMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    int startInt = pagingParams.getFirst();
    int limitInt = pagingParams.getSecond();

    // get job reports of a transferred resource or SIP or all
    Reports reportList = Browser.listReports(user, id, resourceOrSip, startInt, limitInt, acceptFormat);
    return Response.ok(reportList, mediaType).build();
  }

  @GET
  @Path("/last")
  @Operation(summary = "Get last transferred resource report", description = "Gets the last transferred resource report", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Report.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response getTransferredResourceLastReport(
    @Parameter(description = "The ID of the existing transferred resource or SIP") @QueryParam(RodaConstants.API_QUERY_PARAM_ID) String id,
    @Parameter(description = "Choose the ID related object", schema = @Schema(allowableValues = {
      "transferred_resource_uuid", "transferred_resource_path", "sip",
      "transferred_resource_original_name"}, defaultValue = RodaConstants.CONTROLLER_ID_OBJECT_RESOURCE_PATH)) @QueryParam(RodaConstants.API_QUERY_PARAM_TYPE) String resourceOrSip,
    @Parameter(description = "Choose format in which to get the reports", schema = @Schema(implementation = RodaConstants.ListMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // get last job reports of a transferred resource or SIP or all
    Report lastReport = Browser.lastReport(user, id, resourceOrSip, acceptFormat);
    return Response.ok(lastReport, mediaType).build();
  }
}
