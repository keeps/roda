package org.roda.wui.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.server.JSONP;
import org.roda.core.common.EntityResponse;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;
import org.roda.wui.api.v1.utils.ObjectResponse;
import org.roda.wui.client.services.TransferredResourceService;
import org.roda.wui.common.I18nUtility;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Path(TransferredResourceTest.ENDPOINT)
@Tag(name = TransferredResourceTest.SWAGGER_ENDPOINT)
public class TransferredResourceTest implements TransferredResourceService {
  public static final String ENDPOINT = "/v1/transfer/test";
  public static final String SWAGGER_ENDPOINT = "v1 transfer test";
  @Context
  private HttpServletRequest request;


  @Override
  public List<TransferredResource> listTransferredResources(String start, String limit, String acceptFormat, String jsonpCallbackName) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    boolean justActive = false;
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    //ajeitar controller
    IndexResult<TransferredResource> result = Browser.find(
      TransferredResource.class, Filter.ALL, Sorter.NONE,
      new Sublist(pagingParams.getFirst(), pagingParams.getSecond()), null, user, justActive, new ArrayList<>());
    return result.getResults();

  }

  @Override
  public TransferredResource getResource(String resourceId, String acceptFormat, String jsonpCallbackName) {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    try {
      // delegate action to controller
      EntityResponse response = Browser.retrieveTransferredResource(user, resourceId, acceptFormat);
      if (response instanceof ObjectResponse) {
        TransferredResource tr = (TransferredResource) ((ObjectResponse) response).getObject();
        return tr;
      } else {
        return null;
      }
    } catch (RODAException e) {
      throw new RuntimeException(e);
    }


  }
/*
  @Override
  public Boolean createResource(String parentUUID, String name, String localeString, InputStream inputStream, FormDataContentDisposition fileDetail, String commitString, String acceptFormat, String jsonpCallbackName) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

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

      return true;
    } catch (AlreadyExistsException e) {
      throw new RuntimeException(I18nUtility.getMessage("ui.upload.error.alreadyexists", e.getMessage(), localeString));
    }
  }

  @Override
  public Boolean updateTransferredResource(String relativePath, InputStream inputStream, FormDataContentDisposition fileDetail, String acceptFormat, String jsonpCallbackName) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    try {
      Browser.updateTransferredResource(user, Optional.of(relativePath), inputStream, fileDetail.getFileName(), false);
      return true;
    } catch (IOException e) {
      return false;
    }
  }
*/
  @Override
  public Boolean deleteResource(String path, String acceptFormat, String jsonpCallbackName) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    SelectedItemsList<org.roda.core.data.v2.ip.TransferredResource> selected = new SelectedItemsList<>(
      Collections.singletonList(path), org.roda.core.data.v2.ip.TransferredResource.class.getName());
    Browser.deleteTransferredResources(user, selected);
    return true;
  }

  @Override
  public Boolean deleteMultipleResources(List<String> paths, String acceptFormat, String jsonpCallbackName) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    SelectedItemsList<TransferredResource> selected = new SelectedItemsList<>(paths,
      org.roda.core.data.v2.ip.TransferredResource.class.getName());

    Browser.deleteTransferredResources(user, selected);
    return true;

  }

  @Override
  public TransferredResource reindexResources(String path, String acceptFormat, String jsonpCallbackName) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    TransferredResource resource = Browser.reindexTransferredResource(user, path);
    return resource;

  }
}
