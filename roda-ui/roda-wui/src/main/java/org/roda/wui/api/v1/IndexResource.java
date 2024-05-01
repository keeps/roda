/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.JSONP;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;
import org.roda.wui.api.v1.utils.FacetsCSVOutputStream;
import org.roda.wui.api.v1.utils.ResultsCSVOutputStream;
import org.roda.wui.common.I18nUtility;
import org.roda.wui.common.server.RodaStreamingOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST API resource Index.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
@Path("/index")
@Tag(name = "v1 index")
public class IndexResource {
  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(IndexResource.class);
  /**
   * Default value for <i>start</i> parameter.
   */
  private static final int DEFAULT_START = 0;
  /**
   * Default value for <i>limit</i> parameter.
   */
  private static final int DEFAULT_LIMIT = 100;
  /**
   * Default value for <i>onlyActive</i> parameter.
   */
  private static final boolean DEFAULT_ONLY_ACTIVE = true;
  /**
   * Default filename for CSV files.
   */
  private static final String DEFAULT_CSV_FILENAME = "export.csv";
  /**
   * CSV type.
   */
  private static final String TYPE_CSV = "csv";
  /**
   * Default value for <i>facetLimit</i> parameter.
   */
  private static final int DEFAULT_FACET_LIMIT = 100;
  /** CSV field delimiter config key. */
  private static final String CONFIG_KEY_CSV_DELIMITER = "csv.delimiter";

