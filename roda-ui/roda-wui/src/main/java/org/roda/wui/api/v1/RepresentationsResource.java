/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
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
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.StreamResponse;

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
  @Path("/{" + RodaConstants.REPRESENTATION_UUID + "}")
  @Produces({"application/json", "application/zip"})
  @ApiOperation(value = "Get representation", notes = "Get representation", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 404, message = "Not found", response = Representation.class)})

  public Response getRepresentation(
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.REPRESENTATION_UUID) String representationUUID,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = "json, bin") @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    StreamResponse aipRepresentation = Browser.retrieveAIPRepresentation(user, representationUUID, acceptFormat);

    return ApiUtils.okResponse(aipRepresentation);
  }

  @GET
  @Path("/{" + RodaConstants.REPRESENTATION_UUID + "}/{part}")
  // @Produces({"application/json", MediaType.APPLICATION_OCTET_STREAM})
  @ApiOperation(value = "Download part of the representation")
  public Response getRepresentationPart(
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.REPRESENTATION_UUID) String representationUUID,
    @ApiParam(value = "The part of the representation to download", required = true, allowableValues = "data, metadata, documentation, schemas") @PathParam("part") String part)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    StreamResponse aipRepresentation = Browser.retrieveAIPRepresentationPart(user, representationUUID, part);

    return ApiUtils.okResponse(aipRepresentation);
  }

  @PUT
  @Path("{representation_uuid}")
  @ApiOperation(value = "Update representation", notes = "Update existing representation", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 404, message = "Not found", response = Representation.class)})

  public Response updateRepresentation(
    @ApiParam(value = "The ID of the AIP where to update the representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation to update", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The path to the directory in the shared file system where the representation should be provided.", required = true) @FormParam("filepath") String filepath)
    throws RODAException {
    // TODO
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @POST
  @Path("/{aip_id}/data/{representation_id}")
  @ApiOperation(value = "Create representation", notes = "Create a new representation on the AIP", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 409, message = "Already exists", response = Representation.class)})

  public Response aipsAipIdDataRepresentationIdPost(
    @ApiParam(value = "The ID of the AIP where to create the representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The requested ID for the new representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The path to the directory in the shared file system where the representation should be provided.", required = true) @FormParam("filepath") String filepath)
    throws RODAException {
    // TODO
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @DELETE
  @Path("/{aip_id}/data/{representation_id}")
  @ApiOperation(value = "Delete representation", notes = "Delete representation", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = Void.class)})

  public Response aipsAipIdDataRepresentationIdDelete(
    @ApiParam(value = "The ID of the AIP where the representation is.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation to delete", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Browser.deleteRepresentation(user, aipId, representationId);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @GET
  @Path("/{representation_uuid}/descriptive_metadata/")
  @ApiOperation(value = "List descriptive metadata", notes = "List descriptive metadata", response = DescriptiveMetadata.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Representation not found", response = DescriptiveMetadata.class, responseContainer = "List")})

  public Response aipsRepresentationIdDescriptiveMetadataGet(
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationId,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = "json, bin") @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    StreamResponse descriptiveMetadata = Browser.listRepresentationDescriptiveMetadata(user, representationId, start,
      limit, acceptFormat);

    return ApiUtils.okResponse(descriptiveMetadata);
  }

  @GET
  @Path("/{representation_uuid}/descriptive_metadata/{metadata_id}")
  @Produces({"application/json", "application/xml", "text/html"})
  @ApiOperation(value = "Get descriptive metadata", notes = "Get descriptive metadata (JSON info, XML file or HTML conversion)", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class)})

  public Response aipsAipIdDescriptiveMetadataMetadataIdGet(
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationId,
    @ApiParam(value = "The ID of the existing metadata file to retrieve", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = "xml, html, json", defaultValue = "xml") @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "The language for the HTML output", allowableValues = "pt_PT, en_US", defaultValue = RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @DefaultValue(RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @QueryParam(RodaConstants.API_QUERY_KEY_LANG) String language)
    throws RODAException {
    try {
      // get user
      RodaUser user = UserUtility.getApiUser(request);
      // delegate action to controller
      StreamResponse aipDescriptiveMetadata = Browser.retrieveRepresentationDescriptiveMetadata(user, representationId,
        metadataId, acceptFormat, language);
      return ApiUtils.okResponse(aipDescriptiveMetadata);

    } catch (TransformerException e) {
      return ApiUtils.errorResponse(e);
    }
  }

  @PUT
  @Path("/{representation_uuid}/descriptive_metadata/{metadata_id}")
  @ApiOperation(value = "Update descriptive metadata", notes = "Upload a descriptive metadata file to update an existing one", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = DescriptiveMetadata.class)})

  public Response aipsAipIdDescriptiveMetadataMetadataIdPut(
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationId,
    @ApiParam(value = "The ID of the existing metadata file to update", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail,
    @ApiParam(value = "The type of the metadata file (e.g. eadc2014, dc)", required = true) @FormParam("metadataType") String metadataType,
    @ApiParam(value = "The version of the metadata type used", required = false) @FormParam("metadataVersion") String metadataVersion)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Browser.putRepresentationDescriptiveMetadataFile(user, representationId, metadataId, metadataType, metadataVersion,
      inputStream, fileDetail);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @POST
  @Path("/{representation_uuid}/descriptive_metadata/{metadata_id}")
  @ApiOperation(value = "Create descriptive metadata", notes = "Upload a new descriptive metadata file", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 409, message = "Already exists", response = DescriptiveMetadata.class)})

  public Response aipsAipIdDescriptiveMetadataMetadataIdPost(
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationId,
    @ApiParam(value = "The suggested ID metadata file to create", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail,
    @ApiParam(value = "The type of the metadata file (e.g. eadc2014, dc)", required = true) @FormParam("metadataType") String metadataType,
    @ApiParam(value = "The version of the metadata type used", required = false) @FormParam("metadataVersion") String metadataVersion)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Browser.postRepresentationDescriptiveMetadataFile(user, representationId, metadataId, metadataType, metadataVersion,
      inputStream, fileDetail);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @DELETE
  @Path("/{representation_uuid}/descriptive_metadata/{metadata_id}")
  @ApiOperation(value = "Delete descriptive metadata", notes = "Delete an existing descriptive metadata file", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = Void.class)})

  public Response aipsAipIdDescriptiveMetadataMetadataIdDelete(
    @ApiParam(value = "The ID of the existing Representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID) String representationId,
    @ApiParam(value = "The ID of the existing metadata file to delete", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId)
    throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Browser.deleteRepresentationDescriptiveMetadataFile(user, representationId, metadataId);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

}
