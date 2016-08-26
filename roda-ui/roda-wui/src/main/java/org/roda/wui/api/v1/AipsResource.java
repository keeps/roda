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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.user.RodaSimpleUser;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.StreamResponse;

import io.swagger.annotations.*;

@Path(AipsResource.ENDPOINT)
@Api(value = AipsResource.SWAGGER_ENDPOINT)
public class AipsResource {
  public static final String ENDPOINT = "/v1/aips";
  public static final String SWAGGER_ENDPOINT = "v1 aips";

  @Context
  private HttpServletRequest request;

  @GET
  @ApiOperation(value = "List AIPs", notes = "Gets a list of archival information packages (AIPs).", response = AIP.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = AIP.class, responseContainer = "List")})

  public Response listAIPs(
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit)
    throws RODAException {
    // TODO
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @GET
  @Path("/{aip_id}")
  @Produces({"application/json", "application/zip"})
  @ApiOperation(value = "Get AIP", notes = "Get AIP information", response = AIP.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AIP.class),
    @ApiResponse(code = 404, message = "Not found", response = AIP.class)})

  public Response getAIP(
    @ApiParam(value = "The ID of the AIP to retrieve.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "Choose format in which to get the AIP", allowableValues = "json, zip", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    StreamResponse aipRepresentation = Browser.retrieveAIP(user, aipId, acceptFormat);

    return ApiUtils.okResponse(aipRepresentation);
  }

  @GET
  @Path("/{" + RodaConstants.AIP_ID + "}/{part}")
  // @Produces({"application/zip"})
  @ApiOperation(value = "Download part of the AIP")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 404, message = "Not found")})
  public Response getAIPPart(
    @ApiParam(value = "The ID of the AIP to retrieve.", required = true) @PathParam(RodaConstants.AIP_ID) String aipId,
    @ApiParam(value = "The ID of the AIP to retrieve.", required = true) @PathParam("part") String part)
    throws RODAException {
    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);

    // delegate action to controller
    StreamResponse aipRepresentation = Browser.retrieveAIPPart(user, aipId, part);

    return ApiUtils.okResponse(aipRepresentation);
  }

  @PUT
  @Path("/{aip_id}")
  @ApiOperation(value = "Update AIP", notes = "Update existing AIP", response = AIP.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AIP.class),
    @ApiResponse(code = 404, message = "Not found", response = AIP.class)})

  public Response updateAIP(
    @ApiParam(value = "The ID of the existing AIP to update", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The path to the directory in the shared file system where the AIP should be provided.", required = true) @FormParam("filepath") String filepath)
    throws RODAException {
    // TODO
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @POST
  @Path("/{aip_id}")
  @ApiOperation(value = "Create AIP", notes = "Create a new AIP", response = AIP.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AIP.class),
    @ApiResponse(code = 409, message = "Already exists", response = AIP.class)})

  public Response createAIP(
    @ApiParam(value = "The requested ID of the new AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The path to the directory in the shared file system where the AIP should be provided.", required = true) @FormParam("filepath") String filepath)
    throws RODAException {
    // TODO
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @DELETE
  @Path("/{aip_id}")
  @ApiOperation(value = "Delete AIP", notes = "Delete AIP", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = Void.class)})

  public Response deleteAIP(
    @ApiParam(value = "The ID of the AIP to delete.", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId)
    throws RODAException {
    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);

    // delegate action to controller

    SelectedItems<IndexedAIP> aips = new SelectedItemsList<>(Arrays.asList(aipId), IndexedAIP.class.getName());
    Browser.deleteAIP(user, aips);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Done!")).build();
  }

  @GET
  @Path("/{aip_id}/descriptive_metadata/")
  @ApiOperation(value = "List descriptive metadata", notes = "List descriptive metadata", response = DescriptiveMetadata.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "AIP not found", response = DescriptiveMetadata.class, responseContainer = "List")})

  public Response aipsAipIdDescriptiveMetadataGet(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = "json, bin") @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    StreamResponse aipRepresentation = Browser.listAIPDescriptiveMetadata(user, aipId, start, limit, acceptFormat);

    return ApiUtils.okResponse(aipRepresentation);
  }

  @GET
  @Path("/{aip_id}/descriptive_metadata/{metadata_id}")
  @Produces({"application/json", "application/xml", "text/html"})
  @ApiOperation(value = "Get descriptive metadata", notes = "Get descriptive metadata (JSON info, XML file or HTML conversion)", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class)})

  public Response aipsAipIdDescriptiveMetadataMetadataIdGet(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing metadata file to retrieve", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = "xml, html, json", defaultValue = "xml") @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "The language for the HTML output", allowableValues = "pt_PT, en_US", defaultValue = RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @DefaultValue(RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @QueryParam(RodaConstants.API_QUERY_KEY_LANG) String language)
    throws RODAException {
    try {
      // get user
      RodaSimpleUser user = UserUtility.getApiUser(request);
      // delegate action to controller
      StreamResponse aipDescriptiveMetadata = Browser.retrieveAIPDescriptiveMetadata(user, aipId, metadataId,
        acceptFormat, language);
      return ApiUtils.okResponse(aipDescriptiveMetadata);

    } catch (TransformerException e) {
      return ApiUtils.errorResponse(e);
    }
  }

  @PUT
  @Path("/{aip_id}/descriptive_metadata/{metadata_id}")
  @ApiOperation(value = "Update descriptive metadata", notes = "Upload a descriptive metadata file to update an existing one", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = DescriptiveMetadata.class)})

  public Response aipsAipIdDescriptiveMetadataMetadataIdPut(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing metadata file to update", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail,
    @ApiParam(value = "The type of the metadata file (e.g. eadc2014, dc)", required = true) @QueryParam("metadataType") String metadataType,
    @ApiParam(value = "The version of the metadata type used", required = false) @QueryParam("metadataVersion") String metadataVersion)
    throws RODAException {
    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    DescriptiveMetadata dm = Browser.putAIPDescriptiveMetadataFile(user, aipId, metadataId, metadataType,
      metadataVersion, inputStream, fileDetail);

    // FIXME give a better answer
    return Response.ok().entity(dm).build();
  }

  @POST
  @Path("/{aip_id}/descriptive_metadata/{metadata_id}")
  @ApiOperation(value = "Create descriptive metadata", notes = "Upload a new descriptive metadata file", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 409, message = "Already exists", response = DescriptiveMetadata.class)})

  public Response aipsAipIdDescriptiveMetadataMetadataIdPost(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The suggested ID metadata file to create", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId,
    @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail,
    @ApiParam(value = "The type of the metadata file (e.g. eadc2014, dc)", required = true) @QueryParam("metadataType") String metadataType,
    @ApiParam(value = "The version of the metadata type used", required = false) @QueryParam("metadataVersion") String metadataVersion)
    throws RODAException {
    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    DescriptiveMetadata dm = Browser.postAIPDescriptiveMetadataFile(user, aipId, metadataId, metadataType,
      metadataVersion, inputStream, fileDetail);

    // FIXME give a better answer
    return Response.ok().entity(dm).build();
  }

  @DELETE
  @Path("/{aip_id}/descriptive_metadata/{metadata_id}")
  @ApiOperation(value = "Delete descriptive metadata", notes = "Delete an existing descriptive metadata file", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = Void.class)})

  public Response aipsAipIdDescriptiveMetadataMetadataIdDelete(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing metadata file to delete", required = true) @PathParam(RodaConstants.API_PATH_PARAM_METADATA_ID) String metadataId)
    throws RODAException {
    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    Browser.deleteAIPDescriptiveMetadataFile(user, aipId, metadataId);

    // FIXME give a better answer
    return Response.ok()
      .entity(new ApiResponseMessage(ApiResponseMessage.OK, "The descriptive metadata was successfully deleted"))
      .build();
  }

  @GET
  @Path("/{aip_id}/preservation_metadata/")
  @Produces({"application/json", "application/zip", "text/html"})
  @ApiOperation(value = "Get preservation metadata", notes = "Get preservation metadata (JSON info, ZIP file or HTML conversion).\nOptional query params of **start** and **limit** defined the returned array.", response = PreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PreservationMetadata.class)})

  public Response aipsAipIdPreservationMetadataGet(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = "json, xml, html", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    StreamResponse aipPreservationMetadataList = Browser.listAIPPreservationMetadata(user, aipId, acceptFormat);

    return ApiUtils.okResponse(aipPreservationMetadataList);
  }

  @GET
  @Path("/{aip_id}/preservation_metadata/{representation_id}")
  @Produces({"application/json", "application/zip"})
  @ApiOperation(value = "Get representation preservation metadata", notes = "Get representation preservation metadata (JSON info, ZIP file conversion) for a given representation.\nOptional query params of **start** and **limit** defined the returned array.", response = PreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PreservationMetadata.class)})

  public Response aipsAipIdPreservationMetadataRepresentationIdGet(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = "json, xml, html", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "Index of the agent element to return", defaultValue = "0") @QueryParam("startAgent") String startAgent,
    @ApiParam(value = "Maximum number of agents to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam("limitAgent") String limitAgent,
    @ApiParam(value = "Index of the first event to return", defaultValue = "0") @QueryParam("startEvent") String startEvent,
    @ApiParam(value = "Maximum number of events to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam("limitEvent") String limitEvent,
    @ApiParam(value = "Index of the first file to return", defaultValue = "0") @QueryParam("startFile") String startFile,
    @ApiParam(value = "Maximum number of files to return", defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE) @QueryParam("limitFile") String limitFile,
    @ApiParam(value = "The language for the HTML output", allowableValues = "pt_PT, en_US", defaultValue = RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @DefaultValue(RodaConstants.API_QUERY_VALUE_LANG_DEFAULT) @QueryParam(RodaConstants.API_QUERY_KEY_LANG) String language)
    throws RODAException {
    try {
      // get user
      RodaSimpleUser user = UserUtility.getApiUser(request);
      // delegate action to controller
      StreamResponse aipRepresentationPreservationMetadata = Browser.retrieveAIPRepresentationPreservationMetadata(user,
        aipId, representationId, startAgent, limitAgent, startEvent, limitEvent, startFile, limitFile, acceptFormat,
        language);

      return ApiUtils.okResponse(aipRepresentationPreservationMetadata);

    } catch (TransformerException e) {
      return ApiUtils.errorResponse(e);
    }
  }

  @GET
  @Path("/{aip_id}/preservation_metadata/{representation_id}/{file_id}")
  @Produces({"application/xml"})
  @ApiOperation(value = "Get representation preservation metadata file", notes = "Get the preservation file (XML) for a given representation.", response = PreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PreservationMetadata.class)})

  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdGet(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The ID of the existing file", required = true) @PathParam(RodaConstants.API_PATH_PARAM_FILE_UUID) String fileId)
    throws RODAException {
    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    StreamResponse aipRepresentationPreservationMetadataFile = Browser
      .retrieveAIPRepresentationPreservationMetadataFile(user, aipId, representationId, fileId);

    return ApiUtils.okResponse(aipRepresentationPreservationMetadataFile);
  }

  @POST
  @Path("/{aip_id}/preservation_metadata/{representation_id}/{file_id}")
  @ApiOperation(value = "Create representation preservation file", notes = "Upload a preservation file to a representation (create)", response = PreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = DescriptiveMetadata.class)})

  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdPost(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The ID of the existing file", required = true) @PathParam(RodaConstants.API_PATH_PARAM_FILE_UUID) String fileId,
    @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail)
    throws RODAException {
    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);
    // delegate action to controller

    // TODO set this by params
    List<String> fileDirectoryPath = new ArrayList<>();

    Browser.postAIPRepresentationPreservationMetadataFile(user, aipId, representationId, fileDirectoryPath, fileId,
      inputStream, fileDetail);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @PUT
  @Path("/{aip_id}/preservation_metadata/{representation_id}/{file_id}")
  @ApiOperation(value = "Update representation preservation file", notes = "Upload a preservation file to a representation (update)", response = PreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = DescriptiveMetadata.class)})

  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdPut(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The ID of the existing file", required = true) @PathParam(RodaConstants.API_PATH_PARAM_FILE_UUID) String fileId,
    @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail)
    throws RODAException {
    // get user
    RodaSimpleUser user = UserUtility.getApiUser(request);
    // delegate action to controller
    // TODO set this by params
    List<String> fileDirectoryPath = new ArrayList<>();
    Browser.putAIPRepresentationPreservationMetadataFile(user, aipId, representationId, fileDirectoryPath, fileId,
      inputStream, fileDetail);

    // FIXME give a better answer
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @DELETE
  @Path("/{aip_id}/preservation_metadata/{representation_id}/{file_id}")
  @ApiOperation(value = "Delete representation preservation file", notes = "Delete a preservation file for a representation.", response = PreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PreservationMetadata.class)})
  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam(RodaConstants.API_PATH_PARAM_AIP_ID) String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID) String representationId,
    @ApiParam(value = "The ID of the existing file", required = true) @PathParam(RodaConstants.API_PATH_PARAM_FILE_UUID) String fileId,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = "json, xml, html", defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    // get user
    @SuppressWarnings("unused")
    RodaSimpleUser user = UserUtility.getApiUser(request);

    // TODO implement...
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
}
