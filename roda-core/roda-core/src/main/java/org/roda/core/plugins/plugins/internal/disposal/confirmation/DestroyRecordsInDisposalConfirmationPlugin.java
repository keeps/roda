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
import org.roda.core.data.exceptions.AlreadyExistsException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DestroyRecordsInDisposalConfirmationPlugin extends AbstractPlugin<DisposalConfirmation> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DestroyRecordsInDisposalConfirmationPlugin.class);

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
    return RodaConstants.PreservationEventType.DELETION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Create disposal confirmation report";
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

    String disposalConfirmationId = disposalConfirmationReport.getId();
    String outcomeText;
    try {
      StoragePath disposalConfirmationAIPsPath = ModelUtils.getDisposalConfirmationAIPsPath(disposalConfirmationId);
      Binary binary = storage.getBinary(disposalConfirmationAIPsPath);

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(binary.getContent().createInputStream()))) {
        // Iterate over the AIP
        while (reader.ready()) {
          String aipEntryJson = reader.readLine();
          DisposalConfirmationAIPEntry aipEntry = JsonUtils.getObjectFromJson(aipEntryJson,
            DisposalConfirmationAIPEntry.class);

          String aipId = aipEntry.getAipId();

          // Make a copy to disposal bin before update
          StoragePath aipStoragePath = ModelUtils.getAIPStoragePath(aipId);
          Path aipPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), aipStoragePath);

          // disposal-bin/<disposalConfirmationId>/<aipId>
          Path disposalBinPath = RodaCoreFactory.getDisposalBinDirectoryPath().resolve(disposalConfirmationId)
            .resolve(aipStoragePath.getName());

          FSUtils.copy(aipPath, disposalBinPath, false);

          AIP aip = model.retrieveAIP(aipId);
          aip.setDestroyedOn(disposalConfirmationReport.getExecutedOn());
          aip.setDestroyedBy(disposalConfirmationReport.getExecutedBy());

          aip.getDescriptiveMetadata().removeIf(p -> p.getType().equals("ead"));

          // Mark the AIP as residual
          aip.setState(AIPState.RESIDUAL);

          model.updateAIP(aip, cachedJob.getUsername());

          // Apply a stylesheet over the aip metadata


          // add preservation event

          // remove the AIP files according to the configuration


        }
      }

      // Mark disposal confirmation as APPROVED
      disposalConfirmationReport.setState(DisposalConfirmationState.APPROVED);
      disposalConfirmationReport.setExecutedBy(cachedJob.getUsername());
      disposalConfirmationReport.setExecutedOn(new Date());
      model.updateDisposalConfirmation(disposalConfirmationReport);

      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    } catch (IOException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | NotFoundException | AlreadyExistsException e) {
      LOGGER.error("Error destroying intellectual entities of disposal confirmation {}: {}", disposalConfirmationId,
        e.getMessage(), e);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      report.setPluginState(PluginState.FAILURE)
        .setPluginDetails("Error destroying intellectual entities of disposal confirmation " + disposalConfirmationId
          + ": " + e.getMessage());
      // outcomeText =
      // PluginHelper.createOutcomeTextForDisposalConfirmationEvent("failed to
      // delete",
      // disposalConfirmationId);
    }
  }

  private void processAIP() {

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
    return new DestroyRecordsInDisposalConfirmationPlugin();
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
