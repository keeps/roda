package org.roda.core.plugins.plugins.internal.disposal.confirmation;

import static org.roda.core.data.common.RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE;
import static org.roda.core.data.common.RodaConstants.PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_EXTRA_INFO;
import static org.roda.core.data.common.RodaConstants.PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_TITLE;
import static org.roda.core.data.common.RodaConstants.PreservationEventType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DestroyedSelectionState;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationAIPEntry;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalConfirmationAIPMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CreateDisposalConfirmationPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateDisposalConfirmationPlugin.class);
  private static final String EVENT_DESCRIPTION = "Disposal confirmation assign";

  private final Set<String> disposalSchedules = new HashSet<>();
  private final Set<String> disposalHolds = new HashSet<>();
  private long storageSize = 0L;
  private int aipCounter = 0;

  private int total = 0;

  private String title;
  private Map<String, String> extraInformation;

  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_TITLE,
      new PluginParameter(PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_TITLE, "Disposal confirmation title",
        PluginParameter.PluginParameterType.STRING, "", true, false, "Disposal confirmation report title"));
    pluginParameters.put(PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_EXTRA_INFO,
      new PluginParameter(PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_EXTRA_INFO, "Disposal confirmation information",
        PluginParameter.PluginParameterType.STRING, "", true, false, "Disposal confirmation information"));
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_TITLE));
    parameters.add(pluginParameters.get(PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_EXTRA_INFO));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_TITLE)) {
      title = parameters.get(PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_TITLE);
    }

    if (parameters.containsKey(PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_EXTRA_INFO)) {
      String extraInfoString = parameters.get(PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_EXTRA_INFO);
      extraInformation = JsonUtils.getMapFromJson(extraInfoString);
    }
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "Create disposal confirmation report";
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
    return PreservationEventType.CREATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Create disposal confirmation report";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Disposal confirmation report was successfully created";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Disposal confirmation report failed to be created";
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
      (RODAObjectsProcessingLogic<AIP>) (indexService, modelService, storageService, report, cachedJob, jobPluginInfo,
        plugin, objects) -> processAIP(modelService, indexService, report, jobPluginInfo, cachedJob, objects),
      index, model, storage, liteList);
  }

  private void processAIP(ModelService model, IndexService index, Report report, JobPluginInfo jobPluginInfo,
    Job cachedJob, List<AIP> aips) {

    String confirmationId = IdUtils.createUUID();

    for (AIP aip : aips) {
      boolean processChildren = true;
      PluginState state = PluginState.SUCCESS;
      String outcomeText;
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
      LOGGER.debug("Processing AIP {}", aip.getId());

      // Validates if the AIP can be assigned to a disposal confirmation
      if (StringUtils.isNotBlank(aip.getDisposalConfirmationId())) {
        LOGGER.error(
          "Error creating disposal confirmation {}: AIP '{}' marked as already assigned to a disposal confirmation '{}'",
          confirmationId, aip.getId(), aip.getDisposalConfirmationId());
        state = PluginState.FAILURE;
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(state)
          .setPluginDetails("AIP '" + aip.getId() + "' is already assigned to a disposal confirmation '" + title + "' ("
            + aip.getDisposalConfirmationId() + ")");
        outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationCreation(
          "failed to be assigned to disposal confirmation", title, aip.getId());
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        processChildren = false;
      } else if (aip.onHold()) {
        LOGGER.error(
          "Failed to assign AIP '{}' to disposal confirmation {} because AIP is currently with one or more holds applied",
          confirmationId, aip.getId());
        state = PluginState.FAILURE;
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(state)
          .setPluginDetails("AIP '" + aip.getId() + "' is currently with one or more holds applied");
        outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationCreation(
          "failed to be assigned to disposal confirmation", title, aip.getId());
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        processChildren = false;
      } else {
        try {
          // Fetch the AIP information to crystallize in the confirmation report
          DisposalConfirmationAIPEntry entry = DisposalConfirmationPluginUtils.getAIPEntryFromAIP(index, aip,
            DestroyedSelectionState.DIRECT, disposalSchedules, disposalHolds);
          model.addAIPEntry(confirmationId, entry);

          // Mark the AIP as "on confirmation" so they cannot be added to another
          // disposal confirmation
          DisposalConfirmationAIPMetadata disposalConfirmationAIPMetadata = new DisposalConfirmationAIPMetadata();
          aip.getDisposal().setConfirmation(disposalConfirmationAIPMetadata);
          disposalConfirmationAIPMetadata.setId(confirmationId);
          model.updateAIP(aip, cachedJob.getUsername());

          // increment the storage size
          storageSize += entry.getAipSize();

          jobPluginInfo.incrementObjectsProcessedWithSuccess();
          reportItem.setPluginState(state)
            .setPluginDetails("AIP '" + aip.getId() + "' was successfully assigned to disposal confirmation '" + title
              + "' (" + disposalConfirmationAIPMetadata.getId() + ")");

          outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationCreation(
            "was successfully assign to disposal confirmation", confirmationId, aip.getId());

          aipCounter++;
        } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
          LOGGER.error("Failed to assign AIP '{}' to disposal confirmation '{}': {}", aip.getId(), confirmationId,
            e.getMessage(), e);
          state = PluginState.FAILURE;
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(state).setPluginDetails("Failed to assign AIP '" + aip.getId()
            + "' to disposal confirmation '" + title + "' (" + confirmationId + "): " + e.getMessage());
          outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationCreation(
            "failed to be assigned to disposal confirmation", title, aip.getId());
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        }
      }

      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);

      model.createUpdateAIPEvent(aip.getId(), null, null, null, RodaConstants.PreservationEventType.UPDATE,
        EVENT_DESCRIPTION, state, outcomeText, "", cachedJob.getUsername(), true);

      if (processChildren) {
        processAIPChildren(aip, confirmationId, index, model, cachedJob);
      }
    }

    // Make disposal schedules as a jsonl
    try {
      for (String disposalScheduleId : disposalSchedules) {
        DisposalSchedule disposalSchedule = model.retrieveDisposalSchedule(disposalScheduleId);
        model.addDisposalScheduleEntry(confirmationId, disposalSchedule);
      }
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Failed to create disposal schedules jsonl file", e);
      report.addPluginDetails("Failed to create jsonl with disposal schedules");
    }

    // Make disposal holds as a jsonl
    try {
      model.createDisposalHoldFileIfNotExists(confirmationId);
      for (String disposalHoldId : disposalHolds) {
        DisposalHold disposalHold = model.retrieveDisposalHold(disposalHoldId);
        model.addDisposalHoldEntry(confirmationId, disposalHold);
      }
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException e) {
      LOGGER.error("Failed to create disposal holds jsonl file", e);
      report.addPluginDetails("Failed to create jsonl with disposal holds");
    }

    // create disposal confirmation report metadata
    try {
      model.createDisposalConfirmation(DisposalConfirmationPluginUtils.getDisposalConfirmation(confirmationId, title,
        storageSize, disposalHolds, disposalSchedules, aipCounter, extraInformation), cachedJob.getUsername());

      model.createRepositoryEvent(getPreservationEventType(), getPreservationEventDescription(), PluginState.SUCCESS,
        getPreservationEventSuccessMessage(), confirmationId, cachedJob.getUsername(), true);
    } catch (RequestNotValidException | NotFoundException | GenericException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      LOGGER.error("Failed to create disposal confirmation metadata file", e);
      report.setPluginState(PluginState.FAILURE)
        .setPluginDetails("Failed to create disposal confirmation metadata file: " + e.getMessage());

      model.createRepositoryEvent(getPreservationEventType(), getPreservationEventDescription(), PluginState.FAILURE,
        getPreservationEventFailureMessage(), confirmationId, cachedJob.getUsername(), true);
    }
  }

  private void processAIPChildren(AIP aipParent, String confirmationId, IndexService index, ModelService model,
    Job cachedJob) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aipParent.getId()));

    try {
      IterableIndexResult<IndexedAIP> children = index.findAll(IndexedAIP.class, filter,
        Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_ID, RodaConstants.AIP_OVERDUE_DATE));

      for (IndexedAIP child : children) {
        processChild(child, confirmationId, index, model, cachedJob);
      }
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Failed to retrieve AIP '{}' children", aipParent.getId());
    }
  }

  private void processChild(IndexedAIP child, String confirmationId, IndexService index, ModelService model,
    Job cachedJob) {
    boolean processChild = true;

    LOGGER.debug("Processing child AIP {}", child.getId());

    PluginState state = PluginState.SUCCESS;
    String outcomeText;

    try {
      AIP aip = model.retrieveAIP(child.getId());

      if (aip.getDisposalScheduleId() != null) {
        DisposalSchedule disposalSchedule = RodaCoreFactory.getDisposalSchedule(aip.getDisposalScheduleId());
        if (disposalSchedule != null && (DisposalActionCode.RETAIN_PERMANENTLY.equals(disposalSchedule.getActionCode())
          || DisposalActionCode.REVIEW.equals(disposalSchedule.getActionCode()))) {
          processChild = false;
        }
      }

      if (child.getOverdueDate() != null && new Date().before(child.getOverdueDate())) {
        processChild = false;
      }

      if (processChild) {
        // Fetch the AIP information to crystallize in the confirmation report
        DisposalConfirmationAIPEntry entry = DisposalConfirmationPluginUtils.getAIPEntryFromAIP(index, aip,
          DestroyedSelectionState.TRANSITIVE, disposalSchedules, disposalHolds);
        model.addAIPEntry(confirmationId, entry);

        // Mark the AIP as "on confirmation" so they cannot be added to another
        // disposal confirmation
        DisposalConfirmationAIPMetadata disposalConfirmationAIPMetadata = new DisposalConfirmationAIPMetadata();
        aip.getDisposal().setConfirmation(disposalConfirmationAIPMetadata);
        disposalConfirmationAIPMetadata.setId(confirmationId);
        model.updateAIP(aip, cachedJob.getUsername());

        // increment the storage size
        storageSize += entry.getAipSize();

        outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationCreation(
          "was successfully assign to disposal confirmation", confirmationId, aip.getId());
        aipCounter++;
      } else {
        state = PluginState.SKIPPED;
        outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationCreation(
          "was skipped from being assign to disposal confirmation due to incompatible disposal schedule",
          confirmationId, aip.getId());
      }
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Failed to assign AIP '{}' to disposal confirmation '{}': {}", child.getId(), confirmationId,
        e.getMessage(), e);
      state = PluginState.FAILURE;
      outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationCreation(
        "failed to be assigned to disposal confirmation", title, child.getId());
    }

    model.createUpdateAIPEvent(child.getId(), null, null, null, RodaConstants.PreservationEventType.UPDATE,
      EVENT_DESCRIPTION, state, outcomeText, "", cachedJob.getUsername(), true);
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
    return Collections.singletonList(PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new CreateDisposalConfirmationPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return StringUtils.isNotEmpty(title);
  }
}
