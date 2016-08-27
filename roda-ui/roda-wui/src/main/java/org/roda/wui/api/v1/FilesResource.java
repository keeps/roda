/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.StreamResponse;

import io.swagger.annotations.*;

@Path(FilesResource.ENDPOINT)
@Api(value = FilesResource.SWAGGER_ENDPOINT)
public class FilesResource {
  public static final String ENDPOINT = "/v1/files";
  public static final String SWAGGER_ENDPOINT = "v1 files";

  @Context
  private HttpServletRequest request;

  @GET
  @Path("/{file_uuid}")
  @Produces({"application/json", "application/octetstream"})
  @ApiOperation(value = "Get file", notes = "Get file", response = File.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = File.class),
    @ApiResponse(code = 404, message = "Not found", response = File.class)})

  public Response retrieve(
    @ApiParam(value = "The ID of the existing file", required = true) @PathParam(RodaConstants.API_PATH_PARAM_FILE_UUID) String fileUuid,
    @ApiParam(value = "Choose format in which to get the file", allowableValues = "json, bin") @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    // get user
    User user = UserUtility.getApiUser(request);

    StreamResponse aipRepresentationFile = Browser.retrieveAIPRepresentationFile(user, fileUuid, acceptFormat);

    return ApiUtils.okResponse(aipRepresentationFile);
  }

  @PUT
  @Path("/{file_uuid}")
  @ApiOperation(value = "Update representation file", notes = "Update existing file", response = File.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = File.class),
    @ApiResponse(code = 404, message = "Not found", response = File.class)})

  public Response update(
    @ApiParam(value = "The ID of the existing file", required = true) @PathParam(RodaConstants.API_PATH_PARAM_FILE_UUID) String fileUUID,
    @ApiParam(value = "The path to the file in the shared file system where the file should be provided.", required = true) @FormParam("filepath") String filepath)
    throws RODAException {
    // TODO
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @POST
  @ApiOperation(value = "Create representation with one file", notes = "Create a new representation on the AIP", response = File.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = File.class),
    @ApiResponse(code = 409, message = "Already exists", response = File.class)})

  public Response aipsAipIdDataRepresentationIdFileIdPost(
    @ApiParam(value = "The ID of the AIP where to create the representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The requested ID for the new representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The requested ID of the new file", required = true) @PathParam(RodaConstants.API_PATH_PARAM_FILE_UUID) String fileId,
    @ApiParam(value = "The path to the directory in the shared file system where the representation should be provided.", required = true) @FormParam("filepath") String filepath)
    throws RODAException {
    // TODO
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @DELETE
  @Path("/{file_uuid}")
  @ApiOperation(value = "Delete representation file", notes = "Delete representation file", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = Void.class)})

  public Response aipsAipIdDataRepresentationIdFileIdDelete(
    @ApiParam(value = "The ID of the existing file", required = true) @PathParam(RodaConstants.API_PATH_PARAM_FILE_UUID) String fileUUID)
    throws RODAException {
    // get user
    User user = UserUtility.getApiUser(request);
    // delegate action to controller
    Browser.deleteRepresentationFile(user, fileUUID);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

}
