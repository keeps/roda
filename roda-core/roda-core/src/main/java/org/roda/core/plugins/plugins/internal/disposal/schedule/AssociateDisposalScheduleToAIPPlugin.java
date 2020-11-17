package org.roda.core.plugins.plugins.internal.disposal.schedule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPDisposalScheduleAssociationType;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class AssociateDisposalScheduleToAIPPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AssociateDisposalScheduleToAIPPlugin.class);

  private String disposalScheduleId;
  private static Set<String> previousDisposalSchedules;
  private boolean recursive = true;
  private boolean overwriteAll = false;

  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_ID, "Disposal schedule id",
        PluginParameter.PluginParameterType.STRING, "", true, false, "Disposal schedule identifier"));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RECURSIVE,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_RECURSIVE, "Recursive mode",
        PluginParameter.PluginParameterType.BOOLEAN, "true", true, false, "Associate the schedule to descendants"));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_OVERWRITE_ALL,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_OVERWRITE_ALL, "Overwrite mode",
        PluginParameter.PluginParameterType.BOOLEAN, "false", true, false,
        "Overwrite all descendants disposal schedules."));
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_ID));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RECURSIVE));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_OVERWRITE_ALL));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_ID)) {
      disposalScheduleId = parameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_ID);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RECURSIVE)) {
      recursive = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_RECURSIVE));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_OVERWRITE_ALL)) {
      overwriteAll = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_OVERWRITE_ALL));
    }
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "Associate disposal schedule";
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
    return "Associate disposal schedule to AIP";
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
    LOGGER.debug("Associating disposal schedule {}", disposalScheduleId);
    previousDisposalSchedules = new HashSet<>();
    if (recursive) {
      calculateResourcesCounter(index, jobPluginInfo, aips);
    }

    // Uses cache to retrieve the disposal schedule
    DisposalSchedule disposalSchedule = RodaCoreFactory.getDisposalSchedule(disposalScheduleId);

    if (disposalSchedule == null) {
      LOGGER.error("Failed to retrieve disposal schedule {} from model", disposalScheduleId);
      for (AIP aip : aips) {
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
        PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
        reportItem.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Failed to retrieve disposal schedule " + disposalScheduleId);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
      }
    } else {
      for (AIP aip : aips) {
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
        PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
        PluginState state = PluginState.SUCCESS;
        String outcomeText;
        LOGGER.debug("Processing AIP {}", aip.getId());

        if (aip.getDisposalConfirmationId() != null) {
          state = PluginState.FAILURE;
          LOGGER.error("Error associating disposal schedule to AIP '" + aip.getId()
            + "': This AIP is part of a disposal confirmation report and the schedule cannot be changed");
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(state).setPluginDetails("Error associating disposal schedule to AIP '" + aip.getId()
            + "': This AIP is part of a disposal confirmation report and the schedule cannot be changed");
          outcomeText = PluginHelper.createOutcomeTextForDisposalSchedule(
            "failed to be associated to AIP '" + aip.getId()
              + "'; This AIP is part of a disposal confirmation report and the schedule cannot be changed",
            disposalScheduleId, null);
        } else {
          previousDisposalSchedules.add(aip.getDisposalScheduleId());
          aip.setDisposalScheduleId(disposalScheduleId);
          aip.setScheduleAssociationType(AIPDisposalScheduleAssociationType.MANUAL);
          try {
            model.updateAIP(aip, cachedJob.getUsername());
            disposalSchedule.setFirstTimeUsed(new Date());

            model.updateDisposalSchedule(disposalSchedule, cachedJob.getUsername());

            jobPluginInfo.incrementObjectsProcessedWithSuccess();

            reportItem.setPluginState(state).setPluginDetails("Disposal schedule '" + disposalSchedule.getTitle()
              + "' (" + disposalScheduleId + ") was successfully associated to AIP");

            outcomeText = PluginHelper.createOutcomeTextForDisposalSchedule("was successfully associated to AIP",
              disposalSchedule.getId(), disposalSchedule.getTitle());
            if (recursive) {
              processChildren(model, index, cachedJob, aip, disposalSchedule, jobPluginInfo, report);
            }
          } catch (NotFoundException | RequestNotValidException | AuthorizationDeniedException | GenericException
            | IllegalOperationException e) {
            LOGGER.error("Error associating disposal schedule {} to {}: {}", disposalScheduleId, aip.getId(),
              e.getMessage(), e);
            state = PluginState.FAILURE;
            jobPluginInfo.incrementObjectsProcessedWithFailure();
            reportItem.setPluginState(state)
              .setPluginDetails("Error associating disposal schedule '" + disposalSchedule.getTitle() + "' ("
                + disposalScheduleId + ") to AIP '" + aip.getId() + "': " + e.getMessage());
            outcomeText = PluginHelper.createOutcomeTextForDisposalSchedule(" failed to be associated to AIP",
              disposalSchedule.getId(), disposalSchedule.getTitle());
          }
        }

        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);

        try {
          PluginHelper.createPluginEvent(this, aip.getId(), model, index, null, null, state, outcomeText, true,
            cachedJob);
        } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
          | AuthorizationDeniedException | AlreadyExistsException e) {
          LOGGER.error("Error creating event: {}", e.getMessage(), e);
        }
      }
    }
  }

  private void calculateResourcesCounter(IndexService index, JobPluginInfo jobPluginInfo, List<AIP> aips) {
    try {
      ArrayList<String> ancestorList = aips.stream().map(AIP::getId).collect(Collectors.toCollection(ArrayList::new));
      Filter ancestorFilter;
      if (!overwriteAll) {
        ancestorFilter = new Filter();
        ancestorFilter.add(new OneOfManyFilterParameter(RodaConstants.AIP_ANCESTORS, ancestorList));
        ancestorFilter.add(new EmptyKeyFilterParameter(RodaConstants.AIP_DISPOSAL_SCHEDULE_ID));
      } else {
        ancestorFilter = new Filter(new OneOfManyFilterParameter(RodaConstants.AIP_ANCESTORS, ancestorList));
      }
      int resourceCounter = index.count(IndexedAIP.class, ancestorFilter).intValue();
      jobPluginInfo.setSourceObjectsCount(resourceCounter + aips.size());
    } catch (GenericException | RequestNotValidException e) {
      // do nothing
    }
  }

  private void processChildren(ModelService model, IndexService index, Job cachedJob, AIP aipParent,
    DisposalSchedule disposalSchedule, JobPluginInfo jobPluginInfo, Report report) {
    Filter ancestorFilter;
    if (!overwriteAll) {
      ancestorFilter = new Filter();
      ancestorFilter.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aipParent.getId()));
      ancestorFilter.add(new EmptyKeyFilterParameter(RodaConstants.AIP_DISPOSAL_SCHEDULE_ID));
    } else {
      ancestorFilter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aipParent.getId()));
    }
    try (IterableIndexResult<IndexedAIP> result = index.findAll(IndexedAIP.class, ancestorFilter, false,
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_ID))) {

      for (IndexedAIP indexedAIP : result) {
        String outcomeText;
        Report reportItem = PluginHelper.initPluginReportItem(this, indexedAIP.getId(), AIP.class);
        PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
        LOGGER.debug("Processing children AIP {}", indexedAIP.getId());

        try {
          AIP aipChildren = model.retrieveAIP(indexedAIP.getId());
          reportItem.setPluginState(PluginState.SUCCESS);
          reportItem.setPluginDetails("Apply disposal schedule: " + disposalScheduleId);
          aipChildren.setDisposalScheduleId(aipParent.getDisposalScheduleId());
          aipChildren.setScheduleAssociationType(AIPDisposalScheduleAssociationType.MANUAL);
          model.updateAIP(aipChildren, cachedJob.getUsername());
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
          outcomeText = PluginHelper.createOutcomeTextForDisposalSchedule("was successfully applied to AIP",
            disposalSchedule.getId(), disposalSchedule.getTitle());
        } catch (NotFoundException | AuthorizationDeniedException e) {
          LOGGER.debug("Can't associate disposal schedule to child. It wasn't found.", e);
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(PluginState.FAILURE)
            .setPluginDetails("Error applying disposal schedule " + indexedAIP.getId() + ": " + e.getMessage());
          outcomeText = PluginHelper.createOutcomeTextForDisposalSchedule(" failed to be applied to AIP",
            disposalSchedule.getId(), disposalSchedule.getTitle());
        } finally {
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        }

        try {
          PluginHelper.createPluginEvent(this, indexedAIP.getId(), model, index, null, null,
            reportItem.getPluginState(), outcomeText, true, cachedJob);
        } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
          | AuthorizationDeniedException | AlreadyExistsException e) {
          LOGGER.error("Error creating event: {}", e.getMessage(), e);
        }
      }
    } catch (IOException | GenericException | RequestNotValidException e) {
      LOGGER.error("Error getting children AIPs when associate to a disposal schedule", e);
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
    return new AssociateDisposalScheduleToAIPPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }
}
