package org.roda.core.plugins.plugins.internal.disposal.confirmation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationAIPEntry;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationState;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalDestructionAIPMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
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
public class DestroyRecordsPlugin extends AbstractPlugin<DisposalConfirmation> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DestroyRecordsPlugin.class);
  private static final String EVENT_DESCRIPTION = "AIP destroyed by disposal confirmation";

  private boolean processedWithErrors = false;
  private final Date executionDate = new Date();
  private boolean processSkipped = true;

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "Destroy records under disposal confirmation report";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Destroys records under a disposal confirmation report moving "
      + "them to a disposal bin structure so they can be later on restored or "
      + "permanently deleted from the storage. This process marks the AIP as "
      + "destroyed and a PREMIS event is recorded after finishing the task.";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.DESTRUCTION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Destroy records under disposal confirmation report";
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
        jobPluginInfo, plugin,
        object) -> processDisposalConfirmation(modelService, storageService, report, cachedJob, jobPluginInfo, object),
      index, model, storage, liteList);
  }

  private void processDisposalConfirmation(ModelService model, StorageService storage, Report report, Job cachedJob,
    JobPluginInfo jobPluginInfo, DisposalConfirmation disposalConfirmation) {

    // iterate over the AIP list
    // copy the AIP using rsync to the disposal bin and another place
    // mark the AIP as destroyed
    // add executedOn and executedBy on the AIP
    // Apply stylesheet over the descriptive metadata
    // Remove representations
    // add preservation event
    // copy the preservation event using rsync to the disposal bin

    // IF any of this steps fails for any AIP the process continues and destroys as
    // much as it can

    String disposalConfirmationId = disposalConfirmation.getId();

    try {
      StoragePath disposalConfirmationAIPsPath = ModelUtils.getDisposalConfirmationAIPsPath(disposalConfirmationId);
      Binary binary = storage.getBinary(disposalConfirmationAIPsPath);

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(binary.getContent().createInputStream()))) {
        jobPluginInfo.setSourceObjectsCount(disposalConfirmation.getNumberOfAIPs().intValue());
        // Iterate over the AIP
        while (reader.ready()) {
          String aipEntryJson = reader.readLine();
          processAipEntry(aipEntryJson, disposalConfirmation, model, cachedJob, report, jobPluginInfo);
        }
      }
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException
      | IOException e) {
      LOGGER.error("Failed to destroy intellectual entities of disposal confirmation '{}' ({}): {}",
        disposalConfirmation.getTitle(), disposalConfirmationId, e.getMessage(), e);
      Report reportItem = PluginHelper.initPluginReportItem(this, disposalConfirmation.getId(),
        DisposalConfirmation.class);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      reportItem.setPluginState(PluginState.FAILURE)
        .setPluginDetails("Failed to destroy intellectual entities on disposal confirmation '"
          + disposalConfirmation.getTitle() + "' (" + disposalConfirmation.getId() + "): " + e.getMessage());
      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
      processedWithErrors = true;
    }

    DisposalConfirmationState disposalConfirmationState = DisposalConfirmationState.APPROVED;

    if (!processedWithErrors) {
      disposalConfirmation.setExecutedOn(executionDate);
      disposalConfirmation.setExecutedBy(cachedJob.getUsername());
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

  private void processAipEntry(String aipEntryJson, DisposalConfirmation disposalConfirmation, ModelService model,
    Job cachedJob, Report report, JobPluginInfo jobPluginInfo) {
    try {
      DisposalConfirmationAIPEntry aipEntry = JsonUtils.getObjectFromJson(aipEntryJson,
        DisposalConfirmationAIPEntry.class);
      AIP aip = model.retrieveAIP(aipEntry.getAipId());
      processAIP(aip, disposalConfirmation, model, cachedJob, report, jobPluginInfo);
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Failed to process AIP entry '{}': {}", aipEntryJson, e.getMessage(), e);
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

  private void processAIP(AIP aip, DisposalConfirmation disposalConfirmation, ModelService model, Job cachedJob,
    Report report, JobPluginInfo jobPluginInfo) {

    LOGGER.debug("Processing AIP {}", aip.getId());

    Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);

    PluginState pluginState = PluginState.SUCCESS;
    String outcomeText;

    try {
      aip.setState(AIPState.DESTROY_PROCESSING);
      model.updateAIPState(aip, cachedJob.getUsername());

      testAndExecuteCopyAIP2DisposalBin(aip, disposalConfirmation.getId());

      testAndExecuteSetAIPMetadataInformation(aip, cachedJob.getUsername());

      testAndExecuteApplyStylesheet(aip, model);

      testAndExecuteRemoveAllRepresentations(aip, model);

      // destroy the AIP
      model.destroyAIP(aip, cachedJob.getUsername());

      outcomeText = "AIP '" + aip.getId() + "' has been destroyed with disposal confirmation '"
        + disposalConfirmation.getTitle() + "' (" + disposalConfirmation.getId() + ")";

      reportItem.setPluginDetails(outcomeText);

    } catch (IOException | CommandException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | NotFoundException e) {
      LOGGER.error("Failed to destroy AIP '{}': {}", aip.getId(), e.getMessage(), e);
      pluginState = PluginState.FAILURE;
      outcomeText = "AIP '" + aip.getId() + "' has not been destroyed with disposal confirmation '"
        + disposalConfirmation.getTitle() + "' (" + disposalConfirmation.getId() + ")";
      reportItem.setPluginDetails(outcomeText + ": " + e.getMessage());
      processedWithErrors = true;
    }

    model.createEvent(aip.getId(), null, null, null, RodaConstants.PreservationEventType.DESTRUCTION, EVENT_DESCRIPTION,
      null, null, pluginState, outcomeText, "", cachedJob.getUsername(), true);

    // copy the preservation event to the AIP in the disposal bin
    // using the --ignore-existing flag in the rsync process, copying only the new
    // preservation event, leaving the remaining AIP structure intact
    try {
      DisposalConfirmationPluginUtils.copyAIPToDisposalBin(aip, disposalConfirmation.getId(),
        Arrays.asList("-r", "--ignore-existing"));
    } catch (RequestNotValidException | GenericException | CommandException e) {
      LOGGER.error("Failed to copy preservation event: {}", e.getMessage(), e);
    }

    jobPluginInfo.incrementObjectsProcessed(pluginState);

    reportItem.setPluginState(pluginState);
    report.addReport(reportItem);

    PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
  }

  private void testAndExecuteRemoveAllRepresentations(AIP aip, ModelService model)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    // remove all representations
    for (Representation representation : aip.getRepresentations()) {
      model.deleteRepresentation(aip.getId(), representation.getId());
    }
    aip.getRepresentations().clear();
  }

  private void testAndExecuteCopyAIP2DisposalBin(AIP aip, String disposalConfirmationId)
    throws GenericException, CommandException, RequestNotValidException {
    // Copy AIP to disposal bin
    DisposalConfirmationPluginUtils.copyAIPToDisposalBin(aip, disposalConfirmationId, Collections.singletonList("-r"));
    processSkipped = false;
  }

  private void testAndExecuteSetAIPMetadataInformation(AIP aip, String destructionBy) {
    DisposalDestructionAIPMetadata destruction = aip.getDisposal().getConfirmation().getDestruction();
    if (destruction == null) {
      destruction = new DisposalDestructionAIPMetadata();
    }

    destruction.setDestructionBy(destructionBy);
    destruction.setDestructionOn(executionDate);
    aip.getDisposal().getConfirmation().setDestruction(destruction);

    processSkipped = false;
  }

  private void testAndExecuteApplyStylesheet(AIP aip, ModelService model)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException, IOException {
    // Apply stylesheet to descriptive metadata
    for (DescriptiveMetadata metadata : aip.getDescriptiveMetadata()) {
      Binary binary = model.retrieveDescriptiveMetadataBinary(aip.getId(), metadata.getId());

      StoragePath descriptiveMetadataStoragePath = ModelUtils.getDescriptiveMetadataStoragePath(metadata);
      Path descriptiveMetadataPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(),
        descriptiveMetadataStoragePath);

      Reader reader = RodaUtils.applyMetadataStylesheet(binary, RodaConstants.CORE_DISPOSAL_METADATA_TRANSFORMERS,
        metadata.getType(), metadata.getVersion(), Collections.emptyMap());

      ReaderInputStream readerInputStream = new ReaderInputStream(reader, StandardCharsets.UTF_8);

      FileUtils.copyInputStreamToFile(readerInputStream, descriptiveMetadataPath.toFile());
    }
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
    return new DestroyRecordsPlugin();
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
