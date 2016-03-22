/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.util.List;

import org.roda.core.data.common.RodaConstants.PreservationEventType;
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

public class SIPRemovePlugin extends AbstractPlugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SIPRemovePlugin.class);

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "Delete SIP from transfer";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Deletes SIP from the transfer area if the ingest process is successful.";
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
  public Report execute(IndexService index, ModelService model, StorageService storage, List<TransferredResource> list)
    throws PluginException {
    Report report = PluginHelper.createPluginReport(this);

    for (TransferredResource transferredResource : list) {
      Report reportItem = PluginHelper.createPluginReportItem(this, transferredResource);

      try {
        LOGGER.debug("Removing SIP {}", transferredResource.getFullPath());
        model.deleteTransferredResource(transferredResource);
        LOGGER.debug("Done with removing SIP {}", transferredResource.getFullPath());
        // TODO: create event...
        // PluginHelper.createPluginEvent(this, aipID, model, index, sources,
        // targets, outcome, outcomeDetailExtension, notify)

      } catch (Throwable e) {
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
        LOGGER.error("Error removing transferred resource " + transferredResource.getFullPath(), e);
      }
      report.addReport(reportItem);
      PluginHelper.createJobReport(this, model, reportItem);
    }

    return report;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Plugin<TransferredResource> cloneMe() {
    return new BagitToAIPPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.SIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.DELETION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Deleted SIP from the transfer area.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The original SIP has been deleted from the transfer area.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to delete the original SIP from the transfer area.";
  }

}