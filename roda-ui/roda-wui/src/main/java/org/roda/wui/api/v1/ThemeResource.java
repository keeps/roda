/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.roda.core.data.exceptions.NotFoundException;
import org.roda.wui.api.controllers.Theme;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Path(ThemeResource.ENDPOINT)
@Api(value = ThemeResource.SWAGGER_ENDPOINT)
public class ThemeResource {
  public static final String ENDPOINT = "/v1/theme";
  public static final String SWAGGER_ENDPOINT = "v1 theme";

  public static final int CACHE_CONTROL_MAX_AGE = 86400;

  @SuppressWarnings("unused")
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Context
  private HttpServletRequest request;

  @GET
  public Response getResource(
    @ApiParam(value = "The resource id", required = false) @QueryParam("resourceId") String resourceId,
    @Context Request req) throws IOException, NotFoundException {

    boolean externalFile = Theme.validExternalFile(resourceId);
    boolean internalFile = Theme.validInternalFile(resourceId);

    if (externalFile || internalFile) {
      CacheControl cc = new CacheControl();
      cc.setMaxAge(CACHE_CONTROL_MAX_AGE);
      cc.setPrivate(true);

      Date lastModifiedDate = Theme.getLastModifiedDate(resourceId, externalFile, internalFile);
      EntityTag etag = new EntityTag(Long.toString(lastModifiedDate.getTime()));
      ResponseBuilder builder = req.evaluatePreconditions(etag);

      if (builder == null) {
        return ApiUtils.okResponse(Theme.getResource(resourceId), cc, etag);
      } else {
        return builder.cacheControl(cc).tag(etag).build();
      }
    } else {
      return Response.status(Response.Status.NOT_FOUND).entity("File not found: " + resourceId).build();
    }
  }
}
