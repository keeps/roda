/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

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

import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.formats.Formats;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path(FormatsResource.ENDPOINT)
@Api(value = FormatsResource.SWAGGER_ENDPOINT)
public class FormatsResource {
  public static final String ENDPOINT = "/v1/formats";
  public static final String SWAGGER_ENDPOINT = "v1 formats";

  @Context
  private HttpServletRequest request;

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "List Formats", notes = "Gets a list of Formats.", response = Format.class, responseContainer = "List")
  public Response listFormats(
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the formats", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Formats formats = (Formats) Browser.retrieveObjects(user, Format.class, start, limit, acceptFormat);
    return Response.ok(formats, mediaType).build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Creates a new Format", notes = "Creates a new Format.", response = Format.class)
  public Response createFormat(Format format,
    @ApiParam(value = "Choose format in which to get the format", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Format newFormat = org.roda.wui.api.controllers.Formats.createFormat(user, format);
    return Response.ok(newFormat, mediaType).build();
  }

  @PUT
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Updated a Format", notes = "Updates a Format.", response = Format.class)
  public Response updateFormat(Format format,
    @ApiParam(value = "Choose format in which to get the format", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Format updatedFormat = org.roda.wui.api.controllers.Formats.updateFormat(user, format);
    return Response.ok(updatedFormat, mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_FORMAT_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get Format", notes = "Gets a particular Format.", response = Format.class)
  public Response getFormat(@PathParam(RodaConstants.API_PATH_PARAM_FORMAT_ID) String formatId,
    @ApiParam(value = "Choose format in which to get the format", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Format format = org.roda.wui.api.controllers.Browser.retrieve(user, Format.class, formatId);
    return Response.ok(format, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_FORMAT_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete Format", notes = "Delete a particular Format.", response = ApiResponseMessage.class)
  public Response deleteFormat(@PathParam(RodaConstants.API_PATH_PARAM_FORMAT_ID) String formatId,
    @ApiParam(value = "Choose format in which to get the result", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    org.roda.wui.api.controllers.Formats.deleteFormat(user, formatId);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Format deleted"), mediaType).build();
  }
}
