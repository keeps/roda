/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.ObjectPermissionResult;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path(PermissionsResource.ENDPOINT)
@Api(value = PermissionsResource.SWAGGER_ENDPOINT)
public class PermissionsResource {
  public static final String ENDPOINT = "/v1/permissions";
  public static final String SWAGGER_ENDPOINT = "v1 permissions";

  @Context
  private HttpServletRequest request;

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_USERNAME + "}/{" + RodaConstants.API_PATH_PARAM_PERMISSION_TYPE + "}")
  @Produces({MediaType.APPLICATION_JSON})
  @ApiOperation(value = "Test permissions", notes = "Test if user has permissions.", response = ObjectPermissionResult.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = ObjectPermissionResult.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response getPermissions(
    @ApiParam(value = "The username to test permission.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_USERNAME) String username,
    @ApiParam(value = "The permission type to test.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_PERMISSION_TYPE) String permissionType,
    @Context UriInfo uriInfo) throws RODAException {
    String mediaType = MediaType.APPLICATION_JSON + "; charset=UTF-8";
    MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    ObjectPermissionResult result = Browser.verifyPermissions(user, username, permissionType, queryParams);
    return Response.ok(result, mediaType).build();
  }
}
