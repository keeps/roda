/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.JSONP;
import org.roda.core.common.EntityResponse;
import org.roda.core.common.StreamResponse;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPs;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataList;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadataList;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadataList;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;
import org.roda.wui.api.v1.utils.ObjectResponse;

import com.google.json.JsonSanitizer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(AipsResource.ENDPOINT)
@Tag(name = AipsResource.SWAGGER_ENDPOINT)
public class AipsResource {
  public static final String ENDPOINT = "/v1/aips";
  public static final String SWAGGER_ENDPOINT = "v1 aips";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "List AIPs", description = "Gets a list of archival information packages (AIPs).", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AIPs.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response listAIPs(
    @Parameter(description = "Index of the first element to return", schema = @Schema(defaultValue = "0")) @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @Parameter(description = "Maximum number of elements to return", schema = @Schema(defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE)) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @Parameter(description = "Choose format in which to get the AIP", schema = @Schema(implementation = RodaConstants.ListMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    boolean justActive = false;
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    IndexResult<IndexedAIP> result = Browser.find(IndexedAIP.class, Filter.ALL, Sorter.NONE,
      new Sublist(pagingParams.getFirst(), pagingParams.getSecond()), null, user, justActive, new ArrayList<>());

    return Response.ok(ApiUtils.indexedResultToRODAObjectList(IndexedAIP.class, result), mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP,
    ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Get AIP", description = "Gets AIP information", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AIP.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response retrieveAIP(
    @Parameter(description = "The ID of the AIP to retrieve.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "Choose format in which to get the AIP", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", schema = @Schema()) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    IndexedAIP indexedAIP = Browser.retrieve(user, IndexedAIP.class, aipId, new ArrayList<>());
    return ApiUtils.okResponse(indexedAIP, acceptFormat, mediaType);
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_PART + "}")
  @Operation(summary = "Download part of the AIP", responses = {@ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response retrieveAIPPart(
    @Parameter(description = "The ID of the AIP to retrieve.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The part of the AIP to download.", required = true, schema = @Schema(allowableValues = {
      "submission", "documentation", "schemas"})) @PathParam(RodaConstants.API_PATH_PARAM_PART) String part)
    throws RODAException {
    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    return ApiUtils.okResponse(Browser.retrieveAIPPart(user, aipId, part));
  }

  @PUT
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Update AIP", description = "Update existing AIP", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AIP.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response updateAIP(AIP aip,
    @Parameter(description = "Choose format in which to get the AIP", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // sanitize the input
    String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(aip));
    aip = JsonUtils.getObjectFromJson(sanitize, AIP.class);

    // delegate action to controller
    AIP updatedAIP = Browser.updateAIP(user, aip);
    return Response.ok(updatedAIP, mediaType).build();
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Create AIP", description = "Create a new AIP", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AIP.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response createAIP(
    @Parameter(description = "The ID of the parent AIP") @QueryParam(RodaConstants.API_QUERY_PARAM_PARENT_ID) String parentId,
    @Parameter(description = "The type of the new AIP") @QueryParam(RodaConstants.API_QUERY_PARAM_TYPE) String type,
    @Parameter(description = "Choose format in which to get the AIP", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    AIP aip = Browser.createAIP(user, parentId, type);
    return Response.ok(aip, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Delete AIP", description = "Delete existing AIP", responses = {
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response deleteAIP(
    @Parameter(description = "The ID of the AIP to delete.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "Reason to delete AIP", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_DETAILS) String details,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    SelectedItems<IndexedAIP> aips = new SelectedItemsList<>(Collections.singletonList(aipId),
      IndexedAIP.class.getName());
    Browser.deleteAIP(user, aips, details);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "AIP deleted"), mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_DESCRIPTIVE_METADATA + "/")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP,
    ExtraMediaType.APPLICATION_JAVASCRIPT, MediaType.TEXT_HTML})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "List descriptive metadata", description = "List descriptive metadata", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DescriptiveMetadataList.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response retrieveDescriptiveMetadataListFromAIP(
    @Parameter(description = "The ID of an existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "Index of the first element to return", schema = @Schema(defaultValue = "0")) @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @Parameter(description = "Maximum number of elements to return", schema = @Schema(defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE)) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @Parameter(description = "Choose format in which to get the list", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "The language for the HTML output", schema = @Schema(implementation = RodaConstants.DescriptibeMetadataLanguages.class, defaultValue = RodaConstants.API_QUERY_VALUE_LANG_DEFAULT)) @DefaultValue(RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @QueryParam(RodaConstants.API_QUERY_KEY_LANG) String language,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse metadataList = Browser.listAIPDescriptiveMetadata(user, aipId, start, limit, acceptFormat, language);

    if (metadataList instanceof ObjectResponse) {
      ObjectResponse<DescriptiveMetadataList> dmlist = (ObjectResponse<DescriptiveMetadataList>) metadataList;
      return Response.ok(dmlist.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) metadataList);
    }
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_DESCRIPTIVE_METADATA + "/{"
    + RodaConstants.API_PATH_PARAM_METADATA_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML,
    MediaType.APPLICATION_OCTET_STREAM, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Get descriptive metadata", description = "Get descriptive metadata", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DescriptiveMetadata.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response retrieveDescriptiveMetadataFromAIP(
    @Parameter(description = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The ID of the existing metadata file to retrieve", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @Parameter(description = "The ID of the existing metadata file version to retrieve", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_VERSION_ID) String versionId,
    @Parameter(description = "Choose format in which to get the metadata", schema = @Schema(implementation = RodaConstants.MetadataMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "The language for the HTML output", schema = @Schema(implementation = RodaConstants.DescriptibeMetadataLanguages.class, defaultValue = RodaConstants.API_QUERY_VALUE_LANG_DEFAULT)) @DefaultValue(RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @QueryParam(RodaConstants.API_QUERY_KEY_LANG) String language,
    @Parameter(description = "JSONP callback name", schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse aipDescriptiveMetadata;
    if (versionId == null) {
      aipDescriptiveMetadata = Browser.retrieveAIPDescriptiveMetadata(user, aipId, metadataId, acceptFormat, language);
    } else {
      aipDescriptiveMetadata = Browser.retrieveAIPDescriptiveMetadataVersion(user, aipId, metadataId, versionId,
        acceptFormat, language);
    }

    if (aipDescriptiveMetadata instanceof ObjectResponse) {
      ObjectResponse<DescriptiveMetadata> dm = (ObjectResponse<DescriptiveMetadata>) aipDescriptiveMetadata;
      return Response.ok(dm.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) aipDescriptiveMetadata);
    }
  }

  @PUT
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_DESCRIPTIVE_METADATA + "/{"
    + RodaConstants.API_PATH_PARAM_METADATA_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Update descriptive metadata", description = "Upload a descriptive metadata file to update an existing one", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DescriptiveMetadata.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response updateDescriptiveMetadataOnAIP(
    @Parameter(description = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The ID of the existing metadata file to update", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @Parameter(description = "The type of the metadata file (e.g. eadc2014, dc)", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_METADATA_TYPE) String metadataType,
    @Parameter(description = "The version of the metadata type used", required = false) @QueryParam(RodaConstants.API_QUERY_PARAM_METADATA_VERSION) String metadataVersion,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    DescriptiveMetadata dm = Browser.updateAIPDescriptiveMetadataFile(user, aipId, metadataId, metadataType,
      metadataVersion, inputStream);

    return Response.ok(dm, mediaType).build();
  }

  @POST
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_DESCRIPTIVE_METADATA + "/{"
    + RodaConstants.API_PATH_PARAM_METADATA_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Create descriptive metadata", description = "Upload a new descriptive metadata file", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DescriptiveMetadata.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response createDescriptiveMetadataOnAIP(
    @Parameter(description = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The suggested ID metadata file to create", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @Parameter(description = "The type of the metadata file (e.g. eadc2014, dc)", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_METADATA_TYPE) String metadataType,
    @Parameter(description = "The version of the metadata type used", required = false) @QueryParam(RodaConstants.API_QUERY_PARAM_METADATA_VERSION) String metadataVersion,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    DescriptiveMetadata dm = Browser.createAIPDescriptiveMetadataFile(user, aipId, metadataId, metadataType,
      metadataVersion, inputStream);

    return Response.ok(dm, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_DESCRIPTIVE_METADATA + "/{"
    + RodaConstants.API_PATH_PARAM_METADATA_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Delete descriptive metadata", description = "Delete an existing descriptive metadata file", responses = {
    @ApiResponse(responseCode = "204", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response deleteDescriptiveMetadataFromAIP(
    @Parameter(description = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The ID of the existing metadata file to delete", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.deleteDescriptiveMetadataFile(user, aipId, null, metadataId);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Descriptive metadata deleted"), mediaType)
      .build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_PRESERVATION_METADATA + "/")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP,
    ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Get preservation metadata", description = "Get preservation\n metadata (JSON info, ZIP file or HTML conversion).\nOptional query params of **start** and **limit** defined the returned array.", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PreservationMetadataList.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response retrievePreservationMetadataListFromAIP(
    @Parameter(description = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "Index of the first element to return", schema = @Schema(defaultValue = "0")) @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @Parameter(description = "Maximum number of elements to return", schema = @Schema(defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE)) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @Parameter(description = "Choose format in which to get the metadata", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse preservationMetadataList = Browser.listAIPPreservationMetadata(user, aipId, acceptFormat);

    if (preservationMetadataList instanceof ObjectResponse) {
      ObjectResponse<PreservationMetadataList> pmlist = (ObjectResponse<PreservationMetadataList>) preservationMetadataList;
      return Response.ok(pmlist.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) preservationMetadataList);
    }
  }

  @POST
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_PRESERVATION_METADATA + "/")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP,
    ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Create preservation file", description = "Create a preservation file to a AIP", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PreservationMetadata.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response createPreservationMetadataOnAIP(
    @Parameter(description = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The ID of the preservation metadata file", required = false) @QueryParam(RodaConstants.API_PATH_PARAM_FILE_ID) String fileId,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.createOrUpdatePreservationMetadataWithAIP(user, aipId, fileId, inputStream, fileDetail.getFileName(), true);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Preservation file created"), mediaType).build();
  }

  @PUT
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_PRESERVATION_METADATA + "/")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP,
    ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Update representation preservation file", description = "Update a preservation file to a representation", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PreservationMetadata.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response updatePreservationMetadataOnAIP(
    @Parameter(description = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The ID of the preservation metadata file", required = false) @QueryParam(RodaConstants.API_PATH_PARAM_FILE_ID) String fileId,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.createOrUpdatePreservationMetadataWithAIP(user, aipId, fileId, inputStream, fileDetail.getFileName(),
      false);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Preservation file updated"), mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_PRESERVATION_METADATA + "/{"
    + RodaConstants.API_PATH_PARAM_FILE_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP,
    ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Delete representation preservation file", description = "Delete a preservation file to a representation", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PreservationMetadata.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response deletePreservationMetadata(
    @Parameter(description = "The ID of the existing AIP") @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The ID of the existing file", required = true) @PathParam(RodaConstants.API_PATH_PARAM_FILE_ID) String fileId,
    @Parameter(description = "Choose preservation metadata type", schema = @Schema(allowableValues = {"REPRESENTATION",
      "FILE", "INTELLECTUAL_ENTITY", "AGENT", "EVENT", "RIGHTS_STATEMENT", "ENVIRONMENT",
      "OTHER"}, defaultValue = "FILE", required = true)) @QueryParam(RodaConstants.API_QUERY_PARAM_TYPE) String type,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    Browser.deletePreservationMetadataWithAIP(user, aipId, fileId, type);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Preservation file deleted"), mediaType).build();
  }

  // GET file premis
  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_PRESERVATION_METADATA + "/{"
    + RodaConstants.API_PATH_PARAM_FILE_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP,
    ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Get representation preservation file", description = "Get a preservation file to a representation", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PreservationMetadata.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response getPreservationMetadata(
    @Parameter(description = "The ID of the existing AIP") @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The ID of the existing file", required = true) @PathParam(RodaConstants.API_PATH_PARAM_FILE_ID) String fileId,
    @Parameter(description = "The ID of the existing metadata file version to retrieve", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_VERSION_ID) String versionId,
    @Parameter(description = "Choose preservation metadata type", schema = @Schema(allowableValues = {"REPRESENTATION",
      "FILE", "INTELLECTUAL_ENTITY", "AGENT", "EVENT", "RIGHTS_STATEMENT", "ENVIRONMENT",
      "OTHER"}, defaultValue = "FILE", required = true)) @QueryParam(RodaConstants.API_QUERY_PARAM_TYPE) String type,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "The language for the HTML output", schema = @Schema(implementation = RodaConstants.DescriptibeMetadataLanguages.class, defaultValue = RodaConstants.API_QUERY_VALUE_LANG_DEFAULT)) @DefaultValue(RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @QueryParam(RodaConstants.API_QUERY_KEY_LANG) String language,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse filePreservationMetadata = null;
    if (versionId == null) {
      filePreservationMetadata = Browser.retrieveFilePreservationMetadata(user, aipId, fileId, acceptFormat, language);
    } else {
      filePreservationMetadata = Browser.retrieveAIPDescriptiveMetadataVersion(user, aipId, fileId, versionId,
        acceptFormat, language);
    }

    if (filePreservationMetadata instanceof ObjectResponse) {
      ObjectResponse<DescriptiveMetadata> dm = (ObjectResponse<DescriptiveMetadata>) filePreservationMetadata;
      return Response.ok(dm.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) filePreservationMetadata);
    }
  }

  /*** OTHER METADATA ****/

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_OTHER_METADATA + "/{"
    + RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE + "}/{" + RodaConstants.API_PATH_PARAM_OTHER_METADATA_FILE_SUFFIX
    + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP,
    ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Get other metadata", description = "Get other metadata (JSON info or ZIP file).\\nOptional query params of **start** and **limit** defined the returned array.", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = OtherMetadataList.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response retrieveOtherMetadata(
    @Parameter(description = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The type of the other metadata", required = true) @PathParam(RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE) String type,
    @Parameter(description = "The file suffix of the other metadata", required = true) @PathParam(RodaConstants.API_PATH_PARAM_OTHER_METADATA_FILE_SUFFIX) String suffix,
    @Parameter(description = "Choose format in which to get the metadata", schema = @Schema(implementation = RodaConstants.GetFileMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);
    String fileSuffix = suffix;

    if (!fileSuffix.startsWith(".")) {
      fileSuffix = '.' + fileSuffix;
    }

    // delegate action to controller
    EntityResponse otherMetadata = Browser.retrieveOtherMetadata(user, aipId, null, type, fileSuffix, acceptFormat);

    if (otherMetadata instanceof ObjectResponse) {
      ObjectResponse<OtherMetadata> om = (ObjectResponse<OtherMetadata>) otherMetadata;
      return Response.ok(om.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) otherMetadata);
    }
  }

  @POST
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_OTHER_METADATA + "/{"
    + RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Create other metadata file", description = "Create other metadata file", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = OtherMetadata.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response createOtherMetadata(
    @Parameter(description = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The type of the other metadata", required = true) @PathParam(RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE) String type,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.createOrUpdateOtherMetadata(user, aipId, null, type, inputStream, fileDetail.getFileName());
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Other metadata file created"), mediaType).build();
  }

  @PUT
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_OTHER_METADATA + "/{"
    + RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Update other metadata file", description = "Update other metadata file", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = OtherMetadata.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response updateOtherMetadata(
    @Parameter(description = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The type of the other metadata", required = true) @PathParam(RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE) String type,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.createOrUpdateOtherMetadata(user, aipId, null, type, inputStream, fileDetail.getFileName());
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Other metadata file updated"), mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_OTHER_METADATA + "/{"
    + RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE + "}/{" + RodaConstants.API_PATH_PARAM_OTHER_METADATA_FILE_SUFFIX
    + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Delete other metadata file", description = "Delete other metadata file", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = OtherMetadata.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response deleteOtherMetadata(
    @Parameter(description = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @Parameter(description = "The type of the other metadata", required = true) @PathParam(RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE) String type,
    @Parameter(description = "The file suffix of the other metadata", required = true) @PathParam(RodaConstants.API_PATH_PARAM_OTHER_METADATA_FILE_SUFFIX) String suffix,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);
    String fileSuffix = suffix;

    if (!fileSuffix.startsWith(".")) {
      fileSuffix = '.' + fileSuffix;
    }

    Browser.deleteOtherMetadata(user, aipId, null, fileSuffix, type);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Other metadata file deleted"), mediaType).build();
  }
}
