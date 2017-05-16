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
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
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

@Path(PreservationMetadataEventsResource.ENDPOINT)
@Api(value = PreservationMetadataEventsResource.SWAGGER_ENDPOINT)
public class PreservationMetadataEventsResource {
  public static final String ENDPOINT = "/v1/events";
  public static final String SWAGGER_ENDPOINT = "v1 events";

  @Context
  private HttpServletRequest request;

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get events preservation metadata", notes = "Get events preservation metadata", response = String.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful response", response = String.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response getPreservationMetadataEvent(
    @ApiParam(value = "The ID of the event", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_ID) String id,
    @ApiParam(value = "The ID of the AIP related to the event") @QueryParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The UUID of the representation related to the event") @QueryParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationUUID,
    @ApiParam(value = "The UUID of the file related to the event") @QueryParam(RodaConstants.API_PATH_PARAM_FILE_UUID) String fileUUID,
    @ApiParam(value = "Get only event detail information", defaultValue = "true") @QueryParam(RodaConstants.API_QUERY_PARAM_ONLY_DETAILS) boolean onlyDetails,
    @ApiParam(value = "Choose format in which to get the event", allowableValues = RodaConstants.API_GET_METADATA_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "The language for the HTML output", allowableValues = RodaConstants.API_DESCRIPTIVE_METADATA_LANGUAGES, defaultValue = RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @DefaultValue(RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @QueryParam(RodaConstants.API_QUERY_KEY_LANG) String language)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse event = Browser.retrievePreservationMetadataEvent(user, id, aipId, representationUUID, fileUUID,
      onlyDetails, acceptFormat, language);

    if (event instanceof ObjectResponse) {
      ObjectResponse<PreservationMetadata> pm = (ObjectResponse<PreservationMetadata>) event;
      return Response.ok(pm.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) event);
    }
  }

}
