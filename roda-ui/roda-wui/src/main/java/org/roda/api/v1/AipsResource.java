package org.roda.api.v1;

import java.io.File;
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

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.roda.api.v1.impl.AipsResourceImpl;
import org.roda.api.v1.utils.NotFoundException;
import org.roda.model.AIP;
import org.roda.model.DescriptiveMetadata;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.SimpleEventPreservationMetadata;

@Path(AipsResource.ENDPOINT)
@Api(value = AipsResource.SWAGGER_ENDPOINT)
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2015-09-03T11:38:49.275+01:00")
public class AipsResource {
  public static final String ENDPOINT = "/v1/aips";
  public static final String SWAGGER_ENDPOINT = "v1 aips";

  private static final AipsResourceImpl DELEGATE = new AipsResourceImpl();

  @Context
  private HttpServletRequest request;

  @GET
  @ApiOperation(value = "List AIPs", notes = "Gets a list of archival information packages (AIPs).", response = AIP.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = AIP.class, responseContainer = "List")})

  public Response aipsGet(
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam("start") String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam("limit") String limit)
      throws NotFoundException {
    return DELEGATE.aipsGet(request, start, limit);
  }

  @GET
  @Path("/{aip_id}")
  @Produces({"application/json", "application/zip"})
  @ApiOperation(value = "Get AIP", notes = "Get AIP information", response = AIP.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AIP.class),
    @ApiResponse(code = 404, message = "Not found", response = AIP.class)})

  public Response aipsAipIdGet(
    @ApiParam(value = "The ID of the AIP to retrieve.", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "Choose format in which to get the AIP", allowableValues = "json, zip", defaultValue = "json") @QueryParam("acceptFormat") String acceptFormat)
      throws NotFoundException {
    return DELEGATE.aipsAipIdGet(request, aipId, acceptFormat);
  }

  @PUT
  @Path("/{aip_id}")
  @ApiOperation(value = "Update AIP", notes = "Update existing AIP", response = AIP.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AIP.class),
    @ApiResponse(code = 404, message = "Not found", response = AIP.class)})

  public Response aipsAipIdPut(
    @ApiParam(value = "The ID of the existing AIP to update", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The path to the directory in the shared file system where the AIP should be provided.", required = true) @FormParam("filepath") String filepath)
      throws NotFoundException {
    return DELEGATE.aipsAipIdPut(request, aipId, filepath);
  }

  @POST
  @Path("/{aip_id}")
  @ApiOperation(value = "Create AIP", notes = "Create a new AIP", response = AIP.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AIP.class),
    @ApiResponse(code = 409, message = "Already exists", response = AIP.class)})

  public Response aipsAipIdPost(
    @ApiParam(value = "The requested ID of the new AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The path to the directory in the shared file system where the AIP should be provided.", required = true) @FormParam("filepath") String filepath)
      throws NotFoundException {
    return DELEGATE.aipsAipIdPost(request, aipId, filepath);
  }

  @DELETE
  @Path("/{aip_id}")
  @ApiOperation(value = "Delete AIP", notes = "Delete AIP", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = Void.class)})

  public Response aipsAipIdDelete(
    @ApiParam(value = "The ID of the AIP to delete.", required = true) @PathParam("aip_id") String aipId)
      throws NotFoundException {
    return DELEGATE.aipsAipIdDelete(request, aipId);
  }

  @GET
  @Path("/{aip_id}/data/")
  @ApiOperation(value = "List representations", notes = "List AIP representations", response = Representation.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = Representation.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "AIP not found", response = Representation.class, responseContainer = "List")})

  public Response aipsAipIdDataGet(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam("start") String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam("limit") String limit)
      throws NotFoundException {
    return DELEGATE.aipsAipIdDataGet(request, aipId, start, limit);
  }

  @GET
  @Path("/{aip_id}/data/{representation_id}")
  @Produces({"application/json", "application/zip"})
  @ApiOperation(value = "Get representation", notes = "Get representation", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 404, message = "Not found", response = Representation.class)})

  public Response getAipRepresentation(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam("representation_id") String representationId,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = "json, bin") @QueryParam("acceptFormat") String acceptFormat)
      throws NotFoundException {
    return DELEGATE.getAipRepresentation(request, aipId, representationId, acceptFormat);
  }

  @PUT
  @Path("/{aip_id}/data/{representation_id}")
  @ApiOperation(value = "Update representation", notes = "Update existing representation", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 404, message = "Not found", response = Representation.class)})

  public Response aipsAipIdDataRepresentationIdPut(
    @ApiParam(value = "The ID of the AIP where to update the representation", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The ID of the existing representation to update", required = true) @PathParam("representation_id") String representationId,
    @ApiParam(value = "The path to the directory in the shared file system where the representation should be provided.", required = true) @FormParam("filepath") String filepath)
      throws NotFoundException {
    return DELEGATE.aipsAipIdDataRepresentationIdPut(request, aipId, representationId, filepath);
  }

  @POST
  @Path("/{aip_id}/data/{representation_id}")
  @ApiOperation(value = "Create representation", notes = "Create a new representation on the AIP", response = Representation.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Representation.class),
    @ApiResponse(code = 409, message = "Already exists", response = Representation.class)})

  public Response aipsAipIdDataRepresentationIdPost(
    @ApiParam(value = "The ID of the AIP where to create the representation", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The requested ID for the new representation", required = true) @PathParam("representation_id") String representationId,
    @ApiParam(value = "The path to the directory in the shared file system where the representation should be provided.", required = true) @FormParam("filepath") String filepath)
      throws NotFoundException {
    return DELEGATE.aipsAipIdDataRepresentationIdPost(request, aipId, representationId, filepath);
  }

  @DELETE
  @Path("/{aip_id}/data/{representation_id}")
  @ApiOperation(value = "Delete representation", notes = "Delete representation", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = Void.class)})

  public Response aipsAipIdDataRepresentationIdDelete(
    @ApiParam(value = "The ID of the AIP where the representation is.", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The ID of the existing representation to delete", required = true) @PathParam("representation_id") String representationId)
      throws NotFoundException {
    return DELEGATE.aipsAipIdDataRepresentationIdDelete(request, aipId, representationId);
  }

  @GET
  @Path("/{aip_id}/data/{representation_id}/{file_id}")
  @Produces({"application/json", "application/octetstream"})
  @ApiOperation(value = "Get representation file", notes = "Get representation file", response = File.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = File.class),
    @ApiResponse(code = 404, message = "Not found", response = File.class)})

  public Response aipsAipIdDataRepresentationIdFileIdGet(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam("representation_id") String representationId,
    @ApiParam(value = "The ID of the existing file", required = true) @PathParam("file_id") String fileId,
    @ApiParam(value = "Choose format in which to get the file", allowableValues = "json, bin") @QueryParam("acceptFormat") String acceptFormat)
      throws NotFoundException {
    return DELEGATE.aipsAipIdDataRepresentationIdFileIdGet(request, aipId, representationId, fileId, acceptFormat);
  }

  @PUT
  @Path("/{aip_id}/data/{representation_id}/{file_id}")
  @ApiOperation(value = "Update representation file", notes = "Update existing file", response = File.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = File.class),
    @ApiResponse(code = 404, message = "Not found", response = File.class)})

  public Response aipsAipIdDataRepresentationIdFileIdPut(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam("representation_id") String representationId,
    @ApiParam(value = "The ID of the existing file", required = true) @PathParam("file_id") String fileId,
    @ApiParam(value = "The path to the file in the shared file system where the file should be provided.", required = true) @FormParam("filepath") String filepath)
      throws NotFoundException {
    return DELEGATE.aipsAipIdDataRepresentationIdFileIdPut(request, aipId, representationId, fileId, filepath);
  }

  @POST
  @Path("/{aip_id}/data/{representation_id}/{file_id}")
  @ApiOperation(value = "Create representation with one file", notes = "Create a new representation on the AIP", response = File.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = File.class),
    @ApiResponse(code = 409, message = "Already exists", response = File.class)})

  public Response aipsAipIdDataRepresentationIdFileIdPost(
    @ApiParam(value = "The ID of the AIP where to create the representation", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The requested ID for the new representation", required = true) @PathParam("representation_id") String representationId,
    @ApiParam(value = "The requested ID of the new file", required = true) @PathParam("file_id") String fileId,
    @ApiParam(value = "The path to the directory in the shared file system where the representation should be provided.", required = true) @FormParam("filepath") String filepath)
      throws NotFoundException {
    return DELEGATE.aipsAipIdDataRepresentationIdFileIdPost(request, aipId, representationId, fileId, filepath);
  }

  @DELETE
  @Path("/{aip_id}/data/{representation_id}/{file_id}")
  @ApiOperation(value = "Delete representation file", notes = "Delete representation file", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = Void.class)})

  public Response aipsAipIdDataRepresentationIdFileIdDelete(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam("representation_id") String representationId,
    @ApiParam(value = "The ID of the existing file", required = true) @PathParam("file_id") String fileId)
      throws NotFoundException {
    return DELEGATE.aipsAipIdDataRepresentationIdFileIdDelete(request, aipId, representationId, fileId);
  }

  @GET
  @Path("/{aip_id}/descriptive_metadata/")
  @ApiOperation(value = "List descriptive metadata", notes = "List descriptive metadata", response = DescriptiveMetadata.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "AIP not found", response = DescriptiveMetadata.class, responseContainer = "List")})

  public Response aipsAipIdDescriptiveMetadataGet(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam("start") String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam("limit") String limit,
    @ApiParam(value = "Choose format in which to get the representation", allowableValues = "json, bin") @QueryParam("acceptFormat") String acceptFormat)
      throws NotFoundException {
    return DELEGATE.listAipDescriptiveMetadata(request, aipId, start, limit, acceptFormat);
  }

  @GET
  @Path("/{aip_id}/descriptive_metadata/{metadata_id}")
  @Produces({"application/json", "application/xml", "text/html"})
  @ApiOperation(value = "Get descriptive metadata", notes = "Get descriptive metadata (JSON info, XML file or HTML conversion)", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class)})

  public Response aipsAipIdDescriptiveMetadataMetadataIdGet(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The ID of the existing metadata file to retrieve", required = true) @PathParam("metadata_id") String metadataId,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = "json, xml, html", defaultValue = "json") @QueryParam("acceptFormat") String acceptFormat,
    @ApiParam(value = "The language for the HTML output", allowableValues = "pt_PT, en_US", defaultValue = "pt_PT") @DefaultValue("pt_PT") @QueryParam("lang") String language)
      throws NotFoundException {
    return DELEGATE.getAipDescriptiveMetadata(request, aipId, metadataId, acceptFormat, language);
  }

  @PUT
  @Path("/{aip_id}/descriptive_metadata/{metadata_id}")
  @ApiOperation(value = "Update descriptive metadata", notes = "Upload a descriptive metadata file to update an existing one", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = DescriptiveMetadata.class)})

  public Response aipsAipIdDescriptiveMetadataMetadataIdPut(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The ID of the existing metadata file to update", required = true) @PathParam("metadata_id") String metadataId,
    @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail,
    @ApiParam(value = "The type of the metadata file (e.g. eadc2014, dc)", required = true) @FormParam("metadataType") String metadataType)
      throws NotFoundException {
    return DELEGATE.aipsAipIdDescriptiveMetadataMetadataIdPut(request, aipId, metadataId, fileDetail, metadataType);
  }

  @POST
  @Path("/{aip_id}/descriptive_metadata/{metadata_id}")
  @ApiOperation(value = "Create descriptive metadata", notes = "Upload a new descriptive metadata file", response = DescriptiveMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 409, message = "Already exists", response = DescriptiveMetadata.class)})

  public Response aipsAipIdDescriptiveMetadataMetadataIdPost(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The suggested ID metadata file to create", required = true) @PathParam("metadata_id") String metadataId,
    @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail,
    @ApiParam(value = "The type of the metadata file (e.g. eadc2014, dc)", required = true) @FormParam("metadataType") String metadataType)
      throws NotFoundException {
    return DELEGATE.aipsAipIdDescriptiveMetadataMetadataIdPost(request, aipId, metadataId, fileDetail, metadataType);
  }

  @DELETE
  @Path("/{aip_id}/descriptive_metadata/{metadata_id}")
  @ApiOperation(value = "Delete descriptive metadata", notes = "Delete an existing descriptive metadata file", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = Void.class)})

  public Response aipsAipIdDescriptiveMetadataMetadataIdDelete(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The ID of the existing metadata file to delete", required = true) @PathParam("metadata_id") String metadataId)
      throws NotFoundException {
    return DELEGATE.aipsAipIdDescriptiveMetadataMetadataIdDelete(request, aipId, metadataId);
  }

  @GET
  @Path("/{aip_id}/preservation_metadata/")
  @Produces({"application/json", "application/zip", "text/html"})
  @ApiOperation(value = "Get preservation metadata", notes = "Get preservation metadata (JSON info, ZIP file or HTML conversion).\nOptional query params of **start** and **limit** defined the returned array.", response = SimpleEventPreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = SimpleEventPreservationMetadata.class)})

  public Response aipsAipIdPreservationMetadataGet(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = "json, xml, html", defaultValue = "json") @QueryParam("acceptFormat") String acceptFormat,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam("start") String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam("limit") String limit)
      throws NotFoundException {
    return DELEGATE.listAipPreservationMetadata(request, aipId, start, limit, acceptFormat);
  }

  @GET
  @Path("/{aip_id}/preservation_metadata/{representation_id}")
  @Produces({"application/json", "application/zip", "text/html"})
  @ApiOperation(value = "Get representation preservation metadata", notes = "Get representation preservation metadata (JSON info, ZIP file or HTML conversion) for a given representation.\nOptional query params of **start** and **limit** defined the returned array.", response = SimpleEventPreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = SimpleEventPreservationMetadata.class)})

  public Response aipsAipIdPreservationMetadataRepresentationIdGet(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam("representation_id") String representationId,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = "json, xml, html", defaultValue = "json") @QueryParam("acceptFormat") String acceptFormat,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam("start") String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam("limit") String limit)
      throws NotFoundException {
    return DELEGATE.getAipRepresentationPreservationMetadata(request, aipId, representationId, start, limit,
      acceptFormat);
  }

  @GET
  @Path("/{aip_id}/preservation_metadata/{representation_id}/{file_id}")
  @Produces({"application/xml"})
  @ApiOperation(value = "Get representation preservation metadata file", notes = "Get the preservation file (XML) for a given representation.", response = SimpleEventPreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = SimpleEventPreservationMetadata.class)})

  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdGet(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam("representation_id") String representationId,
    @ApiParam(value = "The ID of the existing file", required = true) @PathParam("file_id") String fileId)
      throws NotFoundException {
    return DELEGATE.getAipRepresentationPreservationMetadataFile(request, aipId, representationId, fileId);
  }

  @POST
  @Path("/{aip_id}/preservation_metadata/{representation_id}/{file_id}")
  @ApiOperation(value = "Create representation preservation file", notes = "Upload a preservation file to a representation (create)", response = SimpleEventPreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = DescriptiveMetadata.class)})

  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdPost(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam("representation_id") String representationId,
    @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail)
      throws NotFoundException {
    return DELEGATE.postAipRepresentationPreservationMetadataFile(request, aipId, representationId,
      inputStream, fileDetail);
  }

  @PUT
  @Path("/{aip_id}/preservation_metadata/{representation_id}/{file_id}")
  @ApiOperation(value = "Update representation preservation file", notes = "Upload a preservation file to a representation (update)", response = SimpleEventPreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = DescriptiveMetadata.class),
    @ApiResponse(code = 404, message = "Not found", response = DescriptiveMetadata.class)})

  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdPut(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam("representation_id") String representationId,
    @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail)
      throws NotFoundException {
    return DELEGATE.putAipRepresentationPreservationMetadataFile(request, aipId, representationId,
      inputStream, fileDetail);
  }

  @DELETE
  @Path("/{aip_id}/preservation_metadata/{representation_id}/{file_id}")
  @ApiOperation(value = "Delete representation preservation file", notes = "Delete a preservation file for a representation.", response = SimpleEventPreservationMetadata.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = SimpleEventPreservationMetadata.class)})

  public Response aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(
    @ApiParam(value = "The ID of the existing AIP", required = true) @PathParam("aip_id") String aipId,
    @ApiParam(value = "The ID of the existing representation", required = true) @PathParam("representation_id") String representationId,
    @ApiParam(value = "The ID of the existing file", required = true) @PathParam("file_id") String fileId,
    @ApiParam(value = "Choose format in which to get the metadata", allowableValues = "json, xml, html", defaultValue = "json") @QueryParam("acceptFormat") String acceptFormat)
      throws NotFoundException {
    return DELEGATE.aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(request, aipId, representationId, fileId);
  }
}
