/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(RisksResource.ENDPOINT)
@Api(value = RisksResource.SWAGGER_ENDPOINT)
public class RisksResource {
  public static final String ENDPOINT = "/v1/risks";
  public static final String SWAGGER_ENDPOINT = "v1 risks";

  private static Logger LOGGER = LoggerFactory.getLogger(RisksResource.class);

  @Context
  private HttpServletRequest request;

  @GET
  @ApiOperation(value = "List Risks", notes = "Gets a list of Risks.", response = Risk.class, responseContainer = "List")
  public Response listRisks(@QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    boolean justActive = false;
    IndexResult<Risk> listRisksIndexResult = org.roda.wui.api.controllers.Browser.find(Risk.class, Filter.NONE,
      Sorter.NONE, new Sublist(new Sublist(pagingParams.getFirst(), pagingParams.getSecond())), Facets.NONE, user,
      justActive);

    // transform controller method output
    List<Risk> risks = org.roda.wui.api.controllers.Risks.retrieveRisks(listRisksIndexResult);

    return Response.ok(risks, mediaType).build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Creates a new Risk", notes = "Creates a new Risk.", response = Risk.class)
  public Response createRisk(Risk risk, @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Risk newRisk = org.roda.wui.api.controllers.Risks.createRisk(user, risk);

    return Response.created(ApiUtils.getUriFromRequest(request)).entity(newRisk).type(mediaType).build();
  }

  @GET
  @Path("/{riskId}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get Risk", notes = "Gets a particular Risk.", response = Risk.class)
  public Response getRisk(@PathParam("riskId") String riskId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Risk risk = org.roda.wui.api.controllers.Browser.retrieve(user, Risk.class, riskId);

    return Response.ok(risk, mediaType).build();
  }

  @DELETE
  @Path("/{riskId}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete Risk", notes = "Delete a particular Risk.", response = ApiResponseMessage.class)
  public Response deleteRisk(@PathParam("riskId") String riskId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    org.roda.wui.api.controllers.Risks.deleteRisk(user, riskId);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Risk deleted"), mediaType).build();
  }
}
