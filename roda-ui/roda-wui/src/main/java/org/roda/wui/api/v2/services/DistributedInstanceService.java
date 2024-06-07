package org.roda.wui.api.v2.services;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.plugins.base.synchronization.instance.LocalInstanceRegisterPlugin;
import org.roda.core.plugins.base.synchronization.proccess.SynchronizeInstancePlugin;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
