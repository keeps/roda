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
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.user.RodaSimpleUser;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.StreamResponse;
import org.roda.wui.common.I18nUtility;

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
    RodaSimpleUser user = UserUtility.getApiUser(request);

    StreamResponse response = Browser.retrieveTransferredResource(user, resourceId);

    return ApiUtils.okResponse(response);
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response createResource(
    @ApiParam(value = "The id of the parent") @QueryParam(RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID) String parentUUID,
    @ApiParam(value = "The name of the directory to create", required = false) @QueryParam(RodaConstants.TRANSFERRED_RESOURCE_DIRECTORY_NAME) String name,
    @ApiParam(value = "Locale", required = false) @QueryParam(RodaConstants.LOCALE) String localeString,
    @FormDataParam("upl") InputStream inputStream, @FormDataParam("upl") FormDataContentDisposition fileDetail)
    throws RODAException {

    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);
    // delegate action to controller

    try {
      org.roda.core.data.v2.ip.TransferredResource transferredResource = Browser.createTransferredResource(user,
        parentUUID, fileDetail.getFileName(), inputStream, name, true);

      return Response.ok().entity(transferredResource).build();
    } catch (AlreadyExistsException e) {
      return Response.status(Status.CONFLICT).entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
        I18nUtility.getMessage("ui.upload.error.alreadyexists", e.getMessage(), localeString))).build();
    }
  }

  @DELETE
  public Response deleteResource(
    @ApiParam(value = "The id of the resource", required = true) @QueryParam("path") String path) throws RODAException {
    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    // TODO support remove multiple resources in one go
    SelectedItemsList<org.roda.core.data.v2.ip.TransferredResource> selected = new SelectedItemsList<org.roda.core.data.v2.ip.TransferredResource>(
      Arrays.asList(path), TransferredResource.class.getName());
    Browser.deleteTransferredResources(user, selected);
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
