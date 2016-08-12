/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.facet.FacetParameter;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * REST API resource Index.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
@Path(IndexResource.ENDPOINT)
@Api(value = IndexResource.SWAGGER_ENDPOINT)
public class IndexResource {
  public static final String ENDPOINT = "/v1/index";
  public static final String SWAGGER_ENDPOINT = "v1 index";

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexResource.class);

  @Context
  private HttpServletRequest request;

  /**
   * Find indexed resources.
   *
   * @param returnClass
   *          {@link Class} of resources to return.
   * @param start
   *          Index of the first element to return (0-based index).
   * @param limit
   *          Maximum number of elements to return.
   * @param facetAttributes
   *          Facets to return.
   * @param onlyActive
   *          Return only active resources?
   * @param <T>
   *          Type of the resources to return.
   * @return a {@link Response} with the resources.
   * @throws RODAException
   *           if some error occurs.
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Find indexed resources.", notes = "Find indexed resources.", response = IsIndexed.class, responseContainer = "List")
  public <T extends IsIndexed> Response list(
    @ApiParam(value = "Class of resources to return") @QueryParam(RodaConstants.API_QUERY_KEY_RETURN_CLASS) final String returnClass,
    @ApiParam(value = "Index of the first element to return (0-based index)", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) final int start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) final int limit,
    @ApiParam(value = "Facets to return") @QueryParam(RodaConstants.API_QUERY_KEY_FACET) final List<String> facetAttributes,
    @ApiParam(value = "Return only active resources?", defaultValue = "true") @QueryParam(RodaConstants.API_QUERY_KEY_ONLY_ACTIVE) final boolean onlyActive)
    throws RODAException {
    final String mediaType = ApiUtils.getMediaType(null, request);
    final RodaUser user = UserUtility.getApiUser(request);
    try {

      final Set<FacetParameter> facetParameters = new HashSet<>();
      for (String facetAttribute : facetAttributes) {
              facetParameters.add(new SimpleFacetParameter(facetAttribute));
      }
      final Facets facets = new Facets(facetParameters);

      final Class<T> classToReturn = (Class<T>) Class.forName(returnClass);
      final IndexResult<T> result = Browser.find(classToReturn, null, null, new Sublist(start, limit), facets, user,
        onlyActive);

      return Response.ok(result, mediaType).build();

    } catch (final ClassNotFoundException e) {
      throw new InvalidParameterException("Invalid parameter " + RodaConstants.API_QUERY_KEY_RETURN_CLASS, e);
    }
  }

}
