package org.roda.wui.api.v1;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.storage.utils.LocalInstanceUtils;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.controllers.BrowserHelper;
import org.roda.wui.api.controllers.RODAInstance;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Path(LocalInstanceResource.ENDPOINT)
@Api(value = LocalInstanceResource.SWAGGER_ENDPOINT)
public class LocalInstanceResource {
  public static final String ENDPOINT = "/v1/instance";
  public static final String SWAGGER_ENDPOINT = "v1 instance";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "Retrieve local instance configuration", notes = "", response = LocalInstance.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful response", response = LocalInstance.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrieveLocalInstanceConfiguration(
    @ApiParam(value = "Choose format in which to get the local instance configuration", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException, IOException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    LocalInstance localInstance = Browser.retrieveLocalInstance(user);

    return Response.ok(localInstance, mediaType).build();
  }

  @GET
  @Path("/periodic_verification")
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Consumes("multipart/*")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful response", response = LocalInstance.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})
  public Response synchronizeIfUpdated(@QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {

    String mediaType = ApiUtils.getMediaType(acceptFormat, request);
    // get user
    User user = UserUtility.getApiUser(request);
    LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
    Long totalUpdates = RODAInstance.synchronizeIfUpdated(user);
    String message = "There are no updates";
    if (totalUpdates > 0) {
      BrowserHelper.synchronizeBundle(user, localInstance);
      message = "There are " + totalUpdates + " updates";
    }
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, message), mediaType).build();
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "Create local instance configuration", notes = "Create a new local instance configuration", response = LocalInstance.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = LocalInstance.class),
      @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})

  public Response createLocalInstanceConfiguration(LocalInstance localInstance,
                                 @ApiParam(value = "Choose format in which to get the local instance configuration", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
                                 @ApiParam(value = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
      throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.createLocalInstance(user, localInstance);
    return Response.ok(localInstance, mediaType).build();
  }
}
