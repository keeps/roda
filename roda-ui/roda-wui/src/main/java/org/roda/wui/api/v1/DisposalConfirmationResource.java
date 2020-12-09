package org.roda.wui.api.v1;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
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
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationMetadata;
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
  @ApiOperation(value = "List disposal confirmations", notes = "Gets a list of disposal confirmations.", response = DisposalConfirmationMetadata.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = DisposalConfirmationMetadata.class, responseContainer = "List"),
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
    IndexResult<DisposalConfirmationMetadata> result = Browser.find(DisposalConfirmationMetadata.class, Filter.NULL,
      Sorter.NONE, new Sublist(pagingParams.getFirst(), pagingParams.getSecond()), null, user, justActive,
      new ArrayList<>());

    return Response.ok(ApiUtils.indexedResultToRODAObjectList(DisposalConfirmationMetadata.class, result), mediaType)
      .build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_DISPOSAL_CONFIRMATION_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "Get Disposal confirmation", notes = "Get disposal confirmation", response = DisposalConfirmationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DisposalConfirmationMetadata.class),
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
    DisposalConfirmationMetadata disposalConfirmationMetadata = Browser.retrieve(user,
      DisposalConfirmationMetadata.class, confirmationId, new ArrayList<>());

    return Response.ok(disposalConfirmationMetadata, mediaType).build();
  }

  /*
   * @POST
   * 
   * @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   * 
   * @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam
   * = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
   * 
   * @ApiOperation(value = "Create disposal confirmation", notes =
   * "Create a new disposal confirmation", response =
   * DisposalConfirmationMetadata.class)
   * 
   * @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response =
   * DisposalConfirmationMetadata.class),
   * 
   * @ApiResponse(code = 409, message = "Already exists", response =
   * ApiResponseMessage.class)}) public Response
   * createDisposalConfirmation(DisposalConfirmationMetadata metadata,
   * 
   * @ApiParam(value = "Choose format in which to get the disposal confirmation",
   * allowableValues =
   * RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.
   * API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
   * 
   * @ApiParam(value = "JSONP callback name") @QueryParam(RodaConstants.
   * API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName) throws RODAException
   * { String mediaType = ApiUtils.getMediaType(acceptFormat, request);
   * 
   * // get user User user = UserUtility.getApiUser(request);
   * 
   * // delegate action to controller DisposalConfirmationMetadata
   * disposalConfirmationMetadata = Disposals.createDisposalConfirmation(user,
   * metadata); return Response.ok(disposalConfirmationMetadata,
   * mediaType).build(); }
   */

  /*@DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_DISPOSAL_CONFIRMATION_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "Delete disposal confirmation", notes = "Delete disposal confirmation", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class),
    @ApiResponse(code = 405, message = "Operation not allowed", response = ApiResponseMessage.class)})

  public Response deleteDisposalConfirmation(
    @ApiParam(value = "The ID of the disposal confirmation to delete.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_DISPOSAL_CONFIRMATION_ID) String disposalConfirmationId,
    @ApiParam(value = "Reason to delete disposal confirmation", required = true) @FormParam(RodaConstants.API_QUERY_PARAM_DETAILS) String details,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Disposals.deleteDisposalConfirmation(user, disposalConfirmationId);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Disposal confirmation deleted"), mediaType)
      .build();
  }*/
}
