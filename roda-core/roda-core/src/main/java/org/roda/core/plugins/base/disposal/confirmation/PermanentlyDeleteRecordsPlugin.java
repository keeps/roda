/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.disposal.confirmation;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationState;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PermanentlyDeleteRecordsPlugin extends AbstractPlugin<DisposalConfirmation> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PermanentlyDeleteRecordsPlugin.class);

  public static String getStaticName() {
    return "Permanently delete records from disposal confirmation report";
  }

  public static String getStaticDescription() {
    return "Permanently deletes from the disposal bin the original records destroyed releasing storage space from the repository."
      + "A PREMIS event is recorded after finishing the task.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.DELETION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Permanently delete records from disposal confirmation report";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The records under disposal confirmation report were deleted permanently";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "The records under disposal confirmation report failed to be deleted permanently";
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, List<LiteOptionalWithCause> liteList)
    throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<DisposalConfirmation>() {
      @Override
      public void process(IndexService index, ModelService model, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<DisposalConfirmation> plugin, DisposalConfirmation object) {
        processDisposalConfirmation(model, report, cachedJob, jobPluginInfo, object);
      }
    }, index, model, liteList);
  }

  private void processDisposalConfirmation(ModelService model, Report report, Job cachedJob,
    JobPluginInfo jobPluginInfo, DisposalConfirmation confirmation) {

    Report reportItem = PluginHelper.initPluginReportItem(this, confirmation.getId(), DisposalConfirmation.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
    PluginState state = PluginState.SUCCESS;
    try {
      // disposal-bin/<disposalConfirmationId>/*
      Path disposalBinPath = RodaCoreFactory.getDisposalBinDirectoryPath().resolve(confirmation.getId());
      FSUtils.deletePath(disposalBinPath);

      confirmation.setState(DisposalConfirmationState.PERMANENTLY_DELETED);

      model.updateDisposalConfirmation(confirmation);
      reportItem.setPluginDetails("Records under disposal confirmation '" + confirmation.getTitle() + "' ("
        + confirmation.getId() + ") were deleted permanently");
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      LOGGER.error("Failed to permanently delete the records under disposal confirmation '{}' ({}): {}",
        confirmation.getTitle(), confirmation.getId(), e.getMessage(), e);
      state = PluginState.FAILURE;
      reportItem
        .setPluginDetails("Failed to permanently delete records under disposal confirmation: " + e.getMessage());
    }

    jobPluginInfo.incrementObjectsProcessed(state);
    reportItem.setPluginState(state);
    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);

    model.createRepositoryEvent(getPreservationEventType(), getPreservationEventDescription(), state,
      PluginState.FAILURE.equals(state) ? getPreservationEventFailureMessage() : getPreservationEventSuccessMessage(),
      confirmation.getId(), cachedJob.getUsername(), true, null);
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public List<Class<DisposalConfirmation>> getObjectClasses() {
    return Collections.singletonList(DisposalConfirmation.class);
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public Plugin<DisposalConfirmation> cloneMe() {
    return new PermanentlyDeleteRecordsPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }
}
