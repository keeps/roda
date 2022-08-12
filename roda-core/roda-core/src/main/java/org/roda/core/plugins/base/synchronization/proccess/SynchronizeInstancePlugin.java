/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.synchronization.proccess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.base.multiple.DefaultMultipleStepPlugin;
import org.roda.core.plugins.base.multiple.Step;
import org.roda.core.plugins.base.synchronization.packages.AipPackagePlugin;
import org.roda.core.plugins.base.synchronization.packages.DipPackagePlugin;
import org.roda.core.plugins.base.synchronization.packages.JobPackagePlugin;
import org.roda.core.plugins.base.synchronization.packages.PreservationAgentPackagePlugin;
import org.roda.core.plugins.base.synchronization.packages.RepositoryEventPackagePlugin;
import org.roda.core.plugins.base.synchronization.packages.RiskIncidencePackagePlugin;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.java8.FuturesConvertersImpl;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SynchronizeInstancePlugin extends DefaultMultipleStepPlugin<IsRODAObject> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizeInstancePlugin.class);
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  private static List<Step> steps = new ArrayList<>();

  static {

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_AIP_PACKAGE_PLUGIN,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AIP_PACKAGE_PLUGIN, AipPackagePlugin.getStaticName(),
        PluginParameter.PluginParameterType.BOOLEAN, "true", true, true, AipPackagePlugin.getStaticDescription()));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_JOB_PACKAGE_PLUGIN,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_JOB_PACKAGE_PLUGIN, JobPackagePlugin.getStaticName(),
        PluginParameter.PluginParameterType.BOOLEAN, "true", true, true, JobPackagePlugin.getStaticDescription()));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_DIP_PACKAGE_PLUGIN,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DIP_PACKAGE_PLUGIN, DipPackagePlugin.getStaticName(),
        PluginParameter.PluginParameterType.BOOLEAN, "true", true, true, DipPackagePlugin.getStaticDescription()));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_RISK_INCIDENCE_PACKAGE_PLUGIN,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_RISK_INCIDENCE_PACKAGE_PLUGIN,
        RiskIncidencePackagePlugin.getStaticName(), PluginParameter.PluginParameterType.BOOLEAN, "true", true, true,
        RiskIncidencePackagePlugin.getStaticDescription()));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_REPOSITORY_EVENT_PACKAGE_PLUGIN,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_REPOSITORY_EVENT_PACKAGE_PLUGIN,
        RepositoryEventPackagePlugin.getStaticName(), PluginParameter.PluginParameterType.BOOLEAN, "true", true, true,
        RepositoryEventPackagePlugin.getStaticDescription()));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_PRESERVATION_AGENT_PACKAGE_PLUGIN,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PRESERVATION_AGENT_PACKAGE_PLUGIN,
        PreservationAgentPackagePlugin.getStaticName(), PluginParameter.PluginParameterType.BOOLEAN, "true", true, true,
        PreservationAgentPackagePlugin.getStaticDescription()));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_BUILD_SYNC_MANIFEST_PLUGIN,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_BUILD_SYNC_MANIFEST_PLUGIN,
        BuildSyncManifestPlugin.getStaticName(), PluginParameter.PluginParameterType.BOOLEAN, "true", true, true,
        BuildSyncManifestPlugin.getStaticDescription()));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_SEND_SYNC_BUNDLE_PLUGIN,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_SEND_SYNC_BUNDLE_PLUGIN, SendSyncBundlePlugin.getStaticName(),
        PluginParameter.PluginParameterType.BOOLEAN, "true", true, true, SendSyncBundlePlugin.getStaticDescription()));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_REQUEST_SYNC_BUNDLE_PLUGIN,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_REQUEST_SYNC_BUNDLE_PLUGIN,
        RequestSyncBundlePlugin.getStaticName(), PluginParameter.PluginParameterType.BOOLEAN, "true", true, true,
        RequestSyncBundlePlugin.getStaticDescription()));

    steps
      .add(new Step(AipPackagePlugin.class.getName(), RodaConstants.PLUGIN_PARAMS_DO_AIP_PACKAGE_PLUGIN, true, true));
    steps
      .add(new Step(JobPackagePlugin.class.getName(), RodaConstants.PLUGIN_PARAMS_DO_JOB_PACKAGE_PLUGIN, true, true));
    steps
      .add(new Step(DipPackagePlugin.class.getName(), RodaConstants.PLUGIN_PARAMS_DO_DIP_PACKAGE_PLUGIN, true, true));
    steps.add(new Step(RiskIncidencePackagePlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_RISK_INCIDENCE_PACKAGE_PLUGIN, true, true));
    steps.add(new Step(RepositoryEventPackagePlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_REPOSITORY_EVENT_PACKAGE_PLUGIN, true, true));
    steps.add(new Step(PreservationAgentPackagePlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_PRESERVATION_AGENT_PACKAGE_PLUGIN, true, true));
    steps.add(new Step(BuildSyncManifestPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_BUILD_SYNC_MANIFEST_PLUGIN, true, true));

    steps.add(new Step(SendSyncBundlePlugin.class.getName(), RodaConstants.PLUGIN_PARAMS_DO_SEND_SYNC_BUNDLE_PLUGIN,
      true, true));
    steps.add(new Step(RequestSyncBundlePlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_REQUEST_SYNC_BUNDLE_PLUGIN, true, true));

  }

  @Override
  public List<PluginParameter> getParameters() {
    final ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AIP_PACKAGE_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_JOB_PACKAGE_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DIP_PACKAGE_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_RISK_INCIDENCE_PACKAGE_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_REPOSITORY_EVENT_PACKAGE_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PRESERVATION_AGENT_PACKAGE_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_BUILD_SYNC_MANIFEST_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_SEND_SYNC_BUNDLE_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_REQUEST_SYNC_BUNDLE_PLUGIN));
    return parameters;
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
  public Plugin<IsRODAObject> cloneMe() {
    return new SynchronizeInstancePlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public List<Class<IsRODAObject>> getObjectClasses() {
    return Collections.emptyList();
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
      LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
      // Bundle working dir
      Path workingDir = SyncUtils.getBundleWorkingDirectory(localInstance.getId());
      getParameterValues().put(RodaConstants.PLUGIN_PARAMS_BUNDLE_WORKING_PATH, workingDir.toString());
      // From date
      DistributedInstance distributedInstance = SyncUtils.requestInstanceStatus(localInstance);
      if (distributedInstance.getStatus().equals(SynchronizingStatus.INACTIVE)) {
        throw new PluginException("Instance is inactive");
      }
      Date lastSyncDate = distributedInstance.getLastSynchronizationDate();
      getParameterValues().put(RodaConstants.PLUGIN_PARAMS_BUNDLE_FROM_DATE, JsonUtils.getJsonFromObject(lastSyncDate));
      // To date
      getParameterValues().put(RodaConstants.PLUGIN_PARAMS_BUNDLE_TO_DATE, JsonUtils.getJsonFromObject(new Date()));
    } catch (IOException e) {
      throw new PluginException("Unable to create working directory", e);
    } catch (GenericException e) {
      throw new PluginException("Unable to request information from the central instance", e);
    }
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    try {
      LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
      if (getParameterValues().containsKey(RodaConstants.PLUGIN_PARAMS_BUNDLE_WORKING_PATH)) {
        Path workingDir = SyncUtils.getBundleWorkingDirectory(localInstance.getId());
        Files.deleteIfExists(workingDir);
      }
      localInstance.setStatus(SynchronizingStatus.ACTIVE);
      RodaCoreFactory.createOrUpdateLocalInstance(localInstance);
    } catch (GenericException | IOException e) {
      LOGGER.warn("Failed to delete working dir: " + e.getMessage());
    }

    return new Report();
  }

  @Override
  public void shutdown() {
    // do nothing
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
    setSourceObjectsCount(1);
    super.setParameterValues(parameters);

    try {
      // Bundle name
      String instanceId = RodaCoreFactory.getLocalInstance().getId();
      getParameterValues().put(RodaConstants.PLUGIN_PARAMS_BUNDLE_NAME, SyncUtils.getInstanceBundleName(instanceId));
    } catch (GenericException e) {
      throw new InvalidParameterException(e);
    }
  }
}
