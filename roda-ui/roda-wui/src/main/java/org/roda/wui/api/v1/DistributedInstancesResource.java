package org.roda.wui.api.v1;

import java.io.IOException;

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

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.server.JSONP;
import org.roda.core.common.EntityResponse;
import org.roda.core.common.StreamResponse;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.RODAInstance;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@Path(DistributedInstancesResource.ENDPOINT)
@Api(value = DistributedInstancesResource.SWAGGER_ENDPOINT)
public class DistributedInstancesResource {
  public static final String ENDPOINT = "/v1/distributed_instances";
  public static final String SWAGGER_ENDPOINT = "v1 distributed instances";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "List distributed instances", notes = "Gets a list of disposal schedule.", response = DistributedInstance.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = DistributedInstance.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response listDistributedInstances(
    @ApiParam(value = "Choose format in which to get the distributed instances", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException, IOException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    DistributedInstances distributedInstances = RODAInstance.listDistributedInstances(user);

    return Response.ok(distributedInstances, mediaType).build();
  }

  @POST
  @Path("/register")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "register a distributed instance", notes = "Register a new distributed instance", response = LocalInstance.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = LocalInstance.class),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})

  public Response registerDistributedInstance(LocalInstance localInstance,
    @ApiParam(value = "Choose format in which to get the local instance configuration", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    RODAInstance.registerDistributedInstance(user, localInstance);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Distributed instance registered"), mediaType)
      .build();
  }

  @POST
  @Path("/sync/{" + RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER + "}")
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Consumes("multipart/*")
  public Response synchronize(
    @ApiParam(value = "The instance identifier", required = true) @PathParam(RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER) String instanceIdentifier,
    FormDataMultiPart file,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    RODAInstance.importSyncBundle(user, instanceIdentifier, file);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Bundle entries imported"), mediaType).build();
  }

  @GET
  @Path("/remote_actions/{" + RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = org.roda.core.data.v2.ip.TransferredResource.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response remoteActions(
    @ApiParam(value = "The instance identifier", required = true) @PathParam(RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER) String instanceIdentifier)
    throws RODAException {

    // get user
    User user = UserUtility.getApiUser(request);

    EntityResponse response = RODAInstance.retrieveRemoteActions(user, instanceIdentifier);

    if (response instanceof StreamResponse) {
      return ApiUtils.okResponse((StreamResponse) response);
    } else {
      return Response.noContent().build();
    }
  }

  @GET
  @Path("/status/{" + RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = org.roda.core.data.v2.ip.TransferredResource.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response status(
    @ApiParam(value = "The instance identifier", required = true) @PathParam(RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER) String instanceIdentifier,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);
    // get user
    User user = UserUtility.getApiUser(request);
    return Response.ok(RODAInstance.retrieveLocalInstanceStatus(user, instanceIdentifier), mediaType).build();
  }

  @GET
  @Path("/sync/status/{" + RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER + "}")
  @Produces({MediaType.APPLICATION_JSON})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = StreamResponse.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})
  public Response synchronizationStatus(
    @ApiParam(value = "The instance identifier", required = true) @PathParam(RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER) String instanceIdentifier,
    @QueryParam(RodaConstants.API_QUERY_KEY_CLASS) String entityClass,
    @QueryParam(RodaConstants.API_QUERY_KEY_TYPE) String type) throws RODAException {
    // get user
    final User user = UserUtility.getApiUser(request);
    // delegate action to controller.
    EntityResponse response = RODAInstance.retrieveLastSyncFile(user, instanceIdentifier, entityClass, type);
    return ApiUtils.okResponse((StreamResponse) response);
  }

  @GET
  @Path("/remove/bundle/")
  @Produces({MediaType.APPLICATION_JSON})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = StreamResponse.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})
  public Response removeSyncBundleFromCentral(@QueryParam(RodaConstants.SYNCHRONIZATION_BUNDLE_NAME) String bundleName,
    @QueryParam(RodaConstants.SYNCHRONIZATION_BUNDLE_DIRECTORY) String bundleDirectory) throws RODAException {
    // get user
    final User user = UserUtility.getApiUser(request);
    // delegate action to controller.
    String response = RODAInstance.removeSyncBundle(bundleName, user, bundleDirectory);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, response)).build();
  }

  /**
   * Route to delete in the central repository. This route is called when the
   * instance in a local repository is deleted.
   * 
   * @param instanceIdentifier
   *          The instanceIdentifier of local instance
   * @return the success message or the failure message.
   */
  @DELETE
  @Path("{" + RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER + "}")
  @Produces({MediaType.APPLICATION_JSON})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = StreamResponse.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})
  public Response removeInstanceConfiguration(
    @ApiParam(value = "The instance identifier", required = true) @PathParam(RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER) String instanceIdentifier) {
    final User user = UserUtility.getApiUser(request);
    try {
      RODAInstance.deleteDistributedInstance(user, instanceIdentifier);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Instance removed with success")).build();
  }

  /**
   * Route to deactivate local instance in central repository.
   *
   * @return the success message or the failure message.
   */
  @PUT
  @Path("/update")
  @Produces({MediaType.APPLICATION_JSON})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = StreamResponse.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})
  public Response update(DistributedInstance distributedInstance) {
    final User user = UserUtility.getApiUser(request);
    try {
      RODAInstance.updateDistributedInstance(user, distributedInstance);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Instance removed with success")).build();
  }
}
