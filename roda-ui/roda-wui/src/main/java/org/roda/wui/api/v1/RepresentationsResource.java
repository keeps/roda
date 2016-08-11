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
import org.roda.core.common.StreamResponse;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.Representations;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.EntityResponse;
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
  @ApiOperation(value = "List Representations", notes = "Gets a list of representations.", response = Representation.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = Representation.class, responseContainer = "List")})

  public Response listRepresentations(
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = "json, xml", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    Representations reps = (Representations) Browser.retrieveObjects(user, IndexedRepresentation.class, start, limit,
      acceptFormat);
    return Response.ok(reps, mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID + "}")
  @Produces({"application/json", "application/zip"})
  @ApiOperation(value = "Get representation", notes = "Get representation", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response retrieveRepresentation(
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationUUID,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = "json, xml, zip") @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse aipRepresentation = Browser.retrieveAIPRepresentation(user, representationUUID, acceptFormat);

    if (aipRepresentation instanceof ObjectResponse) {
      ObjectResponse<Representation> rep = (ObjectResponse<Representation>) aipRepresentation;
      return Response.ok(rep.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) aipRepresentation);
    }
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID + "}/{" + RodaConstants.API_PATH_PARAM_PART + "}")
  @ApiOperation(value = "Download part of the representation")
  public Response retrieveRepresentationPart(
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationUUID,
    @ApiParam(value = "The part of the representation to download", required = true, allowableValues = "data, metadata, documentation, schemas") @PathParam(RodaConstants.API_PATH_PARAM_PART) String part)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    StreamResponse aipRepresentation = Browser.retrieveAIPRepresentationPart(user, representationUUID, part);
    return ApiUtils.okResponse(aipRepresentation);
  }

  @PUT
  @ApiOperation(value = "Update representation", notes = "Update existing representation", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updateRepresentation(Representation representation,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = "json, xml") @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    Representation rep = Browser.updateRepresentation(user, representation);
    return Response.ok(rep, mediaType).build();
  }

  @POST
  @ApiOperation(value = "Create representation", notes = "Create a new representation on an AIP", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response createRepresentation(
    @ApiParam(value = "The AIP to add the representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The desired representation ID", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The type of the new representation", required = true) @FormParam(RodaConstants.API_QUERY_PARAM_TYPE) String type,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = "json, xml") @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    Representation rep = Browser.createRepresentation(user, aipId, representationId, type);
    return Response.ok(rep, mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID + "}")
  @ApiOperation(value = "Delete representation", notes = "Delete representation", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deleteRepresentation(
    @ApiParam(value = "The ID of the existing representation to delete", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationUUID)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.deleteRepresentation(user, representationUUID);
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Representation was deleted!")).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID + "}/descriptive_metadata/")
  @ApiOperation(value = "List descriptive metadata", notes = "List descriptive metadata", response = DescriptiveMetadata.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Representation not found", response = DescriptiveMetadata.class, responseContainer = "List")})

  public Response retrieveDescriptiveMetadataListFromRepresentation(
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationId,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = "json, xml, bin") @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse metadataList = Browser.listRepresentationDescriptiveMetadata(user, representationId, start, limit,
      acceptFormat);

    if (metadataList instanceof ObjectResponse) {
      ObjectResponse<DescriptiveMetadata> rep = (ObjectResponse<DescriptiveMetadata>) metadataList;
      return Response.ok(rep.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) metadataList);
    }
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID + "}/descriptive_metadata/{"
    + RodaConstants.API_PATH_PARAM_METADATA_ID + "}")
  @Produces({"application/json", "application/xml", "text/html"})
  @ApiOperation(value = "Get descriptive metadata", notes = "Get descriptive metadata (JSON info, XML file or HTML conversion)", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class)})

  public Response retrieveDescriptiveMetadataFromRepresentation(
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationId,
    @ApiParam(value = "The ID of the existing metadata file to retrieve", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = "xml, html, json, bin", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "The language for the HTML output", allowableValues = "pt_PT, en_US", defaultValue = RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @DefaultValue(RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @QueryParam(RodaConstants.API_QUERY_KEY_LANG) String language)
    throws RODAException {

    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse aipDescriptiveMetadata = Browser.retrieveRepresentationDescriptiveMetadata(user, representationId,
      metadataId, acceptFormat, language);

    if (aipDescriptiveMetadata instanceof ObjectResponse) {
      ObjectResponse<DescriptiveMetadata> dm = (ObjectResponse<DescriptiveMetadata>) aipDescriptiveMetadata;
      return Response.ok(dm.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) aipDescriptiveMetadata);
    }
  }

  @PUT
  @Path("/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID + "}/descriptive_metadata/{"
    + RodaConstants.API_PATH_PARAM_METADATA_ID + "}")
  @ApiOperation(value = "Update descriptive metadata", notes = "Upload a descriptive metadata file to update an existing one", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updateDescriptiveMetadataOnRepresentation(
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationId,
    @ApiParam(value = "The ID of the existing metadata file to update", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail,
    @ApiParam(value = "The type of the metadata file (e.g. eadc2014, dc)", required = true) @QueryParam("metadataType") String metadataType,
    @ApiParam(value = "The version of the metadata type used", required = false) @QueryParam("metadataVersion") String metadataVersion)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    DescriptiveMetadata dm = Browser.putRepresentationDescriptiveMetadataFile(user, representationId, metadataId,
      metadataType, metadataVersion, inputStream, fileDetail);

    return Response.ok(dm, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON).build();
  }

  @POST
  @Path("/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID + "}/descriptive_metadata/{"
    + RodaConstants.API_PATH_PARAM_METADATA_ID + "}")
  @ApiOperation(value = "Create descriptive metadata", notes = "Upload a new descriptive metadata file", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})

  public Response createDescriptiveMetadataOnRepresentation(
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationId,
    @ApiParam(value = "The suggested ID metadata file to create", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail,
    @ApiParam(value = "The type of the metadata file (e.g. eadc2014, dc)", required = true) @QueryParam("metadataType") String metadataType,
    @ApiParam(value = "The version of the metadata type used", required = false) @QueryParam("metadataVersion") String metadataVersion)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    DescriptiveMetadata dm = Browser.postRepresentationDescriptiveMetadataFile(user, representationId, metadataId,
      metadataType, metadataVersion, inputStream, fileDetail);

    return Response.ok(dm, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID + "}/descriptive_metadata/{"
    + RodaConstants.API_PATH_PARAM_METADATA_ID + "}")
  @ApiOperation(value = "Delete descriptive metadata", notes = "Delete an existing descriptive metadata file", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = Void.class)})

  public Response deleteDescriptiveMetadataFromRepresentation(
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationId,
    @ApiParam(value = "The ID of the existing metadata file to delete", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    Browser.deleteRepresentationDescriptiveMetadataFile(user, representationId, metadataId);

    return Response.ok()
      .entity(new ApiResponseMessage(ApiResponseMessage.OK, "The descriptive metadata was successfully deleted"))
      .build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID + "}/preservation_metadata")
  @Produces({"application/json", "application/zip"})
  @ApiOperation(value = "Get representation preservation metadata", notes = "Get representation preservation metadata (JSON info, ZIP file conversion) for a given representation.\nOptional query params of **start** and **limit** defined the returned array.", response = PreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PreservationMetadata.class)})

  public Response retrievePreservationMetadataListFromRepresentation(
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationId,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = "json, xml, zip", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "Index of the agent element to return", defaultValue = "0") @QueryParam("startAgent") String startAgent,
    @ApiParam(value = "Maximum number of agents to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam("limitAgent") String limitAgent,
    @ApiParam(value = "Index of the first event to return", defaultValue = "0") @QueryParam("startEvent") String startEvent,
    @ApiParam(value = "Maximum number of events to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam("limitEvent") String limitEvent,
    @ApiParam(value = "Index of the first file to return", defaultValue = "0") @QueryParam("startFile") String startFile,
    @ApiParam(value = "Maximum number of files to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam("limitFile") String limitFile,
    @ApiParam(value = "The language for the HTML output", allowableValues = "pt_PT, en_US", defaultValue = RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @DefaultValue(RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @QueryParam(RodaConstants.API_QUERY_KEY_LANG) String language)
    throws RODAException {
    try {
      String mediaType = ApiUtils.getMediaType(acceptFormat, request);

      // get user
      RodaUser user = UserUtility.getApiUser(request);

      // delegate action to controller
      EntityResponse preservationMetadataList = Browser.retrieveAIPRepresentationPreservationMetadata(user,
        representationId, startAgent, limitAgent, startEvent, limitEvent, startFile, limitFile, acceptFormat, language);

      if (preservationMetadataList instanceof ObjectResponse) {
        ObjectResponse<PreservationMetadata> aip = (ObjectResponse<PreservationMetadata>) preservationMetadataList;
        return Response.ok(aip.getObject(), mediaType).build();
      } else {
        return ApiUtils.okResponse((StreamResponse) preservationMetadataList);
      }

    } catch (TransformerException e) {
      return ApiUtils.errorResponse(e);
    } catch (IOException e) {
      return ApiUtils.errorResponse(new TransformerException(e.getMessage()));
    }
  }

}
