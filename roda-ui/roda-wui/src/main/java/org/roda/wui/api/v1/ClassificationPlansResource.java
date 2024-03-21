/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiUtils;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@Path(ClassificationPlansResource.ENDPOINT)
@Tag(name = ClassificationPlansResource.SWAGGER_ENDPOINT)
public class ClassificationPlansResource {
  public static final String ENDPOINT = "/v1/classification_plans";
  public static final String SWAGGER_ENDPOINT = "v1 classification plans";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response getClassificationPlan(
    @DefaultValue("plan.json") @Parameter(description = "Choose file name", schema = @Schema(defaultValue = "plan.json")) @QueryParam(RodaConstants.API_QUERY_KEY_FILENAME) String filename)
    throws RODAException {

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    ConsumesOutputStream cos = Browser.retrieveClassificationPlan(user, filename);

    return ApiUtils.okResponse(new StreamResponse(cos));
  }
}
