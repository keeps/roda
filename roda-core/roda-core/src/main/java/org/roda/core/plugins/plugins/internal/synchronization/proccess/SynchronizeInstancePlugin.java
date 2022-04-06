package org.roda.core.plugins.plugins.internal.synchronization.proccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.bundle.EntitiesBundle;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.internal.synchronization.SyncBundleHelper;
import org.roda.core.plugins.plugins.internal.synchronization.packages.AipPackagePlugin;
import org.roda.core.plugins.plugins.internal.synchronization.packages.DipPackagePlugin;
import org.roda.core.plugins.plugins.internal.synchronization.packages.JobPackagePlugin;
import org.roda.core.plugins.plugins.internal.synchronization.packages.PreservationAgentPackagePlugin;
import org.roda.core.plugins.plugins.internal.synchronization.packages.RepositoryEventPackagePlugin;
import org.roda.core.plugins.plugins.internal.synchronization.packages.RiskIncidencePackagePlugin;
import org.roda.core.plugins.plugins.multiple.DefaultMultipleStepPlugin;
import org.roda.core.plugins.plugins.multiple.Step;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SynchronizeInstancePlugin<T extends IsRODAObject> extends DefaultMultipleStepPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizeInstancePlugin.class);
  private LocalInstance localInstance;
  private BundleState bundleState;
  private Map<String, PluginParameter> pluginParameters = new HashMap<>();
  private static List<Step> steps = new ArrayList<>();

  static {
    steps.add(new Step(AipPackagePlugin.class.getName(), AIP.class, "", true, true));
    steps.add(new Step(JobPackagePlugin.class.getName(), Job.class, "", true, true));
    steps.add(new Step(DipPackagePlugin.class.getName(), DIP.class, "", true, true));
    steps.add(new Step(RiskIncidencePackagePlugin.class.getName(), RiskIncidence.class, "", true, true));
    steps.add(
      new Step(RepositoryEventPackagePlugin.class.getName(), IndexedPreservationEvent.class, "", true, true));
    steps.add(new Step(PreservationAgentPackagePlugin.class.getName(), IndexedPreservationAgent.class, "",
      true, true));

    steps.add(new Step(SendSyncBundlePlugin.class.getName(), SendSyncBundlePlugin.class, "", true, true));
    steps
      .add(new Step(RequestSyncBundlePlugin.class.getName(), RequestSyncBundlePlugin.class, "", true, true));
  }

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
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public Plugin<T> cloneMe() {
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
  public List<Class<T>> getObjectClasses() {
    return null;
  }

  @Override
  public void setTotalSteps() {
    this.totalSteps = steps.size();
  }

  @Override
  public List<Step> getPluginSteps() {
    return steps;
  }



  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    try {
      localInstance = RodaCoreFactory.getLocalInstance();
      bundleState = SyncUtils.createBundleState(localInstance.getId());
      DistributedInstance distributedInstance = SyncUtils.requestInstanceStatus(localInstance);
      setPackagesBlundeFileNames();
      bundleState.setFromDate(distributedInstance.getLastSyncDate());
      try {
        SyncBundleHelper.createLocalInstanceLists(bundleState);
      } catch (IOException | GenericException e) {
        LOGGER.debug("Failed to create List of entities", e.getMessage(), e);
      }
      SyncUtils.updateBundleState(bundleState, localInstance.getId());
    } catch (GenericException | IOException e) {
      throw new PluginException("Error while creating entity bundle state", e);
    }
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // SyncUtils.deleteSyncBundleWorkingDirectory(localInstance.getId());
    return null;
  }

  @Override
  public void shutdown() {

  }

  @Override
  public PluginParameter getPluginParameter(String pluginParameterId) {
    if (pluginParameters.get(pluginParameterId) != null) {
      return pluginParameters.get(pluginParameterId);
    } else {
      return new PluginParameter();
    }
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    setTotalSteps();
    super.setParameterValues(parameters);
  }

  private void setPackagesBlundeFileNames() {
    final EntitiesBundle entitiesBundle = new EntitiesBundle();
    entitiesBundle.setAipFileName(RodaConstants.SYNCHRONIZATION_LOCAL_INSTANCE_AIP_LIST_FILE_NAME);
    entitiesBundle.setDipFileName(RodaConstants.SYNCHRONIZATION_LOCAL_INSTANCE_DIP_LIST_FILE_NAME);
    entitiesBundle.setRiskFileName(RodaConstants.SYNCHRONIZATION_LOCAL_INSTANCE_RISK_LIST_FILE_NAME);
    bundleState.setEntitiesBundle(entitiesBundle);
  }
}
