/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.JSONP;
import org.roda.core.common.EntityResponse;
import org.roda.core.common.StreamResponse;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;
import org.roda.wui.api.v1.utils.ObjectResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(PreservationMetadataEventsResource.ENDPOINT)
@Tag(name = PreservationMetadataEventsResource.SWAGGER_ENDPOINT)
public class PreservationMetadataEventsResource {
  public static final String ENDPOINT = "/v1/events";
  public static final String SWAGGER_ENDPOINT = "v1 events";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML,
    ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Get events preservation metadata", description = "Gets preservation metadata of events", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response getPreservationMetadataEvent(
    @Parameter(description = "The ID of the event", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_ID) String id,
    @Parameter(description = "The ID of the AIP related to the event") @QueryParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The UUID of the representation related to the event") @QueryParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationUUID,
    @Parameter(description = "The UUID of the file related to the event") @QueryParam(RodaConstants.API_PATH_PARAM_FILE_UUID) String fileUUID,
    @Parameter(description = "Get only event detail information", schema = @Schema(defaultValue = "true")) @QueryParam(RodaConstants.API_QUERY_PARAM_ONLY_DETAILS) boolean onlyDetails,
    @Parameter(description = "Choose format in which to get the event", schema = @Schema(implementation = RodaConstants.MetadataMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "The language for the HTML output", schema = @Schema(implementation = RodaConstants.DescriptibeMetadataLanguages.class, defaultValue = RodaConstants.API_QUERY_VALUE_LANG_DEFAULT)) @DefaultValue(RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @QueryParam(RodaConstants.API_QUERY_KEY_LANG) String language,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
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
