package org.roda.core.plugins.plugins.internal.synchronization.proccess;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.bundle.EntitiesBundle;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.internal.synchronization.SyncBundleHelper;
import org.roda.core.plugins.plugins.internal.synchronization.packages.AipPackagePlugin;
import org.roda.core.plugins.plugins.internal.synchronization.packages.DipPackagePlugin;
import org.roda.core.plugins.plugins.internal.synchronization.packages.JobPackagePlugin;
import org.roda.core.plugins.plugins.internal.synchronization.packages.PreservationAgentPackagePlugin;
import org.roda.core.plugins.plugins.internal.synchronization.packages.RepositoryEventPackagePlugin;
import org.roda.core.plugins.plugins.internal.synchronization.packages.RiskIncidencePackagePlugin;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SynchronizeInstancePlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizeInstancePlugin.class);
  private LocalInstance localInstance;
  private BundleState bundleState;

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "Synchronize instances";
  }

  @Override
  public String getDescription() {
    return "Synchronize instances";
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.NONE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "";
  }

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new SynchronizeInstancePlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public void init() throws PluginException {

  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    try {
      localInstance = RodaCoreFactory.getLocalInstance();
      bundleState = SyncUtils.createBundleState(localInstance.getId());
      DistributedInstance distributedInstance = SyncUtils.requestInstanceStatus(localInstance);
      setPackagesBlundeFileNames();
      createBundleFiles();
      bundleState.setFromDate(distributedInstance.getLastSyncDate());
      SyncUtils.updateBundleState(bundleState, localInstance.getId());
    } catch (GenericException | IOException e) {
      throw new PluginException("Error while creating entity bundle state", e);
    }
    return new Report();
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) throws PluginException {
        synchronizeInstance(index, model, storage, report, jobPluginInfo, cachedJob);
      }
    }, index, model, storage);
  }

  private void synchronizeInstance(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob) {
    Report sendReport = null;
    Report remoteActionsReport = null;

    report.setTotalSteps(8);

    final List<Class<? extends IsRODAObject>> classes = getSynchronizedObjectClasses();
    for (Class<? extends IsRODAObject> bundleClass : classes) {
      Report packageReport = null;
      try {
        String bundlePluginName = getPackagePluginName(bundleClass);
        packageReport = executePlugin(index, model, storage, cachedJob, bundlePluginName, bundleClass);
      } catch (NotFoundException | InvalidParameterException | PluginException e) {
        LOGGER.debug("Failed to execute fixity check plugin on {}", e.getMessage(), e);
      }
      if (packageReport != null) {
        report.addReport(packageReport);
      }
    }

    try {
      SyncBundleHelper.createLocalInstanceLists(bundleState);
    } catch (GenericException e) {
      LOGGER.debug("Failed to create AIP List", e.getMessage(), e);
    }

    try {
      sendReport = executePlugin(index, model, storage, cachedJob, SendSyncBundlePlugin.class.getCanonicalName(),
        Void.class);
    } catch (InvalidParameterException | PluginException e) {
      LOGGER.debug("Failed to execute fixity check plugin on {}", e.getMessage(), e);
    }

    try {
      remoteActionsReport = executePlugin(index, model, storage, cachedJob,
        RequestSyncBundlePlugin.class.getCanonicalName(), Void.class);
    } catch (InvalidParameterException | PluginException e) {
      LOGGER.debug("Failed to execute fixity check plugin on {}", e.getMessage(), e);
    }

    if (sendReport != null) {
      report.addReport(sendReport);
    }

    if (remoteActionsReport != null) {
      report.addReport(remoteActionsReport);
    }
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // SyncUtils.deleteSyncBundleWorkingDirectory(localInstance.getId());
    return null;
  }

  @Override
  public void shutdown() {

  }

  private Report executePlugin(IndexService index, ModelService model, StorageService storage, Job job,
    final String pluginId, Class<? extends IsRODAObject> clazz) throws InvalidParameterException, PluginException {
    Plugin<? extends IsRODAObject> plugin = RodaCoreFactory.getPluginManager().getPlugin(pluginId, clazz);
    Map<String, String> mergedParams = new HashMap<>(getParameterValues());
    mergedParams.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, job.getId());
    plugin.setParameterValues(mergedParams);
    plugin.setMandatory(false);
    return plugin.execute(index, model, storage, null);
  }

  private List<Class<? extends IsRODAObject>> getSynchronizedObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(Job.class);
    list.add(DIP.class);
    list.add(RiskIncidence.class);
    list.add(IndexedPreservationEvent.class);
    list.add(IndexedPreservationAgent.class);
    return list;
  }

  private String getPackagePluginName(Class<?> bundleClass) throws NotFoundException {
    if (bundleClass.equals(AIP.class)) {
      return AipPackagePlugin.class.getCanonicalName();
    } else if (bundleClass.equals(Job.class)) {
      return JobPackagePlugin.class.getCanonicalName();
    } else if (bundleClass.equals(DIP.class)) {
      return DipPackagePlugin.class.getCanonicalName();
    } else if (bundleClass.equals(RiskIncidence.class)) {
      return RiskIncidencePackagePlugin.class.getCanonicalName();
    } else if (bundleClass.equals(IndexedPreservationEvent.class)) {
      return RepositoryEventPackagePlugin.class.getCanonicalName();
    } else if (bundleClass.equals(IndexedPreservationAgent.class)) {
      return PreservationAgentPackagePlugin.class.getCanonicalName();
    } else {
      throw new NotFoundException("No Bundle plugin available");
    }
  }

  private void setPackagesBlundeFileNames() {
    final EntitiesBundle entitiesBundle = new EntitiesBundle();
    entitiesBundle.setAipFileName(RodaConstants.SYNCHRONIZATION_LOCAL_INSTANCE_AIP_LIST_FILE_NAME);
    entitiesBundle.setDipFileName(RodaConstants.SYNCHRONIZATION_LOCAL_INSTANCE_DIP_LIST_FILE_NAME);
    entitiesBundle.setJobFileName(RodaConstants.SYNCHRONIZATION_LOCAL_INSTANCE_JOB_LIST_FILE_NAME);
    entitiesBundle
      .setPreservationAgentFileName(RodaConstants.SYNCHRONIZATION_LOCAL_INSTANCE_PRESERVATION_AGENT_LIST_FILE_NAME);
    entitiesBundle
      .setRepositoryEventFileName(RodaConstants.SYNCHRONIZATION_LOCAL_INSTANCE_REPOSITORY_EVENT_LIST_FILE_NAME);
    entitiesBundle.setRiskFileName(RodaConstants.SYNCHRONIZATION_LOCAL_INSTANCE_RISK_LIST_FILE_NAME);
    bundleState.setEntitiesBundle(entitiesBundle);
  }

  private void createBundleFiles() throws GenericException {

    // init AIP List path
    final Path aipListJsonPath = Paths.get(bundleState.getDestinationPath())
      .resolve(String.format("%s.json", bundleState.getEntitiesBundle().getAipFileName()));
    JsonUtils.writeObjectToFile(Collections.emptyList(), aipListJsonPath);

    // DIP List File
    final Path dipListJsonPath = Paths.get(bundleState.getDestinationPath())
      .resolve(String.format("%s.json", bundleState.getEntitiesBundle().getDipFileName()));
    JsonUtils.writeObjectToFile(Collections.emptyList(), dipListJsonPath);

    // Not Necessary
//    // JOB List File
//    final Path jobListJsonPath = Paths.get(bundleState.getDestinationPath())
//      .resolve(String.format("%s.json", bundleState.getEntitiesBundle().getJobFileName()));
//    JsonUtils.writeObjectToFile(Collections.emptyList(), jobListJsonPath);
//
//    // Preservation Agent List File
//    final Path preservationAgentListJsonPath = Paths.get(bundleState.getDestinationPath())
//      .resolve(String.format("%s.json", bundleState.getEntitiesBundle().getPreservationAgentFileName()));
//    JsonUtils.writeObjectToFile(Collections.emptyList(), preservationAgentListJsonPath);
//
//    // Repository Event List File
//    final Path repositoryEventListJsonPath = Paths.get(bundleState.getDestinationPath())
//      .resolve(String.format("%s.json", bundleState.getEntitiesBundle().getRepositoryEventFileName()));
//    JsonUtils.writeObjectToFile(Collections.emptyList(), repositoryEventListJsonPath);

    // RISK List File
    final Path riskListJsonPath = Paths.get(bundleState.getDestinationPath())
      .resolve(String.format("%s.json", bundleState.getEntitiesBundle().getRiskFileName()));
    JsonUtils.writeObjectToFile(Collections.emptyList(), riskListJsonPath);
  }

}
