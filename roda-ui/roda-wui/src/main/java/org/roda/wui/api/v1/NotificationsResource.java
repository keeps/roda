/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

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
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.notifications.Notifications;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path(NotificationsResource.ENDPOINT)
@Api(value = NotificationsResource.SWAGGER_ENDPOINT)
public class NotificationsResource {
  public static final String ENDPOINT = "/v1/notifications";
  public static final String SWAGGER_ENDPOINT = "v1 notifications";

  @Context
  private HttpServletRequest request;

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "List Notifications", notes = "Gets a list of Notifications.", response = Notification.class, responseContainer = "List")
  public Response listNotifications(
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the notifications", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Notifications notifications = (Notifications) Browser.retrieveObjects(user, Notification.class, start, limit,
      acceptFormat);
    return Response.ok(notifications, mediaType).build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Creates a new Notification", notes = "Creates a new Notification.", response = Notification.class)
  public Response createNotification(Notification notification,
    @ApiParam(value = "Choose format in which to get the notification", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Notification newNotification = org.roda.wui.api.controllers.Notifications.createNotification(user, notification);
    return Response.ok(newNotification, mediaType).build();
  }

  @PUT
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Updates a Notification", notes = "Updates a Notification.", response = Notification.class)
  public Response updateNotification(Notification notification,
    @ApiParam(value = "Choose format in which to get the notification", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
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
  @ApiOperation(value = "Get Notification", notes = "Gets a particular Notification.", response = Notification.class)
  public Response getNotification(@PathParam(RodaConstants.API_PATH_PARAM_NOTIFICATION_ID) String notificationId,
    @ApiParam(value = "Choose format in which to get the notification", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Notification notification = org.roda.wui.api.controllers.Browser.retrieve(user, Notification.class, notificationId);
    return Response.ok(notification, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_NOTIFICATION_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete Notification", notes = "Delete a particular Notification.", response = ApiResponseMessage.class)
  public Response deleteNotification(@PathParam(RodaConstants.API_PATH_PARAM_NOTIFICATION_ID) String notificationId,
    @ApiParam(value = "Choose format in which to get the result", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    org.roda.wui.api.controllers.Notifications.deleteNotification(user, notificationId);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Notification deleted"), mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_NOTIFICATION_ID + "}/acknowledge")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Acknowledge Notification", notes = "Acknowledge a particular Notification.", response = Notification.class)
  public Response acknowledgeNotification(
    @ApiParam(value = "The notification id", required = true) @PathParam(RodaConstants.API_PATH_PARAM_NOTIFICATION_ID) String notificationId,
    @ApiParam(value = "The notification user token (with email uuid)", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_NOTIFICATION_TOKEN) String token,
    @ApiParam(value = "Choose format in which to get the result", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    if (token == null) {
      return Response.ok(new ApiResponseMessage(ApiResponseMessage.ERROR, "Token argument is required"), mediaType)
        .build();
    }

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    org.roda.wui.api.controllers.Notifications.acknowledgeNotification(user, notificationId, token);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Notification acknowledged"), mediaType).build();
  }
}
