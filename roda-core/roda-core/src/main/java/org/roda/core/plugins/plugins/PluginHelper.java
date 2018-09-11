/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.RODA_TYPE;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LinkingObjectUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectProcessingLogicNew;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.IngestJobPluginInfo;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.orchestrate.akka.Messages;
import org.roda.core.plugins.plugins.reindex.ReindexAIPPlugin;
import org.roda.core.plugins.plugins.reindex.ReindexActionLogPlugin;
import org.roda.core.plugins.plugins.reindex.ReindexDIPPlugin;
import org.roda.core.plugins.plugins.reindex.ReindexFormatPlugin;
import org.roda.core.plugins.plugins.reindex.ReindexIncidencePlugin;
import org.roda.core.plugins.plugins.reindex.ReindexJobPlugin;
import org.roda.core.plugins.plugins.reindex.ReindexNotificationPlugin;
import org.roda.core.plugins.plugins.reindex.ReindexPreservationAgentPlugin;
import org.roda.core.plugins.plugins.reindex.ReindexPreservationRepositoryEventPlugin;
import org.roda.core.plugins.plugins.reindex.ReindexRepresentationInformationPlugin;
import org.roda.core.plugins.plugins.reindex.ReindexRiskPlugin;
import org.roda.core.plugins.plugins.reindex.ReindexRodaMemberPlugin;
import org.roda.core.plugins.plugins.reindex.ReindexTransferredResourcePlugin;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PluginHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(PluginHelper.class);

  private static final String LOCK_REQUEST_TIMEOUT = "core.orchestrator.lock_request_timeout";
  private static final int DEFAULT_LOCK_REQUEST_TIMEOUT = 600;

  private PluginHelper() {
    // do nothing
  }

  public static <T extends IsRODAObject> Report processObjects(Plugin<T> plugin,
    RODAObjectsProcessingLogic<T> objectsLogic, IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return processObjects(plugin, objectsLogic, index, model, storage, liteList, true);
  }

  public static <T extends IsRODAObject> Report processObjects(Plugin<T> plugin,
    RODAObjectsProcessingLogic<T> objectsLogic, IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList, boolean autoLocking) throws PluginException {
    Report report = PluginHelper.initPluginReport(plugin);
    List<T> list;
    Throwable exceptionOccurred = null;

    try {
      JobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(plugin, liteList.size());
      PluginHelper.updateJobInformationAsync(plugin, jobPluginInfo);

      Job job = PluginHelper.getJob(plugin, model);
      list = PluginHelper.transformLitesIntoObjects(model, plugin, report, jobPluginInfo, liteList, job, autoLocking);

      try {
        objectsLogic.process(index, model, storage, report, job, jobPluginInfo, plugin, list);
      } catch (Throwable e) {
        LOGGER.error("Unexpected exception during 'objectsLogic' execution", e);
        jobPluginInfo.setSourceObjectsProcessedWithFailure(
          jobPluginInfo.getSourceObjectsCount() - jobPluginInfo.getSourceObjectsProcessedWithSuccess());
        exceptionOccurred = e;
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformationAsync(plugin, jobPluginInfo);

      if (exceptionOccurred != null) {
        // 20180822 hsilva: this is required in order to finalize info (code
        // above) & let orchestrator handle this throwable correctly
        throw new PluginException("A plugin exception has occurred", exceptionOccurred);
      }
    } catch (JobException | AuthorizationDeniedException | RequestNotValidException | GenericException
      | NotFoundException e) {
      throw new PluginException("A job exception has occurred", e);
    } catch (LockingException e) {
      throw new PluginException("Unable to acquire locks for the objects being processed", e);
    } finally {
      if (autoLocking) {
        releaseObjectLocks(plugin, liteList);
      }
    }

    return report;
  }

  private static <T extends IsRODAObject> void releaseObjectLocks(Plugin<T> plugin,
    List<LiteOptionalWithCause> liteList) {
    String requestUuid = plugin.getParameterValues().getOrDefault(RodaConstants.PLUGIN_PARAMS_LOCK_REQUEST_UUID,
      IdUtils.createUUID());
    PluginHelper.releaseObjectLock(liteList.stream().filter(obj -> obj.getLite().isPresent())
      .map(obj -> obj.getLite().get().getInfo()).collect(Collectors.toList()), requestUuid);
  }

  public static <T extends IsRODAObject> Report processObjects(Plugin<T> plugin, RODAProcessingLogic<T> beforeLogic,
    RODAObjectProcessingLogicNew<T> perObjectLogic, RODAProcessingLogic<T> afterLogic, IndexService index,
    ModelService model, StorageService storage, List<LiteOptionalWithCause> liteList) throws PluginException {
    return processObjects(plugin, beforeLogic, perObjectLogic, afterLogic, index, model, storage, liteList, true);
  }

  public static <T extends IsRODAObject> Report processObjects(Plugin<T> plugin, RODAProcessingLogic<T> beforeLogic,
    RODAObjectProcessingLogicNew<T> perObjectLogic, RODAProcessingLogic<T> afterLogic, IndexService index,
    ModelService model, StorageService storage, List<LiteOptionalWithCause> liteList, boolean autoLocking)
    throws PluginException {
    Report report = PluginHelper.initPluginReport(plugin);
    List<T> list = new ArrayList<>();
    Throwable exceptionOccurred = null;

    try {
      JobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(plugin, liteList.size());
      PluginHelper.updateJobInformationAsync(plugin, jobPluginInfo);

      Job job = PluginHelper.getJob(plugin, model);
      list = PluginHelper.transformLitesIntoObjects(model, plugin, report, jobPluginInfo, liteList, job, autoLocking);

      if (beforeLogic != null) {
        try {
          beforeLogic.process(index, model, storage, report, job, jobPluginInfo, plugin);
        } catch (Throwable e) {
          LOGGER.error("Unexpected exception during 'beforeLogic' execution", e);
          exceptionOccurred = e;
        }
      }

      if (exceptionOccurred == null) {
        // 20180822 hsilva: try/catch is wrapping the for cycle as otherwise it
        // would be very troublesome to handle the exceptions properly as we
        // need to pass them to the orchestrator (via throw)
        try {
          for (T object : list) {
            perObjectLogic.process(index, model, storage, report, job, jobPluginInfo, plugin, object);
          }
        } catch (Throwable e) {
          LOGGER.error("Unexpected exception during 'perObjectLogic' execution", e);
          exceptionOccurred = e;
        }
      }

      if (afterLogic != null && exceptionOccurred == null) {
        try {
          afterLogic.process(index, model, storage, report, job, jobPluginInfo, plugin);
        } catch (Throwable e) {
          LOGGER.error("Unexpected exception during 'afterLogic' execution", e);
          exceptionOccurred = e;
        }
      }

      if (exceptionOccurred != null) {
        jobPluginInfo.setSourceObjectsProcessedWithFailure(
          jobPluginInfo.getSourceObjectsCount() - jobPluginInfo.getSourceObjectsProcessedWithSuccess());
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformationAsync(plugin, jobPluginInfo);

      if (exceptionOccurred != null) {
        // 20180822 hsilva: this is required in order to finalize info (code
        // above) & let orchestrator handle this throwable correctly
        throw new PluginException("A plugin exception has occurred", exceptionOccurred);
      }
    } catch (JobException | AuthorizationDeniedException | RequestNotValidException | GenericException
      | NotFoundException e) {
      throw new PluginException("A job exception has occurred", e);
    } catch (LockingException e) {
      throw new PluginException("Unable to acquire locks for the objects being processed", e);
    } finally {
      if (autoLocking) {
        releaseObjectLocks(plugin, liteList);
      }
    }

    return report;
  }

  public static <T extends IsRODAObject> Report processObjects(Plugin<T> plugin, RODAProcessingLogic<T> beforeLogic,
    RODAObjectProcessingLogicNew<T> perObjectLogic, IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return processObjects(plugin, beforeLogic, perObjectLogic, null, index, model, storage, liteList);
  }

  public static <T extends IsRODAObject> Report processObjects(Plugin<T> plugin,
    RODAObjectProcessingLogicNew<T> perObjectLogic, RODAProcessingLogic<T> afterLogic, IndexService index,
    ModelService model, StorageService storage, List<LiteOptionalWithCause> liteList) throws PluginException {
    return processObjects(plugin, null, perObjectLogic, afterLogic, index, model, storage, liteList);
  }

  public static <T extends IsRODAObject> Report processObjects(Plugin<T> plugin,
    RODAObjectProcessingLogicNew<T> perObjectLogic, IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return processObjects(plugin, null, perObjectLogic, null, index, model, storage, liteList);
  }

  public static Report processVoids(Plugin<Void> plugin, RODAProcessingLogic<Void> logic, IndexService index,
    ModelService model, StorageService storage) throws PluginException {
    return processVoids(plugin, logic, index, model, storage, 0);
  }

  public static Report processVoids(Plugin<Void> plugin, RODAProcessingLogic<Void> logic, IndexService index,
    ModelService model, StorageService storage, int setSourceObjectsCount) throws PluginException {
    Report report = PluginHelper.initPluginReport(plugin);
    Throwable exceptionOccurred = null;

    try {
      JobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(plugin, 0);
      jobPluginInfo.setSourceObjectsCount(setSourceObjectsCount);
      PluginHelper.updateJobInformationAsync(plugin, jobPluginInfo);

      Job job = PluginHelper.getJob(plugin, model);

      try {
        logic.process(index, model, storage, report, job, jobPluginInfo, plugin);
      } catch (Throwable e) {
        LOGGER.error("Unexpected exception during 'logic' execution", e);
        jobPluginInfo.setSourceObjectsProcessedWithFailure(
          jobPluginInfo.getSourceObjectsCount() - jobPluginInfo.getSourceObjectsProcessedWithSuccess());
        exceptionOccurred = e;
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformationAsync(plugin, jobPluginInfo);

      if (exceptionOccurred != null) {
        // 20180822 hsilva: this is required in order to finalize info (code
        // above) & let orchestrator handle this throwable correctly
        throw new PluginException("A plugin exception has occurred", exceptionOccurred);
      }
    } catch (JobException | AuthorizationDeniedException | RequestNotValidException | GenericException
      | NotFoundException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return report;
  }

  /***************** Job report related *****************/
  /******************************************************/

  public static <T extends IsRODAObject> Report initPluginReport(Plugin<T> plugin) {
    return initPluginReportItem(plugin, Report.NO_OUTCOME_OBJECT_ID, Report.NO_SOURCE_OBJECT_ID);
  }

  public static <T extends IsRODAObject> Report initPluginReportItem(Plugin<T> plugin,
    TransferredResource transferredResource) {
    return initPluginReportItem(plugin, Report.NO_OUTCOME_OBJECT_ID, transferredResource.getUUID())
      .setSourceObjectClass(TransferredResource.class.getName()).setOutcomeObjectClass(AIP.class.getName())
      .setOutcomeObjectState(AIPState.INGEST_PROCESSING).setSourceObjectOriginalName(transferredResource.getName());
  }

  private static <T extends IsRODAObject> List<String> getSourceObjectIdsToInitPluginReportItem(Plugin<T> plugin,
    String outcomeObjectId, String defaultSourceObjectId) {
    Map<String, List<String>> objectFromJson;
    try {
      objectFromJson = JsonUtils.getObjectFromJson(plugin.getParameterValues()
        .getOrDefault(RodaConstants.PLUGIN_PARAMS_OUTCOMEOBJECTID_TO_SOURCEOBJECTID_MAP, "{}"), HashMap.class);
    } catch (GenericException e) {
      objectFromJson = Collections.emptyMap();
    }
    return objectFromJson.getOrDefault(outcomeObjectId, Arrays.asList(defaultSourceObjectId));
  }

  private static <T extends IsRODAObject> String getSourceObjectIdToInitPluginReportItem(Plugin<T> plugin,
    String outcomeObjectId, String defaultSourceObjectId) {
    return getSourceObjectIdsToInitPluginReportItem(plugin, outcomeObjectId, defaultSourceObjectId).get(0);
  }

  public static <T extends IsRODAObject> Report initPluginReportItem(Plugin<T> plugin, String objectId,
    Class<?> clazz) {
    String sourceObjectId = getSourceObjectIdToInitPluginReportItem(plugin, objectId, objectId);
    return initPluginReportItem(plugin, objectId, sourceObjectId).setSourceObjectClass(clazz.getName())
      .setOutcomeObjectClass(clazz.getName());
  }

  public static <T extends IsRODAObject> Report initPluginReportItem(Plugin<T> plugin, String objectId, Class<?> clazz,
    AIPState initialOutcomeObjectState) {
    String sourceObjectId = getSourceObjectIdToInitPluginReportItem(plugin, objectId, objectId);
    return initPluginReportItem(plugin, objectId, sourceObjectId).setSourceObjectClass(clazz.getName())
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
    reportItem.setId(IdUtils.getJobReportId(jobId, sourceObjectId, outcomeObjectId));
    reportItem.setJobId(jobId);
    reportItem.setSourceAndOutcomeObjectId(sourceObjectId, outcomeObjectId);
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
    reportItem.setId(IdUtils.getJobReportId(jobId, reportItem.getSourceObjectId(), reportItem.getOutcomeObjectId()));
    report.setId(reportItem.getId());
    report.setJobId(jobId);
    if (reportItem.getTotalSteps() != 0) {
      report.setTotalSteps(reportItem.getTotalSteps());
    } else {
      report.setTotalSteps(getTotalStepsFromParameters(plugin));
    }
    report.addReport(reportItem);

    try {
      Job job = model.retrieveJob(jobId);
      model.createOrUpdateJobReport(report, job);
    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Error creating Job Report", e);
    }
  }

  public static <T extends IsRODAObject> void updateJobReportState(Plugin<T> plugin, ModelService model,
    String sourceObjectId, String outcomeObjectId, AIPState newState) {
    for (String sourceObjectIdCalculated : getSourceObjectIdsToInitPluginReportItem(plugin, outcomeObjectId,
      sourceObjectId)) {
      try {
        String jobId = getJobId(plugin);
        Report jobReport = model.retrieveJobReport(jobId, sourceObjectIdCalculated, outcomeObjectId);
        jobReport.setOutcomeObjectState(newState);
        Job job = model.retrieveJob(jobId);
        model.createOrUpdateJobReport(jobReport, job);
      } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
        LOGGER.error("Error while updating Job Report", e);
      }
    }
  }

  public static <T extends IsRODAObject> void updatePartialJobReport(Plugin<T> plugin, ModelService model,
    Report reportItem, boolean replaceLastReportItemIfTheSame, Job cachedJob) {
    String jobId = getJobId(plugin);
    for (String sourceObjectId : getSourceObjectIdsToInitPluginReportItem(plugin, reportItem.getOutcomeObjectId(),
      reportItem.getSourceObjectId())) {
      // lets ensure that job report id & source object id is correct
      reportItem.setSourceObjectId(sourceObjectId);
      reportItem.setId(IdUtils.getJobReportId(jobId, sourceObjectId, reportItem.getOutcomeObjectId()));
      try {
        Report jobReport;
        try {
          jobReport = model.retrieveJobReport(jobId, sourceObjectId, reportItem.getOutcomeObjectId());

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
        } catch (NotFoundException e) {
          jobReport = initPluginReportItem(plugin, reportItem.getOutcomeObjectId(), reportItem.getSourceObjectId())
            .setSourceObjectClass(reportItem.getSourceObjectClass())
            .setOutcomeObjectClass(reportItem.getOutcomeObjectClass());

          jobReport.setId(reportItem.getId());
          jobReport.setDateCreated(reportItem.getDateCreated());
          jobReport.addReport(reportItem);
        }

        model.createOrUpdateJobReport(jobReport, cachedJob);
      } catch (GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.error("Error while updating Job Report", e);
      }
    }
  }

  private static void updateJobReport(ModelService model, Report report) {
    try {
      Job job = model.retrieveJob(report.getJobId());
      model.createOrUpdateJobReport(report, job);
    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
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
   * 
   * @throws RequestNotValidException
   */
  public static <T extends IsRODAObject> Job getJob(Plugin<T> plugin, IndexService index)
    throws NotFoundException, GenericException, RequestNotValidException {
    String jobId = getJobId(plugin);
    if (jobId != null) {
      return index.retrieve(Job.class, jobId, new ArrayList<>());
    } else {
      throw new NotFoundException("Job not found");
    }
  }

  public static <T extends IsRODAObject> Job getJob(Plugin<T> plugin, ModelService model)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    String jobId = getJobId(plugin);
    return getJob(jobId, model);
  }

  public static Job getJob(String jobId, ModelService model)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    if (jobId != null) {
      return model.retrieveJob(jobId);
    } else {
      throw new NotFoundException("Job not found");
    }
  }

  public static String getJobUsername(String jobId, IndexService index)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    if (jobId != null) {
      Job job = index.retrieve(Job.class, jobId, Arrays.asList(RodaConstants.JOB_USERNAME));
      return job.getUsername();
    } else {
      throw new NotFoundException("Job not found");
    }
  }

  public static <T extends IsRODAObject> String getJobUsername(Plugin<T> plugin, IndexService index)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    String jobId = getJobId(plugin);
    return getJobUsername(jobId, index);
  }

  public static <T extends IsRODAObject> String getJobUsername(Plugin<T> plugin, ModelService model)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    String jobId = getJobId(plugin);
    if (jobId != null) {
      Job job = model.retrieveJob(jobId);
      return job.getUsername();
    } else {
      throw new NotFoundException("Job not found");
    }
  }

  public static <T extends IsRODAObject> Path getJobWorkingDirectory(Plugin<T> plugin) {
    return RodaCoreFactory.getWorkingDirectory().resolve(getJobId(plugin));
  }

  /**
   * Updates the job status for a particular plugin instance
   * 
   * @deprecated 201712 hsilva: use/rename to updateJobInformationAsync
   */
  @Deprecated
  public static <T extends IsRODAObject> void updateJobInformation(Plugin<T> plugin, JobPluginInfo jobPluginInfo)
    throws JobException {
    updateJobInformationAsync(plugin, jobPluginInfo);
  }

  public static <T extends IsRODAObject> boolean shouldPluginReportForItself(Plugin<T> plugin) throws JobException {
    Map<String, String> parameterValues = plugin.getParameterValues();

    return (!parameterValues.containsKey(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS)
      || plugin.getClass().getName().equals(parameterValues.get(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS)));
  }

  /**
   * Updates the job status for a particular plugin instance
   */
  public static <T extends IsRODAObject> void updateJobInformationAsync(Plugin<T> plugin, JobPluginInfo jobPluginInfo)
    throws JobException {
    if (shouldPluginReportForItself(plugin)) {
      RodaCoreFactory.getPluginOrchestrator().updateJobInformationAsync(plugin, jobPluginInfo);
    }
  }

  public static <T extends IsRODAObject> JobPluginInfo getInitialJobInformation(Plugin<T> plugin,
    int sourceObjectsBeingProcess) {
    JobPluginInfo jobPluginInfo;
    try {
      jobPluginInfo = plugin.getJobPluginInfo(SimpleJobPluginInfo.class);
    } catch (ClassCastException e) {
      jobPluginInfo = plugin.getJobPluginInfo(IngestJobPluginInfo.class);
    }

    if (jobPluginInfo != null) {
      jobPluginInfo.setSourceObjectsBeingProcessed(sourceObjectsBeingProcess).setSourceObjectsWaitingToBeProcessed(0);
      return jobPluginInfo;
    } else {
      return new SimpleJobPluginInfo();
    }
  }

  /**
   * 20180525 hsilva: not in use in RODA base source code, so this will be
   * removed in a near future
   */
  @Deprecated
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

  /***************** Plugin parameters related *****************/
  /*************************************************************/
  public static List<Class<? extends IsRODAObject>> getReindexObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(TransferredResource.class);
    list.add(AIP.class);
    list.add(RODAMember.class);
    list.add(RepresentationInformation.class);
    list.add(Notification.class);
    list.add(Risk.class);
    list.add(LogEntry.class);
    list.add(RiskIncidence.class);
    list.add(Job.class);
    list.add(IndexedPreservationAgent.class);
    list.add(IndexedPreservationEvent.class);
    list.add(DIP.class);
    list.add(Format.class);
    return list;
  }

  public static <T extends IsRODAObject> boolean getBooleanFromParameters(Plugin<T> plugin,
    PluginParameter pluginParameter) {
    return verifyIfStepShouldBePerformed(plugin, pluginParameter);
  }

  public static <T extends IsRODAObject> String getStringFromParameters(Plugin<T> plugin, String pluginParameterId,
    String defaultValue) {
    return plugin.getParameterValues().getOrDefault(pluginParameterId, defaultValue);
  }

  public static <T extends IsRODAObject> String getStringFromParameters(Plugin<T> plugin,
    PluginParameter pluginParameter) {
    return plugin.getParameterValues().getOrDefault(pluginParameter.getId(), pluginParameter.getDefaultValue());
  }

  // FIXME 20161128 hsilva: rename this to search scope
  public static <T extends IsRODAObject> String getParentIdFromParameters(Plugin<T> plugin) {
    return plugin.getParameterValues().get(RodaConstants.PLUGIN_PARAMS_PARENT_ID);
  }

  public static <T extends IsRODAObject> Optional<String> getSearchScopeFromParameters(Plugin<T> plugin,
    ModelService model) {
    Optional<String> ret = Optional.empty();
    try {
      String searchScopeFromParameters = plugin.getParameterValues().get(RodaConstants.PLUGIN_PARAMS_PARENT_ID);
      if (StringUtils.isNotBlank(searchScopeFromParameters)) {
        AIP aip = model.retrieveAIP(searchScopeFromParameters);
        ret = Optional.ofNullable(aip.getId());
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      // do nothing
    }
    return ret;
  }

  // FIXME 20161128 hsilva: rename this to force search scope
  public static <T extends IsRODAObject> boolean getForceParentIdFromParameters(Plugin<T> plugin) {
    return new Boolean(plugin.getParameterValues().get(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID));
  }

  /*********************************/
  public static Optional<String> getComputedParent(ModelService model, IndexService index, List<String> ancestors,
    Optional<String> computedSearchScope, boolean forceSearchScope, String jobId) {
    if (ancestors.isEmpty()) {
      return computedSearchScope;
    }
    return resolveParent(model, index, ancestors, computedSearchScope, forceSearchScope, jobId);
  }

  private static Optional<String> resolveParent(ModelService model, IndexService index, List<String> ancestorsFromSIP,
    Optional<String> computedSearchScope, boolean forceParent, String jobId) {
    Optional<String> parent = computedSearchScope;

    if (forceParent) {
      parent = computedSearchScope;
    } else {
      List<String> ancestors = new ArrayList<>(ancestorsFromSIP);

      // Reverse list so that the top ancestors come first
      Collections.reverse(ancestors);

      try {
        for (String ancestor : ancestors) {
          Optional<String> computedAncestorId = getAncestorById(ancestor, parent, index, RodaConstants.INGEST_SIP_IDS);
          if (!computedAncestorId.isPresent()) {
            computedAncestorId = getAncestorById(ancestor, parent, index, RodaConstants.INDEX_UUID);
          }

          if (computedAncestorId.isPresent()) {
            parent = computedAncestorId;
          } else {
            parent = createGhost(ancestor, parent, model, index, jobId);
          }
        }
      } catch (NotFoundException | GenericException | RequestNotValidException | AlreadyExistsException
        | AuthorizationDeniedException e) {
        parent = computedSearchScope;
      }
    }
    return parent;

  }

  private static Optional<String> createGhost(String ancestor, Optional<String> parent, ModelService model,
    IndexService index, String jobId) throws NotFoundException, GenericException, RequestNotValidException,
    AlreadyExistsException, AuthorizationDeniedException {
    String username = getJobUsername(jobId, index);

    Permissions permissions = new Permissions();
    permissions.setUserPermissions(username,
      new HashSet<>(Arrays.asList(Permissions.PermissionType.CREATE, Permissions.PermissionType.READ,
        Permissions.PermissionType.UPDATE, Permissions.PermissionType.DELETE, Permissions.PermissionType.GRANT)));

    boolean isGhost = true;
    AIP ghostAIP = model.createAIP(parent.orElse(null), "", permissions, Arrays.asList(ancestor), jobId, true, username,
      isGhost);

    return Optional.ofNullable(ghostAIP.getId());
  }

  private static Optional<String> getAncestorById(String ancestor, Optional<String> computedSearchScope,
    IndexService index, String aipField) {
    if (ancestor.equalsIgnoreCase(computedSearchScope.orElse(null))) {
      return computedSearchScope;
    }

    Optional<String> ancestorBySIPId = Optional.empty();
    Filter ancestorFilter = new Filter(new SimpleFilterParameter(aipField, ancestor));
    if (computedSearchScope.isPresent()) {
      try {
        IndexedAIP computedParent = index.retrieve(IndexedAIP.class, computedSearchScope.get(),
          Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_ANCESTORS));
        ancestorFilter.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, computedParent.getId()));
      } catch (NotFoundException | GenericException e) {
        // Do nothing
      }
    }

    try {
      // TODO 2016-11-24 sleroux: add user permission
      IndexResult<IndexedAIP> result = index.find(IndexedAIP.class, ancestorFilter, Sorter.NONE, new Sublist(0, 1),
        Arrays.asList(RodaConstants.INDEX_UUID));

      if (result.getTotalCount() >= 1) {
        IndexedAIP indexedAIP = result.getResults().get(0);
        ancestorBySIPId = Optional.ofNullable(indexedAIP.getId());
      }
    } catch (GenericException | RequestNotValidException e) {
      // Do nothing
      LOGGER.error("Error getting ancestor", e);
    }
    return ancestorBySIPId;
  }

  /*******************************************************/

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
    return verifyIfStepShouldBePerformed(plugin, pluginParameter, null);
  }

  public static <T extends IsRODAObject> boolean verifyIfStepShouldBePerformed(Plugin<T> plugin,
    PluginParameter pluginParameter, String parameterClass) {
    String paramValue = getStringFromParameters(plugin, pluginParameter);
    boolean perform = Boolean.parseBoolean(paramValue);

    if (perform && parameterClass != null && RodaCoreFactory.getPluginManager().getPlugin(parameterClass) == null) {
      perform = false;
    }

    return perform;
  }

  /***************** Plugin related *****************/
  /**************************************************/
  /**
   * 
   * @param requestUuid
   *          uniq identifier of this request
   */
  public static <T extends IsRODAObject> void acquireObjectLock(String lite, Plugin<T> plugin) throws LockingException {
    String requestUuid = plugin.getParameterValues().getOrDefault(RodaConstants.PLUGIN_PARAMS_LOCK_REQUEST_UUID,
      IdUtils.createUUID());
    acquireObjectLock(Arrays.asList(lite), requestUuid);
  }

  public static <O extends IsRODAObject, P extends IsRODAObject> void acquireObjectLock(O object, Plugin<P> plugin)
    throws LockingException {
    Optional<LiteRODAObject> liteOptionl = LiteRODAObjectFactory.get(object);
    if (liteOptionl.isPresent()) {
      PluginHelper.acquireObjectLock(liteOptionl.get().getInfo(), plugin);
    } else {
      throw new LockingException(
        "Error getting lite from IndexedAIP with ID '{}' in order to obtain lock" + object.getId());
    }
  }

  /**
   * 
   * @param requestUuid
   *          uniq identifier of this request
   */
  public static void acquireObjectLock(String lite, String requestUuid) throws LockingException {
    acquireObjectLock(Arrays.asList(lite), requestUuid);
  }

  /**
   * 
   * @param requestUuid
   *          uniq identifier of this request
   */
  public static void acquireObjectLock(List<String> lites, String requestUuid) throws LockingException {
    RodaCoreFactory.getPluginOrchestrator().acquireObjectLock(lites, PluginHelper.getLockRequestTimeout(), true,
      requestUuid);
  }

  private static int getLockRequestTimeout() {
    return RodaCoreFactory.getRodaConfiguration().getInt(LOCK_REQUEST_TIMEOUT, DEFAULT_LOCK_REQUEST_TIMEOUT);
  }

  /**
   * 
   * @param requestUuid
   *          uniq identifier of this request
   */
  public static <T extends IsRODAObject> void releaseObjectLock(String lite, Plugin<T> plugin) throws LockingException {
    String requestUuid = plugin.getParameterValues().getOrDefault(RodaConstants.PLUGIN_PARAMS_LOCK_REQUEST_UUID,
      IdUtils.createUUID());
    releaseObjectLock(Arrays.asList(lite), requestUuid);
  }

  public static <O extends IsRODAObject, P extends IsRODAObject> void releaseObjectLock(O object, Plugin<P> plugin)
    throws LockingException {
    Optional<LiteRODAObject> liteOptionl = LiteRODAObjectFactory.get(object);
    if (liteOptionl.isPresent()) {
      PluginHelper.releaseObjectLock(liteOptionl.get().getInfo(), plugin);
    } else {
      throw new LockingException(
        "Error getting lite from IndexedAIP with ID '{}' in order to obtain lock" + object.getId());
    }
  }

  /**
   * 
   * @param requestUuid
   *          uniq identifier of this request
   */
  public static void releaseObjectLock(String lite, String requestUuid) {
    releaseObjectLock(Arrays.asList(lite), requestUuid);
  }

  /**
   * 
   * @param requestUuid
   *          uniq identifier of this request
   */
  public static void releaseObjectLock(List<String> lites, String requestUuid) {
    RodaCoreFactory.getPluginOrchestrator().releaseObjectLockAsync(lites, requestUuid);
  }

  public static <T extends IsRODAObject> String getPluginAgentId(Plugin<T> plugin) {
    return IdUtils.getPluginAgentId(plugin.getClass().getName(), plugin.getVersion());
  }

  public static void createSubmission(ModelService model, boolean createSubmission, Path submissionPath, String aipId)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    if (createSubmission) {
      if (FSUtils.isDirectory(submissionPath)) {
        StorageService submissionStorage = new FileStorageService(submissionPath, false, null, false);
        StoragePath submissionStoragePath = DefaultStoragePath.empty();
        model.createSubmission(submissionStorage, submissionStoragePath, aipId);
      } else {
        model.createSubmission(submissionPath, aipId);
      }
    }
  }

  public static Risk createRiskIfNotExists(ModelService model, String riskId, ClassLoader pluginClassLoader)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    try {
      return model.retrieveRisk(riskId);
    } catch (NotFoundException e) {
      return createDefaultRisk(model, riskId, pluginClassLoader);
    }
  }

  private static Risk createDefaultRisk(ModelService model, String riskId, ClassLoader pluginClassLoader)
    throws GenericException {
    String defaultFile = RodaConstants.CORE_DATA_FOLDER + '/' + RodaConstants.CORE_STORAGE_FOLDER + '/'
      + RodaConstants.CORE_RISK_FOLDER + '/' + riskId + ".json";

    try (InputStream inputStream = RodaCoreFactory.getDefaultFileAsStream(defaultFile, pluginClassLoader)) {
      if (inputStream == null) {
        throw new GenericException("Could not create a default risk because resource was not found: " + defaultFile);
      }

      Risk risk = JsonUtils.getObjectFromJson(inputStream, Risk.class);
      risk.setId(riskId);
      return model.createRisk(risk, false);
    } catch (GenericException | IOException e) {
      LOGGER.error("Could not create a default risk", e);
      throw new GenericException(e);
    }
  }

  /***************** Preservation events related *****************/
  /***************************************************************/
  /**
   * For SIP > AIP
   */

  public static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipId,
    ModelService model, IndexService index, TransferredResource source, PluginState outcome,
    String outcomeDetailExtension, boolean notify, Date eventDate) throws RequestNotValidException, NotFoundException,
    GenericException, AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    List<LinkingIdentifier> sources = Arrays
      .asList(PluginHelper.getLinkingIdentifier(source, RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));
    List<LinkingIdentifier> outcomes = Arrays
      .asList(PluginHelper.getLinkingIdentifier(aipId, RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));

    return createPluginEvent(plugin, aipId, null, null, null, model, index, sources, outcomes, outcome,
      outcomeDetailExtension, notify, eventDate);
  }

  public static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipId,
    ModelService model, IndexService index, TransferredResource source, PluginState outcome,
    String outcomeDetailExtension, boolean notify) throws RequestNotValidException, NotFoundException, GenericException,
    AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    return createPluginEvent(plugin, aipId, model, index, source, outcome, outcomeDetailExtension, notify, new Date());
  }

  /**
   * For AIP as source only
   */
  public static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipId,
    ModelService model, IndexService index, PluginState outcome, String outcomeDetailExtension, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    ValidationException, AlreadyExistsException {
    List<LinkingIdentifier> sources = Arrays
      .asList(PluginHelper.getLinkingIdentifier(aipId, RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));
    List<LinkingIdentifier> outcomes = null;
    return createPluginEvent(plugin, aipId, null, null, null, model, index, sources, outcomes, outcome,
      outcomeDetailExtension, notify, new Date());
  }

  // used by migration plugin
  public static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipId,
    ModelService model, IndexService index, PluginState outcome, String outcomeDetailExtension, boolean notify,
    Date eventDate) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    ValidationException, AlreadyExistsException {
    List<LinkingIdentifier> sources = Arrays
      .asList(PluginHelper.getLinkingIdentifier(aipId, RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));
    List<LinkingIdentifier> outcomes = Arrays
      .asList(PluginHelper.getLinkingIdentifier(aipId, RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));
    return createPluginEvent(plugin, aipId, null, null, null, model, index, sources, outcomes, outcome,
      outcomeDetailExtension, notify, eventDate);
  }

  /**
   * For AIP
   */
  public static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipId,
    ModelService model, IndexService index, List<LinkingIdentifier> sources, List<LinkingIdentifier> targets,
    PluginState outcome, String outcomeDetailExtension, boolean notify) throws RequestNotValidException,
    NotFoundException, GenericException, AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    return createPluginEvent(plugin, aipId, null, null, null, model, index, sources, targets, outcome,
      outcomeDetailExtension, notify, new Date());
  }

  /**
   * For REPRESENTATION
   */
  public static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipId,
    String representationId, ModelService model, IndexService index, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcome, String outcomeDetailExtension, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    ValidationException, AlreadyExistsException {
    return createPluginEvent(plugin, aipId, representationId, null, null, model, index, sources, targets, outcome,
      outcomeDetailExtension, notify, new Date());
  }

  /**
   * For FILE
   */
  public static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipId,
    String representationId, List<String> filePath, String fileId, ModelService model, IndexService index,
    List<LinkingIdentifier> sources, List<LinkingIdentifier> outcomes, PluginState outcome,
    String outcomeDetailExtension, boolean notify) throws RequestNotValidException, NotFoundException, GenericException,
    AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    return createPluginEvent(plugin, aipId, representationId, filePath, fileId, model, index, sources, outcomes,
      outcome, outcomeDetailExtension, notify, new Date());
  }

  private static <T extends IsRODAObject> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipId,
    String representationId, List<String> filePath, String fileId, ModelService model, IndexService index,
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
      job = getJob(plugin, index);
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
    model.createPreservationMetadata(PreservationMetadataType.EVENT, id, aipId, representationId, filePath, fileId,
      premisEvent, notify);
    PreservationMetadata pm = new PreservationMetadata();
    pm.setId(id);
    pm.setAipId(aipId);
    pm.setRepresentationId(representationId);
    pm.setFileDirectoryPath(filePath);
    pm.setFileId(fileId);
    pm.setType(PreservationMetadataType.EVENT);
    return pm;
  }

  public static LinkingIdentifier getLinkingIdentifier(TransferredResource transferredResource, String role) {
    LinkingIdentifier li = new LinkingIdentifier();
    li.setValue(LinkingObjectUtils.getLinkingIdentifierId(transferredResource));
    li.setType(RodaConstants.URN_TYPE);
    li.setRoles(Arrays.asList(role));
    return li;
  }

  public static LinkingIdentifier getLinkingIdentifier(String aipId, String role) {
    return getLinkingIdentifier(RODA_TYPE.AIP, aipId, role);
  }

  public static LinkingIdentifier getLinkingIdentifier(String aipId, String representationId, String role) {
    return getLinkingIdentifier(RODA_TYPE.REPRESENTATION, IdUtils.getRepresentationId(aipId, representationId), role);
  }

  public static LinkingIdentifier getLinkingIdentifier(String aipId, String representationId, List<String> filePath,
    String fileId, String role) {
    return getLinkingIdentifier(RODA_TYPE.FILE, IdUtils.getFileId(aipId, representationId, filePath, fileId), role);
  }

  private static LinkingIdentifier getLinkingIdentifier(RODA_TYPE type, String uuid, String role) {
    LinkingIdentifier li = new LinkingIdentifier();
    li.setValue(LinkingObjectUtils.getLinkingIdentifierId(type, uuid));
    li.setType(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN);
    li.setRoles(Arrays.asList(role));
    return li;
  }

  public static <T extends IsRODAObject> void moveSIPs(Plugin<T> plugin, ModelService model, IndexService index,
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
        updateReportsAndIngestInfoAfterMovingSIPs(model, jobPluginInfo, successOldToNewTransferredResourceIds);
      }
    } catch (GenericException | NotFoundException e) {
      LOGGER.error("Error moving successfully ingested SIPs", e);
    } catch (IsStillUpdatingException e) {
      LOGGER.warn("TransferredResources are already being indexed");
    }

    try {
      if (!unsuccess.isEmpty()) {
        unsuccessOldToNewTransferredResourceIds = RodaCoreFactory.getTransferredResourcesScanner()
          .moveTransferredResource(unsuccessPath, unsuccess, true);
        updateReportsAndIngestInfoAfterMovingSIPs(model, jobPluginInfo, unsuccessOldToNewTransferredResourceIds);
      }
    } catch (GenericException | NotFoundException e) {
      LOGGER.error("Error moving unsuccessfully ingested SIPs", e);
    } catch (IsStillUpdatingException e) {
      LOGGER.warn("TransferredResources are already being indexed");
    }

    // update Job (with all new ids)
    successOldToNewTransferredResourceIds.putAll(unsuccessOldToNewTransferredResourceIds);
    updateJobAfterMovingSIPsAsync(plugin, index, successOldToNewTransferredResourceIds);
  }

  private static void updateReportsAndIngestInfoAfterMovingSIPs(ModelService model, IngestJobPluginInfo jobPluginInfo,
    Map<String, String> oldToNewTransferredResourceIds) {
    updateReportsAfterMovingSIPs(model, jobPluginInfo, oldToNewTransferredResourceIds);
  }

  /**
   * 20180914 hsilva: use updateReportsAndIngestInfoAfterMovingSIPs instead
   * (just different method name)
   */
  @Deprecated
  private static void updateReportsAfterMovingSIPs(ModelService model, IngestJobPluginInfo jobPluginInfo,
    Map<String, String> oldToNewTransferredResourceIds) {
    for (Entry<String, String> oldToNewId : oldToNewTransferredResourceIds.entrySet()) {
      String oldSIPId = oldToNewId.getKey();
      String newSIPId = oldToNewId.getValue();
      for (Report report : jobPluginInfo.getAllReports().getOrDefault(oldSIPId, Collections.emptyMap()).values()) {
        report.setSourceAndOutcomeObjectId(newSIPId, report.getOutcomeObjectId());
        if (!report.getReports().isEmpty()) {
          report.getReports().get(0).setSourceAndOutcomeObjectId(newSIPId, report.getOutcomeObjectId());
        }

        // update ingest info
        jobPluginInfo.replaceTransferredResourceId(oldSIPId, newSIPId);

        // update in model
        updateJobReport(model, report);
      }
    }
  }

  /**
   * Update Job source object ids (done asynchronously)
   */
  private static <T extends IsRODAObject> void updateJobAfterMovingSIPsAsync(Plugin<T> plugin, IndexService index,
    Map<String, String> oldToNewTransferredResourceIds) {
    try {
      Job job = getJob(plugin, index);
      SelectedItems<?> sourceObjects = job.getSourceObjects();
      if (sourceObjects instanceof SelectedItemsList) {
        RodaCoreFactory.getPluginOrchestrator().updateJobAsync(plugin,
          new Messages.JobSourceObjectsUpdated(oldToNewTransferredResourceIds));
      }
    } catch (NotFoundException | GenericException | RequestNotValidException e) {
      LOGGER.error("Error retrieving Job", e);
    }
  }

  public static void fixParents(IndexService index, ModelService model, Optional<String> jobId,
    Optional<String> computedSearchScope, String updatedBy)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {

    // collect all ghost ids
    Map<String, List<String>> aipIdToGhost = new HashMap<>();
    Map<String, List<String>> sipIdToGhost = new HashMap<>();

    Filter ghostsFilter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_GHOST, Boolean.TRUE.toString()));
    jobId.ifPresent(id -> ghostsFilter.add(new SimpleFilterParameter(RodaConstants.INGEST_JOB_ID, id)));
    try (IterableIndexResult<IndexedAIP> ghosts = index.findAll(IndexedAIP.class, ghostsFilter,
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.INGEST_SIP_IDS, RodaConstants.AIP_GHOST))) {

      for (IndexedAIP aip : ghosts) {
        List<String> temp = new ArrayList<>();

        if (aip.getIngestSIPIds() != null && !aip.getIngestSIPIds().isEmpty()) {
          String firstIngestSIPId = aip.getIngestSIPIds().get(0);
          if (sipIdToGhost.containsKey(firstIngestSIPId)) {
            temp = sipIdToGhost.get(firstIngestSIPId);
          }
          temp.add(aip.getId());
          sipIdToGhost.put(firstIngestSIPId, temp);
        } else {
          if (aipIdToGhost.containsKey(aip.getId())) {
            temp = aipIdToGhost.get(aip.getId());
          }
          temp.add(aip.getId());
          aipIdToGhost.put(aip.getId(), temp);
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error getting AIPs when fixing parents", e);
    }

    for (Map.Entry<String, List<String>> entry : sipIdToGhost.entrySet()) {
      Filter nonGhostsFilter = new Filter(new SimpleFilterParameter(RodaConstants.INGEST_SIP_IDS, entry.getKey()),
        new SimpleFilterParameter(RodaConstants.AIP_GHOST, Boolean.FALSE.toString()));

      computedSearchScope
        .ifPresent(id -> nonGhostsFilter.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, id)));

      IndexResult<IndexedAIP> result = index.find(IndexedAIP.class, nonGhostsFilter, Sorter.NONE, new Sublist(0, 1),
        Arrays.asList(RodaConstants.INDEX_UUID));

      if (result.getTotalCount() > 1) {
        LOGGER.debug("Couldn't find non-ghost AIP with ingest SIP ids {}", entry.getKey());
      } else if (result.getTotalCount() == 1) {
        IndexedAIP newParentIAIP = result.getResults().get(0);
        for (String id : entry.getValue()) {
          moveChildrenAIPsAndDelete(index, model, id, newParentIAIP.getId(), computedSearchScope, updatedBy);
        }
      } else if (result.getTotalCount() == 0) {
        String ghostIdToKeep = entry.getValue().get(0);
        if (entry.getValue().size() > 1) {
          for (int i = 1; i < entry.getValue().size(); i++) {
            updateParent(index, model, entry.getValue().get(i), ghostIdToKeep, computedSearchScope, updatedBy);
          }
        }
      }
    }
  }

  private static void updateParent(IndexService index, ModelService model, String aipId, String newParentId,
    Optional<String> searchScope, String updatedBy)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    Filter parentFilter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, aipId));
    searchScope.ifPresent(id -> parentFilter.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, id)));
    index.execute(IndexedAIP.class, parentFilter, Arrays.asList(RodaConstants.INDEX_UUID), child -> {
      try {
        AIP aip = model.retrieveAIP(child.getId());
        aip.setParentId(newParentId);
        model.updateAIP(aip, updatedBy);
      } catch (NotFoundException e) {
        LOGGER.debug("Can't move child. It wasn't found.", e);
      }
    }, e -> LOGGER.debug("Can't move child.", e));
    try {
      model.deleteAIP(aipId);
    } catch (NotFoundException e) {
      LOGGER.debug("Can't delete ghost or move node. It wasn't found.", e);
    }
  }

  private static void moveChildrenAIPsAndDelete(IndexService index, ModelService model, String aipId,
    String newParentId, Optional<String> searchScope, String updatedBy)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    Filter parentFilter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, aipId));
    searchScope.ifPresent(id -> parentFilter.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, id)));

    try (IterableIndexResult<IndexedAIP> result = index.findAll(IndexedAIP.class, parentFilter, false,
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_ID))) {

      for (IndexedAIP aip : result) {
        try {
          model.moveAIP(aip.getId(), newParentId, updatedBy);
        } catch (NotFoundException e) {
          LOGGER.debug("Can't move child. It wasn't found.", e);
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error getting children AIPs when moving and deleting them", e);
    }

    try {
      model.deleteAIP(aipId);
    } catch (NotFoundException e) {
      LOGGER.debug("Can't delete ghost or move node. It wasn't found.", e);
    }

  }

  public static void createAndExecuteJob(Job job) throws GenericException, JobAlreadyStartedException,
    RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().createJob(job);
    RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);
    RodaCoreFactory.getIndexService().commit(Job.class);
  }

  public static String getReindexPluginName(Class<?> reindexClass) throws NotFoundException {
    if (reindexClass.equals(AIP.class)) {
      return ReindexAIPPlugin.class.getName();
    } else if (reindexClass.equals(RepresentationInformation.class)) {
      return ReindexRepresentationInformationPlugin.class.getName();
    } else if (reindexClass.equals(Risk.class)) {
      return ReindexRiskPlugin.class.getName();
    } else if (reindexClass.equals(RiskIncidence.class)) {
      return ReindexIncidencePlugin.class.getName();
    } else if (reindexClass.equals(Job.class)) {
      return ReindexJobPlugin.class.getName();
    } else if (reindexClass.equals(Notification.class)) {
      return ReindexNotificationPlugin.class.getName();
    } else if (reindexClass.equals(TransferredResource.class)) {
      return ReindexTransferredResourcePlugin.class.getName();
    } else if (reindexClass.equals(RODAMember.class)) {
      return ReindexRodaMemberPlugin.class.getName();
    } else if (reindexClass.equals(LogEntry.class)) {
      return ReindexActionLogPlugin.class.getName();
    } else if (reindexClass.equals(IndexedPreservationAgent.class)) {
      return ReindexPreservationAgentPlugin.class.getName();
    } else if (reindexClass.equals(IndexedPreservationEvent.class)) {
      return ReindexPreservationRepositoryEventPlugin.class.getName();
    } else if (reindexClass.equals(DIP.class)) {
      return ReindexDIPPlugin.class.getName();
    } else if (reindexClass.equals(Format.class)) {
      return ReindexFormatPlugin.class.getName();
    } else {
      throw new NotFoundException("No reindex plugin available");
    }
  }

  /**
   * 20180525 hsilva: autoLocking=true should only be used by
   * PluginHelper.processObjects methods
   * 
   * @throws LockingException
   * 
   * @deprecated 20180525 hsilva: this methods should not be used directly, but
   *             instead one should use PluginHelper.processObjects methods,
   *             that is why this method will become private in a near future
   */
  public static <T extends IsRODAObject> List<T> transformLitesIntoObjects(ModelService model, Plugin<T> plugin,
    Report report, JobPluginInfo pluginInfo, List<LiteOptionalWithCause> lites, Job job, boolean autoLocking)
    throws LockingException {
    List<T> finalObjects = new ArrayList<>();
    List<LiteRODAObject> objectsToLock = new ArrayList<>();

    for (LiteOptionalWithCause lite : lites) {
      String failureMessage = "";
      Optional<LiteRODAObject> optionalLite = lite.getLite();

      if (optionalLite.isPresent() && StringUtils.isNotBlank(optionalLite.get().getInfo())) {
        boolean objectMatchPluginKnownObjectsClass = false;

        String liteString = optionalLite.get().getInfo();
        for (Class<T> pluginClass : plugin.getObjectClasses()) {
          if (liteString.startsWith(pluginClass.getName())) {
            objectMatchPluginKnownObjectsClass = true;
            break;
          }
        }

        if (objectMatchPluginKnownObjectsClass) {
          if (autoLocking) {
            objectsToLock.add(optionalLite.get());
          } else {
            OptionalWithCause<T> retrievedObject = (OptionalWithCause<T>) model
              .retrieveObjectFromLite(optionalLite.get());
            if (retrievedObject.isPresent()) {
              finalObjects.add(retrievedObject.get());
            } else {
              RODAException exception = retrievedObject.getCause();
              if (exception != null) {
                failureMessage = "RODA object conversion from lite throwed an error: [" + exception.getClass().getName()
                  + "] " + exception.getMessage();
              } else {
                failureMessage = "RODA object conversion from lite throwed an error.";
              }
            }
          }
        } else {
          failureMessage = "RODA object conversion from lite has failed because lite object class does not match any of the plugin known object classes "
            + "(which might be caused by blank lite).";
        }
      } else {
        failureMessage = "Lite object has an error: [" + lite.getExceptionClass() + "] " + lite.getExceptionMessage();
      }

      reportFailureTransformingLiteInObject(model, plugin, report, pluginInfo, job, lite, failureMessage, optionalLite);
    }

    if (autoLocking) {
      // in order to allow reentrant locking in sub plugins invocations (if any)
      String requestUuid = plugin.getParameterValues().getOrDefault(RodaConstants.PLUGIN_PARAMS_LOCK_REQUEST_UUID,
        IdUtils.createUUID());
      plugin.getParameterValues().put(RodaConstants.PLUGIN_PARAMS_LOCK_REQUEST_UUID, requestUuid);
      PluginHelper.acquireObjectLock(objectsToLock.stream().map(obj -> obj.getInfo()).collect(Collectors.toList()),
        requestUuid);

      String failureMessage = "";
      for (LiteRODAObject object : objectsToLock) {
        OptionalWithCause<T> retrievedObject = (OptionalWithCause<T>) model.retrieveObjectFromLite(object);
        if (retrievedObject.isPresent()) {
          finalObjects.add(retrievedObject.get());
        } else {
          RODAException exception = retrievedObject.getCause();
          if (exception != null) {
            failureMessage = "RODA object conversion from lite throwed an error: [" + exception.getClass().getName()
              + "] " + exception.getMessage();
          } else {
            failureMessage = "RODA object conversion from lite throwed an error.";
          }
        }

        reportFailureTransformingLiteInObject(model, plugin, report, pluginInfo, job, LiteOptionalWithCause.of(object),
          failureMessage, Optional.of(object));
      }

    }

    return finalObjects;
  }

  private static <T extends IsRODAObject> void reportFailureTransformingLiteInObject(ModelService model,
    Plugin<T> plugin, Report report, JobPluginInfo pluginInfo, Job job, LiteOptionalWithCause lite,
    String failureMessage, Optional<LiteRODAObject> optionalLite) {
    if (StringUtils.isNotBlank(failureMessage)) {
      if (pluginInfo != null) {
        pluginInfo.incrementObjectsProcessedWithFailure();
      }

      if (report != null) {
        String id = lite.toString();
        if (optionalLite.isPresent()) {
          String[] split = optionalLite.get().getInfo().split(LiteRODAObjectFactory.SEPARATOR_REGEX);
          try {
            id = split[1];
          } catch (ArrayIndexOutOfBoundsException e) {
            failureMessage += "\nError when getting uuid of lite object " + id + ".";
          }
        }

        Report reportItem = PluginHelper.initPluginReportItem(plugin, id, LiteRODAObject.class);
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(failureMessage);
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
      }
    }
  }

  public static String createOutcomeTextForAIP(IndexedAIP item, String actionMessage) {
    SimpleDateFormat format = new SimpleDateFormat(RodaConstants.SIMPLE_DATE_FORMATTER);
    StringBuilder outcomeText = new StringBuilder("Archival Information Package [id: ").append(item.getId());

    if (StringUtils.isNotBlank(item.getTitle())) {
      outcomeText.append("; title: ").append(item.getTitle());
    }

    if (StringUtils.isNotBlank(item.getLevel())) {
      outcomeText.append("; level: ").append(item.getLevel());
    }

    if (item.getDateInitial() != null) {
      outcomeText.append("; initial date: ").append(format.format(item.getDateInitial()));
    }

    if (item.getDateFinal() != null) {
      outcomeText.append("; end date: ").append(format.format(item.getDateFinal()));
    }

    outcomeText.append("] ").append(actionMessage);
    return outcomeText.toString();
  }
}
