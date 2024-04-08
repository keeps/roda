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
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.transform.TransformerException;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.JSONP;
import org.roda.core.data.v2.EntityResponse;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.DIPFiles;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;
import org.roda.wui.api.v1.utils.ObjectResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;

@Path(DIPFilesResource.ENDPOINT)
@Tag(name = DIPFilesResource.SWAGGER_ENDPOINT)
public class DIPFilesResource {
  public static final String ENDPOINT = "/dipfiles";
  public static final String SWAGGER_ENDPOINT = "v1 dipfiles";

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "List DIP files", description = "Gets a list of DIP files", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DIPFiles.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response list(
    @Parameter(description = "Index of the first element to return", schema = @Schema(defaultValue = "0")) @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @Parameter(description = "Maximum number of elements to return", schema = @Schema(defaultValue = RodaConstants.DEFAULT_PAGINATION_STRING_VALUE)) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @Parameter(description = "Choose format in which to get the DIP file", schema = @Schema(implementation = RodaConstants.ListMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    boolean justActive = false;
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    IndexResult<DIPFile> result = Browser.find(DIPFile.class, Filter.ALL, Sorter.NONE,
      new Sublist(pagingParams.getFirst(), pagingParams.getSecond()), null, user, justActive, new ArrayList<>());
    return Response.ok(ApiUtils.indexedResultToRODAObjectList(DIPFile.class, result), mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_DIP_FILE_UUID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_OCTET_STREAM,
    ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Get DIP file", description = "Get DIP file", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DIPFile.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response retrieve(
    @Parameter(description = "The UUID of the existing DIP file", required = true) @PathParam(RodaConstants.API_PATH_PARAM_DIP_FILE_UUID) String dipFileUUID,
    @Parameter(description = "Choose format in which to get the file", schema = @Schema(implementation = RodaConstants.GetFileMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName,
    @QueryParam(RodaConstants.API_QUERY_KEY_INLINE) boolean inline, @HeaderParam("Range") String range,
    @Context Request req) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse efile = Browser.retrieveDIPFile(user, dipFileUUID, acceptFormat);

    if (efile instanceof ObjectResponse) {
      ObjectResponse<DIPFile> file = (ObjectResponse<DIPFile>) efile;
      return Response.ok(file.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) efile, inline, range, req);
    }
  }

  @PUT
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Update DIP file", description = "Update existing DIP file", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DIPFile.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response update(
    @Parameter(description = "The UUID of the DIP file") @QueryParam(RodaConstants.API_PATH_PARAM_DIP_FILE_UUID) String fileUUID,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) FormDataContentDisposition fileDetail,
    @Parameter(description = "A new filename to the file") @QueryParam(RodaConstants.API_QUERY_KEY_FILENAME) String filename,
    @Parameter(description = "Choose format in which to get the DIP file", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);
    String name = filename == null ? fileDetail.getFileName() : filename;
    long size = fileDetail.getSize();

    // delegate action to controller
    try {
      DIPFile updatedFile = Browser.updateDIPFile(user, fileUUID, name, size == -1 ? 0 : size, inputStream);
      return Response.ok(updatedFile, mediaType).build();
    } catch (IOException e) {
      return ApiUtils.errorResponse(new TransformerException(e.getMessage()));
    }

  }

  @POST
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Create DIP file", description = "Create a new DIP file", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DIPFile.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response createDIPFile(
    @Parameter(description = "The ID of the DIP where to create the file") @QueryParam(RodaConstants.API_PATH_PARAM_DIP_ID) String dipId,
    @Parameter(description = "The UUID of the parent folder") @QueryParam(RodaConstants.API_PATH_PARAM_FILE_UUID) String fileUUID,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) FormDataContentDisposition fileDetail,
    @Parameter(description = "A new filename to the file") @QueryParam(RodaConstants.API_QUERY_KEY_FILENAME) String filename,
    @Parameter(description = "Choose format in which to get the file", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    try {
      DIPFile file;
      String name = filename == null ? fileDetail.getFileName() : filename;
      long size = fileDetail.getSize();

      if (fileUUID == null) {
        file = Browser.createDIPFile(user, dipId, new ArrayList<>(), name, size == -1 ? 0 : size, inputStream);
      } else {
        file = Browser.createDIPFileWithParentUUID(user, fileUUID, name, size == -1 ? 0 : size, inputStream);
      }

      return Response.ok(file, mediaType).build();
    } catch (IOException e) {
      return ApiUtils.errorResponse(new TransformerException(e.getMessage()));
    }
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_DIP_FILE_UUID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Delete DIP file", description = "Delete DIP file", responses = {
    @ApiResponse(responseCode = "204", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response delete(
    @Parameter(description = "The UUID of the existing DIP file", required = true) @PathParam(RodaConstants.API_PATH_PARAM_DIP_FILE_UUID) String fileUUID,
    @Parameter(description = "Reason to delete AIP", required = true) @QueryParam(RodaConstants.API_QUERY_PARAM_DETAILS) String details,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    SelectedItemsList<DIPFile> list = SelectedItemsList.create(DIPFile.class, Collections.singletonList(fileUUID));
    Browser.deleteDIPFile(user, list, details);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "DIP File deleted"), mediaType).build();
  }
}
