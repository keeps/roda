/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.util.ArrayList;

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
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.RiskIncidences;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path(RiskIncidencesResource.ENDPOINT)
@Api(value = RiskIncidencesResource.SWAGGER_ENDPOINT)
public class RiskIncidencesResource {
  public static final String ENDPOINT = "/v1/incidences";
  public static final String SWAGGER_ENDPOINT = "v1 incidences";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "List incidences", notes = "Get a list of incidences.", response = RiskIncidences.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = RiskIncidences.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response listRiskIncidences(
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the risk incidences", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    boolean justActive = false;
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    IndexResult<RiskIncidence> result = Browser.find(RiskIncidence.class, Filter.NULL, Sorter.NONE,
      new Sublist(pagingParams.getFirst(), pagingParams.getSecond()), null, user, justActive, new ArrayList<>());
    return Response.ok(ApiUtils.indexedResultToRODAObjectList(RiskIncidence.class, result), mediaType).build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Create risk incidence", notes = "Create a new risk incidence.", response = RiskIncidence.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = RiskIncidence.class),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})

  public Response createRisk(RiskIncidence incidence,
    @ApiParam(value = "Choose format in which to get the incidence", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    RiskIncidence newIncidence = org.roda.wui.api.controllers.Risks.createRiskIncidence(user, incidence);
    return Response.ok(newIncidence, mediaType).build();
  }

  @PUT
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Update risk incidence", notes = "Update a risk incidence.", response = RiskIncidence.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = RiskIncidence.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updateRisk(RiskIncidence incidence,
    @ApiParam(value = "Choose format in which to get the incidence", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    RiskIncidence updatedIncidence = org.roda.wui.api.controllers.Risks.updateRiskIncidence(user, incidence);
    return Response.ok(updatedIncidence, mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_RISK_INCIDENCE_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get risk incidence", notes = "Get a risk incidence.", response = RiskIncidence.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = RiskIncidence.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response getRisk(@PathParam(RodaConstants.API_PATH_PARAM_RISK_INCIDENCE_ID) String incidenceId,
    @ApiParam(value = "Choose format in which to get the incidence", allowableValues = RodaConstants.API_GET_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    RiskIncidence incidence = org.roda.wui.api.controllers.Browser.retrieve(user, RiskIncidence.class, incidenceId,
      new ArrayList<>());
    return Response.ok(incidence, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_RISK_INCIDENCE_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete risk incidence", notes = "Delete a risk incidence.", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deleteRisk(@PathParam(RodaConstants.API_PATH_PARAM_RISK_INCIDENCE_ID) String incidenceId,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    org.roda.wui.api.controllers.Risks.deleteRiskIncidence(user, incidenceId);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Risk incidence deleted"), mediaType).build();
  }
}
