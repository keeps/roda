/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.notifications.EmailNotificationProcessor;
import org.roda.core.common.notifications.HTTPNotificationProcessor;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.IngestJobPluginInfo;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.base.DescriptiveMetadataValidationPlugin;
import org.roda.core.plugins.plugins.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.characterization.SiegfriedPlugin;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.base.CaseFormat;

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
  public static final String END_PARTIAL = "The ingest process ended, however, some of the SIPs could not be successfully ingested.";
  public static final String END_DESCRIPTION = "The ingest process has ended.";
  public static final PreservationEventType END_TYPE = PreservationEventType.INGEST_END;

  protected static final int INITIAL_TOTAL_STEPS = 10;
  protected int totalSteps = INITIAL_TOTAL_STEPS;

  public static final String PLUGIN_CLASS_DIGITAL_SIGNATURE = "org.roda.core.plugins.external.DigitalSignaturePlugin";
  public static final String PLUGIN_CLASS_VERAPDF = "org.roda.core.plugins.external.VeraPDFPlugin";
  public static final String PLUGIN_CLASS_TIKA_FULLTEXT = "org.roda.core.plugins.external.TikaFullTextPlugin";

  public static final String PLUGIN_PARAMS_DO_VERAPDF_CHECK = "parameter.do_verapdf_check";
  public static final String PLUGIN_PARAMS_DO_FEATURE_EXTRACTION = "parameter.do_feature_extraction";
  public static final String PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION = "parameter.do_fulltext_extraction";
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
      mergeReports(jobPluginInfo, pluginReport);
      final List<AIP> aips = getAIPsFromReports(model, index, jobPluginInfo);
      PluginHelper.updateJobInformationAsync(this, jobPluginInfo.incrementStepsCompletedByOne());

      // this event can only be created after AIPs exist and that's why it is
      // performed here, after transformTransferredResourceIntoAnAIP
      createIngestStartedEvent(model, index, jobPluginInfo, startDate, cachedJob);

      // 2) virus check
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK))) {
        pluginReport = doVirusCheck(index, model, storage, aips, jobPluginInfo);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, jobPluginInfo, aips, true);
        PluginHelper.updateJobInformationAsync(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 3) descriptive metadata validation
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION))) {
        pluginReport = doDescriptiveMetadataValidation(index, model, storage, aips, jobPluginInfo);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, jobPluginInfo, aips, true);
        PluginHelper.updateJobInformationAsync(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 4) create file fixity information
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON))) {
        pluginReport = createFileFixityInformation(index, model, storage, aips, jobPluginInfo);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, jobPluginInfo, aips, true);
        PluginHelper.updateJobInformationAsync(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 5) format identification (using Siegfried)
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION))) {
        pluginReport = doFileFormatIdentification(index, model, storage, aips, jobPluginInfo);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, jobPluginInfo, aips, false);
        PluginHelper.updateJobInformationAsync(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 6) Format validation - PDF/A format validator (using VeraPDF)
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(PLUGIN_PARAMS_DO_VERAPDF_CHECK), PLUGIN_CLASS_VERAPDF)) {
        Map<String, String> params = new HashMap<>();
        params.put("profile", "1b");
        pluginReport = doVeraPDFCheck(index, model, storage, aips, params, jobPluginInfo);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, jobPluginInfo, aips, false);
        PluginHelper.updateJobInformationAsync(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 7.1) feature extraction (using Apache Tika)
      // 7.2) full-text extraction (using Apache Tika)
      if (!aips.isEmpty() && (PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(PLUGIN_PARAMS_DO_FEATURE_EXTRACTION), PLUGIN_CLASS_TIKA_FULLTEXT)
        || PluginHelper.verifyIfStepShouldBePerformed(this, getPluginParameter(PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION),
          PLUGIN_CLASS_TIKA_FULLTEXT))) {
        Map<String, String> params = new HashMap<>();
        params.put(PLUGIN_PARAMS_DO_FEATURE_EXTRACTION, PluginHelper.verifyIfStepShouldBePerformed(this,
          getPluginParameter(PLUGIN_PARAMS_DO_FEATURE_EXTRACTION), PLUGIN_CLASS_TIKA_FULLTEXT) ? "true" : "false");
        params.put(RodaConstants.PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION, PluginHelper.verifyIfStepShouldBePerformed(this,
          getPluginParameter(PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION), PLUGIN_CLASS_TIKA_FULLTEXT) ? "true" : "false");
        pluginReport = doFeatureAndFullTextExtraction(index, model, storage, aips, params, jobPluginInfo);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, jobPluginInfo, aips, false);
        PluginHelper.updateJobInformationAsync(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 8) validation of digital signature
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION), PLUGIN_CLASS_DIGITAL_SIGNATURE)) {
        pluginReport = doDigitalSignatureValidation(index, model, storage, aips, jobPluginInfo);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, jobPluginInfo, aips, false);
        PluginHelper.updateJobInformationAsync(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 9) verify producer authorization
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK))) {
        pluginReport = verifyProducerAuthorization(index, model, storage, aips, jobPluginInfo);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, jobPluginInfo, aips, true);
        PluginHelper.updateJobInformationAsync(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 10) Auto accept
      if (!aips.isEmpty()) {
        if (PluginHelper.verifyIfStepShouldBePerformed(this,
          getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT))) {
          pluginReport = doAutoAccept(index, model, storage, aips, jobPluginInfo);
          mergeReports(jobPluginInfo, pluginReport);
          recalculateAIPsList(model, index, jobPluginInfo, aips, true);
          jobPluginInfo.incrementStepsCompletedByOne();
        } else {
          updateAIPsToBeAppraised(model, aips, jobPluginInfo, cachedJob);
        }
      }

      // X) move SIPs to PROCESSED folder??? (default: false)
      if (PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT))
        && RodaCoreFactory.getRodaConfiguration()
          .getBoolean(RodaConstants.CORE_TRANSFERRED_RESOURCES_INGEST_MOVE_WHEN_AUTOACCEPT, false)) {
        PluginHelper.moveSIPs(this, model, index, resources, jobPluginInfo);
      }

      createIngestEndedEvent(model, index, jobPluginInfo, cachedJob);

      getAfterExecute().ifPresent(e -> e.execute(jobPluginInfo, aips));

      // X) final job info update
      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformationAsync(this, jobPluginInfo);
    } catch (JobException e) {
      // throw new PluginException("A job exception has occurred", e);
    } finally {
      // remove locks if any
      PluginHelper.releaseObjectLock(this);
    }
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    LOGGER.debug("Doing stuff in afterAllExecute");
    try {
      Job job = PluginHelper.getJob(this, model);
      JobStats jobStats = job.getJobStats();

      boolean whenFailed = PluginHelper.getBooleanFromParameters(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_NOTIFICATION_WHEN_FAILED));

      if (whenFailed && jobStats.getSourceObjectsProcessedWithFailure() == 0) {
        // do nothing
      } else {
        sendNotification(model, index, job, jobStats);
      }
    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Could not send ingest notification", e);
    }

    try {
      index.commitAIPs();
      PluginHelper.fixParents(index, model, Optional.ofNullable(PluginHelper.getJobId(this)),
        PluginHelper.getSearchScopeFromParameters(this, model), PluginHelper.getJobUsername(this, index));
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Could not fix parents", e);
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

  private void mergeReports(IngestJobPluginInfo jobPluginInfo, Report pluginReport) {
    if (pluginReport != null) {
      for (Report reportItem : pluginReport.getReports()) {
        if (TransferredResource.class.getName().equals(reportItem.getSourceObjectClass())) {
          Report report = new Report(reportItem);
          report.addReport(reportItem);
          jobPluginInfo.addReport(report, false);
        } else {
          jobPluginInfo.addReport(reportItem, true);
        }
      }
    }
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

  private void sendNotification(ModelService model, IndexService index, Job job, JobStats jobStats)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    String emails = PluginHelper.getStringFromParameters(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION));

    if (StringUtils.isNotBlank(emails)) {
      List<String> emailList = new ArrayList<>(Arrays.asList(emails.split("\\s*,\\s*")));
      Notification notification = new Notification();
      String outcome = PluginState.SUCCESS.toString();

      if (jobStats.getSourceObjectsProcessedWithFailure() > 0) {
        outcome = PluginState.FAILURE.toString();
      }

      String subject = RodaCoreFactory.getRodaConfigurationAsString("core", "notification", "ingest_subject");
      if (StringUtils.isNotBlank(subject)) {
        subject = subject.replaceAll("\\{RESULT\\}", outcome);
      } else {
        subject = outcome;
      }

      notification.setSubject(subject);
      notification.setFromUser(this.getClass().getSimpleName());
      notification.setRecipientUsers(emailList);

      Map<String, Object> scopes = new HashMap<>();
      scopes.put("outcome", outcome);
      scopes.put("type", CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, job.getPluginType().toString()));
      scopes.put("sips", jobStats.getSourceObjectsCount());
      scopes.put("success", jobStats.getSourceObjectsProcessedWithSuccess());
      scopes.put("failed", jobStats.getSourceObjectsProcessedWithFailure());
      scopes.put("name", job.getName());
      scopes.put("creator", job.getUsername());

      if (outcome.equals(PluginState.FAILURE.toString())) {
        Filter filter = new Filter();
        filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, job.getId()));
        filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_PLUGIN_STATE, PluginState.FAILURE.toString()));
        try (IterableIndexResult<IndexedReport> reports = index.findAll(IndexedReport.class, filter,
          Collections.emptyList())) {

          StringBuilder builder = new StringBuilder();

          for (IndexedReport report : reports) {
            Report last = report.getReports().get(report.getReports().size() - 1);
            builder.append(last.getPluginDetails()).append("\n\n");
          }

          scopes.put("failures", new Handlebars.SafeString(builder.toString()));
        } catch (IOException e) {
          LOGGER.error("Error getting failed reports", e);
        }
      }

      SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      scopes.put("start", parser.format(job.getStartDate()));

      long duration = (new Date().getTime() - job.getStartDate().getTime()) / 1000;
      scopes.put("duration", duration + " seconds");
      model.createNotification(notification,
        new EmailNotificationProcessor(RodaConstants.INGEST_EMAIL_TEMPLATE, scopes));
    }

    String httpNotifications = PluginHelper.getStringFromParameters(this,
      getPluginParameter(RodaConstants.NOTIFICATION_HTTP_ENDPOINT));

    if (StringUtils.isNotBlank(httpNotifications)) {
      Notification notification = new Notification();
      String outcome = PluginState.SUCCESS.toString();

      if (jobStats.getSourceObjectsProcessedWithFailure() > 0) {
        outcome = PluginState.FAILURE.toString();
      }

      notification.setSubject("RODA ingest process finished - " + outcome);
      notification.setFromUser(this.getClass().getSimpleName());
      notification.setRecipientUsers(Collections.singletonList(httpNotifications));
      Map<String, Object> scope = new HashMap<>();
      scope.put(HTTPNotificationProcessor.JOB_KEY, job);
      model.createNotification(notification, new HTTPNotificationProcessor(httpNotifications, scope));
    }
  }

  /**
   * Recalculates (if failures must be noticed) and updates AIP objects (by
   * obtaining them from model)
   */
  private void recalculateAIPsList(ModelService model, IndexService index, IngestJobPluginInfo jobPluginInfo,
    List<AIP> aips, boolean removeAIPProcessingFailed) {
    aips.clear();
    Set<String> aipsToReturn = new HashSet<>();
    Set<String> transferredResourceAips;
    List<String> transferredResourcesToRemoveFromjobPluginInfo = new ArrayList<>();
    boolean oneTransferredResourceAipFailed;

    for (Entry<String, Map<String, Report>> transferredResourcejobPluginInfoEntry : jobPluginInfo
      .getReportsFromBeingProcessed().entrySet()) {
      String transferredResourceId = transferredResourcejobPluginInfoEntry.getKey();
      transferredResourceAips = new HashSet<>();
      oneTransferredResourceAipFailed = false;

      if (jobPluginInfo.getAipIds(transferredResourceId) != null) {
        for (String aipId : jobPluginInfo.getAipIds(transferredResourceId)) {
          Report aipReport = transferredResourcejobPluginInfoEntry.getValue().get(aipId);
          if (removeAIPProcessingFailed && aipReport.getPluginState() == PluginState.FAILURE) {
            LOGGER.trace("Removing AIP {} from the list", aipReport.getOutcomeObjectId());
            oneTransferredResourceAipFailed = true;
            break;
          } else {
            transferredResourceAips.add(aipId);
          }
        }

        if (oneTransferredResourceAipFailed) {
          LOGGER.info(
            "Will not process AIPs from transferred resource '{}' any longer because at least one of them failed",
            transferredResourceId);
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          jobPluginInfo.failOtherTransferredResourceAIPs(model, index, transferredResourceId);
          transferredResourcesToRemoveFromjobPluginInfo.add(transferredResourceId);
        } else {
          aipsToReturn.addAll(transferredResourceAips);
        }
      }
    }

    for (String transferredResourceId : transferredResourcesToRemoveFromjobPluginInfo) {
      jobPluginInfo.remove(transferredResourceId);
    }

    for (String aipId : aipsToReturn) {
      try {
        aips.add(model.retrieveAIP(aipId));
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        LOGGER.error("Error while retrieving AIP", e);
      }
    }
  }

  private int calculateEffectiveTotalSteps() {
    // 20180716 hsilva: the following list contains ids of the parameters that
    // are not steps & therefore must be ignored when calculation the amount of
    // effective steps
    List<String> parameterIdsToIgnore = Arrays.asList(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID,
      PLUGIN_PARAMS_DO_FEATURE_EXTRACTION, PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION,
      RodaConstants.PLUGIN_PARAMS_NOTIFICATION_WHEN_FAILED);
    int effectiveTotalSteps = getTotalSteps();
    boolean tikaParameters = false;
    boolean dontDoFeatureExtraction = false;
    boolean dontDoFulltext = false;

    for (PluginParameter pluginParameter : getParameters()) {
      if (pluginParameter.getType() == PluginParameterType.BOOLEAN
        && !parameterIdsToIgnore.contains(pluginParameter.getId())
        && !PluginHelper.verifyIfStepShouldBePerformed(this, pluginParameter)) {
        effectiveTotalSteps--;
      }

      if (pluginParameter.getId().equals(PLUGIN_PARAMS_DO_FEATURE_EXTRACTION)) {
        tikaParameters = true;
        if (!PluginHelper.verifyIfStepShouldBePerformed(this, pluginParameter)) {
          dontDoFeatureExtraction = true;
        }
      }
      if (pluginParameter.getId().equals(PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION)) {
        tikaParameters = true;
        if (!PluginHelper.verifyIfStepShouldBePerformed(this, pluginParameter)) {
          dontDoFulltext = true;
        }
      }
    }

    if (tikaParameters && (dontDoFeatureExtraction && dontDoFulltext)) {
      effectiveTotalSteps--;
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
          PluginHelper.createPluginEvent(this, entry.getKey(), model, index, tr, PluginState.SUCCESS, "", true,
            eventDate, cachedJob);
        } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException
          | ValidationException | AlreadyExistsException e) {
          LOGGER.warn("Error creating ingest event", e);
        }
      }
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

  private Report createFileFixityInformation(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips, IngestJobPluginInfo jobPluginInfo) {
    return executePlugin(index, model, storage, aips, PremisSkeletonPlugin.class.getName(), jobPluginInfo);
  }

  private Report doVirusCheck(IndexService index, ModelService model, StorageService storage, List<AIP> aips,
    IngestJobPluginInfo jobPluginInfo) {
    return executePlugin(index, model, storage, aips, AntivirusPlugin.class.getName(), jobPluginInfo);
  }

  private Report doVeraPDFCheck(IndexService index, ModelService model, StorageService storage, List<AIP> aips,
    Map<String, String> params, IngestJobPluginInfo jobPluginInfo) {
    return executePlugin(index, model, storage, aips, PLUGIN_CLASS_VERAPDF, params, jobPluginInfo);
  }

  private Report doDescriptiveMetadataValidation(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips, IngestJobPluginInfo jobPluginInfo) {
    return executePlugin(index, model, storage, aips, DescriptiveMetadataValidationPlugin.class.getName(),
      jobPluginInfo);
  }

  private Report verifyProducerAuthorization(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips, IngestJobPluginInfo jobPluginInfo) {
    return executePlugin(index, model, storage, aips, VerifyUserAuthorizationPlugin.class.getName(), jobPluginInfo);
  }

  private Report doFileFormatIdentification(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips, IngestJobPluginInfo jobPluginInfo) {
    return executePlugin(index, model, storage, aips, SiegfriedPlugin.class.getName(), jobPluginInfo);
  }

  private Report doDigitalSignatureValidation(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips, IngestJobPluginInfo jobPluginInfo) {
    return executePlugin(index, model, storage, aips, PLUGIN_CLASS_DIGITAL_SIGNATURE, jobPluginInfo);
  }

  private Report doFeatureAndFullTextExtraction(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips, Map<String, String> params, IngestJobPluginInfo jobPluginInfo) {
    return executePlugin(index, model, storage, aips, PLUGIN_CLASS_TIKA_FULLTEXT, params, jobPluginInfo);
  }

  private Report doAutoAccept(IndexService index, ModelService model, StorageService storage, List<AIP> aips,
    IngestJobPluginInfo jobPluginInfo) {
    return executePlugin(index, model, storage, aips, AutoAcceptSIPPlugin.class.getName(), jobPluginInfo);
  }

  private Report executePlugin(IndexService index, ModelService model, StorageService storage, List<AIP> aips,
    String pluginClassName, IngestJobPluginInfo jobPluginInfo) {
    return executePlugin(index, model, storage, aips, pluginClassName, null, jobPluginInfo);
  }

  private Report executePlugin(IndexService index, ModelService model, StorageService storage, List<AIP> aips,
    String pluginClassName, Map<String, String> params, IngestJobPluginInfo jobPluginInfo) {
    Plugin<AIP> plugin = RodaCoreFactory.getPluginManager().getPlugin(pluginClassName, AIP.class);
    Map<String, String> mergedParams = new HashMap<>(getParameterValues());
    if (params != null) {
      mergedParams.putAll(params);
    }

    // set outcome_object_id > source_object_id relation
    mergedParams.put(RodaConstants.PLUGIN_PARAMS_OUTCOMEOBJECTID_TO_SOURCEOBJECTID_MAP,
      JsonUtils.getJsonFromObject(jobPluginInfo.getAipIdToTransferredResourceIds()));

    try {
      plugin.setParameterValues(mergedParams);
      List<LiteOptionalWithCause> lites = LiteRODAObjectFactory.transformIntoLiteWithCause(model, aips);
      return plugin.execute(index, model, storage, lites);
    } catch (InvalidParameterException | PluginException | RuntimeException e) {
      LOGGER.error("Error executing plugin", e);
    }

    return null;
  }

  private void updateAIPsToBeAppraised(ModelService model, List<AIP> aips, IngestJobPluginInfo jobPluginInfo,
    Job cachedJob) {
    for (AIP aip : aips) {
      aip.setState(AIPState.UNDER_APPRAISAL);
      try {
        aip = model.updateAIPState(aip, cachedJob.getUsername());

        getParameterValues().put(RodaConstants.PLUGIN_PARAMS_OUTCOMEOBJECTID_TO_SOURCEOBJECTID_MAP,
          JsonUtils.getJsonFromObject(jobPluginInfo.getAipIdToTransferredResourceIds()));

        // update main report outcomeObjectState
        PluginHelper.updateJobReportState(this, model, aip.getIngestSIPUUID(), aip.getId(), AIPState.UNDER_APPRAISAL,
          cachedJob);

        // update counters of manual intervention
        jobPluginInfo.incrementOutcomeObjectsWithManualIntervention();

      } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.error("Error while updating AIP state to '{}'. Reason: {}", AIPState.UNDER_APPRAISAL, e.getMessage());
      }
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

  public abstract Optional<? extends AfterExecute> getAfterExecute();

  @FunctionalInterface
  public interface AfterExecute {
    void execute(IngestJobPluginInfo jobPluginInfo, List<AIP> aips);
  }
}
