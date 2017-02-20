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
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
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
import javax.xml.transform.TransformerException;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.JSONP;
import org.roda.core.common.EntityResponse;
import org.roda.core.common.StreamResponse;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
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
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;
import org.roda.wui.api.v1.utils.ObjectResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path(AipsResource.ENDPOINT)
@Api(value = AipsResource.SWAGGER_ENDPOINT)
public class AipsResource {
  public static final String ENDPOINT = "/v1/aips";
  public static final String SWAGGER_ENDPOINT = "v1 aips";

  @Context
  private HttpServletRequest request;

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "List AIPs", notes = "Gets a list of archival information packages (AIPs).", response = AIPs.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = AIPs.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response listAIPs(
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the AIP", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES_2, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name", required = false, allowMultiple = false, defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    boolean justActive = false;
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    IndexResult<IndexedAIP> result = Browser.find(IndexedAIP.class, Filter.NULL, Sorter.NONE,
      new Sublist(pagingParams.getFirst(), pagingParams.getSecond()), null, user, justActive, new ArrayList<>());

    return Response.ok(ApiUtils.indexedResultToRODAObjectList(IndexedAIP.class, result), mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP})
  @ApiOperation(value = "Get AIP", notes = "Get AIP information", response = AIP.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AIP.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrieveAIP(
    @ApiParam(value = "The ID of the AIP to retrieve.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "Choose format in which to get the AIP", allowableValues = RodaConstants.API_GET_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
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
  @ApiOperation(value = "Download part of the AIP")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrieveAIPPart(
    @ApiParam(value = "The ID of the AIP to retrieve.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The part of the AIP to download.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_PART) String part)
    throws RODAException {
    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    StreamResponse aipRepresentation = Browser.retrieveAIPPart(user, aipId, part);
    return ApiUtils.okResponse(aipRepresentation);
  }

  @PUT
  @ApiOperation(value = "Update AIP", notes = "Update existing AIP", response = AIP.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AIP.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updateAIP(AIP aip,
    @ApiParam(value = "Choose format in which to get the AIP", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    AIP updatedAIP = Browser.updateAIP(user, aip);
    return Response.ok(updatedAIP, mediaType).build();
  }

  @POST
  @ApiOperation(value = "Create AIP", notes = "Create a new AIP", response = AIP.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AIP.class),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})

  public Response createAIP(
    @ApiParam(value = "The ID of the parent AIP") @QueryParam(RodaConstants.API_QUERY_PARAM_PARENT_ID) String parentId,
    @ApiParam(value = "The type of the new AIP") @QueryParam(RodaConstants.API_QUERY_PARAM_TYPE) String type,
    @ApiParam(value = "Choose format in which to get the AIP", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
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
  @ApiOperation(value = "Delete AIP", notes = "Delete AIP", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deleteAIP(
    @ApiParam(value = "The ID of the AIP to delete.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "Reason to delete AIP", required = true) @FormParam(RodaConstants.API_QUERY_PARAM_DETAILS) String details,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    SelectedItems<IndexedAIP> aips = new SelectedItemsList<>(Arrays.asList(aipId), IndexedAIP.class.getName());
    Browser.deleteAIP(user, aips, details);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "AIP deleted"), mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_DESCRIPTIVE_METADATA + "/")
  @ApiOperation(value = "List descriptive metadata", notes = "List descriptive metadata", response = DescriptiveMetadataList.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = DescriptiveMetadataList.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrieveDescriptiveMetadataListFromAIP(
    @ApiParam(value = "The ID of an existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the list", allowableValues = RodaConstants.API_GET_LIST_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse metadataList = Browser.listAIPDescriptiveMetadata(user, aipId, start, limit, acceptFormat);

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
    MediaType.APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Get descriptive metadata", notes = "Get descriptive metadata (JSON or XML info, XML file or HTML conversion)", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrieveDescriptiveMetadataFromAIP(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing metadata file to retrieve", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @ApiParam(value = "The ID of the existing metadata file version to retrieve", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_VERSION_ID) String versionId,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = RodaConstants.API_GET_METADATA_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "The language for the HTML output", allowableValues = RodaConstants.API_DESCRIPTIVE_METADATA_LANGUAGES, defaultValue = RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @DefaultValue(RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @QueryParam(RodaConstants.API_QUERY_KEY_LANG) String language)
    throws RODAException {
    try {
      String mediaType = ApiUtils.getMediaType(acceptFormat, request);

      // get user
      User user = UserUtility.getApiUser(request);

      // delegate action to controller
      EntityResponse aipDescriptiveMetadata;
      if (versionId == null) {
        aipDescriptiveMetadata = Browser.retrieveAIPDescriptiveMetadata(user, aipId, metadataId, acceptFormat,
          language);
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

    } catch (TransformerException e) {
      return ApiUtils.errorResponse(e);
    }
  }

  @PUT
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_DESCRIPTIVE_METADATA + "/{"
    + RodaConstants.API_PATH_PARAM_METADATA_ID + "}")
  @ApiOperation(value = "Update descriptive metadata", notes = "Upload a descriptive metadata file to update an existing one", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updateDescriptiveMetadataOnAIP(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing metadata file to update", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @ApiParam(value = "The type of the metadata file (e.g. eadc2014, dc)", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_METADATA_TYPE) String metadataType,
    @ApiParam(value = "The version of the metadata type used", required = false) @QueryParam(RodaConstants.API_QUERY_PARAM_METADATA_VERSION) String metadataVersion,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
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
  @ApiOperation(value = "Create descriptive metadata", notes = "Upload a new descriptive metadata file", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})

  public Response createDescriptiveMetadataOnAIP(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The suggested ID metadata file to create", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @ApiParam(value = "The type of the metadata file (e.g. eadc2014, dc)", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_METADATA_TYPE) String metadataType,
    @ApiParam(value = "The version of the metadata type used", required = false) @QueryParam(RodaConstants.API_QUERY_PARAM_METADATA_VERSION) String metadataVersion,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
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
  @ApiOperation(value = "Delete descriptive metadata", notes = "Delete an existing descriptive metadata file", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deleteDescriptiveMetadataFromAIP(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing metadata file to delete", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
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
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP})
  @ApiOperation(value = "Get preservation metadata", notes = "Get preservation metadata (JSON info, ZIP file or HTML conversion).\nOptional query params of **start** and **limit** defined the returned array.", response = PreservationMetadataList.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PreservationMetadataList.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrievePreservationMetadataListFromAIP(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = RodaConstants.API_GET_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
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
  @ApiOperation(value = "Create preservation file", notes = "Create a preservation file to a AIP", response = PreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PreservationMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response createPreservationMetadataOnAIP(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the preservation metadata file", required = false) @QueryParam(RodaConstants.API_PATH_PARAM_FILE_ID) String fileId,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
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
  @ApiOperation(value = "Update representation preservation file", notes = "Update a preservation file to a representation", response = PreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PreservationMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updatePreservationMetadataOnAIP(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the preservation metadata file", required = false) @QueryParam(RodaConstants.API_PATH_PARAM_FILE_ID) String fileId,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
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
  @ApiOperation(value = "Delete representation preservation file", notes = "Delete a preservation file for a representation.", response = PreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PreservationMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deletePreservationMetadata(
    @ApiParam(value = "The ID of the existing AIP") @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing file", required = true) @PathParam(RodaConstants.API_PATH_PARAM_FILE_ID) String fileId,
    @ApiParam(value = "Choose preservation metadata type", allowableValues = "REPRESENTATION, FILE, INTELLECTUAL_ENTITY, AGENT, EVENT, RIGHTS_STATEMENT, ENVIRONMENT, OTHER", defaultValue = "FILE", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_TYPE) String type,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    Browser.deletePreservationMetadataWithAIP(user, aipId, fileId, type);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Preservation file deleted"), mediaType).build();
  }

  /*** OTHER METADATA ****/

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/" + RodaConstants.API_OTHER_METADATA + "/{"
    + RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE + "}/{" + RodaConstants.API_PATH_PARAM_OTHER_METADATA_FILE_SUFFIX
    + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP})
  @ApiOperation(value = "Get other metadata", notes = "Get other metadata (JSON info or ZIP file).\nOptional query params of **start** and **limit** defined the returned array.", response = OtherMetadataList.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = OtherMetadataList.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrieveOtherMetadata(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The type of the other metadata", required = true) @PathParam(RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE) String type,
    @ApiParam(value = "The file suffix of the other metadata", required = true) @PathParam(RodaConstants.API_PATH_PARAM_OTHER_METADATA_FILE_SUFFIX) String suffix,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = RodaConstants.API_GET_FILE_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    if (!suffix.startsWith(".")) {
      suffix = '.' + suffix;
    }

    // delegate action to controller
    EntityResponse otherMetadata = Browser.retrieveOtherMetadata(user, aipId, null, type, suffix, acceptFormat);

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
  @ApiOperation(value = "Create other metadata file", notes = "Create a other metadata file", response = OtherMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = OtherMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response createOtherMetadata(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The type of the other metadata", required = true) @PathParam(RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE) String type,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
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
  @ApiOperation(value = "Update other metadata file", notes = "Update other metadata file", response = OtherMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = OtherMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updateOtherMetadata(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The type of the other metadata", required = true) @PathParam(RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE) String type,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
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
  @ApiOperation(value = "Delete other metadata file", notes = "Delete other metadata file.", response = OtherMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = OtherMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deleteOtherMetadata(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The type of the other metadata", required = true) @PathParam(RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE) String type,
    @ApiParam(value = "The file suffix of the other metadata", required = true) @PathParam(RodaConstants.API_PATH_PARAM_OTHER_METADATA_FILE_SUFFIX) String suffix,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    if (!suffix.startsWith(".")) {
      suffix = '.' + suffix;
    }

    Browser.deleteOtherMetadata(user, aipId, null, suffix, type);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Other metadata file deleted"), mediaType).build();
  }

}
