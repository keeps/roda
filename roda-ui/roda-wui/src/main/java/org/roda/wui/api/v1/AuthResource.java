/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.JSONP;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;
import org.roda.wui.filter.CasClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST API CAS authentication tickets resource.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
@Path("/v1/auth")
@Api(value = "v1 auth")
public class AuthResource {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthResource.class);

  /** HTTP request. */
  @Context
  private HttpServletRequest request;

  /**
   * Create a <strong>Ticket Granting Ticket</strong> for the specified user.
   * 
   * @param username
   *          the user username.
   * @param password
   *          the user password.
   *
   * @return a {@link Response} with the <strong>Ticket Granting Ticket</strong>
   *         .
   * @throws GenericException
   *           if some error occurred.
   */
  @POST
  @Path("/ticket")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Create an authorization ticket", response = String.class)
  public Response create(@FormParam("username") final String username, @FormParam("password") final String password)
    throws GenericException {
    final String casServerUrlPrefix = RodaCoreFactory.getRodaConfiguration()
      .getString("ui.filter.cas.casServerUrlPrefix");
    final CasClient casClient = new CasClient(casServerUrlPrefix);
    try {
      final String tgt = casClient.getTicketGrantingTicket(username, password);
      return Response.status(Response.Status.CREATED).entity(tgt).build();
    } catch (final AuthenticationDeniedException e) {
      LOGGER.debug(e.getMessage(), e);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    }
  }

  @GET
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Path("/login")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @ApiOperation(value = "GET call to login", notes = "GET call to login", response = User.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = User.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response loginGet(
    @ApiParam(value = "Choose format in which to get the User", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES_2, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name", required = false, allowMultiple = false, defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);
    return Response.ok(UserUtility.getApiUser(request), mediaType).build();
  }

  @POST
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Path("/login")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @ApiOperation(value = "POST call to login", notes = "POST call to login", response = User.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = User.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class),
    @ApiResponse(code = 401, message = "Not authorized", response = ApiResponseMessage.class)})

  public Response loginPost(
    @ApiParam(value = "Choose format in which to get the User", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES_2, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name", required = false, allowMultiple = false, defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    return loginGet(acceptFormat, jsonpCallbackName);
  }

}
