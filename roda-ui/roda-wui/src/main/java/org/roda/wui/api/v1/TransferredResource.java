/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.StreamResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path(TransferredResource.ENDPOINT)
@Api(value = TransferredResource.SWAGGER_ENDPOINT)
public class TransferredResource {
  public static final String ENDPOINT = "/v1/transferred";
  public static final String SWAGGER_ENDPOINT = "v1 transferred";

  @Context
  private HttpServletRequest request;

  @GET
  public Response getResource(
    @ApiParam(value = "The resource id", required = false) @QueryParam(RodaConstants.TRANSFERRED_RESOURCE_RESOURCE_ID) String resourceId)
    throws AuthorizationDeniedException, NotFoundException, RequestNotValidException, GenericException {

    // get user
    RodaUser user = UserUtility.getApiUser(request);

    StreamResponse response = Browser.getTransferredResource(user, resourceId);

    return ApiUtils.okResponse(response);
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response createResource(
    @ApiParam(value = "The id of the parent") @QueryParam(RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID) String parentUUID,
    @ApiParam(value = "The name of the directory to create", required = false) @QueryParam(RodaConstants.TRANSFERRED_RESOURCE_DIRECTORY_NAME) String name,
    @FormDataParam("upl") InputStream inputStream, @FormDataParam("upl") FormDataContentDisposition fileDetail)
    throws RODAException {

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    org.roda.core.data.v2.ip.TransferredResource transferredResource = Browser.createTransferredResource(user,
      parentUUID, fileDetail.getFileName(), inputStream, name, true);

    return Response.ok().entity(transferredResource).build();
  }

  @DELETE
  public Response deleteResource(
    @ApiParam(value = "The id of the resource", required = true) @QueryParam("path") String path) throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    // TODO support remove multiple resources in one go
    SelectedItemsList<org.roda.core.data.v2.ip.TransferredResource> selected = new SelectedItemsList<org.roda.core.data.v2.ip.TransferredResource>(
      Arrays.asList(path), TransferredResource.class.getName());
    Browser.removeTransferredResources(user, selected);
    // FIXME give a better answer
    return Response.ok().entity("{'status':'success'}").build();
  }

  @GET
  @Path("/updateResources")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Updates Resources", notes = "Updates all transferred resources", response = TransferredResource.class)
  public Response updateResources(@QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException, IOException, IsStillUpdatingException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    Browser.updateAllTransferredResources(null, true);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Transferred Resources updated"), mediaType)
      .build();
  }
}
