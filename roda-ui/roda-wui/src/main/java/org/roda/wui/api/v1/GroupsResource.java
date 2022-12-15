/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import com.google.json.JsonSanitizer;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAGroups;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.controllers.UserManagement;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(GroupsResource.ENDPOINT)
@Tag(name = GroupsResource.SWAGGER_ENDPOINT)
public class GroupsResource {
  public static final String ENDPOINT = "/v1/groups";
  public static final String SWAGGER_ENDPOINT = "v1 groups";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "List groups", description = "Gets a list of groups", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RODAGroups.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response listGroups(
    @Parameter(description = "Index of the first element to return", schema = @Schema(defaultValue = "0")) @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @Parameter(description = "Maximum number of elements to return", schema = @Schema(defaultValue = "100")) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @Parameter(description = "Choose format in which to get the groups", schema = @Schema(implementation = RodaConstants.ListMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    boolean isUser = false;
    boolean justActive = false;
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, Boolean.toString(isUser)));

    IndexResult<RODAMember> result = Browser.find(RODAMember.class, filter, Sorter.NONE,
      new Sublist(pagingParams.getFirst(), pagingParams.getSecond()), null, user, justActive, Collections.emptyList());

    return Response
      .ok(new RODAGroups(result.getResults().stream().map(o -> (Group) o).collect(Collectors.toList())), mediaType)
      .build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Create group", description = "Creats a new group", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Group.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response createGroup(Group newGroup,
    @Parameter(description = "Choose format in which to get the group", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // sanitize the input
    String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(newGroup));
    newGroup = JsonUtils.getObjectFromJson(sanitize, Group.class);

    // delegate action to controller
    UserManagement.createGroup(user, newGroup);
    return Response.ok(newGroup, mediaType).build();
  }

  @PUT
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Update group", description = "Updates an existing group", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Group.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response updateGroup(Group modifiedGroup,
    @Parameter(description = "Choose format in which to get the group", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // sanitize the input
    String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(modifiedGroup));
    modifiedGroup = JsonUtils.getObjectFromJson(sanitize, Group.class);

    // delegate action to controller
    UserManagement.updateGroup(user, modifiedGroup);
    return Response.ok(modifiedGroup, mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_NAME + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Get group", description = "Gets a group information", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Group.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response getGroup(@PathParam(RodaConstants.API_PATH_PARAM_NAME) String name,
    @Parameter(description = "Choose format in which to get the group", schema = @Schema(implementation = RodaConstants.ListMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Group rodaGroup = org.roda.wui.api.controllers.UserManagement.retrieveGroup(user, name);
    return Response.ok(rodaGroup, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_NAME + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Delete group", description = "Deletes an existing group", responses = {
    @ApiResponse(responseCode = "204", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response deleteGroup(@PathParam(RodaConstants.API_PATH_PARAM_NAME) String name,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    org.roda.wui.api.controllers.UserManagement.deleteGroup(user, name);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Group deleted"), mediaType).build();
  }
}
