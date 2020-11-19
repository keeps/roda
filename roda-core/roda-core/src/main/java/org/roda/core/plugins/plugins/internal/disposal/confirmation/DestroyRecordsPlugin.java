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
import org.roda.core.storage.rsync.RsyncUtils;
import org.roda.core.util.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DestroyRecordsPlugin extends AbstractPlugin<DisposalConfirmation> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DestroyRecordsPlugin.class);
  private static final String EVENT_DESCRIPTION = "AIP destroyed by disposal confirmation";

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
    return "";
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
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<DisposalConfirmation>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<DisposalConfirmation> plugin, DisposalConfirmation object) {
        processDisposalConfirmation(index, model, storage, report, cachedJob, jobPluginInfo, object);
      }
    }, index, model, storage, liteList);
  }

  private void processDisposalConfirmation(IndexService index, ModelService model, StorageService storage,
    Report report, Job cachedJob, JobPluginInfo jobPluginInfo, DisposalConfirmation disposalConfirmationReport) {

    // iterate over the AIP list
    // copy the AIP using rsync to the disposal bin and another place
    // mark the AIP as destroyed
    // add executedOn and executedBy on the AIP
    // Apply stylesheet over the descriptive metadata
    // Remove representations
    // add preservation event
    // copy the preservation event using rsync to the disposal bin

    // IF any of this steps fails for any AIP recover the original AIP and mark
    // every AIP as failed in the disposal confirmation

    String disposalConfirmationId = disposalConfirmationReport.getId();
    String outcomeText;
    PluginState state = PluginState.SUCCESS;
    Date executionDate = new Date();

    try {
      StoragePath disposalConfirmationAIPsPath = ModelUtils.getDisposalConfirmationAIPsPath(disposalConfirmationId);
      Binary binary = storage.getBinary(disposalConfirmationAIPsPath);

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(binary.getContent().createInputStream()))) {
        jobPluginInfo.setSourceObjectsCount(disposalConfirmationReport.getNumberOfAIPs().intValue());
        // Iterate over the AIP
        while (reader.ready()) {
          String aipEntryJson = reader.readLine();

          try {
            processAIP(aipEntryJson, disposalConfirmationReport, executionDate, model, cachedJob);
          } catch (CommandException e) {
            e.printStackTrace();
          }

        }

        disposalConfirmationReport.setExecutedOn(executionDate);
        disposalConfirmationReport.setExecutedBy(cachedJob.getUsername());
        disposalConfirmationReport.setState(DisposalConfirmationState.APPROVED);

        model.updateDisposalConfirmation(disposalConfirmationReport);

      } catch (IOException e) {
        LOGGER.error("Error destroying intellectual entities of disposal confirmation '{}' ({}): {}",
          disposalConfirmationReport.getTitle(), disposalConfirmationId, e.getMessage(), e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        report.setPluginState(PluginState.FAILURE).setPluginDetails(
          "Error destroying intellectual entities on disposal confirmation '" + disposalConfirmationReport.getTitle()
            + "' (" + disposalConfirmationReport.getId() + "): " + e.getMessage());
      }
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Error destroying intellectual entities of disposal confirmation '{}' ({}): {}",
        disposalConfirmationReport.getTitle(), disposalConfirmationId, e.getMessage(), e);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      report.setPluginState(PluginState.FAILURE).setPluginDetails(
        "Error destroying intellectual entities on disposal confirmation '" + disposalConfirmationReport.getTitle()
          + "' (" + disposalConfirmationReport.getId() + "): " + e.getMessage());
    }
  }

  private void processAIP(String aipEntryJson, DisposalConfirmation disposalConfirmation, Date executionDate,
    ModelService model, Job cachedJob) throws GenericException, AuthorizationDeniedException, NotFoundException,
    RequestNotValidException, CommandException, IOException {

    DisposalConfirmationAIPEntry aipEntry = JsonUtils.getObjectFromJson(aipEntryJson,
      DisposalConfirmationAIPEntry.class);
    AIP aip = model.retrieveAIP(aipEntry.getAipId());

    Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);

    copyAIP(aip, disposalConfirmation.getId(), Collections.singletonList("-r"));

    aip.setState(AIPState.DESTROYED);
    aip.setDestroyedOn(executionDate);
    aip.setDestroyedBy(cachedJob.getUsername());

    // Stylesheet
    for (DescriptiveMetadata metadata : aip.getDescriptiveMetadata()) {
      Binary binary = model.retrieveDescriptiveMetadataBinary(aip.getId(), metadata.getId());

      StoragePath descriptiveMetadataStoragePath = ModelUtils.getDescriptiveMetadataStoragePath(metadata);
      Path descriptiveMetadataPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(),
        descriptiveMetadataStoragePath);

      Reader reader = RodaUtils.applyMetadataStylesheet(binary, RodaConstants.CROSSWALKS_DISSEMINATION_HTML_PATH,
        metadata.getType(), metadata.getVersion(), Collections.emptyMap());

      ReaderInputStream readerInputStream = new ReaderInputStream(reader, StandardCharsets.UTF_8);

      FileUtils.copyInputStreamToFile(readerInputStream, descriptiveMetadataPath.toFile());
    }

    // Representation
    for (Representation representation : aip.getRepresentations()) {
      model.deleteRepresentation(aip.getId(), representation.getId());
    }
    aip.getRepresentations().clear();

    model.destroyAIP(aip, cachedJob.getUsername());

    String outcomeText = "Archival Information Package [id: " + aip.getId()
      + "] has been destroyed with disposal confirmation '" + disposalConfirmation.getTitle() + "' ("
      + disposalConfirmation.getId() + ")";

    model.createEvent(aip.getId(), null, null, null, RodaConstants.PreservationEventType.DESTRUCTION, EVENT_DESCRIPTION,
      null, null, PluginState.SUCCESS, outcomeText, "", cachedJob.getUsername(), true);

    copyAIP(aip, disposalConfirmation.getId(), Arrays.asList("-r", "--ignore-existing"));
  }

  private void copyAIP(AIP aip, String disposalConfirmationId, List<String> rsyncOptions)
    throws RequestNotValidException, GenericException, CommandException {
    // Make a copy to disposal bin before update
    StoragePath aipStoragePath = ModelUtils.getAIPStoragePath(aip.getId());
    Path aipPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), aipStoragePath);

    // disposal-bin/<disposalConfirmationId>/aip/<aipId>
    Path disposalBinPath = RodaCoreFactory.getDisposalBinDirectoryPath().resolve(disposalConfirmationId)
      .resolve(RodaConstants.CORE_AIP_FOLDER).resolve(aipStoragePath.getName());

    RsyncUtils.executeRsync(aipPath, disposalBinPath, rsyncOptions);
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
