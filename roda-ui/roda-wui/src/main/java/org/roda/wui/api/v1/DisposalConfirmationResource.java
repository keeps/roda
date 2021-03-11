/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.JSONP;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.controllers.Disposals;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path(DisposalConfirmationResource.ENDPOINT)
@Api(value = DisposalConfirmationResource.SWAGGER_ENDPOINT)
public class DisposalConfirmationResource {
  public static final String ENDPOINT = "/v1/disposal/confirmation";
  public static final String SWAGGER_ENDPOINT = "v1 disposal";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "List disposal confirmations", notes = "Gets a list of disposal confirmations.", response = DisposalConfirmation.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = DisposalConfirmation.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})
  public Response listDisposalConfirmations(
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the Disposal confirmation", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    boolean justActive = false;
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    IndexResult<DisposalConfirmation> result = Browser.find(DisposalConfirmation.class, Filter.NULL, Sorter.NONE,
      new Sublist(pagingParams.getFirst(), pagingParams.getSecond()), null, user, justActive, new ArrayList<>());

    return Response.ok(ApiUtils.indexedResultToRODAObjectList(DisposalConfirmation.class, result), mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_DISPOSAL_CONFIRMATION_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "Get Disposal confirmation", notes = "Get disposal confirmation", response = DisposalConfirmation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DisposalConfirmation.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})
  public Response retrieveDisposalConfirmation(
    @ApiParam(value = "The ID of the disposal confirmation to retrieve.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_DISPOSAL_CONFIRMATION_ID) String confirmationId,
    @ApiParam(value = "Choose format in which to get the disposal confirmation", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    DisposalConfirmation disposalConfirmation = Browser.retrieve(user, DisposalConfirmation.class, confirmationId,
      new ArrayList<>());

    return Response.ok(disposalConfirmation, mediaType).build();
  }

  @GET
  @Path("/" + RodaConstants.API_PATH_PARAM_DISPOSAL_CONFIRMATION_REPORT + "/{"
    + RodaConstants.API_PATH_PARAM_DISPOSAL_CONFIRMATION_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "Get Disposal confirmation report", notes = "Get disposal confirmation report", response = String.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = String.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})
  public Response retrieveDisposalConfirmationReport(
    @ApiParam(value = "The ID of the disposal confirmation to retrieve.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_DISPOSAL_CONFIRMATION_ID) String confirmationId,
    @ApiParam(value = "Choose report template, if is to print or not", required = true) @QueryParam(RodaConstants.API_PATH_PARAM_DISPOSAL_CONFIRMATION_REPORT_PRINT) boolean isToPrint,
    @ApiParam(value = "Choose format in which to get the disposal confirmation", allowableValues = RodaConstants.API_GET_METADATA_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException, IOException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    String report = Disposals.retrieveDisposalConfirmationReport(user, confirmationId, isToPrint);

    return Response.ok(report, mediaType).build();
  }

}
