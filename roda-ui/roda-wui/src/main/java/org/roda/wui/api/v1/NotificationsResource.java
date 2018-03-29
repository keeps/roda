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

import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.notifications.Notifications;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path(NotificationsResource.ENDPOINT)
@Api(value = NotificationsResource.SWAGGER_ENDPOINT)
public class NotificationsResource {
  public static final String ENDPOINT = "/v1/notifications";
  public static final String SWAGGER_ENDPOINT = "v1 notifications";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "List notifications", notes = "Get a list of notifications.", response = Notifications.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = Notifications.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response listNotifications(
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the notifications", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    boolean justActive = false;
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    IndexResult<Notification> result = Browser.find(Notification.class, Filter.NULL, Sorter.NONE,
      new Sublist(pagingParams.getFirst(), pagingParams.getSecond()), null, user, justActive, new ArrayList<>());
    return Response.ok(ApiUtils.indexedResultToRODAObjectList(Notification.class, result), mediaType).build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Create notification", notes = "Create a new notification.", response = Notification.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Notification.class),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})

  public Response createNotification(Notification notification,
    @ApiParam(value = "Template name", defaultValue = RodaConstants.API_NOTIFICATION_DEFAULT_TEMPLATE) @QueryParam(RodaConstants.API_QUERY_PARAM_TEMPLATE) String template,
    @ApiParam(value = "Choose format in which to get the notification", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Notification newNotification = org.roda.wui.api.controllers.Notifications.createNotification(user, notification,
      template);
    return Response.ok(newNotification, mediaType).build();
  }

  @PUT
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Update notification", notes = "Update a notification.", response = Notification.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Notification.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updateNotification(Notification notification,
    @ApiParam(value = "Choose format in which to get the notification", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Notification updatedNotification = org.roda.wui.api.controllers.Notifications.updateNotification(user,
      notification);
    return Response.ok(updatedNotification, mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_NOTIFICATION_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get notification", notes = "Get a notification.", response = Notification.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Notification.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response getNotification(@PathParam(RodaConstants.API_PATH_PARAM_NOTIFICATION_ID) String notificationId,
    @ApiParam(value = "Choose format in which to get the notification", allowableValues = RodaConstants.API_GET_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Notification notification = org.roda.wui.api.controllers.Browser.retrieve(user, Notification.class, notificationId,
      new ArrayList<>());
    return Response.ok(notification, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_NOTIFICATION_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete notification", notes = "Delete a notification.", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deleteNotification(@PathParam(RodaConstants.API_PATH_PARAM_NOTIFICATION_ID) String notificationId,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    org.roda.wui.api.controllers.Notifications.deleteNotification(user, notificationId);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Notification deleted"), mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_NOTIFICATION_ID + "}/" + RodaConstants.API_ACKNOWLEDGE)
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Acknowledge notification", notes = "Acknowledge a notification.", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response acknowledgeNotification(
    @ApiParam(value = "The notification id", required = true) @PathParam(RodaConstants.API_PATH_PARAM_NOTIFICATION_ID) String notificationId,
    @ApiParam(value = "The notification user token (with email uuid)", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_NOTIFICATION_TOKEN) String token,
    @ApiParam(value = "Choose format in which to get the result", allowableValues = RodaConstants.API_GET_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    if (token == null) {
      return Response.ok(new ApiResponseMessage(ApiResponseMessage.ERROR, "Token argument is required"), mediaType)
        .build();
    }

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.acknowledgeNotification(user, notificationId, token);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Notification acknowledged"), mediaType).build();
  }
}
