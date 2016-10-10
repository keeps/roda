/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Reports;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path(ReportsResource.ENDPOINT)
@Api(value = ReportsResource.SWAGGER_ENDPOINT)
public class ReportsResource {
  public static final String ENDPOINT = "/v1/reports";
  public static final String SWAGGER_ENDPOINT = "v1 reports";

  @Context
  private HttpServletRequest request;

  @GET
  @ApiOperation(value = "List reports", notes = "List reports", response = Reports.class, responseContainer = "List")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Reports.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Transferred resource or SIP not found", response = ApiResponseMessage.class)})

  public Response getTransferredResourceReports(
    @ApiParam(value = "The ID of the existing transferred resource or SIP") @QueryParam(RodaConstants.API_QUERY_PARAM_ID) String id,
    @ApiParam(value = "Choose the ID related object", allowableValues = RodaConstants.API_GET_REPORTS_ID_OBJECT, defaultValue = RodaConstants.API_GET_REPORTS_ID_OBJECT_RESOURCE_PATH) @QueryParam(RodaConstants.API_QUERY_PARAM_TYPE) String resourceOrSip,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the reports", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // get job reports of a transferred resource or SIP or all
    Reports reportList = Browser.listReports(user, id, resourceOrSip, start, limit, acceptFormat);
    return Response.ok(reportList, mediaType).build();
  }

  @GET
  @Path("/last")
  @ApiOperation(value = "Last report", notes = "Last report", response = Report.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Report.class),
    @ApiResponse(code = 404, message = "Transferred resource or SIP not found", response = ApiResponseMessage.class)})

  public Response getTransferredResourceLastReport(
    @ApiParam(value = "The ID of the existing transferred resource or SIP") @QueryParam(RodaConstants.API_QUERY_PARAM_ID) String id,
    @ApiParam(value = "Choose the ID related object", allowableValues = RodaConstants.API_GET_REPORTS_ID_OBJECT, defaultValue = RodaConstants.API_GET_REPORTS_ID_OBJECT_RESOURCE_PATH) @QueryParam(RodaConstants.API_QUERY_PARAM_TYPE) String resourceOrSip,
    @ApiParam(value = "Choose format in which to get the reports", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // get last job reports of a transferred resource or SIP or all
    try {
      Report lastReport = Browser.lastReport(user, id, resourceOrSip, acceptFormat);
      return Response.ok(lastReport, mediaType).build();
    } catch (RODAException e) {
      return Response.ok(new ApiResponseMessage(ApiResponseMessage.ERROR, "Error getting the last report"), mediaType)
        .build();
    }
  }

}
