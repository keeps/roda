package org.roda.wui.api.v2.services;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.DownloadUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.generics.UpdatePermissionsRequest;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.plugins.base.maintenance.UpdatePermissionsPlugin;
import org.roda.core.storage.StorageService;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.springframework.stereotype.Service;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class DIPService {

  public StreamResponse createStreamResponse(String dipUUID)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    StorageService storage = RodaCoreFactory.getStorageService();
    StoragePath storagePath = ModelUtils.getDIPDataStoragePath(dipUUID);

    if (!storage.hasDirectory(storagePath)) {
      storagePath = ModelUtils.getDIPStoragePath(dipUUID);
    }

    ConsumesOutputStream download = DownloadUtils.download(RodaCoreFactory.getStorageService(),
      storage.getDirectory(storagePath), dipUUID);
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
