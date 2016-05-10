/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(FormatsResource.ENDPOINT)
@Api(value = FormatsResource.SWAGGER_ENDPOINT)
public class FormatsResource {
  public static final String ENDPOINT = "/v1/formats";
  public static final String SWAGGER_ENDPOINT = "v1 formats";

  private static Logger LOGGER = LoggerFactory.getLogger(FormatsResource.class);

  @Context
  private HttpServletRequest request;

  @GET
  @ApiOperation(value = "List Formats", notes = "Gets a list of Formats.", response = Format.class, responseContainer = "List")
  public Response listFormats(
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    IndexResult<Format> listFormatsIndexResult = org.roda.wui.api.controllers.Browser.find(user, Format.class, null,
      null, new Sublist(new Sublist(pagingParams.getFirst(), pagingParams.getSecond())), null);

    // transform controller method output
    List<Format> formats = org.roda.wui.api.controllers.Formats.retrieveFormats(listFormatsIndexResult);

    return Response.ok(formats, mediaType).build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Creates a new Format", notes = "Creates a new Format.", response = Format.class)
  public Response createFormat(Format format, @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Format newFormat = org.roda.wui.api.controllers.Formats.createFormat(user, format);

    return Response.created(ApiUtils.getUriFromRequest(request)).entity(newFormat).type(mediaType).build();
  }

  @GET
  @Path("/{formatId}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get Format", notes = "Gets a particular Format.", response = Format.class)
  public Response getFormat(@PathParam("formatId") String formatId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Format format = org.roda.wui.api.controllers.Browser.retrieve(user, Format.class, formatId);

    return Response.ok(format, mediaType).build();
  }

  @DELETE
  @Path("/{formatId}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete Format", notes = "Delete a particular Format.", response = ApiResponseMessage.class)
  public Response deleteFormat(@PathParam("formatId") String formatId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    org.roda.wui.api.controllers.Formats.deleteFormat(user, formatId);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Format deleted"), mediaType).build();
  }
}
