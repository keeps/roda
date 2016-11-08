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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
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
import javax.ws.rs.core.Response.Status;
import javax.xml.transform.TransformerException;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.roda.core.common.EntityResponse;
import org.roda.core.common.StreamResponse;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.TransferredResources;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ObjectResponse;
import org.roda.wui.common.I18nUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path(TransferredResource.ENDPOINT)
@Api(value = TransferredResource.SWAGGER_ENDPOINT)
public class TransferredResource {
  public static final String ENDPOINT = "/v1/transfers";
  public static final String SWAGGER_ENDPOINT = "v1 transfers";

  @Context
  private HttpServletRequest request;

  @GET
  @ApiOperation(value = "List resources", notes = "Get a list of resources.", response = TransferredResources.class, responseContainer = "List")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successful response", response = TransferredResources.class, responseContainer = "List"),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response listTransferredResources(
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @ApiParam(value = "Choose format in which to get the resources", allowableValues = RodaConstants.API_LIST_MEDIA_TYPES, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    boolean justActive = false;
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    IndexResult<org.roda.core.data.v2.ip.TransferredResource> result = Browser.find(
      org.roda.core.data.v2.ip.TransferredResource.class, Filter.NULL, Sorter.NONE,
      new Sublist(pagingParams.getFirst(), pagingParams.getSecond()), null, user, justActive);
    return Response
      .ok(ApiUtils.indexedResultToRODAObjectList(org.roda.core.data.v2.ip.TransferredResource.class, result), mediaType)
      .build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID + "}")
  @ApiOperation(value = "Get resource", notes = "Get a resource.", response = org.roda.core.data.v2.ip.TransferredResource.class)
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = org.roda.core.data.v2.ip.TransferredResource.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response getResource(
    @ApiParam(value = "The resource id", required = false) @PathParam(RodaConstants.API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID) String resourceId,
    @ApiParam(value = "Choose format in which to get the resource", allowableValues = RodaConstants.API_GET_FILE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws AuthorizationDeniedException, NotFoundException, RequestNotValidException, GenericException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    EntityResponse response = Browser.retrieveTransferredResource(user, resourceId, acceptFormat);

    if (response instanceof ObjectResponse) {
      ObjectResponse<org.roda.core.data.v2.ip.TransferredResource> tr = (ObjectResponse<org.roda.core.data.v2.ip.TransferredResource>) response;
      return Response.ok(tr.getObject(), mediaType).build();
    } else {
      return ApiUtils.okResponse((StreamResponse) response);
    }
  }

  /*
   * 20160909 hsilva: do not change form param strings ("upl")
   */
  @POST
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Create resource", notes = "Create a resource.", response = org.roda.core.data.v2.ip.TransferredResource.class)
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = org.roda.core.data.v2.ip.TransferredResource.class),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})
  public Response createResource(
    @ApiParam(value = "The id of the parent") @QueryParam(RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID) String parentUUID,
    @ApiParam(value = "The name of the directory to create", required = false) @QueryParam(RodaConstants.TRANSFERRED_RESOURCE_DIRECTORY_NAME) String name,
    @ApiParam(value = "Locale", required = false) @QueryParam(RodaConstants.LOCALE) String localeString,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) FormDataContentDisposition fileDetail,
    @ApiParam(value = "Choose format in which to get the resource", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    try {
      org.roda.core.data.v2.ip.TransferredResource transferredResource;
      String fileName = fileDetail.getFileName();
      boolean forceCommit = false;
      if (name == null) {
        transferredResource = Browser.createTransferredResourceFile(user, parentUUID, fileName, inputStream,
          forceCommit);
      } else {
        transferredResource = Browser.createTransferredResourcesFolder(user, parentUUID, name, forceCommit);
      }

      return Response.ok(transferredResource, mediaType).build();
    } catch (AlreadyExistsException e) {
      return Response.status(Status.CONFLICT).entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
        I18nUtility.getMessage("ui.upload.error.alreadyexists", e.getMessage(), localeString))).build();
    }
  }

  @PUT
  @ApiOperation(value = "Update resource", notes = "Update existing resources", response = org.roda.core.data.v2.ip.TransferredResource.class)
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = org.roda.core.data.v2.ip.TransferredResource.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response updateTransferredResource(
    @ApiParam(value = "The relative path of the resource") @QueryParam(RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH) String relativePath,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) FormDataContentDisposition fileDetail,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    try {
      Browser.updateTransferredResource(user, Optional.of(relativePath), inputStream, fileDetail.getFileName(), false);
      return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Transferred resources updated"), mediaType)
        .build();
    } catch (IOException e) {
      return ApiUtils.errorResponse(new TransformerException(e.getMessage()));
    }
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID + "}")
  @ApiOperation(value = "Delete resource", notes = "Delete a resource.", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deleteResource(
    @ApiParam(value = "The id of the resource", required = true) @PathParam(RodaConstants.API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID) String path,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    SelectedItemsList<org.roda.core.data.v2.ip.TransferredResource> selected = new SelectedItemsList<org.roda.core.data.v2.ip.TransferredResource>(
      Arrays.asList(path), org.roda.core.data.v2.ip.TransferredResource.class.getName());
    Browser.deleteTransferredResources(user, selected);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Transferred resource deleted"), mediaType)
      .build();
  }

  @DELETE
  @ApiOperation(value = "Delete resources", notes = "Delete multiple resources.", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response deleteMultipleResources(
    @ApiParam(value = "The id of the resources", allowMultiple = true) @QueryParam("transferred_resource_ids") List<String> paths,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    SelectedItemsList<org.roda.core.data.v2.ip.TransferredResource> selected = new SelectedItemsList<org.roda.core.data.v2.ip.TransferredResource>(
      paths, org.roda.core.data.v2.ip.TransferredResource.class.getName());
    Browser.deleteTransferredResources(user, selected);

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Transferred resources deleted"), mediaType)
      .build();
  }

  @GET
  @Path("/reindex")
  @ApiOperation(value = "Reindex resource", notes = "Reindex resource.", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 404, message = "Not found", response = ApiResponseMessage.class)})

  public Response reindexResources(
    @ApiParam(value = "The path of the resource") @QueryParam("transferred_resource_path") String path,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_DELETE_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    org.roda.core.data.v2.ip.TransferredResource resource = Browser.reindexTransferredResource(user, path);
    return Response.ok(resource, mediaType).build();
  }

}
