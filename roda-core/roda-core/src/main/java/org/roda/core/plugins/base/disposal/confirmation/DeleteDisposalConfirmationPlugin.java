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
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationAIPEntry;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DeleteDisposalConfirmationPlugin extends AbstractPlugin<DisposalConfirmation> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDisposalConfirmationPlugin.class);
  private static final String EVENT_DESCRIPTION = "Disposal confirmation withdraw";
  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DETAILS, "Event details", PluginParameter.PluginParameterType.STRING)
        .withDefaultValue("").isMandatory(false).isReadOnly(false)
        .withDescription("Details that will be used when creating event").build());
  }

  private String details;

  public static String getStaticName() {
    return "Delete disposal confirmation report";
  }

  public static String getStaticDescription() {
    return "";
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
  public Report beforeAllExecute(IndexService index, ModelService model) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, List<LiteOptionalWithCause> liteList)
    throws PluginException {
    return PluginHelper.processObjects(this,
      (RODAObjectProcessingLogic<DisposalConfirmation>) (indexService, modelService, report, cachedJob, jobPluginInfo,
        plugin, object) -> processDisposalConfirmation(modelService, report, jobPluginInfo, cachedJob, object),
      index, model, liteList);
  }

  private void processDisposalConfirmation(ModelService model, Report report, JobPluginInfo jobPluginInfo,
    Job cachedJob, DisposalConfirmation confirmation) {
    String disposalConfirmationId = confirmation.getId();

    LOGGER.debug("Processing disposal confirmation {}", confirmation.getId());
    jobPluginInfo.setSourceObjectsCount(confirmation.getNumberOfAIPs().intValue());

    String outcomeText;
    try {
      Binary binary = model.getBinary(confirmation,
        RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_AIPS_FILENAME);

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(binary.getContent().createInputStream()))) {
        while (reader.ready()) {
          String aipEntryJson = reader.readLine();
          DisposalConfirmationAIPEntry aipEntry = JsonUtils.getObjectFromJson(aipEntryJson,
            DisposalConfirmationAIPEntry.class);

          String aipId = aipEntry.getAipId();
          AIP aip = model.retrieveAIP(aipId);

          processAIP(aip, model, report, jobPluginInfo, cachedJob, confirmation);
        }
      }

      // Delete folder and notify
      model.deleteDisposalConfirmation(disposalConfirmationId);

      outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationEvent("was successfully deleted",
        confirmation.getTitle(), disposalConfirmationId);
    } catch (RequestNotValidException | AuthorizationDeniedException | GenericException | NotFoundException
      | IOException | IllegalOperationException e) {
      LOGGER.error("Error deleting disposal confirmation {}: {}", disposalConfirmationId, e.getMessage(), e);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      report.setPluginState(PluginState.FAILURE)
        .setPluginDetails("Error deleting disposal confirmation " + disposalConfirmationId + ": " + e.getMessage());
      outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationEvent("failed to delete",
        confirmation.getTitle(), disposalConfirmationId);
    }

    model.createRepositoryEvent(RodaConstants.PreservationEventType.DELETION, getPreservationEventDescription(),
      report.getPluginState(), outcomeText, details, cachedJob.getUsername(), true, null);
  }

  private void processAIP(AIP aip, ModelService model, Report report, JobPluginInfo jobPluginInfo, Job cachedJob,
    DisposalConfirmation disposalConfirmationReport) {
    aip.getDisposal().setConfirmation(null);
    PluginState state = PluginState.SUCCESS;
    Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
    try {
      model.updateAIP(aip, cachedJob.getUsername());
      reportItem.setPluginState(state)
        .setPluginDetails("AIP '" + aip.getId() + "' was successfully withdraw from the disposal confirmation '"
          + disposalConfirmationReport.getTitle() + "' (" + disposalConfirmationReport.getId() + ")");
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem
        .addPluginDetails("Could not withdraw AIP '" + aip.getId() + "' from the disposal confirmation '"
          + disposalConfirmationReport.getTitle() + "' (" + disposalConfirmationReport.getId() + "): " + e.getMessage())
        .setPluginState(state);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
    }

    String outcomeText;

    if (state.equals(PluginState.SUCCESS)) {
      outcomeText = "AIP '" + aip.getId() + "' was been withdrawn from disposal confirmation '"
        + disposalConfirmationReport.getTitle() + "' (" + disposalConfirmationReport.getId() + ")";
    } else {
      outcomeText = "AIP '" + aip.getId() + "' was not been withdrawn from disposal confirmation '"
        + disposalConfirmationReport.getTitle() + "' (" + disposalConfirmationReport.getId() + ")";
    }

    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);

    model.createUpdateAIPEvent(aip.getId(), null, null, null, RodaConstants.PreservationEventType.UPDATE,
      EVENT_DESCRIPTION, state, outcomeText, details, cachedJob.getUsername(), true, null);
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
