/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.SortParameter;
import org.roda.core.data.adapter.sort.Sorter;
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

/**
 * REST API resource Index.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
@Path("/v1/index")
@Api(value = "v1 index")
public class IndexResource {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(IndexResource.class);

  /** Default value for <i>start</i> parameter. */
  private static final int DEFAULT_START = 0;
  /** Default value for <i>limit</i> parameter. */
  private static final int DEFAULT_LIMIT = 100;
  /** Default value for <i>onlyActive</i> parameter. */
  private static final boolean DEFAULT_ONLY_ACTIVE = true;

  @Context
  private HttpServletRequest request;

  /**
   * Find indexed resources.
   *
   * @param returnClass
   *          {@link Class} of resources to return.
   * @param filterParameters
   *          List of filter parameters. Example: "formatPronom=fmt/19".
   * @param sortParameters
   *          List of sort parameters. Examples: "formatPronom", "uuid desc".
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
    @ApiParam(value = "Class of resources to return", required = true, example = "org.roda.core.data.v2.ip.IndexedFile") @QueryParam(RodaConstants.API_QUERY_KEY_RETURN_CLASS) final String returnClass,
    @ApiParam(value = "Filter parameters", example = "formatPronom=fmt/19") @QueryParam(RodaConstants.API_QUERY_KEY_FILTER) final List<String> filterParameters,
    @ApiParam(value = "Sort parameters", example = "\"formatPronom\", \"uuid desc\"") @QueryParam(RodaConstants.API_QUERY_KEY_SORT) final List<String> sortParameters,
    @ApiParam(value = "Index of the first element to return (0-based index)", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) final Integer start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) final Integer limit,
    @ApiParam(value = "Facets to return", example = "formatPronom") @QueryParam(RodaConstants.API_QUERY_KEY_FACET) final List<String> facetAttributes,
    @ApiParam(value = "Return only active resources?", defaultValue = "true") @QueryParam(RodaConstants.API_QUERY_KEY_ONLY_ACTIVE) final Boolean onlyActive)
    throws RODAException {
    final String mediaType = ApiUtils.getMediaType(null, request);
    final RodaUser user = UserUtility.getApiUser(request);
    try {

      @SuppressWarnings("unchecked")
      final Class<T> classToReturn = (Class<T>) Class.forName(returnClass);

      final Filter filter = new Filter();
      for (String filterParameter : filterParameters) {
        final String[] parts = filterParameter.split("=");
        if (parts.length == 2) {
          filter.add(new SimpleFilterParameter(parts[0], parts[1]));
        } else {
          LOGGER.warn("Unable to parse filter parameter '{}'. Ignored", filterParameter);
        }
      }

      final Sorter sorter = new Sorter();
      for (String sortParameter : sortParameters) {
        final String[] parts = sortParameter.split(" ");
        final boolean descending = parts.length == 2 && "desc".equalsIgnoreCase(parts[1]);
        if (parts.length > 0) {
          sorter.add(new SortParameter(parts[0], descending));
        } else {
          LOGGER.warn("Unable to parse sorter parameter '{}'. Ignored", sortParameter);
        }
      }

      final Sublist sublist = new Sublist(start == null ? DEFAULT_START : start, limit == null ? DEFAULT_LIMIT : limit);

      final Set<FacetParameter> facetParameters = new HashSet<>();
      for (String facetAttribute : facetAttributes) {
        facetParameters.add(new SimpleFacetParameter(facetAttribute));
      }
      final Facets facets = new Facets(facetParameters);

      final boolean paramOnlyActive = onlyActive == null ? DEFAULT_ONLY_ACTIVE : onlyActive;

      final IndexResult<T> result = Browser.find(classToReturn, filter, sorter, sublist, facets, user, paramOnlyActive);

      return Response.ok(result, mediaType).build();

    } catch (final ClassNotFoundException e) {
      throw new InvalidParameterException("Invalid parameter " + RodaConstants.API_QUERY_KEY_RETURN_CLASS, e);
    }
  }

}
