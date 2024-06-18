package org.roda.wui.api.v2.services;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.common.synchronization.BundleManifestCreator;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.EntityResponse;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.plugins.base.synchronization.instance.LocalInstanceRegisterPlugin;
import org.roda.core.plugins.base.synchronization.proccess.ImportSyncBundlePlugin;
import org.roda.core.plugins.base.synchronization.proccess.SynchronizeInstancePlugin;
import org.roda.wui.api.controllers.BrowserHelper;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Service
public class DistributedInstanceService {

  @Autowired
  private JobService jobsService;

  public DistributedInstance createDistributedInstance(DistributedInstance distributedInstance, User user)
    throws GenericException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException,
    RequestNotValidException, IllegalOperationException {
    return RodaCoreFactory.getModelService().createDistributedInstance(distributedInstance, user.getName());
  }

  public void applyInstanceIdToRodaObject(LocalInstance localInstance, User user, boolean doRegister)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    Map<String, String> pluginParameters = new HashMap<>();
    if (doRegister) {
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_REGISTER_PLUGIN, "true");
    }
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER, localInstance.getId());

    CommonServicesUtils.createAndExecuteInternalJob("Local Instance Register", SelectedItemsNone.create(),
      LocalInstanceRegisterPlugin.class, user, pluginParameters, "Could not register the localInstance");
  }

  public Job synchronize(User user, LocalInstance localInstance)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    localInstance.setStatus(SynchronizingStatus.SYNCHRONIZING);
    RodaCoreFactory.createOrUpdateLocalInstance(localInstance);
    return CommonServicesUtils.createAndExecuteInternalJob("Synchronize bundle", SelectedItemsNone.create(),
      SynchronizeInstancePlugin.class, user, new HashMap<>(), "Could not execute bundle job");
  }

  public EntityResponse createCentralSyncBundle(String instanceIdentifier) throws AuthorizationDeniedException,
    AlreadyExistsException, RequestNotValidException, GenericException, NotFoundException {

    try {
      Path workingDir = SyncUtils.getBundleWorkingDirectory(instanceIdentifier);
      String bundleName = SyncUtils.getInstanceBundleName(instanceIdentifier);
      if (SyncUtils.createCentralSyncBundle(workingDir, instanceIdentifier)) {
        BundleManifestCreator bundleManifestCreator = new BundleManifestCreator(
          RodaConstants.DistributedModeType.CENTRAL, workingDir);
        bundleManifestCreator.create();
        Path zipPath = SyncUtils.compress(workingDir, bundleName);
        return SyncUtils.createBundleStreamResponse(zipPath);
      }
      return null;
    } catch (IOException e) {
      throw new GenericException("Cannot create temporary directory");
    }
  }

  public EntityResponse retrieveLastSyncFileByClass(final String instanceIdentifier, final String entityClass,
                                                           final String type) throws GenericException, NotFoundException {
    final StringBuilder fileNameBuilder = new StringBuilder();
    fileNameBuilder.append(type).append("_").append(instanceIdentifier).append("_").append(entityClass)
      .append(".jsonl");

    final Path filePath = RodaCoreFactory.getSynchronizationDirectoryPath().resolve(fileNameBuilder.toString());

    return SyncUtils.createLastSyncFileStreamResponse(filePath);
  }

  public Job importSyncBundle(User user, String instanceIdentifier, MultipartFile resource)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    String fileName = resource.getOriginalFilename();
    try {
      Path path = SyncUtils.receiveBundle(fileName, resource.getInputStream());
      Path workingDir = SyncUtils.getBundleWorkingDirectory(instanceIdentifier);

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH, path.toString());
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_BUNDLE_WORKING_PATH, workingDir.toString());
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER, instanceIdentifier);
      return BrowserHelper.createAndExecuteInternalJob("Synchronize bundle", SelectedItemsNone.create(),
        ImportSyncBundlePlugin.class, user, pluginParameters, "Could not execute bundle job");
    } catch (IOException e) {
      throw new GenericException("Failed during sync package import", e);
    }
  }
}
