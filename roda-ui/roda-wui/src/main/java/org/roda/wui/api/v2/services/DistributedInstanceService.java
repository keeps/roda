package org.roda.wui.api.v2.services;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;
import org.roda.core.data.v2.synchronization.central.CreateDistributedInstanceRequest;
import org.roda.core.data.v2.synchronization.central.CreateLocalInstanceRequest;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.UpdateDistributedInstanceRequest;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.plugins.base.synchronization.instance.LocalInstanceRegisterPlugin;
import org.roda.core.plugins.base.synchronization.proccess.ImportSyncBundlePlugin;
import org.roda.core.plugins.base.synchronization.proccess.SynchronizeInstancePlugin;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Service
public class DistributedInstanceService {

  @Autowired
  private JobService jobsService;

  public void deleteDistributedInstance(MembersService membersService, String id)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    final DistributedInstance distributedInstance = RodaCoreFactory.getModelService().retrieveDistributedInstance(id);
    final String username = RodaConstants.DISTRIBUTED_INSTANCE_USER_PREFIX + distributedInstance.getName();
    RodaCoreFactory.getModelService().deleteDistributedInstance(id);
    membersService.deleteUser(username);
  }

  public DistributedInstance createDistributedInstance(
    CreateDistributedInstanceRequest createDistributedInstanceRequest, User user)
    throws GenericException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException,
    RequestNotValidException, IllegalOperationException {
    DistributedInstance distributedInstance = new DistributedInstance();

    distributedInstance.setName(createDistributedInstanceRequest.getName());
    distributedInstance.setDescription(createDistributedInstanceRequest.getDescription());

    return RodaCoreFactory.getModelService().createDistributedInstance(distributedInstance, user.getName());
  }

  public LocalInstance subscribe(LocalInstance localInstance, User user)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    localInstance.setStatus(SynchronizingStatus.APPLYINGIDENTIFIER);
    RodaCoreFactory.createOrUpdateLocalInstance(localInstance);
    applyInstanceIdToRodaObject(localInstance.getId(), user, true);
    RODAInstanceUtils.createDistributedGroup(user);
    localInstance.setAccessKey(null);
    return localInstance;
  }

  public void applyInstanceIdToRodaObject(String id, User user, boolean doRegister)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    Map<String, String> pluginParameters = new HashMap<>();
    if (doRegister) {
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_REGISTER_PLUGIN, "true");
    }
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER, id);

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

  public StreamResponse createCentralSyncBundle(String instanceIdentifier) throws AuthorizationDeniedException,
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

  public StreamResponse retrieveLastSyncFileByClass(final String instanceIdentifier, final String entityClass,
    final String type) {
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
      return CommonServicesUtils.createAndExecuteInternalJob("Synchronize bundle", SelectedItemsNone.create(),
        ImportSyncBundlePlugin.class, user, pluginParameters, "Could not execute bundle job");
    } catch (IOException e) {
      throw new GenericException("Failed during sync package import", e);
    }
  }

  public DistributedInstance updateDistributionInstance(UpdateDistributedInstanceRequest request, User user)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    DistributedInstance distributedInstance = RodaCoreFactory.getModelService()
      .retrieveDistributedInstance(request.getId());

    distributedInstance.setDescription(request.getDescription());

    if (StringUtils.isNotBlank(request.getName())) {
      distributedInstance.setName(request.getName());
    } else {
      throw new RequestNotValidException("Name cannot be empty");
    }

    return RodaCoreFactory.getModelService().updateDistributedInstance(distributedInstance, user.getId());
  }

  public DistributedInstance updateDistributionInstanceStatus(String id, boolean activate, User user)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    DistributedInstance distributedInstance = RodaCoreFactory.getModelService().retrieveDistributedInstance(id);
    if (activate) {
      distributedInstance.setStatus(SynchronizingStatus.ACTIVE);
    } else {
      distributedInstance.setStatus(SynchronizingStatus.INACTIVE);
    }
    return RodaCoreFactory.getModelService().updateDistributedInstance(distributedInstance, user.getId());
  }

  public LocalInstance createLocalInstance(CreateLocalInstanceRequest createLocalInstanceRequest)
    throws GenericException {
    LocalInstance localInstance = new LocalInstance();
    localInstance.setCentralInstanceURL(createLocalInstanceRequest.getCentralInstanceURL());
    localInstance.setAccessKey(createLocalInstanceRequest.getAccessKey());
    localInstance.setId(createLocalInstanceRequest.getId());

    RodaCoreFactory.createOrUpdateLocalInstance(localInstance);
    localInstance.setAccessKey(null);

    return localInstance;
  }
}
