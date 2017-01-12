/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
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
    // do nothing
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
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    Report report = PluginHelper.initPluginReport(this);
    try {
      Job job = PluginHelper.getJob(this, model);
      List<TransferredResource> list = PluginHelper.transformLitesIntoObjects(model, index, this, report, null,
        liteList, job);

      for (TransferredResource transferredResource : list) {
        Report reportItem = PluginHelper.initPluginReportItem(this, transferredResource);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, false, job);

        try {
          LOGGER.debug("Removing SIP {}", transferredResource.getFullPath());
          model.deleteTransferredResource(transferredResource);
          LOGGER.debug("Done with removing SIP {}", transferredResource.getFullPath());
          // TODO: create event...
          // PluginHelper.createPluginEvent(this, aipID, model, index, sources,
          // targets, outcome, outcomeDetailExtension, notify)

        } catch (RuntimeException e) {
          reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
          LOGGER.error("Error removing transferred resource " + transferredResource.getFullPath(), e);
        }
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);
      }
    } catch (AuthorizationDeniedException | NotFoundException | GenericException | RequestNotValidException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return report;
  }

  @Override
  public Plugin<TransferredResource> cloneMe() {
    return new SIPRemovePlugin();
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<TransferredResource>> getObjectClasses() {
    return Arrays.asList(TransferredResource.class);
  }

}