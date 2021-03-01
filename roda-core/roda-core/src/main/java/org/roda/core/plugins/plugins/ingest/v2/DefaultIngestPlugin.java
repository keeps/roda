/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.v2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.SIPInformation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.IngestJobPluginInfo;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.ingest.v2.steps.IngestStep;
import org.roda.core.plugins.plugins.ingest.v2.steps.IngestStepBundle;
import org.roda.core.plugins.plugins.ingest.v2.steps.IngestStepsUtils;
import org.roda.core.plugins.plugins.notifications.JobNotification;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * https://docs.google.com/spreadsheets/d/
 * 1Ncu0My6tf19umSClIA6iXeYlJ4_FP6MygRwFCe0EzyM
 *
 * @author Hélder Silva <hsilva@keep.pt>
 */
public abstract class DefaultIngestPlugin extends AbstractPlugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIngestPlugin.class);

  public static final String START_MESSAGE = "The ingest process has started.";
  public static final PreservationEventType START_TYPE = PreservationEventType.INGEST_START;

  public static final String END_SUCCESS = "The ingest process has successfully ended.";
  public static final String END_FAILURE = "Failed to conclude the ingest process.";
  public static final String END_PARTIAL = "The ingest process ended, however, some optional ingest steps failed.";
  public static final String END_DESCRIPTION = "The ingest process has ended.";
  public static final PreservationEventType END_TYPE = PreservationEventType.INGEST_END;

  protected static final int INITIAL_TOTAL_STEPS = 10;
  protected int totalSteps = INITIAL_TOTAL_STEPS;

  public static final String PLUGIN_CLASS_DIGITAL_SIGNATURE = "org.roda.core.plugins.external.DigitalSignaturePlugin";
  public static final String PLUGIN_CLASS_VERAPDF = "org.roda.core.plugins.external.VeraPDFPlugin";
  public static final String PLUGIN_CLASS_TIKA_FULLTEXT = "org.roda.core.plugins.external.TikaFullTextPlugin";

  public static final String PLUGIN_PARAMS_DO_VERAPDF_CHECK = "parameter.do_verapdf_check";
  public static final String PLUGIN_PARAMS_DO_FEATURE_AND_FULL_TEXT_EXTRACTION = "parameter.do_feature_full_text_extraction";
  public static final String PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION = "parameter.do_digital_signature_validation";

  private String successMessage;
  private String failureMessage;
  private PreservationEventType eventType;
  private String eventDescription;

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    totalSteps = calculateEffectiveTotalSteps();
    getParameterValues().put(RodaConstants.PLUGIN_PARAMS_TOTAL_STEPS, Integer.toString(getTotalSteps()));
    Boolean createSubmission = RodaCoreFactory.getRodaConfiguration()
      .getBoolean("core.ingest.sip2aip.create_submission", false);
    getParameterValues().put(RodaConstants.PLUGIN_PARAMS_CREATE_SUBMISSION, createSubmission.toString());
    getParameterValues().put(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS, getClass().getName());
  }

  public abstract PluginParameter getPluginParameter(String pluginParameterId);

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    LOGGER.debug("Doing nothing in beforeAllExecute");
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<TransferredResource>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<TransferredResource> plugin, List<TransferredResource> objects) {
        processObjects(index, model, storage, report, jobPluginInfo, cachedJob, objects);
      }
    }, index, model, storage, liteList);
  }

  protected void processObjects(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo outerJobPluginInfo, Job cachedJob, List<TransferredResource> resources) {
    try {
      Date startDate = new Date();
      Report pluginReport;

      final IngestJobPluginInfo jobPluginInfo = (IngestJobPluginInfo) outerJobPluginInfo;
      PluginHelper.updateJobInformationAsync(this, jobPluginInfo.setTotalSteps(getTotalSteps()));

      // PluginHelper.createJobReport(this, model, report, cachedJob, resources);

      // 0) process "parent id" and "force parent id" info. (because we might
      // need to fallback to default values)
      String parentId = PluginHelper.getStringFromParameters(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID));
      boolean forceParentId = PluginHelper.getBooleanFromParameters(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID));
      getParameterValues().put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, parentId);
      getParameterValues().put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, forceParentId ? "true" : "false");

      // 1) unpacking & wellformedness check (transform TransferredResource into
      // an AIP)
      pluginReport = transformTransferredResourceIntoAnAIP(index, model, storage, resources);
      IngestStepsUtils.mergeReports(jobPluginInfo, pluginReport);
      final SIPInformation sipInformation = pluginReport.getSipInformation();
      final List<AIP> aips = getAIPsFromReports(model, index, jobPluginInfo);
      jobPluginInfo.updateMetaPluginInformation(report, cachedJob);
      PluginHelper.updateJobReportMetaPluginInformation(this, model, report, cachedJob, jobPluginInfo);
      PluginHelper.updateJobInformationAsync(this, jobPluginInfo.incrementStepsCompletedByOne());

      // this event can only be created after AIPs exist and that's why it is
      // performed here, after transformTransferredResourceIntoAnAIP
      createIngestStartedEvent(model, index, jobPluginInfo, startDate, cachedJob);

      List<IngestStep> steps = getIngestSteps();

      for (IngestStep step : steps) {
        IngestStepBundle bundle = new IngestStepBundle(this, index, model, storage, jobPluginInfo,
          getPluginParameter(step.getParameterName()), getParameterValues(), resources, aips, cachedJob,
          sipInformation);
        step.execute(bundle);
      }

      createIngestEndedEvent(model, index, jobPluginInfo, cachedJob);

      getAfterExecute().ifPresent(e -> e.execute(jobPluginInfo, aips));

      // remove SIP if property set and SIP was not moved to another location
      removeSIPAfterIngestEnded(model, resources, jobPluginInfo, cachedJob);

      // X) final job info update
      jobPluginInfo.updateSourceObjectsProcessed();
      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformationAsync(this, jobPluginInfo);
    } catch (JobException e) {
      // do nothing
    } finally {
      // remove locks if any
      PluginHelper.releaseObjectLock(this);
    }
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    LOGGER.debug("Doing stuff in afterAllExecute");

    try {
      index.commitAIPs();
      PluginHelper.fixParents(index, model, Optional.ofNullable(PluginHelper.getJobId(this)),
        PluginHelper.getSearchScopeFromParameters(this, model), PluginHelper.getJobUsername(this, index));
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Could not fix parents", e);
    }

    try {
      Job job = PluginHelper.getJob(this, model);
      ModelUtils.removeTemporaryResourceShallow(job.getId());
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | NotFoundException
        | IOException e) {
      LOGGER.error("Could not remove temporary file shallow", e);
    }

    return null;
  }

  private Report transformTransferredResourceIntoAnAIP(IndexService index, ModelService model, StorageService storage,
    List<TransferredResource> transferredResources) {
    Report report = null;

    String pluginClassName = getParameterValues().getOrDefault(
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS).getId(),
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS).getDefaultValue());

    Plugin<TransferredResource> plugin = RodaCoreFactory.getPluginManager().getPlugin(pluginClassName,
      TransferredResource.class);
    try {
      plugin.setParameterValues(getParameterValues());
      List<LiteOptionalWithCause> lites = LiteRODAObjectFactory.transformIntoLiteWithCause(model, transferredResources);
      report = plugin.execute(index, model, storage, lites);
    } catch (PluginException | InvalidParameterException e) {
      // TODO handle failure
      LOGGER.error("Error executing plugin to transform transferred resource into AIP", e);
    }

    return report;
  }

  private List<AIP> getAIPsFromReports(ModelService model, IndexService index, IngestJobPluginInfo jobPluginInfo) {
    processReports(model, index, jobPluginInfo);

    List<String> aipIds = jobPluginInfo.getAipIds();
    LOGGER.debug("Getting AIPs from reports: {}", aipIds);

    List<AIP> aips = new ArrayList<>();
    for (String aipId : aipIds) {
      try {
        aips.add(model.retrieveAIP(aipId));
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        LOGGER.error("Error while retrieving AIP from reports", e);
      }
    }

    LOGGER.debug("Done retrieving AIPs from reports");

    jobPluginInfo.updateCounters();
    return aips;
  }

  private void processReports(ModelService model, IndexService index, IngestJobPluginInfo jobPluginInfo) {
    Map<String, Map<String, Report>> reportsFromBeingProcessed = jobPluginInfo.getReportsFromBeingProcessed();
    List<String> transferredResourcesToRemoveFromjobPluginInfo = new ArrayList<>();

    for (Entry<String, Map<String, Report>> reportEntry : reportsFromBeingProcessed.entrySet()) {
      String transferredResourceId = reportEntry.getKey();
      Collection<Report> reports = reportEntry.getValue().values();
      for (Report report : reports) {
        if (report.getPluginState().equals(PluginState.FAILURE)) {
          // 20190329 hsilva: all AIPs from this SIP will be marked as failed
          // and will not continue
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          jobPluginInfo.failOtherTransferredResourceAIPs(model, index, transferredResourceId);
          transferredResourcesToRemoveFromjobPluginInfo.add(transferredResourceId);
          break;
        }
      }
    }

    for (String resourceId : transferredResourcesToRemoveFromjobPluginInfo) {
      jobPluginInfo.remove(resourceId);
    }
  }

  private int calculateEffectiveTotalSteps() {
    int effectiveTotalSteps = getTotalSteps();

    for (IngestStep step : getIngestSteps()) {
      if (!PluginHelper.verifyIfStepShouldBePerformed(this, getPluginParameter(step.getParameterName()))) {
        effectiveTotalSteps--;
      }
    }

    return effectiveTotalSteps;
  }

  private void createIngestEvent(ModelService model, IndexService index, IngestJobPluginInfo jobPluginInfo,
    Date eventDate, Job cachedJob) {
    Map<String, List<String>> aipIdToTransferredResourceId = jobPluginInfo.getAipIdToTransferredResourceIds();
    for (Map.Entry<String, List<String>> entry : aipIdToTransferredResourceId.entrySet()) {
      for (String transferredResourceId : entry.getValue()) {
        try {
          TransferredResource tr = index.retrieve(TransferredResource.class, transferredResourceId,
            Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH));
          final PluginState pluginState = jobPluginInfo.getAllReports().get(transferredResourceId).get(entry.getKey())
            .getPluginState();
          setPreservationSuccessMessage(calculatePreservationSuccessMessageFromPluginState(pluginState));

          PluginHelper.createPluginEvent(this, entry.getKey(), model, index, tr, pluginState, "", true, eventDate,
            cachedJob);
        } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException
          | ValidationException | AlreadyExistsException e) {
          LOGGER.warn("Error creating ingest event", e);
        }
      }
    }
  }

  private String calculatePreservationSuccessMessageFromPluginState(PluginState state) {
    // returns END_SUCCESS message if plugin state is success
    // returns END_PARTIAL message if plugin state is partial_success
    switch (state) {
      case PARTIAL_SUCCESS:
        return END_PARTIAL;
      case SUCCESS:
      default:
        return END_SUCCESS;
    }
  }

  private void createIngestStartedEvent(ModelService model, IndexService index, IngestJobPluginInfo jobPluginInfo,
    Date startDate, Job cachedJob) {
    setPreservationEventType(START_TYPE);
    setPreservationSuccessMessage(START_MESSAGE);
    setPreservationFailureMessage(START_MESSAGE);
    setPreservationEventDescription(START_MESSAGE);
    createIngestEvent(model, index, jobPluginInfo, startDate, cachedJob);
  }

  private void createIngestEndedEvent(ModelService model, IndexService index, IngestJobPluginInfo jobPluginInfo,
    Job cachedJob) {
    setPreservationEventType(END_TYPE);
    setPreservationSuccessMessage(END_SUCCESS);
    setPreservationFailureMessage(END_FAILURE);
    setPreservationEventDescription(END_DESCRIPTION);
    createIngestEvent(model, index, jobPluginInfo, new Date(), cachedJob);
  }

  private void removeSIPAfterIngestEnded(ModelService model, List<TransferredResource> resources,
    IngestJobPluginInfo jobPluginInfo, Job cachedJob) {

    boolean moveSIPs = RodaCoreFactory.getRodaConfiguration()
      .getBoolean(RodaConstants.CORE_TRANSFERRED_RESOURCES_INGEST_MOVE_WHEN_AUTOACCEPT, false);

    if (!moveSIPs && RodaCoreFactory.getRodaConfiguration()
      .getBoolean(RodaConstants.CORE_TRANSFERRED_RESOURCES_DELETE_WHEN_SUCCESSFULLY_INGESTED, false)) {
      PluginHelper.removeSIPs(model, resources, jobPluginInfo, cachedJob);
    }
  }

  @Override
  public PluginType getType() {
    return PluginType.INGEST;
  }

  @Override
  public boolean areParameterValuesValid() {
    boolean areValid = true;
    PluginParameter sipToAipClassPluginParameter = getPluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS);
    String sipToAipClass = getParameterValues().getOrDefault(sipToAipClassPluginParameter.getId(),
      sipToAipClassPluginParameter.getDefaultValue());
    if (StringUtils.isNotBlank(sipToAipClass)) {
      Plugin<TransferredResource> plugin = RodaCoreFactory.getPluginManager().getPlugin(sipToAipClass,
        TransferredResource.class);
      if (plugin == null || plugin.getType() != PluginType.SIP_TO_AIP) {
        areValid = false;
      }
    } else {
      areValid = false;
    }

    return areValid;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return eventType;
  }

  @Override
  public String getPreservationEventDescription() {
    return eventDescription;
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return successMessage;
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return failureMessage;
  }

  public void setPreservationEventType(PreservationEventType t) {
    this.eventType = t;
  }

  public void setPreservationSuccessMessage(String message) {
    this.successMessage = message;
  }

  public void setPreservationFailureMessage(String message) {
    this.failureMessage = message;
  }

  public void setPreservationEventDescription(String description) {
    this.eventDescription = description;
  }

  public int getTotalSteps() {
    return totalSteps;
  }

  public abstract void setTotalSteps();

  public abstract List<IngestStep> getIngestSteps();

  @Override
  public abstract List<JobNotification> getNotifications();

  public abstract Optional<? extends AfterExecute> getAfterExecute();

  @FunctionalInterface
  public interface AfterExecute {
    void execute(IngestJobPluginInfo jobPluginInfo, List<AIP> aips);
  }
}
