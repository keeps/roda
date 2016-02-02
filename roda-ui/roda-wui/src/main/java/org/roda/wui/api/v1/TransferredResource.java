/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.io.InputStream;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.StreamResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Path(TransferredResource.ENDPOINT)
@Api(value = TransferredResource.SWAGGER_ENDPOINT)
public class TransferredResource {
  public static final String ENDPOINT = "/v1/transferred";
  public static final String SWAGGER_ENDPOINT = "v1 transferred";

  @SuppressWarnings("unused")
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Context
  private HttpServletRequest request;

  @GET
  public Response getResource(
    @ApiParam(value = "The resource id", required = false) @QueryParam("resourceId") String resourceId)
      throws AuthorizationDeniedException, NotFoundException, RequestNotValidException, GenericException {

    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());

    StreamResponse response = Browser.getTransferredResource(user, resourceId);

    return ApiUtils.okResponse(response);
  }

  @POST
  public Response createResource(
    @ApiParam(value = "The id of the parent", required = true) @QueryParam("parentId") String parentId,
    @ApiParam(value = "The name of the directory to create", required = false) @QueryParam("name") String name,
    @FormDataParam("upl") InputStream inputStream, @FormDataParam("upl") FormDataContentDisposition fileDetail)
      throws RODAException {

    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    Browser.createTransferredResource(user, parentId, fileDetail.getFileName(), inputStream, name);

    // FIXME give a better answer
    return Response.ok().entity("{'status':'success'}").build();
  }

  @DELETE
  public Response deleteResource(
    @ApiParam(value = "The id of the resource", required = true) @QueryParam("path") String path) throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    // TODO support remove multiple resources in one go
    Browser.removeTransferredResources(user, Arrays.asList(path));
    // FIXME give a better answer
    return Response.ok().entity("{'status':'success'}").build();
  }
}
