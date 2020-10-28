package org.roda.core.plugins.plugins.internal.disposal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class AssociateDisposalScheduleToAIPPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AssociateDisposalScheduleToAIPPlugin.class);

  private String disposalScheduleId;

  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_ID, "Disposal schedule id",
        PluginParameter.PluginParameterType.STRING, "", true, false, "Disposal schedule identifier"));
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_ID));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_ID)) {
      disposalScheduleId = parameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_ID);
    }
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "Apply disposal schedule";
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
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.POLICY_ASSIGNMENT;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Apply disposal schedule to AIP";
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
      (RODAObjectsProcessingLogic<AIP>) (index1, model1, storage1, report, cachedJob, jobPluginInfo, plugin,
        objects) -> processAIP(model1, index1, report, jobPluginInfo, cachedJob, objects),
      index, model, storage, liteList);
  }

  private void processAIP(ModelService model, IndexService index, Report report, JobPluginInfo jobPluginInfo,
    Job cachedJob, List<AIP> aips) {
    LOGGER.debug("Applying disposal schedule {}", disposalScheduleId);

    try {
      DisposalSchedule disposalSchedule;

      try {
        disposalSchedule = model.retrieveDisposalSchedule(disposalScheduleId);
      } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
        throw new GenericException(e);
      }

      for (AIP aip : aips) {
        String outcomeText;
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
        PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
        LOGGER.debug("Processing AIP {}", aip.getId());

        aip.setDisposalScheduleId(disposalScheduleId);
        try {
          model.updateAIP(aip, cachedJob.getUsername());
          disposalSchedule.incrementNumberOfAIPsByOne();
          disposalSchedule.setFirstTimeUsed(new Date());
          reportItem.setPluginState(PluginState.SUCCESS);
          reportItem.setPluginDetails("Apply disposal schedule: " + disposalScheduleId);
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
          outcomeText = PluginHelper.createOutcomeTextForDisposalSchedule("was successfully applied to AIP",
            disposalSchedule.getId(), disposalSchedule.getTitle());
        } catch (NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
          LOGGER.error("Error applying disposal schedule {} to {}: {}", disposalScheduleId, aip.getId(), e.getMessage(),
            e);
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(PluginState.FAILURE)
            .setPluginDetails("Error applying disposal schedule " + aip.getId() + ": " + e.getMessage());
          outcomeText = PluginHelper.createOutcomeTextForDisposalSchedule(" failed to be applied to AIP",
            disposalSchedule.getId(), disposalSchedule.getTitle());
        } finally {
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        }

        try {
          PluginHelper.createPluginEvent(this, aip.getId(), model, index, null, null, reportItem.getPluginState(),
            outcomeText, true, cachedJob);
        } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
          | AuthorizationDeniedException | AlreadyExistsException e) {
          LOGGER.error("Error creating event: {}", e.getMessage(), e);
        }
      }

      try {
        model.updateDisposalSchedule(disposalSchedule, cachedJob.getUsername());
      } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException e) {
        report.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Failed to update disposal schedule " + disposalScheduleId);
      }

    } catch (GenericException e) {
      report.setPluginState(PluginState.FAILURE)
        .setPluginDetails("Failed to retrieve disposal schedule " + disposalScheduleId);
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
  public List<Class<AIP>> getObjectClasses() {
    return Collections.singletonList(AIP.class);
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new AssociateDisposalScheduleToAIPPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }
}
