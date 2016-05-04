/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
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

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.StreamResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path(RepresentationsResource.ENDPOINT)
@Api(value = RepresentationsResource.SWAGGER_ENDPOINT)
public class RepresentationsResource {
  public static final String ENDPOINT = "/v1/representations";
  public static final String SWAGGER_ENDPOINT = "v1 representations";

  @Context
  private HttpServletRequest request;

  @GET
  @Path("/{" + RodaConstants.REPRESENTATION_UUID + "}")
  @Produces({"application/json", "application/zip"})
  @ApiOperation(value = "Get representation", notes = "Get representation", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 404, message = "Not found", response = Representation.class)})

  public Response getRepresentation(
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.REPRESENTATION_UUID) String representationUUID,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = "json, bin") @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    StreamResponse aipRepresentation = Browser.getAipRepresentation(user, representationUUID, acceptFormat);

    return ApiUtils.okResponse(aipRepresentation);
  }

  @GET
  @Path("/{" + RodaConstants.REPRESENTATION_UUID + "}/{part}")
  //@Produces({"application/json", MediaType.APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Download part of the representation")
  public Response getRepresentationPart(
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.REPRESENTATION_UUID) String representationUUID,
    @ApiParam(value = "The part of the representation to download", required = true, allowableValues = "data, metadata, documentation, schemas") @PathParam("part") String part)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    StreamResponse aipRepresentation = Browser.getAipRepresentationPart(user, representationUUID, part);

    return ApiUtils.okResponse(aipRepresentation);
  }

  @PUT
  @Path("{representation_uuid}")
  @ApiOperation(value = "Update representation", notes = "Update existing representation", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 404, message = "Not found", response = Representation.class)})

  public Response updateRepresentation(
    @ApiParam(value = "The ID of the AIP where to update the representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation to update", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The path to the directory in the shared file system where the representation should be provided.", required = true) @FormParam("filepath") String filepath)
    throws RODAException {
    // TODO
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @POST
  @Path("/{aip_id}/data/{representation_id}")
  @ApiOperation(value = "Create representation", notes = "Create a new representation on the AIP", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 409, message = "Already exists", response = Representation.class)})

  public Response aipsAipIdDataRepresentationIdPost(
    @ApiParam(value = "The ID of the AIP where to create the representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The requested ID for the new representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The path to the directory in the shared file system where the representation should be provided.", required = true) @FormParam("filepath") String filepath)
    throws RODAException {
    // TODO
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @DELETE
  @Path("/{aip_id}/data/{representation_id}")
  @ApiOperation(value = "Delete representation", notes = "Delete representation", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = Void.class)})

  public Response aipsAipIdDataRepresentationIdDelete(
    @ApiParam(value = "The ID of the AIP where the representation is.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation to delete", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    Browser.removeRepresentation(user, aipId, representationId);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

}
