/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path(NotificationsResource.ENDPOINT)
@Api(value = NotificationsResource.SWAGGER_ENDPOINT)
public class NotificationsResource {
  public static final String ENDPOINT = "/v1/notifications";
  public static final String SWAGGER_ENDPOINT = "v1 notifications";

  private static Logger LOGGER = LoggerFactory.getLogger(NotificationsResource.class);

  @Context
  private HttpServletRequest request;

  @GET
  @ApiOperation(value = "List Notifications", notes = "Gets a list of Notifications.", response = Notification.class, responseContainer = "List")
  public Response listNotifications(@QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    IndexResult<Notification> listNotificationsIndexResult = org.roda.wui.api.controllers.Browser.find(user,
      Notification.class, null, null, new Sublist(new Sublist(pagingParams.getFirst(), pagingParams.getSecond())),
      null);

    // transform controller method output
    List<Notification> notifications = org.roda.wui.api.controllers.Notifications
      .retrieveNotifications(listNotificationsIndexResult);

    return Response.ok(notifications, mediaType).build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Creates a new Notification", notes = "Creates a new Notification.", response = Notification.class)
  public Response createNotification(Notification notification,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Notification newNotification = org.roda.wui.api.controllers.Notifications.createNotification(user, notification);

    return Response.created(ApiUtils.getUriFromRequest(request)).entity(newNotification).type(mediaType).build();
  }

  @GET
  @Path("/{notificationId}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get Notification", notes = "Gets a particular Notification.", response = Notification.class)
  public Response getNotification(@PathParam("notificationId") String notificationId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Notification notification = org.roda.wui.api.controllers.Browser.retrieve(user, Notification.class, notificationId);

    return Response.ok(notification, mediaType).build();
  }

  @DELETE
  @Path("/{notificationId}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete Notification", notes = "Delete a particular Notification.", response = ApiResponseMessage.class)
  public Response deleteNotification(@PathParam("notificationId") String notificationId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    org.roda.wui.api.controllers.Notifications.deleteNotification(user, notificationId);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Notification deleted"), mediaType).build();
  }

  @GET
  @Path("/{notificationId}/acknowledge")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Acknowledge Notification", notes = "Acknowledge a particular Notification.", response = Notification.class)
  public Response acknowledgeNotification(@PathParam("notificationId") String notificationId,
    @QueryParam("token") String token, @QueryParam("email") String email,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    if (token == null) {
      return Response.ok(new ApiResponseMessage(ApiResponseMessage.ERROR, "Token argument is required"), mediaType)
        .build();
    }

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    org.roda.wui.api.controllers.Notifications.acknowledgeNotification(user, notificationId, token, email);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Notification acknowledged"), mediaType).build();
  }
}
