/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.disposal.confirmation;

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
import org.roda.core.config.SpringContext;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.disposal.confirmation.DestroyedSelectionState;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.metadata.DisposalConfirmationAIPMetadata;
import org.roda.core.data.v2.disposal.schedule.DisposalActionCode;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.entity.disposal.confirmation.DisposalConfirmationAIPEntry;
import org.roda.core.entity.disposal.confirmation.DisposalConfirmations;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.repository.disposal.DisposalConfirmationAIPEntryRepository;
import org.roda.core.repository.disposal.DisposalConfirmationRepository;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CreateDisposalConfirmationPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateDisposalConfirmationPlugin.class);
  private static final String EVENT_DESCRIPTION = "Disposal confirmation assign";
  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_TITLE,
      PluginParameter
        .getBuilder(PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_TITLE,
          "plugin.createDisposalConfirmationPlugin.parameter.title.name", PluginParameter.PluginParameterType.STRING)
        .withDescription("plugin.createDisposalConfirmationPlugin.parameter.title.description").build());
    pluginParameters.put(PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_EXTRA_INFO,
      PluginParameter
        .getBuilder(PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_EXTRA_INFO,
          "plugin.createDisposalConfirmationPlugin.parameter.info.name", PluginParameter.PluginParameterType.STRING)
        .withDescription("plugin.createDisposalConfirmationPlugin.parameter.info.description").build());
  }

  private final Set<String> disposalSchedules = new HashSet<>();
  private final Set<String> disposalHolds = new HashSet<>();
  private final Set<String> disposalHoldTransitives = new HashSet<>();
  private String title;
  private Map<String, String> extraInformation;

  private DisposalConfirmationAIPEntryRepository disposalConfirmationAIPEntryRepository;
  private DisposalConfirmationRepository disposalConfirmationRepository;

  public static String getStaticName() {
    return "plugin.createDisposalConfirmationPlugin.name";
  }

  public static String getStaticDescription() {
    return "";
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

  @Override
  public String getName() {
    return getStaticName();
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
  public Report beforeAllExecute(IndexService index, ModelService model) throws PluginException {
    // do nothing

    // create a disposal confirmation uuid and store in the database
    getDisposalConfirmationRepository();
    DisposalConfirmations object = new DisposalConfirmations();
    object.setJobId(PluginHelper.getJobId(this));
    object.setDisposalConfirmationId(IdUtils.createUUID());
    disposalConfirmationRepository.save(object);

    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, List<LiteOptionalWithCause> liteList)
    throws PluginException {
    return PluginHelper.processObjects(this,
      (RODAObjectsProcessingLogic<AIP>) (indexService, modelService, report, cachedJob, jobPluginInfo, plugin,
        objects) -> processAIP(modelService, indexService, report, jobPluginInfo, cachedJob, objects),
      index, model, liteList);
  }

  private void processAIP(ModelService model, IndexService index, Report report, JobPluginInfo jobPluginInfo,
    Job cachedJob, List<AIP> aips) {

    IndexedAIP indexedAIP = null;
    getDisposalConfirmationRepository();
    getAIPEntryRepository();

    String disposalConfirmationId = disposalConfirmationRepository.findByJobId(PluginHelper.getJobId(this))
      .getDisposalConfirmationId();

    for (AIP aip : aips) {
      try {
        indexedAIP = index.retrieve(IndexedAIP.class, aip.getId(),
          Collections.singletonList(RodaConstants.AIP_DISPOSAL_HOLD_STATUS));
      } catch (NotFoundException | GenericException e) {
        // do nothing; if it fails to retrieve the IndexedAIP it will prompt an error
        // and caught in the if sequence
      }

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
          disposalConfirmationId, aip.getId(), aip.getDisposalConfirmationId());
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
      } else if (indexedAIP != null && indexedAIP.isOnHold()) {
        LOGGER.error(
          "Failed to assign AIP '{}' to disposal confirmation {} because AIP is currently with one or more holds applied",
          disposalConfirmationId, aip.getId());
        state = PluginState.FAILURE;
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(state)
          .setPluginDetails("AIP '" + aip.getId() + "' is currently with one or more holds applied");
        outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationCreation(
          "failed to be assigned to disposal confirmation", title, aip.getId());
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        processChildren = false;
      } else if (indexedAIP == null) {
        LOGGER.error("Failed to assign AIP '{}' to disposal confirmation {} because retrieving the IndexedAIP failed",
          disposalConfirmationId, aip.getId());
        state = PluginState.FAILURE;
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(state)
          .setPluginDetails("AIP '" + aip.getId() + "' failed to be retrieve from the index");
        outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationCreation(
          "failed to be assigned to disposal confirmation", title, aip.getId());
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        processChildren = false;
      } else {
        try {
          // Fetch the AIP information to crystallize in the confirmation report
          org.roda.core.entity.disposal.confirmation.DisposalConfirmationAIPEntry entry = DisposalConfirmationPluginUtils
            .getAIPEntryFromAIP(index, aip, DestroyedSelectionState.DIRECT, disposalSchedules, disposalHolds,
              disposalHoldTransitives);
          entry.setJobId(PluginHelper.getJobId(this));
          disposalConfirmationAIPEntryRepository.save(entry);
          // model.addAIPEntry(confirmationId, entry);

          // Mark the AIP as "on confirmation" so they cannot be added to another
          // disposal confirmation
          DisposalConfirmationAIPMetadata disposalConfirmationAIPMetadata = new DisposalConfirmationAIPMetadata();
          aip.getDisposal().setConfirmation(disposalConfirmationAIPMetadata);
          disposalConfirmationAIPMetadata.setId(disposalConfirmationId);
          model.updateAIP(aip, cachedJob.getUsername());

          jobPluginInfo.incrementObjectsProcessedWithSuccess();
          reportItem.setPluginState(state)
            .setPluginDetails("AIP '" + aip.getId() + "' was successfully assigned to disposal confirmation '" + title
              + "' (" + disposalConfirmationAIPMetadata.getId() + ")");

          outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationCreation(
            "was successfully assign to disposal confirmation", disposalConfirmationId, aip.getId());
        } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
          LOGGER.error("Failed to assign AIP '{}' to disposal confirmation '{}': {}", aip.getId(),
            disposalConfirmationId, e.getMessage(), e);
          state = PluginState.FAILURE;
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(state).setPluginDetails("Failed to assign AIP '" + aip.getId()
            + "' to disposal confirmation '" + title + "' (" + disposalConfirmationId + "): " + e.getMessage());
          outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationCreation(
            "failed to be assigned to disposal confirmation", title, aip.getId());
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        }
      }

      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);

      model.createUpdateAIPEvent(aip.getId(), null, null, null, RodaConstants.PreservationEventType.UPDATE,
        EVENT_DESCRIPTION, state, outcomeText, "", cachedJob.getUsername(), true, null);

      if (processChildren) {
        processAIPChildren(aip, disposalConfirmationId, index, model, cachedJob);
      }
    }
  }

  private void processAIPChildren(AIP aipParent, String confirmationId, IndexService index, ModelService model,
    Job cachedJob) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aipParent.getId()));

    try {
      IterableIndexResult<IndexedAIP> children = index.findAll(IndexedAIP.class, filter,
        Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_ID, RodaConstants.AIP_OVERDUE_DATE));

      for (IndexedAIP child : children) {
        processChild(child, aipParent.getId(), confirmationId, index, model, cachedJob);
      }
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Failed to retrieve AIP '{}' children", aipParent.getId());
    }
  }

  private void processChild(IndexedAIP child, String topAncestorId, String confirmationId, IndexService index,
    ModelService model, Job cachedJob) {
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
        org.roda.core.entity.disposal.confirmation.DisposalConfirmationAIPEntry entry = DisposalConfirmationPluginUtils
          .getAIPEntryFromAIP(index, aip, topAncestorId, DestroyedSelectionState.TRANSITIVE, disposalSchedules,
            disposalHolds, disposalHoldTransitives);
        entry.setJobId(PluginHelper.getJobId(this));
        disposalConfirmationAIPEntryRepository.save(entry);

        // Mark the AIP as "on confirmation" so they cannot be added to another
        // disposal confirmation
        DisposalConfirmationAIPMetadata disposalConfirmationAIPMetadata = new DisposalConfirmationAIPMetadata();
        aip.getDisposal().setConfirmation(disposalConfirmationAIPMetadata);
        disposalConfirmationAIPMetadata.setId(confirmationId);
        model.updateAIP(aip, cachedJob.getUsername());

        outcomeText = PluginHelper.createOutcomeTextForDisposalConfirmationCreation(
          "was successfully assign to disposal confirmation", confirmationId, aip.getId());
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
      EVENT_DESCRIPTION, state, outcomeText, "", cachedJob.getUsername(), true, null);
  }

  private DisposalConfirmationAIPEntryRepository getAIPEntryRepository() {
    if (disposalConfirmationAIPEntryRepository == null && SpringContext.isContextInitialized()) {
      disposalConfirmationAIPEntryRepository = SpringContext.getBean(DisposalConfirmationAIPEntryRepository.class);
    }
    return disposalConfirmationAIPEntryRepository;
  }

  private DisposalConfirmationRepository getDisposalConfirmationRepository() {
    if (disposalConfirmationRepository == null && SpringContext.isContextInitialized()) {
      disposalConfirmationRepository = SpringContext.getBean(DisposalConfirmationRepository.class);
    }
    return disposalConfirmationRepository;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) throws PluginException {
    // do nothing
    getAIPEntryRepository();
    getDisposalConfirmationRepository();

    String disposalConfirmationId = disposalConfirmationRepository.findByJobId(PluginHelper.getJobId(this))
      .getDisposalConfirmationId();

    try {
      for (String disposalScheduleId : disposalConfirmationAIPEntryRepository
        .findUniqueScheduleIdsByJobId(PluginHelper.getJobId(this))) {
        DisposalSchedule disposalSchedule = model.retrieveDisposalSchedule(disposalScheduleId);
        model.addDisposalScheduleEntry(disposalConfirmationId, disposalSchedule);
      }

      for (String disposalHoldId : disposalConfirmationAIPEntryRepository
        .findDistinctDisposalHoldIdsByJobId(PluginHelper.getJobId(this))) {
        DisposalHold disposalHold = model.retrieveDisposalHold(disposalHoldId);
        model.addDisposalHoldEntry(disposalConfirmationId, disposalHold);
      }

      for (String disposalHoldId : disposalConfirmationAIPEntryRepository
        .findDistinctDisposalHoldTransitiveIdsByJobId(PluginHelper.getJobId(this))) {
        DisposalHold disposalHold = model.retrieveDisposalHold(disposalHoldId);
        model.addDisposalHoldTransitiveEntry(disposalConfirmationId, disposalHold);
      }

      for (DisposalConfirmationAIPEntry entry : disposalConfirmationAIPEntryRepository
        .findByJobId(PluginHelper.getJobId(this))) {
        model.addAIPEntry(disposalConfirmationId,
          DisposalConfirmationPluginUtils.convertToDisposalConfirmationAIPEntry(entry));
      }

      model.createDisposalConfirmation(DisposalConfirmationPluginUtils.getDisposalConfirmation(disposalConfirmationId,
        title, disposalConfirmationAIPEntryRepository.sumAipSizeByJobId(PluginHelper.getJobId(this)),
        new HashSet<>(
          disposalConfirmationAIPEntryRepository.findDistinctDisposalHoldIdsByJobId(PluginHelper.getJobId(this))),
        new HashSet<>(disposalConfirmationAIPEntryRepository.findUniqueScheduleIdsByJobId(PluginHelper.getJobId(this))),
        disposalConfirmationAIPEntryRepository.countByJobId(PluginHelper.getJobId(this)), extraInformation),
        PluginHelper.getJobUsername(this, model));

    } catch (AuthorizationDeniedException | RequestNotValidException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      LOGGER.error("Failed to create disposal confirmation files", e);
      throw new PluginException("Failed to create disposal confirmation files", e);
    }

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
