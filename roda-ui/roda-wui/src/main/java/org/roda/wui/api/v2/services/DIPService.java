package org.roda.wui.api.v2.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.generics.UpdatePermissionsRequest;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.plugins.base.maintenance.UpdatePermissionsPlugin;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.common.model.RequestContext;
import org.springframework.stereotype.Service;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class DIPService {

  public StreamResponse createStreamResponse(RequestContext requestContext, String dipUUID)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    ModelService model = requestContext.getModelService();

    Optional<LiteRODAObject> liteDIP = LiteRODAObjectFactory.get(DIP.class, dipUUID);
    if (liteDIP.isEmpty()) {
      throw new RequestNotValidException("Couldn't retrieve DIP with UUID: " + dipUUID);
    }

    ConsumesOutputStream download;
    if (model.hasDirectory(liteDIP.get(), RodaConstants.STORAGE_DIRECTORY_DATA)) {
      download = model.exportObjectToStream(liteDIP.get(), dipUUID, false, RodaConstants.STORAGE_DIRECTORY_DATA);
    } else {
      download = model.exportObjectToStream(liteDIP.get(), dipUUID, false);
    }
    return new StreamResponse(download);
  }

  public Job deleteDIPsJob(DeleteRequest request, User user)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, request.getDetails());
    return CommonServicesUtils.createAndExecuteInternalJob("Delete DIPs",
      CommonServicesUtils.convertSelectedItems(request.getItemsToDelete(), IndexedDIP.class),
      DeleteRODAObjectPlugin.class, user, pluginParameters, "Could not execute delete DIP action");
  }

  public Job updateDIPPermissions(User user, UpdatePermissionsRequest request)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_PERMISSIONS_JSON,
      JsonUtils.getJsonFromObject(request.getPermissions()));
    return CommonServicesUtils.createAndExecuteInternalJob("Update DIP permissions recursively",
      CommonServicesUtils.convertSelectedItems(request.getItemsToUpdate(), IndexedDIP.class),
      UpdatePermissionsPlugin.class, user, pluginParameters, "Could not execute DIP permissions recursively action");
  }
}
