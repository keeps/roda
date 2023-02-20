/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.roda.core.common.ProvidesInputStream;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
import org.roda.wui.api.controllers.Theme;
import org.roda.wui.api.v1.utils.ApiUtils;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(ThemeResource.ENDPOINT)
@Tag(name = ThemeResource.SWAGGER_ENDPOINT)
public class ThemeResource {
  public static final String ENDPOINT = "/v1/theme";
  public static final String SWAGGER_ENDPOINT = "v1 theme";

  @GET
  public Response getResource(
    @Parameter(description = "The resource id", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_RESOURCE_ID) String resourceId,
    @Parameter(description = "The default resource id", required = false) @QueryParam(RodaConstants.API_QUERY_PARAM_DEFAULT_RESOURCE_ID) String fallbackResourceId,
    @Parameter(description = "If the resource is served inline", required = false) @QueryParam(RodaConstants.API_QUERY_PARAM_INLINE) boolean inline,
    @Parameter(description = "The resource type, can be internal or plugin", required = false, schema = @Schema(implementation = RodaConstants.ResourcesTypes.class, defaultValue = RodaConstants.API_QUERY_PARAM_DEFAULT_RESOURCE_TYPE)) @DefaultValue(RodaConstants.API_QUERY_PARAM_DEFAULT_RESOURCE_TYPE) @QueryParam(RodaConstants.API_QUERY_PARAM_RESOURCE_TYPE) String type,
    @HeaderParam("Range") String range, @Context Request req) throws NotFoundException {

    Pair<String, ProvidesInputStream> themeResource = Theme.getThemeResource(resourceId, fallbackResourceId, type);

    if (themeResource.getSecond() != null) {
      return ApiUtils.okResponse(Theme.getThemeResourceStreamResponse(themeResource, type), inline, range, req);
    } else {
      throw new NotFoundException("File not found: " + resourceId);
    }
  }
}
