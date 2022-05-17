package org.roda.wui.api.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.EntityResponse;
import org.roda.core.common.SyncUtils;
import org.roda.core.common.TokenManager;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.wui.api.v1.utils.ObjectResponse;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

/**
 * @author Shahzod Yusupov <syusupov@keep.pt>
 */
public class RODAInstance extends RodaWuiController {
  private RODAInstance() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static DistributedInstance createDistributedInstance(User user, DistributedInstance distributedInstance)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException, IllegalOperationException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RODAInstanceHelper.createDistributedInstance(distributedInstance, user);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_DISTRIBUTED_INSTANCE_PARAM,
        distributedInstance);
    }
  }

  public static DistributedInstances listDistributedInstances(User user)
    throws GenericException, RequestNotValidException, IOException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RodaCoreFactory.getModelService().listDistributedInstances();
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  public static DistributedInstance retrieveDistributedInstance(User user, String distributedInstanceId)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RodaCoreFactory.getModelService().retrieveDistributedInstance(distributedInstanceId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  public static DistributedInstance updateDistributedInstance(User user, DistributedInstance distributedInstance)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RodaCoreFactory.getModelService().updateDistributedInstance(distributedInstance, user.getId());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  public static void deleteDistributedInstance(User user, String distributedInstanceId)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      final DistributedInstance distributedInstance = RodaCoreFactory.getModelService()
        .retrieveDistributedInstance(distributedInstanceId);
      final String username = RodaConstants.DISTRIBUTED_INSTANCE_USER_PREFIX + distributedInstance.getName();
      RodaCoreFactory.getModelService().deleteDistributedInstance(distributedInstanceId);
      UserManagement.deleteUser(user, username);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  public static void registerDistributedInstance(User user, LocalInstance localInstance)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      DistributedInstance distributedInstance = RodaCoreFactory.getModelService()
        .retrieveDistributedInstance(localInstance.getId());
      distributedInstance.setStatus(SynchronizingStatus.ACTIVE);
      RodaCoreFactory.getModelService().updateDistributedInstance(distributedInstance, user.getId());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }

  public static void createLocalInstance(User user, LocalInstance localInstance)
    throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      RodaCoreFactory.createOrUpdateLocalInstance(localInstance);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM, localInstance);
    }
  }

  public static LocalInstance retrieveLocalInstance(User user) throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      return RodaCoreFactory.getLocalInstance();
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }

  public static void deleteLocalInstanceConfiguration(User user) throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      DistributedInstance distributedInstance = SyncUtils.requestInstanceStatus(RodaCoreFactory.getLocalInstance());
      distributedInstance.setStatus(SynchronizingStatus.INACTIVE);

      SyncUtils.updateDistributedInstance(RodaCoreFactory.getLocalInstance(), distributedInstance);

      TokenManager.getInstance().removeToken();
      RodaCoreFactory.createOrUpdateLocalInstance(null);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }

  public static void updateLocalInstanceConfiguration(User user, LocalInstance localInstance)
    throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      RodaCoreFactory.createOrUpdateLocalInstance(localInstance);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }

  public static List<String> testLocalInstanceConfiguration(User user, LocalInstance localInstance)
    throws AuthorizationDeniedException, GenericException, AuthenticationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    List<String> responseList = new ArrayList();

    // check user permissions
    controllerAssistant.checkRoles(user);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      TokenManager.getInstance().getAccessToken(localInstance);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }

    return responseList;
  }

  public static void removeLocalConfiguration(User user, LocalInstance localInstance)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    List<String> responseList = new ArrayList();

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      RODAInstanceHelper.applyInstanceIdToRodaObject(localInstance, user, false);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }

  public static LocalInstance registerLocalInstance(User user, LocalInstance localInstance)
    throws AuthorizationDeniedException, GenericException, AuthenticationDeniedException, RequestNotValidException,
    NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // Apply Identifiers
      localInstance.setStatus(SynchronizingStatus.APPLYINGIDENTIFIER);
      RodaCoreFactory.createOrUpdateLocalInstance(localInstance);
      RODAInstanceHelper.applyInstanceIdToRodaObject(localInstance, user, true);
      RODAInstanceUtils.createDistributedGroup(user);
      return localInstance;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }

  public static Job synchronizeBundle(User user, LocalInstance localInstance)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {

    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      if (!SynchronizingStatus.ACTIVE.equals(localInstance.getStatus())) {
        state = LogEntryState.FAILURE;
        throw new GenericException("The instance isn't in Active state.");
      } else {
        return RODAInstanceHelper.synchronizeBundle(user, localInstance);
      }
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }

  public static Job importSyncBundle(User user, String instanceIdentifier, FormDataMultiPart multiPart)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {

    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RODAInstanceHelper.importSyncBundle(user, instanceIdentifier, multiPart);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }

  public static EntityResponse retrieveRemoteActions(User user, String instanceIdentifier)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      try {
        return RODAInstanceHelper.createCentralSyncBundle(instanceIdentifier);
      } catch (NotFoundException e) {
        return new ObjectResponse<>(null, null);
      }
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM,
        instanceIdentifier);
    }
  }

  public static DistributedInstance retrieveLocalInstanceStatus(User user, String instanceIdentifier)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    // check permissions
    controllerAssistant.checkRoles(user);
    LogEntryState state = LogEntryState.SUCCESS;
    try {
      return RodaCoreFactory.getModelService().retrieveDistributedInstance(instanceIdentifier);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM,
        instanceIdentifier);
    }
  }

  public static EntityResponse retrieveLastSyncFile(final User user, final String instanceIdentifier,
    final String entityClass, final String type)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return RODAInstanceHelper.retrieveLastSyncFileByClass(instanceIdentifier, entityClass, type);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM,
        instanceIdentifier);
    }
  }

  public static Long synchronizeIfUpdated(User user) throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, JobAlreadyStartedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;
    Long totalupdates = 0L;
    try {
      // delegate
      totalupdates += RODAInstanceHelper.synchronizeIfUpdated(user);
      return totalupdates;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  public static String removeSyncBundle(String bundleName, User user, String bundleDirectory)
    throws AuthorizationDeniedException {
    // check user permissions
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.registerAction(user, state);

    return RODAInstanceHelper.removeSyncBundle(bundleName, bundleDirectory);

  }

  public static Long retrieveUpdates(User user, String instanceIdentifier)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    Long total = 0L;
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();

      DistributedInstance distributedInstance = model.retrieveDistributedInstance(instanceIdentifier);
      Date lastSynchronizationDate = distributedInstance.getLastSynchronizationDate();
      Date toDate = new Date();
      // get Jobs
      final Filter jobFilter = new Filter();
      jobFilter.add(new SimpleFilterParameter(RodaConstants.INDEX_INSTANCE_ID, instanceIdentifier));
      jobFilter.add(new SimpleFilterParameter(RodaConstants.JOB_STATE, Job.JOB_STATE.CREATED.name()));
      jobFilter.add(new DateIntervalFilterParameter(RodaConstants.JOB_START_DATE, RodaConstants.JOB_END_DATE,
        lastSynchronizationDate, toDate));
      total += index.count(Job.class, jobFilter);

      // get Risks
      final Filter riskFilter = new Filter();
      riskFilter.add(new DateIntervalFilterParameter(RodaConstants.RISK_UPDATED_ON, RodaConstants.RISK_UPDATED_ON,
        lastSynchronizationDate, toDate));
      total += index.count(IndexedRisk.class, riskFilter);

      // get RepresentationInformation
      final Filter repFilter = new Filter();
      repFilter.add(new DateIntervalFilterParameter(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_ON,
              RodaConstants.REPRESENTATION_INFORMATION_UPDATED_ON, lastSynchronizationDate, toDate));
      total += index.count(RepresentationInformation.class, riskFilter);

    return total;
  }
}
