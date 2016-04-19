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
import org.roda.core.data.v2.messages.Message;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path(MessagesResource.ENDPOINT)
@Api(value = MessagesResource.SWAGGER_ENDPOINT)
public class MessagesResource {
  public static final String ENDPOINT = "/v1/messages";
  public static final String SWAGGER_ENDPOINT = "v1 messages";

  private static Logger LOGGER = LoggerFactory.getLogger(MessagesResource.class);

  @Context
  private HttpServletRequest request;

  @GET
  @ApiOperation(value = "List Messages", notes = "Gets a list of Messages.", response = Message.class, responseContainer = "List")
  public Response listMessages(@QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());

    // delegate action to controller
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    IndexResult<Message> listMessagesIndexResult = org.roda.wui.api.controllers.Browser.find(user, Message.class, null,
      null, new Sublist(new Sublist(pagingParams.getFirst(), pagingParams.getSecond())), null);

    // transform controller method output
    List<Message> messages = org.roda.wui.api.controllers.Messages.retrieveMessages(listMessagesIndexResult);

    return Response.ok(messages, mediaType).build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Creates a new Message", notes = "Creates a new Message.", response = Message.class)
  public Response createMessage(Message message,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    Message newMessage = org.roda.wui.api.controllers.Messages.createMessage(user, message);

    return Response.created(ApiUtils.getUriFromRequest(request)).entity(newMessage).type(mediaType).build();
  }

  @GET
  @Path("/{messageId}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get Message", notes = "Gets a particular Message.", response = Message.class)
  public Response getMessage(@PathParam("messageId") String messageId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    Message message = org.roda.wui.api.controllers.Browser.retrieve(user, Message.class, messageId);

    return Response.ok(message, mediaType).build();
  }

  @DELETE
  @Path("/{messageId}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete Message", notes = "Delete a particular Message.", response = ApiResponseMessage.class)
  public Response deleteMessage(@PathParam("messageId") String messageId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    org.roda.wui.api.controllers.Messages.deleteMessage(user, messageId);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Message deleted"), mediaType).build();
  }

  @GET
  @Path("/{messageId}/acknowledge")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Acknowledge Message", notes = "Acknowledge a particular Message.", response = Message.class)
  public Response acknowledgeMessage(@PathParam("messageId") String messageId, @QueryParam("token") String token,
    @QueryParam("email") String email, @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    if (token == null) {
      return Response.ok(new ApiResponseMessage(ApiResponseMessage.ERROR, "Token argument is required"), mediaType)
        .build();
    }

    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    org.roda.wui.api.controllers.Messages.acknowledgeMessage(user, messageId, token, email);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Message acknowledged"), mediaType).build();
  }
}
