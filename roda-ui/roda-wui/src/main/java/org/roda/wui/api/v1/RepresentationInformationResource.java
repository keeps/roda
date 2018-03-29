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

import org.roda.core.common.EntityResponse;
import org.roda.core.common.StreamResponse;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationList;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ObjectResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path(RepresentationInformationResource.ENDPOINT)
@Api(value = RepresentationInformationResource.SWAGGER_ENDPOINT)
public class RepresentationInformationResource {
  public static final String ENDPOINT = "/v1/representation_information";
  public static final String SWAGGER_ENDPOINT = "v1 representation_information";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "List representation information", notes = "Get a list of representation information.", response = RepresentationInformationList.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = RepresentationInformationList.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response listRepresentationInformation(
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the representation information", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    boolean justActive = false;
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    IndexResult<RepresentationInformation> result = Browser.find(RepresentationInformation.class, Filter.NULL,
      Sorter.NONE, new Sublist(pagingParams.getFirst(), pagingParams.getSecond()), null, user, justActive,
      new ArrayList<>());
    return Response.ok(ApiUtils.indexedResultToRODAObjectList(RepresentationInformation.class, result), mediaType)
      .build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Create representation information", notes = "Create a new representation information.", response = RepresentationInformation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = RepresentationInformation.class),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})

  public Response createRepresentationInformation(RepresentationInformation ri,
    @ApiParam(value = "Choose format in which to get the representation information", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    RepresentationInformation newRepresentationInformation = org.roda.wui.api.controllers.RepresentationInformations
      .createRepresentationInformation(user, ri);
    return Response.ok(newRepresentationInformation, mediaType).build();
  }

  @PUT
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Update representation information", notes = "Update representation information.", response = RepresentationInformation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = RepresentationInformation.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updateRepresentationInformation(RepresentationInformation ri,
    @ApiParam(value = "Choose format in which to get the representation information", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    RepresentationInformation updatedRepresentationInformation = org.roda.wui.api.controllers.RepresentationInformations
      .updateRepresentationInformation(user, ri);
    return Response.ok(updatedRepresentationInformation, mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_INFORMATION_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get representation information", notes = "Get a representation information.", response = RepresentationInformation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = RepresentationInformation.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response getRepresentationInformation(
    @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_INFORMATION_ID) String representationInformationId,
    @ApiParam(value = "Choose format in which to get the representation information", allowableValues = RodaConstants.API_GET_FILE_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse riEntity = Browser.retrieveRepresentationInformation(user, representationInformationId,
      acceptFormat);

    if (riEntity instanceof ObjectResponse) {
      ObjectResponse<RepresentationInformation> ri = (ObjectResponse<RepresentationInformation>) riEntity;
      return Response.ok(ri.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) riEntity);
    }
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_INFORMATION_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete representation information", notes = "Delete a representation information.", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deleteRepresentationInformation(
    @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_INFORMATION_ID) String representationInformationId,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    org.roda.wui.api.controllers.RepresentationInformations.deleteRepresentationInformation(user,
      representationInformationId);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Representation information deleted"), mediaType)
      .build();
  }
}
