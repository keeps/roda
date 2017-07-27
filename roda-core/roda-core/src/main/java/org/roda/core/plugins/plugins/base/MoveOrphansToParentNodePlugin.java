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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
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
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveOrphansToParentNodePlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MoveOrphansToParentNodePlugin.class);
  private String newParentId = "";

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_AIP_PARENT_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_AIP_PARENT_ID, "Parent AIP", PluginParameterType.AIP_ID, "",
        false, false, "Add the parent AIP."));
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Move orphan(s) to a parent node";
  }

  @Override
  public String getDescription() {
    return "Moves selected AIP(s) that are also orphans, i.e. AIPs whose direct ancestor in the catalogue hierarchy does not exist "
      + "(except root level nodes) to a new parent node defined by the user.\nThis task aims to fix problems that may occur when SIPs are "
      + "ingested but not all the necessary items to construct the catalogue hierarchy have been received or properly ingested.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_AIP_PARENT_ID));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_AIP_PARENT_ID)) {
      newParentId = parameters.get(RodaConstants.PLUGIN_PARAMS_AIP_PARENT_ID);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<AIP> plugin, List<AIP> objects) {
        processAIPs(model, jobPluginInfo, cachedJob, objects);
      }
    }, index, model, storage, liteList);
  }

  private void processAIPs(ModelService model, SimpleJobPluginInfo jobPluginInfo, Job cachedJob, List<AIP> list) {
    for (AIP aip : list) {
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE)
        .setPluginState(PluginState.SUCCESS);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);

      try {
        LOGGER.debug("Processing AIP {}", aip.getId());
        String parentId = aip.getParentId();

        try {
          model.retrieveAIP(parentId);
        } catch (RODAException e) {
          aip.setParentId(newParentId);
          model.updateAIP(aip, cachedJob.getUsername());
        }

        jobPluginInfo.incrementObjectsProcessedWithSuccess();
      } catch (RODAException e) {
        LOGGER.error("Error processing AIP {} (RemoveOrphansAction)", aip.getId(), e);
        reportItem.setPluginState(PluginState.FAILURE).addPluginDetails(e.getMessage());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      }
    }
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
  public Plugin<AIP> cloneMe() {
    return new MoveOrphansToParentNodePlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.UPDATE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Moved orphan AIPs to a new parent";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Moved orphan AIPs to a new parent successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Move of orphan AIPs to a new parent failed";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }

}
