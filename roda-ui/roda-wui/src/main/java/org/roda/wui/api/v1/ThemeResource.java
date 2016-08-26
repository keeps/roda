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
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

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
  public static final int CACHE_CONTROL_MAX_AGE = 60;

  @GET
  public Response getResource(
    @ApiParam(value = "The resource id", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_RESOURCE_ID) String resourceId,
    @ApiParam(value = "The default resource id", required = false) @QueryParam(RodaConstants.API_QUERY_PARAM_DEFAULT_RESOURCE_ID) String fallbackResourceId,
    @ApiParam(value = "If the resource is served inline", required = false) @QueryParam(RodaConstants.API_QUERY_PARAM_INLINE) boolean inline,
    @Context Request req) throws IOException, NotFoundException {

    Pair<String, InputStream> themeResource = Theme.getThemeResource(resourceId, fallbackResourceId);

    if (themeResource.getSecond() != null) {
      CacheControl cc = new CacheControl();
      cc.setMaxAge(CACHE_CONTROL_MAX_AGE);
      cc.setPrivate(true);

      Date lastModifiedDate = Theme.getLastModifiedDate(themeResource.getFirst());
      EntityTag etag = new EntityTag(Long.toString(lastModifiedDate.getTime()));
      ResponseBuilder builder = req.evaluatePreconditions(etag);

      if (builder == null) {
        return ApiUtils.okResponse(Theme.getThemeResourceStreamResponse(themeResource), cc, etag, inline);
      } else {
        return builder.cacheControl(cc).tag(etag).build();
      }
    } else {
      throw new NotFoundException("File not found: " + resourceId);
    }
  }
}
