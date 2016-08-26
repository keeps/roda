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
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.user.RodaSimpleUser;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.StreamResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.*;

@Path(ReportsResource.ENDPOINT)
@Api(value = ReportsResource.SWAGGER_ENDPOINT)
public class ReportsResource {
  public static final String ENDPOINT = "/v1/reports";
  public static final String SWAGGER_ENDPOINT = "v1 reports";

  private static Logger LOGGER = LoggerFactory.getLogger(ReportsResource.class);

  @Context
  private HttpServletRequest request;

  @GET
  @Path("/{transferred_resource_uuid}")
  @ApiOperation(value = "List reports", notes = "List reports", response = Report.class, responseContainer = "List")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Report.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Transferred resource not found", response = Report.class, responseContainer = "List")})

  public Response getTransferredResourceReports(
    @ApiParam(value = "The ID of the existing transferred resource", required = true) @PathParam(RodaConstants.API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID) String resourceId,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Is the indicated ID the original one?", defaultValue = "false") @QueryParam(RodaConstants.API_QUERY_IS_ORIGINAL) boolean isOriginal,
    @ApiParam(value = "Choose format in which to get the reports", allowableValues = "json") @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);

    StreamResponse reportList = Browser.listTransferredResourcesReports(user, resourceId, start, limit, isOriginal,
      acceptFormat);

    return ApiUtils.okResponse(reportList);
  }

  @GET
  @Path("/{transferred_resource_uuid}/last")
  @ApiOperation(value = "Last report", notes = "Last report", response = Report.class, responseContainer = "List")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Report.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Transferred resource not found", response = Report.class, responseContainer = "List")})

  public Response getTransferredResourceLastReport(
    @ApiParam(value = "The ID of the existing transferred resource", required = true) @PathParam(RodaConstants.API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID) String resourceId,
    @ApiParam(value = "Is the indicated ID the original one?", defaultValue = "false") @QueryParam(RodaConstants.API_QUERY_IS_ORIGINAL) boolean isOriginal,
    @ApiParam(value = "Choose format in which to get the reports", allowableValues = "json") @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);

    // get last job reports of a transferred resource
    StreamResponse lastReports = Browser.listTransferredResourcesLastReport(user, resourceId, isOriginal, acceptFormat);

    return ApiUtils.okResponse(lastReports);
  }

}
