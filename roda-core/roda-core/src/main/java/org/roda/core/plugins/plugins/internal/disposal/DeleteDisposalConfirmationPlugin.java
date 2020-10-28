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
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationAIPEntry;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationMetadata;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationState;
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
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DeleteDisposalConfirmationPlugin extends AbstractPlugin<Void> {

  private String disposalConfirmationId;
  private String details;

  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_ID, "Disposal confirmation id",
        PluginParameter.PluginParameterType.STRING, "", true, false, "Disposal confirmation identifier"));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DETAILS, "Event details",
        PluginParameter.PluginParameterType.STRING, "", false, false, "Details that will be used when creating event"));
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_ID));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_ID)) {
      disposalConfirmationId = parameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_ID);
    }

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
  public List<Class<Void>> getObjectClasses() {
    return Collections.singletonList(Void.class);
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
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) {
        processDisposalConfirmation(model, storage, report, jobPluginInfo, cachedJob);
      }
    }, index, model, storage);
  }

  private void processDisposalConfirmation(ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob) {

    try {
      // Verify this disposal schedule can be deleted (PENDING state)
      try {
        DisposalConfirmationMetadata confirmationMetadata = model.retrieveDisposalConfirmationMetadata(disposalConfirmationId);
        if (!DisposalConfirmationState.PENDING.equals(confirmationMetadata.getState())) {
            throw new GenericException();
        }
      } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
        throw new GenericException(e);
      }

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
          aip.setDisposalConfirmationId(null);
          model.updateAIP(aip, cachedJob.getUsername());
        }

        // Delete folder and notify
        try {
          model.deleteDisposalConfirmation(disposalConfirmationId);
        } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
            | IllegalOperationException e) {
          e.printStackTrace();
        }

      } catch (RequestNotValidException | AuthorizationDeniedException | GenericException | NotFoundException
          | IOException e) {
        e.printStackTrace();
      }
    } catch (GenericException e) {
      report.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Failed to retrieve disposal confirmation " + disposalConfirmationId);
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
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public Plugin<Void> cloneMe() {
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
