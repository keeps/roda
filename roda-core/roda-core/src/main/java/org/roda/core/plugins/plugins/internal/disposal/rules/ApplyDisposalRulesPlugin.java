package org.roda.core.plugins.plugins.internal.disposal.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPDisposalScheduleAssociationType;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalRules;
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
public class ApplyDisposalRulesPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApplyDisposalRulesPlugin.class);

  private boolean overrideManualAssociations = false;

  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_OVERWRITE_MANUAL,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_OVERWRITE_MANUAL, "Override disposal schedule",
        PluginParameter.PluginParameterType.BOOLEAN, "false", true, false,
        "Overrides disposal schedules manually associated"));
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_OVERWRITE_MANUAL));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_OVERWRITE_MANUAL)) {
      overrideManualAssociations = Boolean
        .parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_OVERWRITE_MANUAL));
    }
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "Disposal schedule association via disposal rule";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Associates a disposal schedule to an AIP via rules previously defined for the repository.";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.POLICY_ASSIGNMENT;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Apply the disposal rules to AIPs in the repository";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Disposal schedule successfully associated to AIP via disposal rule";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to associate disposal schedule to AIP via disposal rule";
  }

  @Override
  public String getPreservationEventSkippedMessage() {
    return "Skipped disposal schedule association to AIP via disposal rule";
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
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<AIP> plugin, List<AIP> objects) {
        processAIP(objects, index, model, report, cachedJob, jobPluginInfo);
      }
    }, index, model, storage, liteList);
  }

  private void processAIP(List<AIP> aips, IndexService index, ModelService model, Report report, Job cachedJob,
    JobPluginInfo jobPluginInfo) {
    try {
      DisposalRules disposalRules = model.listDisposalRules();
      Collections.sort(disposalRules.getObjects());

      if (disposalRules.getObjects().isEmpty()) {
        processEmptyDisposalRules(aips, model, report, cachedJob, jobPluginInfo);
      } else {
        processDisposalRules(aips, disposalRules, index, model, report, cachedJob, jobPluginInfo);
      }

    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException e) {
      LOGGER.error("Failed to obtain disposal rules: {}", e.getMessage(), e);
      for (AIP aip : aips) {
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
        PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails("Failure to obtain disposal rules");
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
      }
    }
  }

  private void processEmptyDisposalRules(List<AIP> aips, ModelService model, Report report, Job cachedJob,
    JobPluginInfo jobPluginInfo) {
    for (AIP aip : aips) {
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
      reportItem.setPluginState(PluginState.SKIPPED).setPluginDetails("Skipping associating disposal schedule to AIP '"
        + aip.getId() + "' due to no disposal rule being present in the repository");
      jobPluginInfo.incrementObjectsProcessedWithSkipped();
      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
    }
  }

  private void processDisposalRules(List<AIP> aips, DisposalRules disposalRules, IndexService index, ModelService model,
    Report report, Job cachedJob, JobPluginInfo jobPluginInfo) {
    for (AIP aip : aips) {
      LOGGER.debug("Processing AIP: {}", aip.getId());

      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
      PluginState state = PluginState.SUCCESS;
      Optional<DisposalRule> disposalRuleUsed;
      String outcomeDetailsText = "";

      try {
        // Check if manual associated schedules are to be overridden
        if (AIPDisposalScheduleAssociationType.MANUAL.equals(aip.getDisposalScheduleAssociationType())
          && !overrideManualAssociations) {
          state = PluginState.SKIPPED;
          outcomeDetailsText = "Skipping associating disposal schedule to AIP '" + aip.getId()
            + "' because the disposal schedule was manually associated";
          reportItem.setPluginState(state).setPluginDetails(outcomeDetailsText);
          jobPluginInfo.incrementObjectsProcessedWithSkipped();
        } else {
          disposalRuleUsed = ApplyDisposalRulesPluginUtils.applyRule(aip, disposalRules, index);

          if (disposalRuleUsed.isPresent()) {
            // Schedule was applied
            try {
              model.updateAIP(aip, cachedJob.getUsername());
              outcomeDetailsText = "Disposal schedule '" + disposalRuleUsed.get().getDisposalScheduleName()
                + "' was successfully associated to AIP '" + aip.getId() + "' via disposal rule '"
                + disposalRuleUsed.get().getTitle() + "'";
              reportItem.setPluginState(state).setPluginDetails(outcomeDetailsText);
              jobPluginInfo.incrementObjectsProcessedWithSuccess();
            } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
              LOGGER.error("Failed to associate disposal schedule to AIP {}: {}", aip.getId(), e.getMessage(), e);
              state = PluginState.FAILURE;
              reportItem.setPluginState(state).setPluginDetails(
                "Failed to associate disposal schedule to AIP '" + aip.getId() + "': '" + e.getMessage() + "'");
              jobPluginInfo.incrementObjectsProcessedWithFailure();
            }
          } else {
            state = PluginState.SKIPPED;
            outcomeDetailsText = "The AIP '" + aip.getId()
              + "' did not match any disposal rule therefore the disposal schedule association was skipped";
            reportItem.setPluginState(state).setPluginDetails(outcomeDetailsText);
            jobPluginInfo.incrementObjectsProcessedWithSkipped();
          }

        }
      } catch (GenericException | NotFoundException e) {
        LOGGER.error("Failed to obtain index version of AIP '{}'", aip.getId(), e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        state = PluginState.FAILURE;
        reportItem.setPluginState(state)
          .setPluginDetails("Error matching disposal rules to AIP '" + aip.getId() + "': " + e.getMessage());
      }

      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);

      try {
        PluginHelper.createPluginEvent(this, aip.getId(), model, index, null, null, state, outcomeDetailsText, true,
          cachedJob);
      } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error creating event: {}", e.getMessage(), e);
      }
    }
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return new Report();
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
    return new ApplyDisposalRulesPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }
}
