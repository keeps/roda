package org.roda.wui.api.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.EntityResponse;
import org.roda.core.common.SyncUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.plugins.internal.synchronization.BundleManifestCreator;
import org.roda.core.plugins.plugins.internal.synchronization.instanceIdentifier.LocalInstanceRegisterPlugin;
import org.roda.core.plugins.plugins.internal.synchronization.proccess.ImportSyncBundlePlugin;
import org.roda.core.plugins.plugins.internal.synchronization.proccess.SynchronizeInstancePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shahzod Yusupov <syusupov@keep.pt>
 */
public class RODAInstanceHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(RODAInstanceHelper.class);

  public static DistributedInstance createDistributedInstance(DistributedInstance distributedInstance, User user)
    throws GenericException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException,
    RequestNotValidException, IllegalOperationException {
    return RodaCoreFactory.getModelService().createDistributedInstance(distributedInstance, user.getName());
  }

  public static void applyInstanceIdToRodaObject(LocalInstance localInstance, User user, boolean doRegister)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    Map<String, String> pluginParameters = new HashMap<>();
    if(!doRegister){
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_REGISTER_PLUGIN, "false");
    }
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER, localInstance.getId());

    BrowserHelper.createAndExecuteInternalJob("Local Instance Register", new SelectedItemsNone<>(),
      LocalInstanceRegisterPlugin.class, user, pluginParameters, "Could not register the localInstance");
  }

  public static Job synchronizeBundle(User user, LocalInstance localInstance)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    return BrowserHelper.createAndExecuteInternalJob("Synchronize bundle", SelectedItemsNone.create(),
      SynchronizeInstancePlugin.class, user, new HashMap<>(), "Could not execute bundle job");
  }

  public static Job importSyncBundle(User user, String instanceIdentifier, FormDataMultiPart multiPart)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    FormDataBodyPart file = multiPart.getField(RodaConstants.API_PARAM_FILE);
    BodyPartEntity bodyPartEntity = (BodyPartEntity) file.getEntity();
    String fileName = file.getContentDisposition().getFileName();
    try {
      Path path = SyncUtils.receiveBundle(fileName, bodyPartEntity.getInputStream());
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

  /**
   * Get the last synchronization file to this instance.
   *
   * @param instanceIdentifier
   *          The instance identifier.
   * @return {@link EntityResponse}.
   * @throws GenericException
   *           if some error occurs.
   * @throws NotFoundException
   *           if file does not exists.
   */
  public static EntityResponse retrieveLastSyncFileByClass(final String instanceIdentifier, final String entityClass,
    final String type) throws GenericException, NotFoundException {
    final StringBuilder fileNameBuilder = new StringBuilder();
    fileNameBuilder.append(type).append("_").append(instanceIdentifier).append("_").append(entityClass)
      .append(".jsonl");

    final Path filePath = RodaCoreFactory.getSynchronizationDirectoryPath().resolve(fileNameBuilder.toString());

    return SyncUtils.createLastSyncFileStreamResponse(filePath);
  }

  public static Long synchronizeIfUpdated(User user) throws GenericException, NotFoundException {

    IndexService index = RodaCoreFactory.getIndexService();
    Long total = 0L;

    LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
    if (localInstance != null) {
      Date fromDate = localInstance.getLastSynchronizationDate();
      Date toDate = new Date();

      // check if updates in AIPs
      total += retrieveNumberOfUpdated(IndexedAIP.class, RodaConstants.AIP_UPDATED_ON, RodaConstants.AIP_UPDATED_ON,
        index, fromDate, toDate);

      // check if updates in Jobs
      total += retrieveNumberOfUpdated(Job.class, RodaConstants.JOB_START_DATE, RodaConstants.JOB_END_DATE, index,
        fromDate, toDate);
      // check if updates in Dips
      total += retrieveNumberOfUpdated(IndexedDIP.class, RodaConstants.DIP_LAST_MODIFIED,
        RodaConstants.DIP_LAST_MODIFIED, index, fromDate, toDate);
      // check if updates in RiskIncidences
      total += retrieveNumberOfUpdated(RiskIncidence.class, RodaConstants.RISK_INCIDENCE_UPDATED_ON,
        RodaConstants.RISK_INCIDENCE_UPDATED_ON, index, fromDate, toDate);
      // check if updates in RepositoryEvent
      total += retrieveNumberOfUpdated(IndexedPreservationEvent.class, RodaConstants.PRESERVATION_EVENT_DATETIME,
        RodaConstants.PRESERVATION_EVENT_DATETIME, index, fromDate, toDate);
      // TODO check if updates in PreservationAgents
      /*
       * total += retrieveNumberOfUpdated(IndexedPreservationAgent.class,
       * RodaConstants.PRESERVATION_EVENT_DATETIME,
       * RodaConstants.PRESERVATION_EVENT_DATETIME, index, fromDate, toDate);
       */
    } else {
      LOGGER.warn("Could not find local instance");
      throw new NotFoundException("Could not find local instance");
    }
    return total;
  }

  public static <T extends IsIndexed> Long retrieveNumberOfUpdated(Class<T> returnClass, String startDate,
    String endDate, IndexService indexService, Date fromDate, Date toDate) throws GenericException {

    Long updatedItems = 0L;
    Filter filter = new Filter();

    if (returnClass.equals(Job.class)) {
      filter.add(new NotSimpleFilterParameter(RodaConstants.JOB_PLUGIN_TYPE, PluginType.INTERNAL.toString()));
      filter.add(new NotSimpleFilterParameter(RodaConstants.JOB_STATE, Job.JOB_STATE.CREATED.name()));
      filter.add(new NotSimpleFilterParameter(RodaConstants.JOB_STATE, Job.JOB_STATE.PENDING_APPROVAL.name()));
    } else if (returnClass.equals(IndexedAIP.class)) {
      Filter pfilter = new Filter();
      pfilter.add(new DateIntervalFilterParameter(RodaConstants.PRESERVATION_EVENT_DATETIME,
        RodaConstants.PRESERVATION_EVENT_DATETIME, fromDate, toDate));
      try {
        updatedItems += indexService.count(IndexedPreservationEvent.class, pfilter);
      } catch (GenericException | RequestNotValidException e) {
        LOGGER.warn("Internal error searching for updates in " + IndexedPreservationEvent.class, e);
        throw new GenericException("Internal error searching for updates in " + IndexedPreservationEvent.class);
      }
    }
    filter.add(new DateIntervalFilterParameter(startDate, endDate, fromDate, toDate));
    try {
      updatedItems += indexService.count(returnClass, filter);
    } catch (RequestNotValidException | GenericException e) {
      LOGGER.warn("Internal error searching for updates in " + returnClass, e);
      throw new GenericException("Internal error searching for updates in " + returnClass);
    }
    return updatedItems;
  }

  public static String removeSyncBundle(String bundlename, String bundleDirectory) {
    String message = null;

    if (RodaConstants.CORE_SYNCHRONIZATION_INCOMING_FOLDER.equals(bundleDirectory)) {
      Path syncBundlePath = SyncUtils.getSyncIncomingBundlePath(bundlename);
      try {
        Boolean deleteFromIncome = Files.deleteIfExists(syncBundlePath);
        if (deleteFromIncome == true) {
          message = "Deleted bundle from income folder with success";
        } else {
          message = "Could not find bundle in income folder";
        }
      } catch (IOException e) {
        LOGGER.error("Can not delete bundle from income folder because " + e.getMessage());
      }
    } else {
      Path syncBundlePath = SyncUtils.getSyncOutcomeBundlePath(bundlename);
      try {
        Boolean deleteFromOutcome = Files.deleteIfExists(syncBundlePath);
        if (deleteFromOutcome == true) {
          message = "Deleted bundle from outcome folder with success";
        } else {
          message = "Could not find bundle in outcome folder";
        }
      } catch (IOException e) {
        LOGGER.error("Can not delete bundle from outcome folder because " + e.getMessage());
      }
    }
    return message;
  }

  public static EntityResponse createCentralSyncBundle(String instanceIdentifier) throws AuthorizationDeniedException,
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
}
