/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.disposal.confirmation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationAIPEntry;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationState;
import org.roda.core.data.v2.disposal.metadata.DisposalDestructionAIPMetadata;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
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
import org.roda.core.storage.Binary;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.util.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DestroyRecordsPlugin extends AbstractPlugin<DisposalConfirmation> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DestroyRecordsPlugin.class);
  private static final String EVENT_DESCRIPTION = "AIP destroyed by disposal confirmation";
  private final Date executionDate = new Date();
  private boolean processedWithErrors = false;

  public static String getStaticName() {
    return "Destroy records under disposal confirmation report";
  }

  public static String getStaticDescription() {
    return "Destroys records under a disposal confirmation report moving "
      + "them to a disposal bin structure so they can be later on restored or "
      + "permanently deleted from the storage. This process marks the AIP as "
      + "destroyed and a PREMIS event is recorded after finishing the task.";
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
  public Report beforeAllExecute(IndexService index, ModelService model) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, List<LiteOptionalWithCause> liteList)
    throws PluginException {
    return PluginHelper.processObjects(this,
      (RODAObjectProcessingLogic<DisposalConfirmation>) (indexService, modelService, report, cachedJob, jobPluginInfo,
        plugin, object) -> processDisposalConfirmation(modelService, report, cachedJob, jobPluginInfo, object),
      index, model, liteList);
  }

  private void processDisposalConfirmation(ModelService model, Report report, Job cachedJob,
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
      Binary binary = model.getBinary(disposalConfirmation,
        RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_AIPS_FILENAME);

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

    PluginState state = PluginState.SUCCESS;
    String outcomeText;

    try {
      if (AIPState.DESTROYED.equals(aip.getState())) {
        state = PluginState.SKIPPED;
        outcomeText = "AIP '" + aip.getId() + " has been skipped because destruction process was successfully done";
      } else {
        aip.setState(AIPState.DESTROY_PROCESSING);
        model.updateAIPState(aip, cachedJob.getUsername());

        testAndExecuteCopyAIP2DisposalBin(aip, disposalConfirmation.getId());

        executeSetAIPMetadataInformation(aip, cachedJob.getUsername());

        executeApplyStylesheet(aip, model, cachedJob.getUsername());

        executeRemoveAllRepresentations(aip, model, cachedJob.getUsername());

        // destroy the AIP
        model.destroyAIP(aip, cachedJob.getUsername());

        outcomeText = "AIP '" + aip.getId() + "' has been destroyed with disposal confirmation '"
          + disposalConfirmation.getTitle() + "' (" + disposalConfirmation.getId() + ")";
      }

      reportItem.setPluginDetails(outcomeText);
    } catch (IOException | CommandException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | NotFoundException | AlreadyExistsException e) {
      LOGGER.error("Failed to destroy AIP '{}': {}", aip.getId(), e.getMessage(), e);
      state = PluginState.FAILURE;
      outcomeText = "AIP '" + aip.getId() + "' has not been destroyed with disposal confirmation '"
        + disposalConfirmation.getTitle() + "' (" + disposalConfirmation.getId() + ")";
      reportItem.setPluginDetails(outcomeText + ": " + e.getMessage());
      processedWithErrors = true;
    }

    model.createEvent(aip.getId(), null, null, null, RodaConstants.PreservationEventType.DESTRUCTION, EVENT_DESCRIPTION,
      null, null, state, outcomeText, "", cachedJob.getUsername(), true, null);

    // copy the preservation event to the AIP in the disposal bin
    // using the --ignore-existing flag in the rsync process, copying only the new
    // preservation event, leaving the remaining AIP structure intact
    try {
      DisposalConfirmationPluginUtils.copyAIPToDisposalBin(aip, disposalConfirmation.getId(),
        Arrays.asList("-r", "--ignore-existing"));
    } catch (RequestNotValidException | GenericException | CommandException e) {
      LOGGER.error("Failed to copy preservation event: {}", e.getMessage(), e);
    }

    jobPluginInfo.incrementObjectsProcessed(state);

    reportItem.setPluginState(state);
    report.addReport(reportItem);

    PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
  }

  private void executeRemoveAllRepresentations(AIP aip, ModelService model, String username)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    // remove all representations
    for (Representation representation : aip.getRepresentations()) {
      model.deleteRepresentation(aip.getId(), representation.getId(), username);
    }
    aip.getRepresentations().clear();
  }

  private void testAndExecuteCopyAIP2DisposalBin(AIP aip, String disposalConfirmationId)
    throws GenericException, CommandException, RequestNotValidException {
    // test if the AIP was copied to disposal bin
    if (!DisposalConfirmationPluginUtils.aipExistsInDisposalBin(aip.getId(), disposalConfirmationId)) {
      // Copy AIP to disposal bin
      DisposalConfirmationPluginUtils.copyAIPToDisposalBin(aip, disposalConfirmationId,
        Collections.singletonList("-r"));
    }
  }

  private void executeSetAIPMetadataInformation(AIP aip, String destructionBy) {
    DisposalDestructionAIPMetadata destruction = aip.getDisposal().getConfirmation().getDestruction();
    if (destruction == null) {
      destruction = new DisposalDestructionAIPMetadata();
    }

    destruction.setDestructionBy(destructionBy);
    destruction.setDestructionOn(executionDate);
    aip.getDisposal().getConfirmation().setDestruction(destruction);
  }

  private void executeApplyStylesheet(AIP aip, ModelService model, String username) throws NotFoundException,
    AuthorizationDeniedException, GenericException, RequestNotValidException, IOException, AlreadyExistsException {
    // Apply stylesheet to descriptive metadata
    for (DescriptiveMetadata metadata : aip.getDescriptiveMetadata()) {
      Binary binary = model.retrieveDescriptiveMetadataBinary(aip.getId(), metadata.getId());
      Reader reader = RodaUtils.applyMetadataStylesheet(binary, RodaConstants.CORE_DISPOSAL_METADATA_TRANSFORMERS,
        metadata.getType(), metadata.getVersion(), Collections.emptyMap());
      ReaderInputStream readerInputStream = new ReaderInputStream(reader, StandardCharsets.UTF_8);
      String content = IOUtils.toString(readerInputStream, StandardCharsets.UTF_8);

      model.deleteDescriptiveMetadata(aip.getId(), metadata.getId(), username);

      model.createDescriptiveMetadata(aip.getId(), metadata.getId(), new StringContentPayload(content),
        metadata.getType(), metadata.getVersion(), username);
    }
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
