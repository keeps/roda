package org.roda.wui.client.services;

import com.google.gwt.core.client.GWT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.xpath.operations.Bool;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.JSONP;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.TransferredResources;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;
import org.roda.wui.client.common.DefaultMethodCallback;
import org.roda.wui.common.I18nUtility;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Path("../api/v1/transfer/test")
@Tag(name = "v1-test")
public interface TransferredResourceService extends DirectRestService {
  class Util {
    /**
     * @return the singleton instance
     */
    public static TransferredResourceService get() {
      return GWT.create(TransferredResourceService.class);
    }

    public static <T> TransferredResourceService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> TransferredResourceService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> TransferredResourceService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  @GET
  @Path("/")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "List transferred resources")
  List<org.roda.core.data.v2.ip.TransferredResource> listTransferredResources(
    @Parameter(description = "Index of the first element to return", schema = @Schema(defaultValue = "0")) @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @Parameter(description = "Maximum number of elements to return", schema = @Schema(defaultValue = "100")) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit,
    @Parameter(description = "Choose format in which to get the resources", schema = @Schema(implementation = RodaConstants.ListMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException;

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Get transferred resource")
  org.roda.core.data.v2.ip.TransferredResource getResource(
    @Parameter(description = "The resource id", required = false) @PathParam(RodaConstants.API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID) String resourceId,
    @Parameter(description = "Choose format in which to get the resource", schema = @Schema(implementation = RodaConstants.GetFileMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName);
/*
  @POST
  @Path("/")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Create transferred resource")
  Boolean createResource(
    @Parameter(description = "The id of the parent") @QueryParam(RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID) String parentUUID,
    @Parameter(description = "The name of the directory to create") @QueryParam(RodaConstants.TRANSFERRED_RESOURCE_DIRECTORY_NAME) String name,
    @Parameter(description = "Locale") @QueryParam(RodaConstants.LOCALE) String localeString,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) FormDataContentDisposition fileDetail,
    @Parameter(description = "Commit after creation", schema = @Schema(defaultValue = "false")) @QueryParam(RodaConstants.API_QUERY_PARAM_COMMIT) String commitString,
    @Parameter(description = "Choose format in which to get the resource", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException;

  @PUT
  @Path("/")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Update transferred resource")
  Boolean updateTransferredResource(
    @Parameter(description = "The relative path of the resource") @QueryParam(RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH) String relativePath,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) InputStream inputStream,
    @FormDataParam(RodaConstants.API_PARAM_UPLOAD) FormDataContentDisposition fileDetail,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException;
*/
  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Delete transferred resource")
  Boolean deleteResource(
    @Parameter(description = "The id of the resource", required = true) @PathParam(RodaConstants.API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID) String path,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException;

  @DELETE
  @Path("/")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Delete multiple transferred resource")
  Boolean deleteMultipleResources(
    @Parameter(description = "The id of the resources", array = @ArraySchema()) @QueryParam("transferred_resource_ids") List<String> paths,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException;

  @GET
  @Path("/reindex")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Reindex transferred resource")
  org.roda.core.data.v2.ip.TransferredResource reindexResources(
    @Parameter(description = "The path of the resource") @QueryParam("transferred_resource_path") String path,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.APIMediaTypes.class)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @Parameter(description = "JSONP callback name", required = false, schema = @Schema(defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK)) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException;
}




