/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
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

import org.glassfish.jersey.server.JSONP;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHolds;
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
 * @author Tiago Fraga <tfraga@keep.pt>
 */

@Path(DisposalHoldResource.ENDPOINT)
@Api(value = DisposalHoldResource.SWAGGER_ENDPOINT)
public class DisposalHoldResource {
  public static final String ENDPOINT = "/v1/disposal/hold";
  public static final String SWAGGER_ENDPOINT = "v1 disposal";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "List disposal holds", notes = "Gets a list of disposal holds.", response = DisposalHold.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = DisposalHold.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response listHolds(
    @ApiParam(value = "Choose format in which to get the disposal hold", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException, IOException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    DisposalHolds disposalHolds = Browser.listDisposalHolds(user);

    return Response.ok(disposalHolds, mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_DISPOSAL_HOLD_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "Get Disposal hold", notes = "Get disposal hold information", response = DisposalHold.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DisposalHold.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrieveHold(
    @ApiParam(value = "The ID of the disposal hold to retrieve.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_DISPOSAL_HOLD_ID) String holdId,
    @ApiParam(value = "Choose format in which to get the disposal hold", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    DisposalHold hold = Browser.retrieveDisposalHold(user, holdId);

    return Response.ok(hold, mediaType).build();
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "Create disposal hold", notes = "Create a new disposal hold", response = DisposalHold.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DisposalHold.class),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})

  public Response createHold(DisposalHold hold,
    @ApiParam(value = "Choose format in which to get the disposal hold", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    DisposalHold disposalhold = Disposals.createDisposalHold(user, hold);
    return Response.ok(disposalhold, mediaType).build();
  }

  @PUT
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "Update disposal hold", notes = "Update existing disposal hold", response = DisposalHold.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DisposalHold.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updateDisposalHold(DisposalHold hold,
    @ApiParam(value = "Choose format in which to get the disposal hold", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    DisposalHold updateDisposalHold = Disposals.updateDisposalHold(user, hold);
    return Response.ok(updateDisposalHold, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_DISPOSAL_HOLD_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "Delete disposal hold", notes = "Delete disposal hold", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deleteHold(
    @ApiParam(value = "The ID of the disposal hold to delete.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_DISPOSAL_HOLD_ID) String disposalHoldId,
    @ApiParam(value = "Reason to delete disposal hold", required = true) @FormParam(RodaConstants.API_QUERY_PARAM_DETAILS) String details,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Disposals.deleteDisposalHold(user, disposalHoldId);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Disposal hold deleted"), mediaType).build();
  }

}
