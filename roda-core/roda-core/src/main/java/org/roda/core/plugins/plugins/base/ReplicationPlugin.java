/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.reindex.ReindexAIPPlugin;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;
import org.roda.core.util.RESTClientUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicationPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationPlugin.class);
  private boolean hasCompression = true;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_HAS_COMPRESSION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_HAS_COMPRESSION, "Has compression", PluginParameterType.BOOLEAN,
        "true", false, false, "Adds compression when executing rsync."));
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "AIP remote replication";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Copies AIPs and all its files to a secondary RODA instance for redundancy purposes (e.g. Active-passive high-availability architecture)."
      + " This task makes use of “rsync” to synchronize AIP folders between two servers (storage level replication) and calls the secondary API to "
      + "re-index the replicated AIPs (index level replication). The task can only be used if the appropriate configuration settings are defined in "
      + "the “roda-core.properties”.";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_HAS_COMPRESSION));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    // updates the flag responsible to add or not compression
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_HAS_COMPRESSION)) {
      hasCompression = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_HAS_COMPRESSION));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<AIP> plugin, List<AIP> objects) {
        processAIPs(index, model, report, cachedJob, jobPluginInfo, plugin, objects);
      }
    }, index, model, storage, liteList);
  }

  private void processAIPs(IndexService index, ModelService model, Report report, Job cachedJob,
    SimpleJobPluginInfo jobPluginInfo, Plugin<AIP> plugin, List<AIP> objects) {
    Map<String, Report> reports = new HashMap<>();
    PluginState pluginState = PluginState.SUCCESS;
    try {
      for (AIP aip : objects) {
        Report reportItem = PluginHelper.initPluginReportItem(plugin, aip.getId(), AIP.class, aip.getState());
        PluginHelper.updatePartialJobReport(plugin, model, index, reportItem, false, cachedJob);
        reports.put(aip.getId(), reportItem);

        String rsyncResult = ReplicationPluginUtils.executeRsyncAIP(aip, hasCompression);
        if (rsyncResult.equals(ReplicationPluginUtils.PROPERTIES_ERROR_MESSAGE)) {
          pluginState = PluginState.FAILURE;
        }

        reportItem.addPluginDetails(rsyncResult);

        PreservationMetadata pm = PluginHelper.createPluginEvent(plugin, aip.getId(), model, index, pluginState,
          rsyncResult, true);

        rsyncResult = ReplicationPluginUtils.executeRsyncEvent(pm, hasCompression);
        if (rsyncResult.equals(ReplicationPluginUtils.PROPERTIES_ERROR_MESSAGE)) {
          pluginState = PluginState.FAILURE;
        }
        reportItem.addPluginDetails("\n" + rsyncResult);
      }

      // rsync agents
      String rsyncAgentsResult = ReplicationPluginUtils.executeRsyncAgents(hasCompression);

      // create reindex job on the remote side
      sendReindexRequest(model, index, objects.stream().map(aip -> aip.getId()).collect(Collectors.toList()));

      // create success reports for all AIPs
      for (AIP aip : objects) {
        Report reportItem = reports.get(aip.getId());
        reportItem.setPluginState(pluginState);
        reportItem.addPluginDetails("\n" + rsyncAgentsResult);

        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(plugin, model, index, reportItem, true, cachedJob);
        jobPluginInfo.incrementObjectsProcessed(pluginState);
      }

    } catch (UnsupportedOperationException | RODAException e) {
      pluginState = PluginState.FAILURE;
      String outcomeDetailExtension = "Rsync of this AIP did not run successfully. Reason: " + e.getMessage();
      if (e instanceof CommandException) {
        outcomeDetailExtension += "; Output: " + ((CommandException) e).getOutput();
      }

      // fail all AIPs
      for (AIP aip : objects) {
        Report reportItem = reports.get(aip.getId());
        if (reportItem == null) {
          reportItem = PluginHelper.initPluginReportItem(plugin, aip.getId(), AIP.class, aip.getState());
        }
        reportItem.setPluginState(pluginState).setPluginDetails(outcomeDetailExtension);
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(plugin, model, index, reportItem, true, cachedJob);
        jobPluginInfo.incrementObjectsProcessed(pluginState);
      }
    }
  }

  private void sendReindexRequest(ModelService model, IndexService index, List<String> aipIds) throws RODAException {
    String targetApi = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_api");
    String targetResource = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_job_resource");
    String username = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "username");
    String password = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "password");

    if (targetApi != null && targetResource != null && username != null) {
      Job job = new Job();
      job.setId(UUID.randomUUID().toString());
      job.setName(getName());
      job.setSourceObjects(SelectedItemsList.create(AIP.class, aipIds));
      job.setPlugin(ReindexAIPPlugin.class.getCanonicalName());
      job.setPluginType(PluginType.MISC);
      job.setUsername(PluginHelper.getJobUsername(this, index));

      if (LOGGER.isDebugEnabled()) {
        String jsonFromObject = JsonUtils.getJsonFromObject(job);
        LOGGER.debug("Job: {}; targetApi: {}; targetResource: {}; username: {}; password: {}; jsonFromObject: {}", job,
          targetApi, targetResource, username, password, jsonFromObject);
      }

      try {
        RESTClientUtility.sendPostRequest(job, Job.class, targetApi, targetResource, username, password);
      } catch (RODAException e) {
        Report reportItem = PluginHelper.initPluginReport(this);
        reportItem.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Error sending post request to reindex AIPs after replication");
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);
        LOGGER.error("Error sending post request to reindex AIPs", e);
      }
    } else {
      LOGGER.error("Error getting rsync properties to send post request");
    }
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new ReplicationPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.REPLICATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Replication of AIPs, its events and agents to other RODA";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "AIPs, its events and agents were successfully replicated";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "AIPs, its events and agents were not successfully replicated";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_REPLICATION);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }

}
