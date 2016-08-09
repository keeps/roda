/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.RODA_TYPE;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LinkingObjectUtils;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.IngestJobPluginInfo;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PluginHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(PluginHelper.class);

  private PluginHelper() {
  }

  /***************** Job report related *****************/
  /******************************************************/

  public static <T extends IsRODAObject> Report initPluginReport(Plugin<T> plugin) {
    return initPluginReportItem(plugin, "", "");
  }

  public static <T extends IsRODAObject> Report initPluginReportItem(Plugin<T> plugin,
    TransferredResource transferredResource) {
    return initPluginReportItem(plugin, "", transferredResource.getUUID())
      .setSourceObjectClass(TransferredResource.class.getName()).setOutcomeObjectClass(AIP.class.getName())
      .setOutcomeObjectState(AIPState.INGEST_PROCESSING).setSourceObjectOriginalName(transferredResource.getName());
  }

  public static <T extends IsRODAObject> Report initPluginReportItem(Plugin<T> plugin, String outcomeObjectId,
    AIPState initialOutcomeObjectState) {
    return initPluginReportItem(plugin, outcomeObjectId, "").setOutcomeObjectState(initialOutcomeObjectState);
  }

  public static <T extends IsRODAObject> Report initPluginReportItem(Plugin<T> plugin, String objectId,
    Class<?> clazz) {
    return initPluginReportItem(plugin, objectId, objectId).setSourceObjectClass(clazz.getName())
      .setOutcomeObjectClass(clazz.getName());
  }

  public static <T extends IsRODAObject> Report initPluginReportItem(Plugin<T> plugin, String objectId, Class<?> clazz,
    AIPState initialOutcomeObjectState) {
    return initPluginReportItem(plugin, objectId, objectId).setSourceObjectClass(clazz.getName())
      .setOutcomeObjectClass(clazz.getName()).setOutcomeObjectState(initialOutcomeObjectState);
  }

  public static <T extends IsRODAObject> Report initPluginReportItem(Plugin<T> plugin, String sourceObjectId,
    String outcomeObjectId, Class<?> clazz, AIPState initialOutcomeObjectState) {
    return initPluginReportItem(plugin, outcomeObjectId, sourceObjectId).setSourceObjectClass(clazz.getName())
      .setOutcomeObjectClass(clazz.getName()).setOutcomeObjectState(initialOutcomeObjectState);
  }

  public static <T extends IsRODAObject> Report initPluginReportItem(Plugin<T> plugin, String outcomeObjectId,
    String sourceObjectId) {
    String jobId = getJobId(plugin);
    Report reportItem = new Report();
    reportItem.injectLineSeparator(System.lineSeparator());
    String jobReportPartialId = outcomeObjectId;
    // FIXME 20160516 hsilva: this has problems when doing one to many SIP > AIP
    // operation
    if (jobReportPartialId == null || "".equals(jobReportPartialId)) {
      jobReportPartialId = sourceObjectId;
    }

    reportItem.setId(IdUtils.getJobReportId(jobId, jobReportPartialId));
    reportItem.setJobId(jobId);
    reportItem.setSourceObjectId(sourceObjectId);
    reportItem.setOutcomeObjectId(outcomeObjectId);
    reportItem.setTitle(plugin.getName());
    reportItem.setPlugin(plugin.getClass().getName());
    reportItem.setPluginName(plugin.getName());
    reportItem.setPluginVersion(plugin.getVersion());
    reportItem.setTotalSteps(getTotalStepsFromParameters(plugin));

    return reportItem;
  }

  public static <T extends IsRODAObject> void createJobReport(Plugin<T> plugin, ModelService model, Report reportItem) {
    String jobId = getJobId(plugin);
    Report report = new Report(reportItem);
    report.injectLineSeparator(System.lineSeparator());
    String reportPartialId = reportItem.getOutcomeObjectId();
    // FIXME 20160516 hsilva: this has problems when doing one to many SIP > AIP
    // operation
    if (reportPartialId == null || "".equals(reportPartialId)) {
      reportPartialId = reportItem.getSourceObjectId();
    }
    reportItem.setId(IdUtils.getJobReportId(jobId, reportPartialId));
    report.setId(reportItem.getId());
    report.setJobId(jobId);
    if (reportItem.getTotalSteps() != 0) {
      report.setTotalSteps(reportItem.getTotalSteps());
    } else {
      report.setTotalSteps(getTotalStepsFromParameters(plugin));
    }
    report.addReport(reportItem);

    try {
      model.createOrUpdateJobReport(report);
    } catch (GenericException e) {
      LOGGER.error("Error creating Job Report", e);
    }
  }

  public static <T extends IsRODAObject> void updateJobReportState(Plugin<T> plugin, ModelService model, String aipId,
    AIPState newState) {
    try {
      String jobId = getJobId(plugin);
      Report jobReport = model.retrieveJobReport(jobId, aipId);
      jobReport.setOutcomeObjectState(newState);
      model.createOrUpdateJobReport(jobReport);
    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Error while updating Job Report", e);
    }
  }

  public static <T extends IsRODAObject> void updatePartialJobReport(Plugin<T> plugin, ModelService model,
    IndexService index, Report reportItem, boolean replaceLastReportItemIfTheSame) {
    String jobId = getJobId(plugin);
    try {
      Report jobReport;
      try {
        jobReport = model.retrieveJobReport(jobId, reportItem.getOutcomeObjectId());
      } catch (NotFoundException e) {
        jobReport = initPluginReportItem(plugin, reportItem.getOutcomeObjectId(), reportItem.getSourceObjectId())
          .setSourceObjectClass(reportItem.getSourceObjectClass())
          .setOutcomeObjectClass(reportItem.getOutcomeObjectClass());

        jobReport.setId(reportItem.getOutcomeObjectId());
        jobReport.addReport(reportItem);
      }

      if (!replaceLastReportItemIfTheSame) {
        jobReport.addReport(reportItem);
      } else {
        List<Report> reportItems = jobReport.getReports();
        Report report = reportItems.get(reportItems.size() - 1);
        if (report.getPlugin().equalsIgnoreCase(reportItem.getPlugin())) {
          reportItems.remove(reportItems.size() - 1);
          jobReport.setStepsCompleted(jobReport.getStepsCompleted() - 1);
          jobReport.addReport(reportItem);
        }
      }

      model.createOrUpdateJobReport(jobReport);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Error while updating Job Report", e);
    }
  }

  private static <T extends IsRODAObject> void updateJobReport(ModelService model, Report report) {
    try {
      model.createOrUpdateJobReport(report);
    } catch (GenericException e) {
      LOGGER.error("Error while updating Job Report", e);
    }
  }

  /***************** Job related *****************/
  /***********************************************/
  public static <T extends IsRODAObject> String getJobId(Plugin<T> plugin) {
    return plugin.getParameterValues().get(RodaConstants.PLUGIN_PARAMS_JOB_ID);
  }

  /**
   * 20160329 hsilva: use this method only to get job information that most
   * certainly won't change in time (e.g. username, etc.)
   */
  public static <T extends IsRODAObject> Job getJobFromIndex(Plugin<T> plugin, IndexService index)
    throws NotFoundException, GenericException {
    String jobID = getJobId(plugin);
    if (jobID != null) {
      return index.retrieve(Job.class, jobID);
    } else {
      throw new NotFoundException("Job not found");
    }

  }

  public static <T extends IsRODAObject> Job getJobFromModel(Plugin<T> plugin, ModelService model)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    String jobId = getJobId(plugin);
    if (jobId != null) {
      return model.retrieveJob(jobId);
    } else {
      throw new NotFoundException("Job not found");
    }

  }

  /**
   * Updates the job state
   */
  public static <T extends IsRODAObject> void updateJobState(Plugin<T> plugin, JOB_STATE state,
    Optional<String> stateDetails) {
    RodaCoreFactory.getPluginOrchestrator().updateJobState(plugin, state, stateDetails);
  }

  public static <T extends IsRODAObject> void updateJobState(Plugin<T> plugin, JOB_STATE state, Throwable throwable) {
    updateJobState(plugin, state, Optional.ofNullable(throwable.getClass().getName() + ": " + throwable.getMessage()));
  }

  /**
   * Updates the job status for a particular plugin instance
   */
  public static <T extends IsRODAObject> void updateJobInformation(Plugin<T> plugin, JobPluginInfo jobPluginInfo)
    throws JobException {

    Map<String, String> parameterValues = plugin.getParameterValues();

    if (!parameterValues.containsKey(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS)
      || (parameterValues.containsKey(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS)
        && parameterValues.get(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS).equals(plugin.getClass().getName()))) {
      RodaCoreFactory.getPluginOrchestrator().updateJobInformation(plugin, jobPluginInfo);
    }
  }

  public static <T extends IsRODAObject> SimpleJobPluginInfo getInitialJobInformation(Plugin<T> plugin,
    int sourceObjectsCount) throws JobException {
    SimpleJobPluginInfo jobPluginInfo = plugin.getJobPluginInfo(SimpleJobPluginInfo.class);
    if (jobPluginInfo != null) {
      jobPluginInfo.setSourceObjectsBeingProcessed(sourceObjectsCount).setSourceObjectsWaitingToBeProcessed(0);
      return jobPluginInfo;
    } else {
      return new SimpleJobPluginInfo();
    }
  }

  public static <T extends IsRODAObject, T1 extends JobPluginInfo> T1 getInitialJobInformation(Plugin<T> plugin,
    Class<T1> jobPluginInfoClass) throws JobException {
    T1 jobPluginInfo = plugin.getJobPluginInfo(jobPluginInfoClass);
    if (jobPluginInfo != null) {
      jobPluginInfo.setSourceObjectsBeingProcessed(jobPluginInfo.getSourceObjectsCount())
        .setSourceObjectsWaitingToBeProcessed(0);
    } else {
      throw new JobException("Cannot obtain job plugin info (that supposedly was set by plugin orchestrator)");
    }
    return jobPluginInfo;
  }

  /**
   * 20160531 hsilva:Only orchestrators/orchestrators related classes should
   * invoke this method
   */
  public static <T extends IsRODAObject> void updateJobState(Plugin<T> plugin, ModelService model, JOB_STATE state,
    Optional<String> stateDetails) {
    try {
      Job job = PluginHelper.getJobFromModel(plugin, model);
      job.setState(state);
      if (stateDetails.isPresent()) {
        job.setStateDetails(stateDetails.get());
      }
      if (job.getState() == JOB_STATE.COMPLETED || job.getState() == JOB_STATE.FAILED_TO_COMPLETE) {
        job.setEndDate(new Date());
      }

      model.createOrUpdateJob(job);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Unable to get or update Job from model", e);
    }
  }

  /**
   * 20160531 hsilva:Only orchestrators/orchestrators related classes should
   * invoke this method
   */
  public static <T extends IsRODAObject> void updateJobObjectsCount(Plugin<T> plugin, ModelService model,
    Long objectsCount) {
    try {
      Job job = PluginHelper.getJobFromModel(plugin, model);
      job.getJobStats().setSourceObjectsCount(objectsCount.intValue())
        .setSourceObjectsWaitingToBeProcessed(objectsCount.intValue());

      model.createOrUpdateJob(job);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Unable to get or update Job from model", e);
    }
  }

  /**
   * 20160531 hsilva:Only orchestrators/orchestrators related classes should
   * invoke this method
   */
  public static <T extends IsRODAObject> void updateJobInformation(Plugin<T> plugin, ModelService model,
    JobPluginInfo jobPluginInfo) {

    // update job
    try {
      LOGGER.debug("New job completionPercentage: {}", jobPluginInfo.getCompletionPercentage());
      Job job = PluginHelper.getJobFromModel(plugin, model);
      job = setJobCounters(job, jobPluginInfo);

      model.createOrUpdateJob(job);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Unable to get or update Job from model", e);
    }
  }

  public static Job updateJobInTheStateStartedOrCreated(Job job) {
    job.setState(JOB_STATE.FAILED_TO_COMPLETE);
    JobStats jobStats = job.getJobStats();
    jobStats.setSourceObjectsBeingProcessed(0);
    jobStats.setSourceObjectsProcessedWithSuccess(0);
    jobStats.setSourceObjectsProcessedWithFailure(jobStats.getSourceObjectsCount());
    jobStats.setSourceObjectsWaitingToBeProcessed(0);
    job.setEndDate(new Date());
    return job;
  }

  public static Job setJobCounters(Job job, JobPluginInfo jobPluginInfo) {
    JobStats jobStats = job.getJobStats();

    jobStats.setCompletionPercentage(jobPluginInfo.getCompletionPercentage());
    jobStats.setSourceObjectsCount(jobPluginInfo.getSourceObjectsCount());
    jobStats.setSourceObjectsBeingProcessed(jobPluginInfo.getSourceObjectsBeingProcessed());
    jobStats.setSourceObjectsProcessedWithSuccess(jobPluginInfo.getSourceObjectsProcessedWithSuccess());
    jobStats.setSourceObjectsProcessedWithFailure(jobPluginInfo.getSourceObjectsProcessedWithFailure());
    jobStats
      .setSourceObjectsWaitingToBeProcessed(jobStats.getSourceObjectsCount() - jobStats.getSourceObjectsBeingProcessed()
        - jobStats.getSourceObjectsProcessedWithFailure() - jobStats.getSourceObjectsProcessedWithSuccess());
    jobStats.setOutcomeObjectsWithManualIntervention(jobPluginInfo.getOutcomeObjectsWithManualIntervention());
    return job;
  }

  /***************** Plugin parameters related *****************/
  /*************************************************************/
  public static <T extends IsRODAObject> void setPluginParameters(Plugin<T> plugin, Job job) {
    Map<String, String> parameters = new HashMap<String, String>(job.getPluginParameters());
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, job.getId());
    try {
      plugin.setParameterValues(parameters);
    } catch (InvalidParameterException e) {
      LOGGER.error("Error setting plug-in parameters", e);
    }
  }

  public static <T extends IsRODAObject> boolean getBooleanFromParameters(Plugin<T> plugin,
    PluginParameter pluginParameter) {
    return verifyIfStepShouldBePerformed(plugin, pluginParameter);
  }

  public static <T extends IsRODAObject> String getStringFromParameters(Plugin<T> plugin,
    PluginParameter pluginParameter) {
    return plugin.getParameterValues().getOrDefault(pluginParameter.getId(), pluginParameter.getDefaultValue());
  }

  public static <T extends IsRODAObject> String getParentIdFromParameters(Plugin<T> plugin) {
    return plugin.getParameterValues().get(RodaConstants.PLUGIN_PARAMS_PARENT_ID);
  }

  private static <T extends IsRODAObject> boolean getForceParentIdFromParameters(Plugin<T> plugin) {
    return new Boolean(plugin.getParameterValues().get(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID));
  }

  public static <T extends IsRODAObject> int getTotalStepsFromParameters(Plugin<T> plugin) {
    int totalSteps = 1;
    String totalStepsString = plugin.getParameterValues().get(RodaConstants.PLUGIN_PARAMS_TOTAL_STEPS);
    if (totalStepsString != null) {
      try {
        totalSteps = Integer.parseInt(totalStepsString);
      } catch (NumberFormatException e) {
        // return default value
      }
    }
    return totalSteps;
  }

  public static <T extends IsRODAObject> boolean verifyIfStepShouldBePerformed(Plugin<T> plugin,
    PluginParameter pluginParameter) {
    String paramValue = getStringFromParameters(plugin, pluginParameter);
    boolean perform = Boolean.parseBoolean(paramValue);

    if (perform) {
      String parameterClass = RodaConstants.PLUGIN_PARAMETER_TO_CLASS.get(pluginParameter.getId());
      if (parameterClass != null && RodaCoreFactory.getPluginManager().getPlugin(parameterClass) == null) {
        perform = false;
      }
    }

    return perform;
  }

  public static <T extends IsRODAObject> String computeParentId(Plugin<T> plugin, IndexService index,
    String sipParentId) {
    String parentId = sipParentId;
    String jobDefinedParentId = getParentIdFromParameters(plugin);
    boolean jobDefinedForceParentId = getForceParentIdFromParameters(plugin);

    // lets see if parentId should be overwritten
    if ((StringUtils.isBlank(sipParentId) && StringUtils.isNotBlank(jobDefinedParentId)) || jobDefinedForceParentId) {
      parentId = jobDefinedParentId;
    }

    // last check: see if parent exist in the index
    if (StringUtils.isNotBlank(parentId)) {
      try {
        index.retrieve(IndexedAIP.class, parentId);
      } catch (NotFoundException | GenericException e) {
        // could not find parent id
        parentId = null;
      }
    } else {
      parentId = null;
    }

    return parentId;
  }

  /***************** Plugin related *****************/
  /**************************************************/
  public static void createSubmission(ModelService model, boolean createSubmission, Path submissionPath, String aipID)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    if (createSubmission) {
      if (Files.isDirectory(submissionPath)) {
        StorageService submissionStorage = new FileStorageService(submissionPath);
        StoragePath submissionStoragePath = DefaultStoragePath.empty();
        model.createSubmission(submissionStorage, submissionStoragePath, aipID);
      } else {
        model.createSubmission(submissionPath, aipID);
      }
    }
  }

  public static <T extends AbstractPlugin> Risk createRiskIfNotExists(ModelService model, int riskIndex, String riskId,
    Class<T> pluginClass) throws RequestNotValidException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, NotFoundException {
    try {
      return model.retrieveRisk(riskId);
    } catch (NotFoundException e) {
      return createDefaultRisk(model, riskIndex, riskId, pluginClass);
    }
  }

  private static <T extends AbstractPlugin> Risk createDefaultRisk(ModelService model, int riskIndex, String riskId,
    Class<T> pluginClass) throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {

    String defaultFile = RodaConstants.CORE_DATA_FOLDER + '/' + RodaConstants.CORE_STORAGE_FOLDER + '/'
      + RodaConstants.CORE_RISK_FOLDER + '/' + riskId + ".json";

    InputStream inputStream = RodaCoreFactory.getDefaultFileAsStream(defaultFile);
    Risk risk = null;

    try {
      risk = JsonUtils.getObjectFromJson(inputStream, Risk.class);
      risk.setId(riskId);
      model.createRisk(risk, false);
      return risk;
    } catch (GenericException e) {
      LOGGER.error("Could not create a default risk");
    }

    IOUtils.closeQuietly(inputStream);
    return risk;
  }

  /***************** Preservation events related *****************/
  /***************************************************************/
  /**
   * For SIP > AIP
   */
  public static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    ModelService model, IndexService index, TransferredResource source, PluginState outcome,
    String outcomeDetailExtension, boolean notify, Date eventDate) throws RequestNotValidException, NotFoundException,
    GenericException, AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    List<LinkingIdentifier> sources = Arrays
      .asList(PluginHelper.getLinkingIdentifier(source, RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));
    List<LinkingIdentifier> outcomes = Arrays
      .asList(PluginHelper.getLinkingIdentifier(aipID, RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));

    return createPluginEvent(plugin, aipID, null, null, null, model, index, sources, outcomes, outcome,
      outcomeDetailExtension, notify, eventDate);
  }

  public static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    ModelService model, IndexService index, TransferredResource source, PluginState outcome,
    String outcomeDetailExtension, boolean notify) throws RequestNotValidException, NotFoundException, GenericException,
    AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    return createPluginEvent(plugin, aipID, model, index, source, outcome, outcomeDetailExtension, notify, new Date());
  }

  /**
   * For AIP as source only
   */
  public static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    ModelService model, IndexService index, PluginState outcome, String outcomeDetailExtension, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    ValidationException, AlreadyExistsException {
    List<LinkingIdentifier> sources = Arrays
      .asList(PluginHelper.getLinkingIdentifier(aipID, RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));
    List<LinkingIdentifier> outcomes = null;
    return createPluginEvent(plugin, aipID, null, null, null, model, index, sources, outcomes, outcome,
      outcomeDetailExtension, notify, new Date());
  }

  public static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    ModelService model, IndexService index, PluginState outcome, String outcomeDetailExtension, boolean notify,
    Date eventDate) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    ValidationException, AlreadyExistsException {
    List<LinkingIdentifier> sources = Arrays
      .asList(PluginHelper.getLinkingIdentifier(aipID, RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));
    List<LinkingIdentifier> outcomes = null;
    return createPluginEvent(plugin, aipID, null, null, null, model, index, sources, outcomes, outcome,
      outcomeDetailExtension, notify, eventDate);
  }

  /**
   * For AIP
   */
  public static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    ModelService model, IndexService index, List<LinkingIdentifier> sources, List<LinkingIdentifier> targets,
    PluginState outcome, String outcomeDetailExtension, boolean notify) throws RequestNotValidException,
    NotFoundException, GenericException, AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    return createPluginEvent(plugin, aipID, null, null, null, model, index, sources, targets, outcome,
      outcomeDetailExtension, notify, new Date());
  }

  /**
   * For REPRESENTATION
   */
  public static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    String representationID, ModelService model, IndexService index, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcome, String outcomeDetailExtension, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    ValidationException, AlreadyExistsException {
    return createPluginEvent(plugin, aipID, representationID, null, null, model, index, sources, targets, outcome,
      outcomeDetailExtension, notify, new Date());
  }

  /**
   * For FILE
   */
  public static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    String representationID, List<String> filePath, String fileID, ModelService model, IndexService index,
    List<LinkingIdentifier> sources, List<LinkingIdentifier> outcomes, PluginState outcome,
    String outcomeDetailExtension, boolean notify) throws RequestNotValidException, NotFoundException, GenericException,
    AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    return createPluginEvent(plugin, aipID, representationID, filePath, fileID, model, index, sources, outcomes,
      outcome, outcomeDetailExtension, notify, new Date());
  }

  private static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    String representationID, List<String> filePath, String fileID, ModelService model, IndexService index,
    List<LinkingIdentifier> sources, List<LinkingIdentifier> outcomes, PluginState outcome,
    String outcomeDetailExtension, boolean notify, Date startDate) throws RequestNotValidException, NotFoundException,
    GenericException, AuthorizationDeniedException, ValidationException, AlreadyExistsException {

    List<String> agentIds = new ArrayList<>();

    try {
      boolean notifyAgent = true;
      PreservationMetadata pm = PremisV3Utils.createPremisAgentBinary(plugin, model, notifyAgent);
      agentIds.add(pm.getId());
    } catch (AlreadyExistsException e) {
      agentIds.add(IdUtils.getPluginAgentId(plugin.getClass().getName(), plugin.getVersion()));
    } catch (RODAException e) {
      LOGGER.error("Error creating PREMIS agent", e);
    }

    Job job;
    try {
      job = getJobFromIndex(plugin, index);
    } catch (NotFoundException e) {
      job = null;
    }

    if (job != null) {
      try {
        boolean notifyAgent = true;
        PreservationMetadata pm = PremisV3Utils.createPremisUserAgentBinary(job.getUsername(), model, index,
          notifyAgent);
        if (pm != null) {
          agentIds.add(pm.getId());
        }
      } catch (AlreadyExistsException e) {
        agentIds.add(IdUtils.getUserAgentId(job.getUsername()));
      } catch (RODAException e) {
        LOGGER.error("Error creating PREMIS agent", e);
      }
    }
    String id = IdUtils.createPreservationMetadataId(PreservationMetadataType.EVENT);
    String outcomeDetailNote = (outcome == PluginState.SUCCESS) ? plugin.getPreservationEventSuccessMessage()
      : plugin.getPreservationEventFailureMessage();
    ContentPayload premisEvent = PremisV3Utils.createPremisEventBinary(id, startDate,
      plugin.getPreservationEventType().toString(), plugin.getPreservationEventDescription(), sources, outcomes,
      outcome.name(), outcomeDetailNote, outcomeDetailExtension, agentIds);
    model.createPreservationMetadata(PreservationMetadataType.EVENT, id, aipID, representationID, filePath, fileID,
      premisEvent, notify);
    PreservationMetadata pm = new PreservationMetadata();
    pm.setId(id);
    pm.setAipId(aipID);
    pm.setRepresentationId(representationID);
    pm.setType(PreservationMetadataType.EVENT);
    return pm;
  }

  public static LinkingIdentifier getLinkingIdentifier(TransferredResource transferredResource, String role) {
    LinkingIdentifier li = new LinkingIdentifier();
    li.setValue(LinkingObjectUtils.getLinkingIdentifierId(transferredResource));
    li.setType("URN");
    li.setRoles(Arrays.asList(role));
    return li;
  }

  public static LinkingIdentifier getLinkingIdentifier(String aipID, String role) {
    return getLinkingIdentifier(RODA_TYPE.AIP, aipID, role);
  }

  public static LinkingIdentifier getLinkingIdentifier(String aipID, String representationID, String role) {
    return getLinkingIdentifier(RODA_TYPE.REPRESENTATION, IdUtils.getRepresentationId(aipID, representationID), role);
  }

  public static LinkingIdentifier getLinkingIdentifier(String aipID, String representationID, List<String> filePath,
    String fileID, String role) {
    return getLinkingIdentifier(RODA_TYPE.FILE, IdUtils.getFileId(aipID, representationID, filePath, fileID), role);
  }

  private static LinkingIdentifier getLinkingIdentifier(RODA_TYPE type, String uuid, String role) {
    LinkingIdentifier li = new LinkingIdentifier();
    li.setValue(LinkingObjectUtils.getLinkingIdentifierId(type, uuid));
    li.setType("URN");
    li.setRoles(Arrays.asList(role));
    return li;
  }

  public static List<LinkingIdentifier> getLinkingRepresentations(AIP aip, String role) {
    List<LinkingIdentifier> identifiers = new ArrayList<LinkingIdentifier>();
    if (aip.getRepresentations() != null && !aip.getRepresentations().isEmpty()) {
      for (Representation representation : aip.getRepresentations()) {
        identifiers.add(getLinkingIdentifier(aip.getId(), representation.getId(), role));
      }
    }
    return identifiers;
  }

  public static List<LinkingIdentifier> getLinkingIdentifiers(List<TransferredResource> resources, String role) {
    List<LinkingIdentifier> identifiers = new ArrayList<LinkingIdentifier>();
    if (resources != null && !resources.isEmpty()) {
      for (TransferredResource tr : resources) {
        identifiers.add(getLinkingIdentifier(tr, role));
      }
    }
    return identifiers;
  }

  public static <T extends IsRODAObject> void moveSIPs(Plugin<T> plugin, ModelService model,
    List<TransferredResource> transferredResources, IngestJobPluginInfo jobPluginInfo) {
    List<String> success = new ArrayList<>();
    List<String> unsuccess = new ArrayList<>();

    String baseFolder = RodaCoreFactory.getRodaConfiguration().getString("core.ingest.processed.base_folder",
      "PROCESSED");
    String successFolder = RodaCoreFactory.getRodaConfiguration()
      .getString("core.ingest.processed.successfully_ingested", "SUCCESSFULLY_INGESTED");
    String unsuccessFolder = RodaCoreFactory.getRodaConfiguration()
      .getString("core.ingest.processed.unsuccessfully_ingested", "UNSUCCESSFULLY_INGESTED");
    String successPath = Paths.get(baseFolder, successFolder).toString();
    String unsuccessPath = Paths.get(baseFolder, unsuccessFolder).toString();

    // determine which SIPs will be moved based on 1) if at least one AIP was
    // created from each SIP; 2) if it was created, in which state it is
    for (TransferredResource transferredResource : transferredResources) {
      String transferredResourceId = transferredResource.getUUID();
      List<String> aipIds = jobPluginInfo.getAipIds(transferredResourceId);
      if (aipIds != null && !aipIds.isEmpty()) {
        try {
          AIPState aipState = model.retrieveAIP(aipIds.get(0)).getState();
          if (AIPState.ACTIVE == aipState) {
            success.add(transferredResourceId);
          } else if (AIPState.UNDER_APPRAISAL != aipState) {
            unsuccess.add(transferredResourceId);
          }
        } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
          LOGGER.error("Error retrieving AIP", e);
        }
      } else {
        // no AIP was generated
        unsuccess.add(transferredResourceId);
      }
    }

    // move SIPs and update reports
    Map<String, String> successOldToNewTransferredResourceIds = new HashMap<>();
    Map<String, String> unsuccessOldToNewTransferredResourceIds = new HashMap<>();
    try {
      if (!success.isEmpty()) {
        successOldToNewTransferredResourceIds = RodaCoreFactory.getTransferredResourcesScanner()
          .moveTransferredResource(successPath, success, true);
        updateReportsAfterMovingSIPs(model, jobPluginInfo, successOldToNewTransferredResourceIds);
      }
    } catch (AlreadyExistsException | GenericException | NotFoundException e) {
      LOGGER.error("Error moving successfully ingested SIPs", e);
    } catch (IsStillUpdatingException e) {
      LOGGER.warn("TransferredResources are already being indexed");
    }

    try {
      if (!unsuccess.isEmpty()) {
        unsuccessOldToNewTransferredResourceIds = RodaCoreFactory.getTransferredResourcesScanner()
          .moveTransferredResource(unsuccessPath, unsuccess, true);
        updateReportsAfterMovingSIPs(model, jobPluginInfo, unsuccessOldToNewTransferredResourceIds);
      }
    } catch (AlreadyExistsException | GenericException | NotFoundException e) {
      LOGGER.error("Error moving unsuccessfully ingested SIPs", e);
    } catch (IsStillUpdatingException e) {
      LOGGER.warn("TransferredResources are already being indexed");
    }

    // update Job (with all new ids)
    successOldToNewTransferredResourceIds.putAll(unsuccessOldToNewTransferredResourceIds);
    updateJobAfterMovingSIPs(plugin, model, successOldToNewTransferredResourceIds);

  }

  private static void updateReportsAfterMovingSIPs(ModelService model, IngestJobPluginInfo jobPluginInfo,
    Map<String, String> oldToNewTransferredResourceIds) {
    for (Entry<String, String> oldToNewId : oldToNewTransferredResourceIds.entrySet()) {
      String oldSIPId = oldToNewId.getKey();
      String newSIPId = oldToNewId.getValue();
      for (Report report : jobPluginInfo.getAllReports().get(oldSIPId).values()) {
        report.setSourceObjectId(newSIPId);
        if (!report.getReports().isEmpty()) {
          report.getReports().get(0).setSourceObjectId(newSIPId);
        }

        // update in model
        updateJobReport(model, report);
      }
    }
  }

  private static <T extends IsRODAObject> void updateJobAfterMovingSIPs(Plugin<T> plugin, ModelService model,
    Map<String, String> oldToNewTransferredResourceIds) {
    try {
      Job job = getJobFromModel(plugin, model);
      SelectedItems<?> sourceObjects = job.getSourceObjects();
      if (sourceObjects instanceof SelectedItemsList) {
        SelectedItemsList<T> list = (SelectedItemsList<T>) sourceObjects;
        ArrayList<String> newIds = new ArrayList<String>();
        for (String oldId : list.getIds()) {
          newIds.add(oldToNewTransferredResourceIds.get(oldId));
        }
        list.setIds(newIds);
      }

      model.createOrUpdateJob(job);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Error updating Job", e);
    }
  }

  public static <T extends IsRODAObject> void fixParents(Plugin<T> plugin, IndexService index, ModelService model)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    String forcedParent = getParentIdFromParameters(plugin);
    index.execute(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_GHOST, Boolean.TRUE.toString())), ghost -> {
        Filter nonGhostsFilter = new Filter(
          new SimpleFilterParameter(RodaConstants.INGEST_SIP_ID, ghost.getIngestSIPId()),
          new SimpleFilterParameter(RodaConstants.AIP_GHOST, Boolean.FALSE.toString()));
        if (!StringUtils.isBlank(forcedParent)) {
          nonGhostsFilter.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, forcedParent));
        }
        // if there are AIPs that have the same sip id
        IndexResult<IndexedAIP> result = index.find(IndexedAIP.class, nonGhostsFilter, Sorter.NONE, new Sublist(0, 1));

        if (result.getTotalCount() > 1) {
          LOGGER.debug("Couldn't find non-ghost AIP with ingest SIP id {}", ghost.getIngestSIPId());
        } else if (result.getTotalCount() == 1) {
          IndexedAIP newParentIAIP = result.getResults().get(0);
          moveChildrenAIPsAndDelete(plugin, index, model, ghost.getId(), newParentIAIP.getId(), forcedParent);
        } else if (result.getTotalCount() == 0) {
          // check if there are other ghosts with the same sip id and from the
          // same job, move all of this ghost children
          Filter otherGhostsFilter = new Filter(
            new SimpleFilterParameter(RodaConstants.INGEST_SIP_ID, ghost.getIngestSIPId()),
            new SimpleFilterParameter(RodaConstants.AIP_GHOST, Boolean.TRUE.toString()));
          if (!StringUtils.isBlank(forcedParent)) {
            otherGhostsFilter.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, forcedParent));
          }
          IndexResult<IndexedAIP> otherGhosts = index.find(IndexedAIP.class, otherGhostsFilter, Sorter.NONE,
            new Sublist(0, 1));
          if (otherGhosts.getTotalCount() >= 1) {
            IndexedAIP otherGhost = otherGhosts.getResults().get(0);
            moveChildrenAIPsAndDelete(plugin, index, model, ghost.getId(), otherGhost.getId(), forcedParent);
          }
        }
      });
  }

  private static <T extends IsRODAObject> void moveChildrenAIPsAndDelete(Plugin<T> plugin, IndexService index,
    ModelService model, String aipId, String newParentId, String forcedParent)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    Filter parentFilter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, aipId));
    if (!StringUtils.isBlank(forcedParent)) {
      parentFilter.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, getParentIdFromParameters(plugin)));
    }
    index.execute(IndexedAIP.class, parentFilter, child -> {
      try {
        model.moveAIP(child.getId(), newParentId);
      } catch (NotFoundException e) {
        LOGGER.debug("Can't move child. It wasn't found.", e);
      }
    });
    try {
      model.deleteAIP(aipId);
    } catch (NotFoundException e) {
      LOGGER.debug("Can't delete ghost or move node. It wasn't found.", e);
    }
  }

}
