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
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.Risks;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path(RisksResource.ENDPOINT)
@Api(value = RisksResource.SWAGGER_ENDPOINT)
public class RisksResource {
  public static final String ENDPOINT = "/v1/risks";
  public static final String SWAGGER_ENDPOINT = "v1 risks";

  @Context
  private HttpServletRequest request;

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "List Risks", notes = "Gets a list of Risks.", response = Risk.class, responseContainer = "List")
  public Response listRisks(
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the risks", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Risks risks = (Risks) Browser.retrieveObjects(user, IndexedRisk.class, start, limit, acceptFormat);
    return Response.ok(risks, mediaType).build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Creates a new Risk", notes = "Creates a new Risk.", response = Risk.class)
  public Response createRisk(Risk risk,
    @ApiParam(value = "Choose format in which to get the risk", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Risk newRisk = org.roda.wui.api.controllers.Risks.createRisk(user, risk);
    return Response.ok(newRisk, mediaType).build();
  }

  @PUT
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Updates a Risk", notes = "Updates a Risk.", response = Risk.class)
  public Response updateRisk(Risk risk, @QueryParam("message") String updateMessage,
    @ApiParam(value = "Choose format in which to get the risk", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Risk updatedRisk = org.roda.wui.api.controllers.Risks.updateRisk(user, risk, updateMessage);
    return Response.ok(updatedRisk, mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_RISK_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get Risk", notes = "Gets a particular Risk.", response = Risk.class)
  public Response getRisk(@PathParam(RodaConstants.API_PATH_PARAM_RISK_ID) String riskId,
    @ApiParam(value = "Choose format in which to get the risk", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Risk risk = org.roda.wui.api.controllers.Browser.retrieve(user, IndexedRisk.class, riskId);
    return Response.ok(risk, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_RISK_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete Risk", notes = "Delete a particular Risk.", response = ApiResponseMessage.class)
  public Response deleteRisk(@PathParam(RodaConstants.API_PATH_PARAM_RISK_ID) String riskId,
    @ApiParam(value = "Choose format in which to get the result", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    org.roda.wui.api.controllers.Risks.deleteRisk(user, riskId);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Risk deleted"), mediaType).build();
  }
}
