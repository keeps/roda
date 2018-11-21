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

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
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
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SIPRemovePlugin extends AbstractPlugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SIPRemovePlugin.class);

  private boolean createEvent = true;

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
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<TransferredResource>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<TransferredResource> plugin, TransferredResource object) {
        processTransferredResource(model, report, jobPluginInfo, cachedJob, object);
      }
    }, index, model, storage, liteList);
  }

  private void processTransferredResource(ModelService model, Report report, JobPluginInfo pluginInfo, Job job,
    TransferredResource transferredResource) {
    Report reportItem = PluginHelper.initPluginReportItem(this, transferredResource);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);

    try {
      LOGGER.debug("Removing SIP {}", transferredResource.getFullPath());
      model.deleteTransferredResource(transferredResource);
      LOGGER.debug("Done with removing SIP {}", transferredResource.getFullPath());

      if (createEvent) {
        model.createRepositoryEvent(PreservationEventType.DELETION,
          "The process of deleting an object of the repository", PluginState.SUCCESS,
          "The transferred resource " + transferredResource.getId() + " has been deleted.", "", job.getUsername(),
          true);
      }

      pluginInfo.incrementObjectsProcessedWithSuccess();
    } catch (RuntimeException | GenericException | AuthorizationDeniedException e) {
      if (createEvent) {
        model.createRepositoryEvent(PreservationEventType.DELETION,
          "The process of deleting an object of the repository", PluginState.SUCCESS,
          "The transferred resource " + transferredResource.getId() + " has not been deleted.", "", job.getUsername(),
          true);
      }

      pluginInfo.incrementObjectsProcessedWithFailure();
      reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
      LOGGER.error("Error removing transferred resource " + transferredResource.getFullPath(), e);
    }

    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
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
    createEvent = Boolean.parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("event", "create", "all"));
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
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