package org.roda.core.plugins.plugins.internal.disposal.confirmation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationAIPEntry;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationState;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RestoreRecordsPlugin extends AbstractPlugin<DisposalConfirmation> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestoreRecordsPlugin.class);
  private static final String EVENT_DESCRIPTION = "AIP restored from disposal bin";

  private boolean processedWithErrors = false;

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "Restore records under disposal confirmation report";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.RECOVERY;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Restore records under disposal confirmation report";
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
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this,
      (RODAObjectProcessingLogic<DisposalConfirmation>) (indexService, modelService, storageService, report, cachedJob,
        jobPluginInfo, plugin, object) -> processDisposalConfirmation(indexService, modelService, storageService,
          report, cachedJob, jobPluginInfo, object),
      index, model, storage, liteList);
  }

  private void processDisposalConfirmation(IndexService index, ModelService model, StorageService storage,
    Report report, Job cachedJob, JobPluginInfo jobPluginInfo, DisposalConfirmation disposalConfirmation) {

    try {
      StoragePath disposalConfirmationAIPsPath = ModelUtils
        .getDisposalConfirmationAIPsPath(disposalConfirmation.getId());
      Binary binary = storage.getBinary(disposalConfirmationAIPsPath);

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(binary.getContent().createInputStream()))) {
        jobPluginInfo.setSourceObjectsCount(disposalConfirmation.getNumberOfAIPs().intValue());
        // Iterate over the AIP
        while (reader.ready()) {
          String aipEntryJson = reader.readLine();

          processAipEntry(aipEntryJson, disposalConfirmation, index, model, cachedJob, report, jobPluginInfo);
        }
      }
    } catch (RequestNotValidException | AuthorizationDeniedException | GenericException | NotFoundException
      | IOException e) {
      processedWithErrors = true;
      LOGGER.error("Failed to restore disposal confirmation '{}' ({}) records: {}", disposalConfirmation.getTitle(),
        disposalConfirmation.getId(), e.getMessage(), e);
      Report reportItem = PluginHelper.initPluginReportItem(this, disposalConfirmation.getId(),
        DisposalConfirmation.class);
      reportItem.setPluginState(PluginState.FAILURE).setPluginDetails("Failed to restore the disposal confirmation '"
        + disposalConfirmation.getTitle() + "' (" + disposalConfirmation.getId() + ") records: " + e.getMessage());
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
    }

    DisposalConfirmationState disposalConfirmationState = DisposalConfirmationState.RESTORED;

    if (!processedWithErrors) {
      disposalConfirmation.setRestoredOn(new Date());
      disposalConfirmation.setRestoredBy(cachedJob.getUsername());
      // disposal-bin/<disposalConfirmationId>/*
      Path disposalBinPath = RodaCoreFactory.getDisposalBinDirectoryPath().resolve(disposalConfirmation.getId());
      try {
        FSUtils.deletePath(disposalBinPath);
      } catch (NotFoundException | GenericException e) {
        LOGGER.error("Failed to delete disposal confirmation from disposal bin: {}", e.getMessage(), e);
      }
    } else {
      disposalConfirmationState = DisposalConfirmationState.EXECUTION_FAILED;
    }
    disposalConfirmation.setState(disposalConfirmationState);
    try {
      model.updateDisposalConfirmation(disposalConfirmation);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      LOGGER.error("Failed to update disposal confirmation '{}': {}", disposalConfirmation.getId(), e.getMessage(), e);
    }
  }

  private void processAipEntry(String aipEntryJson, DisposalConfirmation disposalConfirmation, IndexService index,
    ModelService model, Job cachedJob, Report report, JobPluginInfo jobPluginInfo) {
    try {
      DisposalConfirmationAIPEntry aipEntry = JsonUtils.getObjectFromJson(aipEntryJson,
        DisposalConfirmationAIPEntry.class);
      processAIP(aipEntry, disposalConfirmation, index, model, cachedJob, report, jobPluginInfo);
    } catch (GenericException e) {
      LOGGER.error("Failed to process the AIP entry '{}': {}", aipEntryJson, e.getMessage(), e);
      processedWithErrors = true;
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      Report reportItem = PluginHelper.initPluginReportItem(this, disposalConfirmation.getId(),
        DisposalConfirmation.class);
      reportItem.setPluginState(PluginState.FAILURE)
        .setPluginDetails("Failed to process the AIP entry '" + aipEntryJson + "' from disposal confirmation '"
          + disposalConfirmation.getTitle() + "' (" + disposalConfirmation.getId() + "): " + e.getMessage());
      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
    }
  }

  private void processAIP(DisposalConfirmationAIPEntry aipEntry, DisposalConfirmation disposalConfirmation,
    IndexService index, ModelService model, Job cachedJob, Report report, JobPluginInfo jobPluginInfo) {

    LOGGER.debug("Processing AIP entry {}", aipEntry.getAipId());

    Report reportItem = PluginHelper.initPluginReportItem(this, aipEntry.getAipId(), AIP.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);

    PluginState pluginState = PluginState.SUCCESS;
    String outcomeText;

    try {
      // Copy AIP from disposal bin to storage
      DisposalConfirmationPluginUtils.copyAIPFromDisposalBin(aipEntry.getAipId(), disposalConfirmation.getId(),
        Collections.singletonList("-r"));

      // reindex the AIP
      AIP aip = model.retrieveAIP(aipEntry.getAipId());
      aip.setState(AIPState.ACTIVE);
      if (aip.getDisposal() != null) {
        aip.getDisposal().setConfirmation(null);
      }

      model.updateAIP(aip, cachedJob.getUsername());
      index.reindexAIP(aip);

      outcomeText = "AIP '" + aip.getId() + "' has been restored from disposal bin under confirmation '"
        + disposalConfirmation.getTitle() + "' (" + disposalConfirmation.getId() + ")";

      reportItem.setPluginDetails(outcomeText);

    } catch (CommandException | RequestNotValidException | GenericException | NotFoundException
      | AuthorizationDeniedException e) {
      LOGGER.error("Failed to restore AIP '{}': {}", aipEntry.getAipId(), e.getMessage(), e);
      pluginState = PluginState.FAILURE;
      outcomeText = "AIP '" + aipEntry.getAipId()
        + "' has not been restored from disposal bin under disposal confirmation '" + disposalConfirmation.getTitle()
        + "' (" + disposalConfirmation.getId() + ")";
      reportItem.setPluginDetails(outcomeText + ": " + e.getMessage());
      processedWithErrors = true;
    }

    model.createEvent(aipEntry.getAipId(), null, null, null, RodaConstants.PreservationEventType.RECOVERY,
      EVENT_DESCRIPTION, null, null, pluginState, outcomeText, "", cachedJob.getUsername(), true);

    jobPluginInfo.incrementObjectsProcessed(pluginState);

    reportItem.setPluginState(pluginState);
    report.addReport(reportItem);

    PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
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
    return new RestoreRecordsPlugin();
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
