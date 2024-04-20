/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.io.IOException;

import org.glassfish.jersey.server.JSONP;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHolds;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.controllers.Disposals;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;

import com.google.json.JsonSanitizer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

@Path(DisposalHoldResource.ENDPOINT)
@Tag(name = DisposalHoldResource.SWAGGER_ENDPOINT)
public class DisposalHoldResource {
  public static final String ENDPOINT = "/disposal/hold";
  public static final String SWAGGER_ENDPOINT = "v1 disposal";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "List disposal holds", description = "Gets a list of disposal holds", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalHolds.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response listHolds(
    @Parameter(description = "Choose format in which to get the disposal hold", schema = @Schema(implementation = RodaConstants.ListMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
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
  @Operation(summary = "Get disposal hold", description = "Get disposal hold information", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalHold.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response retrieveHold(
    @Parameter(description = "The ID of the disposal hold to retrieve.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_DISPOSAL_HOLD_ID) String holdId,
    @Parameter(description = "Choose format in which to get the disposal hold", schema = @Schema(implementation = RodaConstants.ListMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
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
  @Operation(summary = "Create disposal hold", description = "Create a new disposal hold", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalHold.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response createHold(DisposalHold hold,
    @Parameter(description = "Choose format in which to get the disposal hold", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // sanitize the input
    String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(hold));
    hold = JsonUtils.getObjectFromJson(sanitize, DisposalHold.class);

    // delegate action to controller
    DisposalHold disposalhold = Disposals.createDisposalHold(user, hold);
    return Response.ok(disposalhold, mediaType).build();
  }

  @PUT
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Update disposal hold", description = "Update existing disposal hold", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalHold.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response updateDisposalHold(DisposalHold hold,
    @Parameter(description = "Choose format in which to get the disposal hold", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // sanitize the input
    String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(hold));
    hold = JsonUtils.getObjectFromJson(sanitize, DisposalHold.class);

    // delegate action to controller
    DisposalHold updateDisposalHold = Disposals.updateDisposalHold(user, hold);
    return Response.ok(updateDisposalHold, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_DISPOSAL_HOLD_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Delete disposal hold", description = "Delete disposal hold", responses = {
    @ApiResponse(responseCode = "204", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response deleteHold(
    @Parameter(description = "The ID of the disposal hold to delete.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_DISPOSAL_HOLD_ID) String disposalHoldId,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Disposals.deleteDisposalHold(user, disposalHoldId);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Disposal hold deleted"), mediaType).build();
  }

}
