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

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
import org.roda.wui.api.controllers.Theme;
import org.roda.wui.api.v1.utils.ApiUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Path(ThemeResource.ENDPOINT)
@Api(value = ThemeResource.SWAGGER_ENDPOINT)
public class ThemeResource {
  public static final String ENDPOINT = "/v1/theme";
  public static final String SWAGGER_ENDPOINT = "v1 theme";

  @GET
  public Response getResource(
    @ApiParam(value = "The resource id", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_RESOURCE_ID) String resourceId,
    @ApiParam(value = "The default resource id", required = false) @QueryParam(RodaConstants.API_QUERY_PARAM_DEFAULT_RESOURCE_ID) String fallbackResourceId,
    @ApiParam(value = "If the resource is served inline", required = false) @QueryParam(RodaConstants.API_QUERY_PARAM_INLINE) boolean inline,
    @HeaderParam("Range") String range, @Context Request req) throws NotFoundException {

    Pair<String, InputStream> themeResource = Theme.getThemeResource(resourceId, fallbackResourceId);

    if (themeResource.getSecond() != null) {
      return ApiUtils.okResponse(Theme.getThemeResourceStreamResponse(themeResource), inline, range, req);
    } else {
      throw new NotFoundException("File not found: " + resourceId);
    }
  }
}
