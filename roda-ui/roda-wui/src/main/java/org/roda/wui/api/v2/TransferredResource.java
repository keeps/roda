
package org.roda.wui.api.v2;

import com.google.gwt.core.client.GWT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.JSONP;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.client.services.TransferredResourceService;
import org.roda.wui.common.I18nUtility;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author António Lindo <alindo@keep.pt>
 */

@Path(TransferredResource.ENDPOINT)
@Tag(name = TransferredResource.SWAGGER_ENDPOINT)
public class TransferredResource implements TransferredResourceService {
  public static final String ENDPOINT = "/transfers";
  public static final String SWAGGER_ENDPOINT = "v2 transfers";
  @Context
  private HttpServletRequest request;

  @Override
  public List<org.roda.core.data.v2.ip.TransferredResource> listTransferredResources(String start, String limit) {
    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    boolean justActive = false;
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);

    try {
      IndexResult<org.roda.core.data.v2.ip.TransferredResource> result = Browser.find(
        org.roda.core.data.v2.ip.TransferredResource.class, Filter.ALL, Sorter.NONE,
        new Sublist(pagingParams.getFirst(), pagingParams.getSecond()), null, user, justActive, new ArrayList<>());
      return result.getResults();
    } catch (RODAException e) {
      throw new RESTException(e);
    }

  }

  @Override
  public List<org.roda.core.data.v2.ip.TransferredResource> getSelectedTransferredResources(
    SelectedItems<org.roda.core.data.v2.ip.TransferredResource> selected) {
    User user = UserUtility.getApiUser(request);

    try {
      return Browser.retrieveSelectedTransferredResource(user, selected);
    } catch (RODAException e) {
      throw new RESTException(e);
    }

  }

  @Override
  public Job moveTransferredResources(SelectedItems<org.roda.core.data.v2.ip.TransferredResource> items,
    String resourceId) {
    User user = UserUtility.getApiUser(request);

    try {
      if (resourceId != null) {
        return Browser.moveTransferredResource(user, items, getResource(resourceId));
      } else {
        return Browser.moveTransferredResource(user, items, null);
      }
    } catch (RODAException e) {
      throw new RESTException(e);
    }


  }

  @Override
  public List<org.roda.core.data.v2.ip.TransferredResource> getSelectedTransferredResources(SelectedItems<org.roda.core.data.v2.ip.TransferredResource> selected) {
    User user = UserUtility.getApiUser(request);

    try {
      return Browser.retrieveSelectedTransferredResource(user, selected);
    } catch (RODAException e) {
      throw new RESTException(e);
    }

  }

  @Override
  public Job moveTransferredResources(SelectedItems<org.roda.core.data.v2.ip.TransferredResource> items, String resourceId) {
    User user = UserUtility.getApiUser(request);

    try {
      if (resourceId != null) {
        return Browser.moveTransferredResource(user, items, getResource(resourceId));
      } else {
        return Browser.moveTransferredResource(user, items, null);
      }
    } catch (RODAException e) {
      throw new RESTException(e);
    }


  }

  @Override
  public org.roda.core.data.v2.ip.TransferredResource getResource(String resourceId) {
    // get user
    User user = UserUtility.getApiUser(request);

    try {
      // delegate action to controller
      return Browser.retrieveTransferredResource(user, resourceId);
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }

  @GET
  @Path("/binary/{" + RodaConstants.API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID + "}")
  @Produces({MediaType.APPLICATION_OCTET_STREAM})
  @Operation(summary = "Get transferred resource", description = "Gets a particular transferred resource", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = org.roda.wui.api.v1.TransferredResource.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response getResourceBinary(
    @Parameter(description = "The resource id", required = false) @PathParam(RodaConstants.API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID) String resourceId) {
    // get user
    User user = UserUtility.getApiUser(request);

    try {
      // delegate action to controller
      return ApiUtils.okResponse((StreamResponse) Browser.retrieveTransferredResourceBinary(user, resourceId));
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @Operation(summary = "Create transferred resource", description = "Creates a new transferred resource", responses = {
    @ApiResponse(responseCode = "201", description = "OK", content = @Content(schema = @Schema(implementation = org.roda.core.data.v2.ip.TransferredResource.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response createResource(
    @Parameter(description = "The id of the parent") @QueryParam(RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID) String parentUUID,
    @Parameter(description = "The name of the directory to create") @QueryParam(RodaConstants.TRANSFERRED_RESOURCE_DIRECTORY_NAME) String name,
    @Parameter(description = "Locale") @QueryParam(RodaConstants.LOCALE) String localeString,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) FormDataContentDisposition fileDetail,
    @Parameter(description = "Commit after creation", schema = @Schema(defaultValue = "false")) @QueryParam(RodaConstants.API_QUERY_PARAM_COMMIT) String commitString) {

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    try {
      org.roda.core.data.v2.ip.TransferredResource transferredResource;
      String fileName = fileDetail.getFileName();
      boolean forceCommit = false;
      if (StringUtils.isNotBlank(commitString)) {
        forceCommit = Boolean.parseBoolean(commitString);
      }

      if (name == null) {
        transferredResource = Browser.createTransferredResourceFile(user, parentUUID, fileName, inputStream,
          forceCommit);
      } else {
        transferredResource = Browser.createTransferredResourcesFolder(user, parentUUID, name, forceCommit);
      }

      return Response.ok(transferredResource).build();
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }

  @PUT
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Update transferred resource", description = "Updates an existing transferred resource", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = org.roda.wui.api.v1.TransferredResource.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response updateTransferredResource(
    @Parameter(description = "The relative path of the resource") @QueryParam(RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH) String relativePath,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) FormDataContentDisposition fileDetail) {

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    try {
      Browser.updateTransferredResource(user, Optional.of(relativePath), inputStream, fileDetail.getFileName(), false);
      return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Transferred resources updated")).build();
    } catch (RODAException | IOException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public void deleteResource(String path) {

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    SelectedItemsList<org.roda.core.data.v2.ip.TransferredResource> selected = new SelectedItemsList<>(
      Collections.singletonList(path), org.roda.core.data.v2.ip.TransferredResource.class.getName());
    try {
      Browser.deleteTransferredResources(user, selected);
    } catch (RODAException e) {
      throw new RESTException(e);
    }

  }

  @Override
  public Void deleteMultipleResources(List<String> paths) {

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    SelectedItemsList<org.roda.core.data.v2.ip.TransferredResource> selected = new SelectedItemsList<>(paths,
      org.roda.core.data.v2.ip.TransferredResource.class.getName());

    try {
      Browser.deleteTransferredResources(user, selected);
    } catch (RODAException e) {
      throw new RESTException(e);
    }

    return null;
  }

  @Override
  public org.roda.core.data.v2.ip.TransferredResource renameTransferredResource(String resourceId, String newName,
    Boolean replaceExisting) {
    User user = UserUtility.getApiUser(request);
    try {
      return getResource(Browser.renameTransferredResource(user, resourceId, newName, replaceExisting));
    } catch (RODAException e) {
      throw new RESTException(e);
    }

  }

  @Override
  public org.roda.core.data.v2.ip.TransferredResource createTransferredResourcesFolder(String parentUUID,
    String folderName, String commitString) {
    User user = UserUtility.getApiUser(request);
    try {
      return Browser.createTransferredResourcesFolder(user, parentUUID, folderName, Boolean.parseBoolean(commitString));
    } catch (RODAException e) {
      throw new RESTException(e);
    }

  }

  @Override
  public Void refreshTransferResource(String transferredResourceRelativePath) {
    User user = UserUtility.getApiUser(request);
    try {
      Browser.updateTransferredResources(user,
        transferredResourceRelativePath != null ? Optional.of(transferredResourceRelativePath) : Optional.empty(),
        true);
    } catch (RODAException e) {
      throw new RESTException(e);
    }
    return null;
  }

  @Override
  public org.roda.core.data.v2.ip.TransferredResource reindexResources(String path) {

    // get user
    User user = UserUtility.getApiUser(request);

    try {
      // delegate action to controller
      return Browser.reindexTransferredResource(user, path);
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public IndexResult<org.roda.core.data.v2.ip.TransferredResource> find(FindRequest findRequest, String localeString) {
    if (findRequest.filter == null || findRequest.filter.getParameters().isEmpty()) {
      return new IndexResult<>();
    }

    final User user = UserUtility.getApiUser(request);

    try {
      final IndexResult<org.roda.core.data.v2.ip.TransferredResource> result = Browser.find(
        org.roda.core.data.v2.ip.TransferredResource.class, findRequest.filter, findRequest.sorter, findRequest.sublist,
        findRequest.facets, user, findRequest.onlyActive, findRequest.fieldsToReturn);

      return I18nUtility.translate(result, org.roda.core.data.v2.ip.TransferredResource.class, localeString);
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public Long count(CountRequest countRequest) {
    final User user = UserUtility.getApiUser(request);

    try {
      return Browser.count(user, org.roda.core.data.v2.ip.TransferredResource.class, countRequest.filter,
        countRequest.onlyActive);
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }
}