  /**
   * HTTP request.
   */
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
   * @param facetLimit
   *          Maximum number of facets to return.
   * @param localeString
   *          the locale.
   * @param onlyActive
   *          Return only active resources?
   * @param exportFacets
   *          for CSV results, export only facets?
   * @param filename
   *          the filename for exported CSV.
   * @param <T>
   *          Type of the resources to return.
   * @return a {@link Response} with the resources.
   * @throws RODAException
   *           if some error occurs.
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.TEXT_CSV,
    ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Find indexed resources", description = "Find indexed resources", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = IndexResult.class)))})
  public <T extends IsIndexed> Response list(
    @Parameter(description = "Class of resources to return", required = true, example = "org.roda.core.data.v2.ip.IndexedFile") @QueryParam(RodaConstants.API_QUERY_KEY_RETURN_CLASS) final String returnClass,
    @Parameter(description = "Filter parameters", example = "formatPronom=fmt/19") @QueryParam(RodaConstants.API_QUERY_KEY_FILTER) final List<String> filterParameters,
    @Parameter(description = "Sort parameters", example = "\"formatPronom\", \"uuid desc\"") @QueryParam(RodaConstants.API_QUERY_KEY_SORT) final List<String> sortParameters,
    @Parameter(description = "Index of the first element to return (0-based index)", schema = @Schema(defaultValue = "0")) @QueryParam(RodaConstants.API_QUERY_KEY_START) final Integer start,
    @Parameter(description = "Maximum number of elements to return", schema = @Schema(defaultValue = "100")) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) final Integer limit,
    @Parameter(description = "Facets to return", example = "formatPronom") @QueryParam(RodaConstants.API_QUERY_KEY_FACET) final List<String> facetAttributes,
    @Parameter(description = "Facet limit", example = "100", schema = @Schema(defaultValue = "100")) @QueryParam(RodaConstants.API_QUERY_KEY_FACET_LIMIT) final Integer facetLimit,
    @Parameter(description = "Language", example = "en", schema = @Schema(defaultValue = "en")) @QueryParam(RodaConstants.API_QUERY_KEY_LANG) final String localeString,
    @Parameter(description = "Return only active resources?", schema = @Schema(defaultValue = "true")) @QueryParam(RodaConstants.API_QUERY_KEY_ONLY_ACTIVE) final Boolean onlyActive,
    @Parameter(description = "Export facet data", schema = @Schema(defaultValue = "false")) @QueryParam(RodaConstants.API_QUERY_KEY_EXPORT_FACETS) final boolean exportFacets,
    @Parameter(description = "Filename", schema = @Schema(defaultValue = DEFAULT_CSV_FILENAME)) @QueryParam(RodaConstants.API_QUERY_KEY_FILENAME) final String filename,
    @Parameter(description = "Choose format in which to get the response") @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    return null;
    /*
     * final String mediaType = ApiUtils.getMediaType(acceptFormat, request); final
     * User user = UserUtility.getApiUser(request);
     * 
     * final FindRequest findRequest = new FindRequest(); findRequest.classToReturn
     * = returnClass; findRequest.exportFacets = exportFacets; findRequest.filename
     * = StringUtils.isBlank(filename) ? DEFAULT_CSV_FILENAME : filename;
     * 
     * findRequest.filter = new Filter(); if (filterParameters.isEmpty()){ return
     * Response.status(Response.Status.BAD_REQUEST) .entity(new
     * ApiResponseMessage(ApiResponseMessage.ERROR,
     * "Filter parameter is required. For an all-inclusive search, use filter=any.")
     * ).build(); } else { for (String filterParameter : filterParameters) { if
     * (filterParameter.equals("any")) { findRequest.filter.add(new
     * AllFilterParameter()); } else { final String[] parts =
     * filterParameter.split("="); if (parts.length == 2) { if
     * (parts[0].startsWith("!")) { String key = parts[0].substring(1);
     * findRequest.filter.add(new NotSimpleFilterParameter(key, parts[1])); } else {
     * findRequest.filter.add(new SimpleFilterParameter(parts[0], parts[1])); } }
     * else { LOGGER.warn("Unable to parse filter parameter '{}'. Ignored",
     * filterParameter); } } } }
     * 
     * 
     * findRequest.sorter = new Sorter(); for (String sortParameter :
     * sortParameters) { final String[] parts = sortParameter.split(" "); final
     * boolean descending = parts.length == 2 && "desc".equalsIgnoreCase(parts[1]);
     * if (parts.length > 0) { findRequest.sorter.add(new SortParameter(parts[0],
     * descending)); } else {
     * LOGGER.warn("Unable to parse sorter parameter '{}'. Ignored", sortParameter);
     * } }
     * 
     * findRequest.sublist = new Sublist(start == null ? DEFAULT_START : start,
     * limit == null ? DEFAULT_LIMIT : limit);
     * 
     * final int paramFacetLimit = facetLimit == null ? DEFAULT_FACET_LIMIT :
     * facetLimit;
     * 
     * final Set<FacetParameter> facetParameters = new HashSet<>(); for (String
     * facetAttribute : facetAttributes) { facetParameters.add(new
     * SimpleFacetParameter(facetAttribute, paramFacetLimit, SORT.COUNT)); }
     * findRequest.facets = new Facets(facetParameters);
     * 
     * findRequest.onlyActive = onlyActive == null ? DEFAULT_ONLY_ACTIVE :
     * onlyActive;
     * 
     * final Response response; if (ExtraMediaType.TEXT_CSV.equals(mediaType)) {
     * response = csvResponse(findRequest, user, localeString); } else { final
     * Class<T> classToReturn = getClass(findRequest.classToReturn);
     * 
     * IndexResult<T> indexResult = Browser.find(classToReturn, findRequest.filter,
     * findRequest.sorter, findRequest.sublist, findRequest.facets, user,
     * findRequest.onlyActive, new ArrayList<>()); indexResult =
     * I18nUtility.translate(indexResult, classToReturn, localeString);
     * 
     * response = Response.ok(indexResult, mediaType).build(); }
     * 
     * return response;
     */
  }

  /**
   * Find indexed resources.
   *
   * @param findRequest
   *          find parameters.
   * @param <T>
   *          Type of the resources to return.
   * @return a {@link Response} with the resources.
   * @throws RODAException
   *           if some error occurs.
   */
  @POST
  @Path("/find")
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.TEXT_CSV})
  @Operation(summary = "Find indexed resources", description = "Finds existing indexed resources", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = IsIndexed.class)))})
  public <T extends IsIndexed> Response find(@Parameter(description = "Find parameters") final FindRequest findRequest)
    throws RODAException {


    if(findRequest.getFilter() == null || findRequest.getFilter().getParameters().isEmpty()){
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Filter parameter is required. For an all-inclusive search, use type AllFilterParameter.")).build();
    }

    final String mediaType = ApiUtils.getMediaType(null, request);
    final User user = UserUtility.getApiUser(request);

    if (ExtraMediaType.TEXT_CSV.equals(mediaType)) {
      return csvResponse(findRequest, user, null);
    } else {
      final IndexResult<T> result = Browser.find(getClass(findRequest.getClassToReturn()), findRequest.getFilter(),
        findRequest.getSorter(), findRequest.getSublist(), findRequest.getFacets(), user, findRequest.isOnlyActive(),
        findRequest.getFieldsToReturn());
      return Response.ok(result, mediaType).build();
    }

  }

  /**
   * Find indexed resources.
   *
   * @param findRequestString
   *          find parameters.
   * @param type
   *          the type of output ("csv").
   * @return a {@link Response} with the resources.
   * @throws RODAException
   *           if some error occurs.
   */
  @POST
  @Path("/findFORM")
  @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
  public Response findFORM(@FormParam("findRequest") final String findRequestString,
    @FormParam("type") final String type) throws RODAException {

    final User user = UserUtility.getApiUser(request);
    final FindRequest findRequest = JsonUtils.getObjectFromJson(findRequestString, FindRequest.class);

    if (type.equals(IndexResource.TYPE_CSV)) {
      return csvResponse(findRequest, user, null);
    } else {
      // TODO support JSON type
      throw new GenericException("Type not yet supported:" + type);
    }
  }

  /**
   * Count indexed resources.
   *
   * @param countRequest
   *          count parameters.
   * @return a {@link Response} with the count.
   * @throws RODAException
   *           if some error occurs.
   */
  @POST
  @Path("/count")
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Operation(summary = "Count indexed resources", description = "Counts indexed resources", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Long.class)))})
  public Response count(@Parameter(description = "Count parameters") final CountRequest countRequest)
    throws RODAException {
    final String mediaType = ApiUtils.getMediaType(null, request);
    final User user = UserUtility.getApiUser(request);
    final long result = Browser.count(user, getClass(countRequest.getClassToReturn()), countRequest.getFilter(),
      countRequest.isOnlyActive());
    return Response.ok(result, mediaType).build();
  }

  /**
   * Produces a CSV response with results or facets.
   *
   * @param findRequest
   *          the request parameters.
   * @param user
   *          the current {@link User}.
   * @param <T>
   *          Type of the resources to return.
   * @return a {@link Response} with CSV.
   * @throws RequestNotValidException
   *           it the request is not valid.
   * @throws AuthorizationDeniedException
   *           if the user is not authorized to perform this operation.
   * @throws GenericException
   *           if some other error occurs.
   */
  private <T extends IsIndexed> Response csvResponse(final FindRequest findRequest, final User user,
    String localeString) throws RequestNotValidException, AuthorizationDeniedException, GenericException {

    final Class<T> returnClass = getClass(findRequest.getClassToReturn());
    final Configuration config = RodaCoreFactory.getRodaConfiguration();
    final char delimiter;
    if (StringUtils.isBlank(config.getString(CONFIG_KEY_CSV_DELIMITER))) {
      delimiter = CSVFormat.DEFAULT.getDelimiter();
    } else {
      delimiter = config.getString(CONFIG_KEY_CSV_DELIMITER).trim().charAt(0);
    }

    if (findRequest.isExportFacets()) {
      IndexResult<T> result = Browser.find(returnClass, findRequest.getFilter(), Sorter.NONE, Sublist.NONE,
        findRequest.getFacets(), user, findRequest.isOnlyActive(), findRequest.getFieldsToReturn());
      if (localeString != null) {
        result = I18nUtility.translate(result, returnClass, localeString);
      }

      return ApiUtils.okResponse(
        new RodaStreamingOutput(new FacetsCSVOutputStream(result.getFacetResults(), findRequest.getFilename(), delimiter))
          .toStreamResponse());
    } else {
      IndexResult<T> result = Browser.find(returnClass, findRequest.getFilter(), findRequest.getSorter(), findRequest.getSublist(),
        findRequest.getFacets(), user, findRequest.isOnlyActive(), findRequest.getFieldsToReturn());
      if (localeString != null) {
        result = I18nUtility.translate(result, returnClass, localeString);
      }

      return ApiUtils
        .okResponse(new RodaStreamingOutput(new ResultsCSVOutputStream<>(result, findRequest.getFilename(), delimiter))
          .toStreamResponse());
    }
  }

  /**
   * Return the {@link Class} with the specified class name.
   *
   * @param className
   *          the fully qualified name of the desired class.
   * @param <T>
   *          the type of {@link Class}.
   * @return the {@link Class} with the specified class name.
   * @throws RequestNotValidException
   *           if the class name is not valid.
   */
  @SuppressWarnings("unchecked")
  private <T> Class<T> getClass(final String className) throws RequestNotValidException {
    if (RodaConstants.WHITELIST_CLASS_NAMES.contains(className)) {
      try {
        return (Class<T>) Class.forName(className);
      } catch (final ClassNotFoundException e) {
        throw new RequestNotValidException(String.format("Invalid value for classToReturn '%s'", className), e);
      }
    } else {
      throw new RequestNotValidException(String.format("Invalid value for classToReturn '%s'", className));
    }
  }
}
