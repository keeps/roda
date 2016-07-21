/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexTransferredResourcePlugin extends AbstractPlugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexTransferredResourcePlugin.class);

  private String folderUUID = null;

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
    return "Reindex transferred resources folder";
  }

  @Override
  public String getDescription() {
    return "Reset transferred resources index and recreate it from data existing in the storage.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (getParameterValues().containsKey(RodaConstants.PLUGIN_PARAMS_STRING_VALUE)
      && StringUtils.isNotBlank(getParameterValues().get(RodaConstants.PLUGIN_PARAMS_STRING_VALUE))) {
      folderUUID = getParameterValues().get(RodaConstants.PLUGIN_PARAMS_STRING_VALUE);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<TransferredResource> list)
    throws PluginException {
    LOGGER.debug("Reindexing transferred resources");
    Report pluginReport = PluginHelper.initPluginReport(this);

    try {
      RodaCoreFactory.getTransferredResourcesScanner().updateAllTransferredResources(folderUUID, true);
    } catch (IsStillUpdatingException e) {
      pluginReport.setPluginState(PluginState.FAILURE)
        .setPluginDetails("Transferred resources are already being reindexed");
      LOGGER.error(pluginReport.getPluginDetails());
    }

    LOGGER.debug("Done reindexing transferred resources");
    return pluginReport;
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
  public Plugin<TransferredResource> cloneMe() {
    return new ReindexTransferredResourcePlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  // TODO FIX
  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  @Override
  public String getPreservationEventDescription() {
    return "XXXXXXXXXX";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT);
  }

  @Override
  public List<Class<TransferredResource>> getObjectClasses() {
    return Arrays.asList(TransferredResource.class);
  }
}
