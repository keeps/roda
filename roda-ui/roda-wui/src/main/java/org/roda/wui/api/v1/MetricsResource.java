/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Metrics;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path(MetricsResource.ENDPOINT)
@Api(value = MetricsResource.SWAGGER_ENDPOINT)
public class MetricsResource {
  public static final String ENDPOINT = "/v1/metrics";
  public static final String SWAGGER_ENDPOINT = "v1 metrics";

  @Context
  private HttpServletRequest request;

  @GET
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @ApiOperation(value = "Get list of RODA metrics", notes = "Get a list of RODA metrics", response = String.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful response", response = String.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response getMetrics(
    @ApiParam(value = "Choose format in which to get the metrics", allowableValues = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) final String acceptFormat,
    @QueryParam(RodaConstants.API_METRICS_TO_OBTAIN) final List<String> metricsToObtain) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    org.roda.core.data.v2.common.Metrics metrics = Metrics.getMetrics(user, metricsToObtain);

    return Response.ok(metrics, mediaType).build();
  }

}
