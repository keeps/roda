/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.io.IOException;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.server.JSONP;
import org.roda.core.data.v2.EntityResponse;
import org.roda.core.data.v2.StreamResponse;
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
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.RODAInstance;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
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
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@Path(DistributedInstancesResource.ENDPOINT)
@Tag(name = DistributedInstancesResource.SWAGGER_ENDPOINT)
public class DistributedInstancesResource {
  public static final String ENDPOINT = "/v1/distributed_instances";
  public static final String SWAGGER_ENDPOINT = "v1 distributed instances";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  // @ApiOperation(value = "List distributed instances", notes = "Gets a list of
  // disposal schedule.", response = DistributedInstance.class, responseContainer
  // = "List")
  // @ApiResponses(value = {
  // @ApiResponse(code = 200, message = "Successful response", response =
  // DistributedInstance.class, responseContainer = "List"),
  // @ApiResponse(code = 404, message = "Not found", response =
  // ApiResponseMessage.class)})

  public Response listDistributedInstances(
    @Parameter(description = "Choose format in which to get the distributed instances", schema = @Schema(implementation = RodaConstants.ListMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
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
  // @ApiOperation(value = "register a distributed instance", notes = "Register a
  // new distributed instance", response = LocalInstance.class)
  // @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response =
  // LocalInstance.class),
  // @ApiResponse(code = 409, message = "Already exists", response =
  // ApiResponseMessage.class)})

  public Response registerDistributedInstance(LocalInstance localInstance,
    @Parameter(description = "Choose format in which to get the local instance configuration", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name") @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
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
    @Parameter(description = "The instance identifier", required = true) @PathParam(RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER) String instanceIdentifier,
    FormDataMultiPart file,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
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
  // @ApiResponses(value = {
  // @ApiResponse(code = 200, message = "OK", response =
  // org.roda.core.data.v2.ip.TransferredResource.class),
  // @ApiResponse(code = 404, message = "Not found", response =
  // ApiResponseMessage.class)})

  public Response remoteActions(
    @Parameter(description = "The instance identifier", required = true) @PathParam(RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER) String instanceIdentifier)
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
  // @ApiResponses(value = {
  // @ApiResponse(code = 200, message = "OK", response =
  // org.roda.core.data.v2.ip.TransferredResource.class),
  // @ApiResponse(code = 404, message = "Not found", response =
  // ApiResponseMessage.class)})

  public Response status(
    @Parameter(description = "The instance identifier", required = true) @PathParam(RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER) String instanceIdentifier,
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
  // @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response =
  // StreamResponse.class),
  // @ApiResponse(code = 404, message = "Not found", response =
  // ApiResponseMessage.class)})
  public Response synchronizationStatus(
    @Parameter(description = "The instance identifier", required = true) @PathParam(RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER) String instanceIdentifier,
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
  // @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response =
  // StreamResponse.class),
  // @ApiResponse(code = 404, message = "Not found", response =
  // ApiResponseMessage.class)})
  public Response removeSyncBundleFromCentral(@QueryParam(RodaConstants.SYNCHRONIZATION_BUNDLE_NAME) String bundleName,
    @QueryParam(RodaConstants.SYNCHRONIZATION_BUNDLE_DIRECTORY) String bundleDirectory) throws RODAException {
    // get user
    final User user = UserUtility.getApiUser(request);
    // delegate action to controller.
    String response = RODAInstance.removeSyncBundle(bundleName, user, bundleDirectory);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, response)).build();
  }

  @GET
  @Path("/updates/{" + RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER + "}")
  @Produces({MediaType.APPLICATION_JSON})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  // @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response =
  // StreamResponse.class),
  // @ApiResponse(code = 404, message = "Not found", response =
  // ApiResponseMessage.class)})
  public Response getUpdates(
    @Parameter(description = "The instance identifier", required = true) @PathParam(RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER) String instanceIdentifier)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    // get user
    final User user = UserUtility.getApiUser(request);
    // delegate action to controller.
    Long result = RODAInstance.retrieveCentralInstanceUpdates(user, instanceIdentifier);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, result.toString())).build();
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
  // @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response =
  // StreamResponse.class),
  // @ApiResponse(code = 404, message = "Not found", response =
  // ApiResponseMessage.class)})
  public Response removeInstanceConfiguration(
    @Parameter(description = "The instance identifier", required = true) @PathParam(RodaConstants.API_PATH_PARAM_INSTANCE_IDENTIFIER) String instanceIdentifier) {
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
  // @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response =
  // StreamResponse.class),
  // @ApiResponse(code = 404, message = "Not found", response =
  // ApiResponseMessage.class)})
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
