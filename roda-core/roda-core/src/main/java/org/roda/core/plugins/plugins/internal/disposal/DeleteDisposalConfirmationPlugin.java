package org.roda.core.plugins.plugins.internal.disposal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationAIPEntry;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DeleteDisposalConfirmationPlugin extends AbstractPlugin<DisposalConfirmationMetadata> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDisposalConfirmationPlugin.class);
  private static final String EVENT_DESCRIPTION = "The process of updating an object of the repository";

  private String details;

  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DETAILS, "Event details",
        PluginParameter.PluginParameterType.STRING, "", false, false, "Details that will be used when creating event"));
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DETAILS)) {
      details = parameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS);
    }
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "Delete disposal confirmation";
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
    return "Remove disposal confirmation";
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
  public String getPreservationEventSkippedMessage() {
    return "";
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public List<Class<DisposalConfirmationMetadata>> getObjectClasses() {
    return Collections.singletonList(DisposalConfirmationMetadata.class);
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
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<DisposalConfirmationMetadata>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<DisposalConfirmationMetadata> plugin, DisposalConfirmationMetadata object) {
        processDisposalConfirmation(index, model, storage, report, jobPluginInfo, cachedJob, object);

      }
    }, index, model, storage, liteList);
  }

  private void processDisposalConfirmation(IndexService index, ModelService model, StorageService storage,
    Report report, JobPluginInfo jobPluginInfo, Job cachedJob, DisposalConfirmationMetadata confirmationMetadata) {
    String disposalConfirmationId = confirmationMetadata.getId();

    LOGGER.debug("Processing disposal confirmation {}", confirmationMetadata.getId());

    String outcomeText;
    try {
      StoragePath disposalConfirmationAIPsPath = ModelUtils.getDisposalConfirmationAIPsPath(disposalConfirmationId);
      Binary binary = storage.getBinary(disposalConfirmationAIPsPath);

      BufferedReader reader = new BufferedReader(new InputStreamReader(binary.getContent().createInputStream()));

      while (reader.ready()) {
        String aipEntryJson = reader.readLine();
        DisposalConfirmationAIPEntry aipEntry = JsonUtils.getObjectFromJson(aipEntryJson,
          DisposalConfirmationAIPEntry.class);

        String aipId = aipEntry.getAipId();
        AIP aip = model.retrieveAIP(aipId);

        processAIP(aip, model, report, cachedJob, disposalConfirmationId);
      }

      // Delete folder and notify
      model.deleteDisposalConfirmation(disposalConfirmationId);

      report.setPluginState(PluginState.SUCCESS);
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
      outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationEvent("was successfully deleted",
        disposalConfirmationId);
    } catch (RequestNotValidException | AuthorizationDeniedException | GenericException | NotFoundException
      | IOException | IllegalOperationException e) {
      LOGGER.error("Error deleting disposal confirmation {}: {}", disposalConfirmationId, e.getMessage(), e);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      report.setPluginState(PluginState.FAILURE)
        .setPluginDetails("Error deleting disposal confirmation " + disposalConfirmationId + ": " + e.getMessage());
      outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationEvent("failed to delete",
        disposalConfirmationId);
    }

    model.createRepositoryEvent(RodaConstants.PreservationEventType.DELETION, getPreservationEventDescription(),
      report.getPluginState(), outcomeText, details, cachedJob.getUsername(), true);
  }

  private void processAIP(AIP aip, ModelService model, Report report, Job cachedJob, String disposalConfirmationId) {
    aip.setDisposalConfirmationId(null);
    PluginState state = PluginState.SUCCESS;

    try {
      model.updateAIP(aip, cachedJob.getUsername());
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);
      reportItem.addPluginDetails("Could not remove the disposal confirmation from AIP: " + e.getMessage())
        .setPluginState(state);
      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
    }

    String outcomeText;

    if (state.equals(PluginState.SUCCESS)) {
      outcomeText = "Archival Information Package [id: " + aip.getId()
        + "] has been disassociate from disposal confirmation '" + disposalConfirmationId + "'";
    } else {
      outcomeText = "Archival Information Package [id: " + aip.getId()
        + "] has not been disassociate from disposal confirmation '" + disposalConfirmationId + "'";
    }

    model.createUpdateAIPEvent(aip.getId(), null, null, null, RodaConstants.PreservationEventType.UPDATE,
      EVENT_DESCRIPTION, state, outcomeText, details, cachedJob.getUsername(), true);
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
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public Plugin<DisposalConfirmationMetadata> cloneMe() {
    return new DeleteDisposalConfirmationPlugin();
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
