/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.io.IOException;
import java.io.InputStream;

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
import org.roda.core.common.EntityResponse;
import org.roda.core.common.StreamResponse;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.Representations;
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

@Path(RepresentationsResource.ENDPOINT)
@Api(value = RepresentationsResource.SWAGGER_ENDPOINT)
public class RepresentationsResource {
  public static final String ENDPOINT = "/v1/representations";
  public static final String SWAGGER_ENDPOINT = "v1 representations";

  @Context
  private HttpServletRequest request;

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "List Representations", notes = "Gets a list of representations.", response = Representations.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = Representations.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response listRepresentations(
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    boolean justActive = false;
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    IndexResult<IndexedRepresentation> result = Browser.find(IndexedRepresentation.class, Filter.NULL, Sorter.NONE,
      new Sublist(pagingParams.getFirst(), pagingParams.getSecond()), null, user, justActive);
    return Response.ok(ApiUtils.indexedResultToRODAObjectList(IndexedRepresentation.class, result), mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP})
  @ApiOperation(value = "Get representation", notes = "Get representation", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrieveRepresentation(
    @ApiParam(value = "The ID of the existing aip", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = RodaConstants.API_GET_LIST_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse aipRepresentation = Browser.retrieveAIPRepresentation(user, aipId, representationId, acceptFormat);

    if (aipRepresentation instanceof ObjectResponse) {
      ObjectResponse<Representation> rep = (ObjectResponse<Representation>) aipRepresentation;
      return Response.ok(rep.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) aipRepresentation);
    }
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/{"
    + RodaConstants.API_PATH_PARAM_PART + "}")
  @ApiOperation(value = "Download part of the representation")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrieveRepresentationPart(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The part of the representation to download", required = true, allowableValues = "data, metadata, documentation, schemas") @PathParam(RodaConstants.API_PATH_PARAM_PART) String part)
    throws RODAException {
    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    StreamResponse aipRepresentation = Browser.retrieveAIPRepresentationPart(user, aipId, representationId, part);
    return ApiUtils.okResponse(aipRepresentation);
  }

  @PUT
  @ApiOperation(value = "Update representation", notes = "Update existing representation", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updateRepresentation(Representation representation,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Representation rep = Browser.updateRepresentation(user, representation);
    return Response.ok(rep, mediaType).build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Create representation", notes = "Create a new representation on an AIP", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})

  public Response createRepresentation(
    @ApiParam(value = "The AIP to add the representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The desired representation ID", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The type of the new representation", required = true) @FormParam(RodaConstants.API_QUERY_PARAM_TYPE) String type,
    @ApiParam(value = "Reason to create representation", required = true) @FormParam(RodaConstants.API_QUERY_PARAM_DETAILS) String details,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Representation rep = Browser.createRepresentation(user, aipId, representationId, type, details);
    return Response.ok(rep, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}")
  @ApiOperation(value = "Delete representation", notes = "Delete representation", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deleteRepresentation(
    @ApiParam(value = "The ID of the existing AIP that contains the representation to delete", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation to delete", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);
    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.deleteRepresentation(user, aipId, representationId);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Representation deleted"), mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/"
    + RodaConstants.API_DESCRIPTIVE_METADATA + "/")
  @ApiOperation(value = "List descriptive metadata", notes = "List descriptive metadata", response = DescriptiveMetadataList.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = DescriptiveMetadataList.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Representation not found", response = ApiResponseMessage.class)})

  public Response retrieveDescriptiveMetadataListFromRepresentation(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = RodaConstants.API_GET_LIST_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse metadataList = Browser.listRepresentationDescriptiveMetadata(user, aipId, representationId, start,
      limit, acceptFormat);

    if (metadataList instanceof ObjectResponse) {
      ObjectResponse<DescriptiveMetadataList> dmlist = (ObjectResponse<DescriptiveMetadataList>) metadataList;
      return Response.ok(dmlist.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) metadataList);
    }
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/"
    + RodaConstants.API_DESCRIPTIVE_METADATA + "/{" + RodaConstants.API_PATH_PARAM_METADATA_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML,
    MediaType.APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Get descriptive metadata", notes = "Get descriptive metadata (JSON info, XML file or HTML conversion)", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrieveDescriptiveMetadataFromRepresentation(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The ID of the existing metadata file to retrieve", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @ApiParam(value = "The ID of the existing metadata file version to retrieve", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_VERSION_ID) String versionId,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = RodaConstants.API_GET_DESCRIPTIVE_METADATA_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "The language for the HTML output", allowableValues = RodaConstants.API_DESCRIPTIVE_METADATA_LANGUAGES, defaultValue = RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @DefaultValue(RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @QueryParam(RodaConstants.API_QUERY_KEY_LANG) String language)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse aipDescriptiveMetadata = Browser.retrieveRepresentationDescriptiveMetadata(user, aipId,
      representationId, metadataId, versionId, acceptFormat, language);

    if (aipDescriptiveMetadata instanceof ObjectResponse) {
      ObjectResponse<DescriptiveMetadata> dm = (ObjectResponse<DescriptiveMetadata>) aipDescriptiveMetadata;
      return Response.ok(dm.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) aipDescriptiveMetadata);
    }
  }

  @PUT
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/"
    + RodaConstants.API_DESCRIPTIVE_METADATA + "/{" + RodaConstants.API_PATH_PARAM_METADATA_ID + "}")
  @ApiOperation(value = "Update descriptive metadata", notes = "Upload a descriptive metadata file to update an existing one", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updateDescriptiveMetadataOnRepresentation(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
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
    DescriptiveMetadata dm = Browser.updateRepresentationDescriptiveMetadataFile(user, aipId, representationId,
      metadataId, metadataType, metadataVersion, inputStream);

    return Response.ok(dm, mediaType).build();
  }

  @POST
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/"
    + RodaConstants.API_DESCRIPTIVE_METADATA + "/{" + RodaConstants.API_PATH_PARAM_METADATA_ID + "}")
  @ApiOperation(value = "Create descriptive metadata", notes = "Upload a new descriptive metadata file", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})

  public Response createDescriptiveMetadataOnRepresentation(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
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
    DescriptiveMetadata dm = Browser.createRepresentationDescriptiveMetadataFile(user, aipId, representationId,
      metadataId, metadataType, metadataVersion, inputStream);

    return Response.ok(dm, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/"
    + RodaConstants.API_DESCRIPTIVE_METADATA + "/{" + RodaConstants.API_PATH_PARAM_METADATA_ID + "}")
  @ApiOperation(value = "Delete descriptive metadata", notes = "Delete an existing descriptive metadata file", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deleteDescriptiveMetadataFromRepresentation(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The ID of the existing metadata file to delete", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.deleteRepresentationDescriptiveMetadataFile(user, aipId, representationId, metadataId);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Descriptive metadata deleted"), mediaType)
      .build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/"
    + RodaConstants.API_PRESERVATION_METADATA + "/")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP})
  @ApiOperation(value = "Get representation preservation metadata", notes = "Get representation preservation metadata (JSON info, ZIP file conversion) for a given representation.\nOptional query params of **start** and **limit** defined the returned array.", response = PreservationMetadataList.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PreservationMetadataList.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrievePreservationMetadataListFromRepresentation(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = RodaConstants.API_GET_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "Index of the agent element to return", defaultValue = "0") @QueryParam("startAgent") String startAgent,
    @ApiParam(value = "Maximum number of agents to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam("limitAgent") String limitAgent,
    @ApiParam(value = "Index of the first event to return", defaultValue = "0") @QueryParam("startEvent") String startEvent,
    @ApiParam(value = "Maximum number of events to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam("limitEvent") String limitEvent,
    @ApiParam(value = "Index of the first file to return", defaultValue = "0") @QueryParam("startFile") String startFile,
    @ApiParam(value = "Maximum number of files to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam("limitFile") String limitFile,
    @ApiParam(value = "The language for the HTML output", allowableValues = RodaConstants.API_DESCRIPTIVE_METADATA_LANGUAGES, defaultValue = RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @DefaultValue(RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @QueryParam(RodaConstants.API_QUERY_KEY_LANG) String language)
    throws RODAException {
    try {
      String mediaType = ApiUtils.getMediaType(acceptFormat, request);

      // get user
      User user = UserUtility.getApiUser(request);

      // delegate action to controller
      EntityResponse preservationMetadataList = Browser.retrieveAIPRepresentationPreservationMetadata(user, aipId,
        representationId, startAgent, limitAgent, startEvent, limitEvent, startFile, limitFile, acceptFormat, language);

      if (preservationMetadataList instanceof ObjectResponse) {
        ObjectResponse<PreservationMetadataList> pmlist = (ObjectResponse<PreservationMetadataList>) preservationMetadataList;
        return Response.ok(pmlist.getObject(), mediaType).build();
      } else {
        return ApiUtils.okResponse((StreamResponse) preservationMetadataList);
      }

    } catch (TransformerException e) {
      return ApiUtils.errorResponse(e);
    } catch (IOException e) {
      return ApiUtils.errorResponse(new TransformerException(e.getMessage()));
    }
  }

  @POST
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/"
    + RodaConstants.API_PRESERVATION_METADATA + "/")
  @ApiOperation(value = "Create representation preservation file", notes = "Create a preservation file to a representation", response = PreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PreservationMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response createPreservationMetadataOnFile(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The ID of the preservation metadata file", required = false) @QueryParam(RodaConstants.API_PATH_PARAM_FILE_ID) String fileId,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.createOrUpdatePreservationMetadataWithRepresentation(user, aipId, representationId, fileId, inputStream,
      fileDetail.getFileName(), true);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Preservation file created"), mediaType).build();
  }

  @PUT
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/"
    + RodaConstants.API_PRESERVATION_METADATA + "/")
  @ApiOperation(value = "Update representation preservation file", notes = "Update a preservation file to a representation", response = PreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PreservationMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updatePreservationMetadataOnFile(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The ID of the preservation metadata file", required = false) @QueryParam(RodaConstants.API_PATH_PARAM_FILE_ID) String fileId,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.createOrUpdatePreservationMetadataWithRepresentation(user, aipId, representationId, fileId, inputStream,
      fileDetail.getFileName(), false);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Preservation file updated"), mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/"
    + RodaConstants.API_PRESERVATION_METADATA + "/{" + RodaConstants.API_PATH_PARAM_FILE_ID + "}")
  @ApiOperation(value = "Delete representation preservation file", notes = "Delete a preservation file for a representation.", response = PreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PreservationMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deletePreservationMetadataFromFile(
    @ApiParam(value = "The ID of the existing AIP") @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation") @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The ID of the existing file", required = true) @PathParam(RodaConstants.API_PATH_PARAM_FILE_ID) String fileId,
    @ApiParam(value = "Choose preservation metadata type", allowableValues = "REPRESENTATION, FILE, INTELLECTUAL_ENTITY, AGENT, EVENT, RIGHTS_STATEMENT, ENVIRONMENT, OTHER", defaultValue = "FILE", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_TYPE) String type,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    Browser.deletePreservationMetadataWithRepresentation(user, aipId, representationId, fileId, type);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Preservation file deleted"), mediaType).build();
  }

  /*** OTHER METADATA ****/

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/"
    + RodaConstants.API_OTHER_METADATA + "/")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP})
  @ApiOperation(value = "List other metadata", notes = "List other metadata (JSON info or ZIP file).\nOptional query params of **start** and **limit** "
    + "defined the returned array.", response = OtherMetadataList.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = OtherMetadataList.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrieveOtherMetadataList(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation") @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = RodaConstants.API_GET_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse otherMetadataList = Browser.listOtherMetadata(user, aipId, representationId, null, acceptFormat);

    if (otherMetadataList instanceof ObjectResponse) {
      ObjectResponse<OtherMetadataList> list = (ObjectResponse<OtherMetadataList>) otherMetadataList;
      return Response.ok(list.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) otherMetadataList);
    }
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/"
    + RodaConstants.API_OTHER_METADATA + "/{" + RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE + "}/{"
    + RodaConstants.API_PATH_PARAM_OTHER_METADATA_FILE_SUFFIX + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_ZIP})
  @ApiOperation(value = "Get other metadata", notes = "Get other metadata (JSON info or ZIP file).\nOptional query params of **start** and **limit** defined the returned array.", response = OtherMetadataList.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = OtherMetadataList.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrieveOtherMetadata(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
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
    EntityResponse otherMetadata = Browser.retrieveOtherMetadata(user, aipId, representationId, type, suffix,
      acceptFormat);

    if (otherMetadata instanceof ObjectResponse) {
      ObjectResponse<OtherMetadata> om = (ObjectResponse<OtherMetadata>) otherMetadata;
      return Response.ok(om.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) otherMetadata);
    }
  }

  @POST
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/"
    + RodaConstants.API_OTHER_METADATA + "/{" + RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE + "}")
  @ApiOperation(value = "Create other metadata file", notes = "Create a other metadata file", response = OtherMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = OtherMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response createOtherMetadata(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The type of the other metadata", required = true) @PathParam(RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE) String type,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.createOrUpdateOtherMetadata(user, aipId, representationId, type, inputStream, fileDetail.getFileName());
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Other metadata file created"), mediaType).build();
  }

  @PUT
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/"
    + RodaConstants.API_OTHER_METADATA + "/{" + RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE + "}")
  @ApiOperation(value = "Update other metadata file", notes = "Update other metadata file", response = OtherMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = OtherMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updatePreservationMetadataOnAIP(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The type of the other metadata", required = true) @PathParam(RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE) String type,
    @FormDataParam(RodaConstants.API_PARAM_FILE) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_FILE) FormDataContentDisposition fileDetail,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.createOrUpdateOtherMetadata(user, aipId, representationId, type, inputStream, fileDetail.getFileName());
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Other metadata file updated"), mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_AIP_ID + "}/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_ID + "}/"
    + RodaConstants.API_OTHER_METADATA + "/{" + RodaConstants.API_PATH_PARAM_OTHER_METADATA_TYPE + "}/{"
    + RodaConstants.API_PATH_PARAM_OTHER_METADATA_FILE_SUFFIX + "}")
  @ApiOperation(value = "Delete other metadata file", notes = "Delete other metadata file.", response = OtherMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = OtherMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deletePreservationMetadata(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
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

    Browser.deleteOtherMetadata(user, aipId, representationId, suffix, type);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Other metadata file deleted"), mediaType).build();
  }

}
