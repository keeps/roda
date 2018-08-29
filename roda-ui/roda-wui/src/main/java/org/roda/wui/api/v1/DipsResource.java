/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.roda.core.common.EntityResponse;
import org.roda.core.common.StreamResponse;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPs;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.controllers.Dips;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;
import org.roda.wui.api.v1.utils.ObjectResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path(DipsResource.ENDPOINT)
@Api(value = DipsResource.SWAGGER_ENDPOINT)
public class DipsResource {
  public static final String ENDPOINT = "/v1/dips";
  public static final String SWAGGER_ENDPOINT = "v1 dips";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "List DIPs", notes = "Gets a list of DIPs.", response = DIPs.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = DIPs.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response listDIPs(
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the DIP", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    IndexResult<IndexedDIP> result = Browser.find(IndexedDIP.class, Filter.NULL, Sorter.NONE,
      new Sublist(pagingParams.getFirst(), pagingParams.getSecond()), null, user, false, new ArrayList<>());

    return Response.ok(ApiUtils.indexedResultToRODAObjectList(IndexedDIP.class, result), mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_DIP_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP})
  @ApiOperation(value = "Get DIP", notes = "Get DIP information", response = DIP.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DIP.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrieveDIP(
    @ApiParam(value = "The ID of the DIP to retrieve.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_DIP_ID) String dipId,
    @ApiParam(value = "Choose format in which to get the DIP", allowableValues = RodaConstants.API_GET_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse dipResponse = Browser.retrieveDIP(user, dipId, acceptFormat);

    if (dipResponse instanceof ObjectResponse) {
      ObjectResponse<DIP> rep = (ObjectResponse<DIP>) dipResponse;
      return Response.ok(rep.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) dipResponse);
    }
  }

  @POST
  @ApiOperation(value = "Create DIP", notes = "Create a new DIP", response = DIP.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DIP.class),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})

  public Response createDIP(DIP dip,
    @ApiParam(value = "Choose format in which to get the DIP", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    DIP createdDip = Dips.createDIP(user, dip);
    return Response.ok(createdDip, mediaType).build();
  }

  @PUT
  @ApiOperation(value = "Update DIP", notes = "Update existing DIP", response = DIP.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DIP.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updateDIP(DIP dip,
    @ApiParam(value = "Choose format in which to get the DIP", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    DIP updatedDIP = Dips.updateDIP(user, dip);
    return Response.ok(updatedDIP, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_DIP_ID + "}")
  @ApiOperation(value = "Delete DIP", notes = "Delete DIP", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deleteDIP(
    @ApiParam(value = "The ID of the DIP to delete.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_DIP_ID) String dipId,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.deleteDIPs(user, SelectedItemsList.create(IndexedDIP.class, dipId));

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "DIP deleted"), mediaType).build();
  }
}
