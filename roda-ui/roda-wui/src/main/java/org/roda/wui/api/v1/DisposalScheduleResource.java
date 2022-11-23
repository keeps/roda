/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import com.google.json.JsonSanitizer;
import java.io.IOException;

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

import org.glassfish.jersey.server.JSONP;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.controllers.Disposals;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Path(DisposalScheduleResource.ENDPOINT)
@Tag(name = DisposalScheduleResource.SWAGGER_ENDPOINT)
public class DisposalScheduleResource {
  public static final String ENDPOINT = "/v1/disposal/schedule";
  public static final String SWAGGER_ENDPOINT = "v1 disposal";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "List disposal schedules", description = "Gets a list of disposal schedules", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalSchedules.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response listSchedules(
    @Parameter(description = "Choose format in which to get the disposal schedule", schema = @Schema(implementation = RodaConstants.ListMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException, IOException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    DisposalSchedules disposalSchedules = Browser.listDisposalSchedules(user);

    return Response.ok(disposalSchedules, mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_DISPOSAL_SCHEDULE_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Get disposal schedule", description = "Get disposal schedule information", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalSchedule.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response retrieveSchedule(
    @Parameter(description = "The ID of the disposal schedule to retrieve.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_DISPOSAL_SCHEDULE_ID) String scheduleId,
    @Parameter(description = "Choose format in which to get the disposal schedule", schema = @Schema(implementation = RodaConstants.ListMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    DisposalSchedule schedule = Browser.retrieveDisposalSchedule(user, scheduleId);

    return Response.ok(schedule, mediaType).build();
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Create disposal schedule", description = "Creats a new disposal schedule", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalSchedule.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response createSchedule(DisposalSchedule schedule,
    @Parameter(description = "Choose format in which to get the disposal schedule", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // sanitize the input
    String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(schedule));
    schedule = JsonUtils.getObjectFromJson(sanitize, DisposalSchedule.class);

    // delegate action to controller
    DisposalSchedule disposalSchedule = Disposals.createDisposalSchedule(user, schedule);
    return Response.ok(disposalSchedule, mediaType).build();
  }

  @PUT
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Update disposal schedule", description = "Updates an existing disposal schedule", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalSchedule.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response updateDisposalSchedule(DisposalSchedule schedule,
    @Parameter(description = "Choose format in which to get the disposal schedule", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // sanitize the input
    String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(schedule));
    schedule = JsonUtils.getObjectFromJson(sanitize, DisposalSchedule.class);

    // delegate action to controller
    DisposalSchedule updateDisposalSchedule = Disposals.updateDisposalSchedule(user, schedule);
    return Response.ok(updateDisposalSchedule, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_DISPOSAL_SCHEDULE_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Delete disposal schedule", description = "Deletes a disposal schedule", responses = {
    @ApiResponse(responseCode = "204", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response deleteSchedule(
    @Parameter(description = "The ID of the disposal schedule to delete.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_DISPOSAL_SCHEDULE_ID) String disposalScheduleId,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Disposals.deleteDisposalSchedule(user, disposalScheduleId);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Disposal schedule deleted"), mediaType).build();
  }
}
