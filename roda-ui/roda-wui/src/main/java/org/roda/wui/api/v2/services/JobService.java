/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.services;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.JobStateNotPendingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.jobs.CreateJobRequest;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobMixIn;
import org.roda.core.data.v2.jobs.JobUserDetails;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginInfoList;
import org.roda.core.data.v2.jobs.PluginInfoRequest;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Reports;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.common.I18nUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobService {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

  @Autowired
  TranslationService translationService;

  public Job getJobFromModel(String jobId)
    throws NotFoundException, AuthorizationDeniedException, RequestNotValidException, GenericException {
    return RodaCoreFactory.getModelService().retrieveJob(jobId);
  }

  public String buildCurlCommand(String path, Job job) {
    String command = RodaCoreFactory.getRodaConfiguration().getString("ui.createJob.curl");
    if (command != null) {
      command = command.replace("{{jsonObject}}",
        StringEscapeUtils.escapeJava(JsonUtils.getJsonFromObject(transformJobToCreateJobRequest(job), JobMixIn.class)));

      command = command.replace("{{RODA_CONTEXT_PATH}}", StringEscapeUtils.escapeJava(path));
      return command;
    } else {
      return "";
    }
  }

  private CreateJobRequest transformJobToCreateJobRequest(Job job) {
    CreateJobRequest createJobRequest = new CreateJobRequest();
    createJobRequest.setName(job.getName());
    createJobRequest.setPlugin(job.getPlugin());
    createJobRequest.setPluginParameters(job.getPluginParameters());
    createJobRequest.setSourceObjects(SelectedItemsUtils.convertToRESTRequest(job.getSourceObjects()));
    createJobRequest.setSourceObjectsClass(job.getSourceObjects().getSelectedClass());
    createJobRequest.setPriority(job.getPriority().toString());
    createJobRequest.setParallelism(job.getParallelism().toString());

    return createJobRequest;
  }

  public Job createJob(Job job, boolean async) throws NotFoundException, GenericException, JobAlreadyStartedException,
    RequestNotValidException, AuthorizationDeniedException {
    Job updatedJob = new Job(job);

    RodaCoreFactory.getPluginOrchestrator().createAndExecuteJobs(updatedJob, async);

    // force commit
    RodaCoreFactory.getIndexService().commit(IndexedJob.class);

    return updatedJob;
  }

  public Job startJob(String jobId) throws RequestNotValidException, GenericException, NotFoundException,
    AuthorizationDeniedException, JobAlreadyStartedException {
    // get job
    Job job = RodaCoreFactory.getModelService().retrieveJob(jobId);

    // ask plugin orchestrator to execute the job
    RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);

    return job;
  }

  public void validateAndSetJobInformation(User user, Job job) throws RequestNotValidException {
    LOGGER.debug("Job being validated: {}", job);
    validateJobPluginInformation(job);

    // always set a new UUID (even if job already brings one)
    job.setId(IdUtils.createUUID());

    // set "missing" information whenever it is not impeditive for job creation
    if (StringUtils.isBlank(job.getName())) {
      job.setName(job.getId());
    }
    job.setUsername(user.getName());

    // Set the JobUserDetails in Job creation
    JobUserDetails jobUserDetails = new JobUserDetails();
    jobUserDetails.setUsername(user.getName());
    jobUserDetails.setEmail(user.getEmail());
    jobUserDetails.setFullname(user.getFullName());
    jobUserDetails.setRole(RodaConstants.PreservationAgentRole.IMPLEMENTER.toString());
    job.getJobUsersDetails().add(jobUserDetails);
  }

  public Job stopJob(String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    // retrieve job
    Job job = RodaCoreFactory.getModelService().retrieveJob(jobId);

    // stop it
    RodaCoreFactory.getPluginOrchestrator().stopJobAsync(job);

    return job;
  }

  public Job rejectJob(Job job, String details)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {

    job.setState(Job.JOB_STATE.REJECTED);
    job.setStateDetails(details);
    // serialize job to file & index it
    job.setEndDate(new Date());
    RodaCoreFactory.getModelService().createOrUpdateJob(job);

    return job;
  }

  public void deleteJob(String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    // stop it (if it is running)
    Job job = stopJob(jobId);

    // delete all job reports associated with this job
    deleteJobReports(job);

    // delete it
    RodaCoreFactory.getModelService().deleteJob(jobId);
  }

  private void deleteJobReports(Job job) throws GenericException, RequestNotValidException {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, job.getId()));
    Sublist sublist = new Sublist(0, RodaConstants.DEFAULT_PAGINATION_VALUE);
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    Long jobReportsCount = index.count(IndexedReport.class, filter);

    for (int i = 0; i < jobReportsCount; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
      sublist.setFirstElementIndex(i);
      IndexResult<IndexedReport> jobReports = index.find(IndexedReport.class, filter, null, sublist,
        Arrays.asList(RodaConstants.JOB_REPORT_ID, RodaConstants.JOB_REPORT_JOB_ID));
      for (IndexedReport report : jobReports.getResults()) {
        try {
          model.deleteJobReport(report.getJobId(), report.getId());
        } catch (NotFoundException | AuthorizationDeniedException e) {
          LOGGER.error("Error while deleting Job Report", e);
        }
      }

    }
  }

  public Reports getJobReportsFromIndexResult(User user, String jobId, boolean justFailed, String start, String limit,
    List<String> fieldsToReturn) throws GenericException, RequestNotValidException {
    Reports reports = new Reports();

    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    boolean justActive = false;
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, jobId));

    if (justFailed) {
      filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_PLUGIN_STATE, PluginState.FAILURE.toString()));
    }
    IndexResult<IndexedReport> listJobReportsIndexResult = RodaCoreFactory.getIndexService().find(IndexedReport.class,
      filter, Sorter.NONE, new Sublist(new Sublist(pagingParams.getFirst(), pagingParams.getSecond())), Facets.NONE,
      user, justActive, fieldsToReturn);

    for (IndexedReport report : listJobReportsIndexResult.getResults()) {
      reports.addObject(report);
    }

    return reports;
  }

  public void validateJobInformation(Job job) throws RequestNotValidException, JobStateNotPendingException {
    LOGGER.debug("Job being validated: {}", job);
    validateJobPluginInformation(job);

    if (!Job.JOB_STATE.PENDING_APPROVAL.equals(job.getState())) {
      throw new JobStateNotPendingException();
    }
  }

  public JobUserDetails buildJobUserDetails(User user) {
    JobUserDetails jobUserDetails = new JobUserDetails();
    jobUserDetails.setUsername(user.getName());
    jobUserDetails.setFullname(user.getFullName());
    jobUserDetails.setRole(RodaConstants.PreservationAgentRole.AUTHORIZER.toString());
    jobUserDetails.setEmail(user.getEmail());

    return jobUserDetails;
  }

  public StreamResponse retrieveJobAttachment(String jobId, String attachmentId)
    throws NotFoundException, GenericException {
    Path filePath = RodaCoreFactory.getJobAttachmentsDirectoryPath().resolve(jobId).resolve(attachmentId);

    if (!RodaCoreFactory.checkPathIsWithin(filePath, RodaCoreFactory.getJobAttachmentsDirectoryPath())) {
      throw new GenericException("Attempt to retrieve files outside the permitted scope");
    }

    if (!Files.exists(filePath)) {
      throw new NotFoundException();
    }
    ConsumesOutputStream stream = new ConsumesOutputStream() {
      @Override
      public void consumeOutputStream(OutputStream out) throws IOException {
        Files.copy(filePath, out);
      }

      @Override
      public long getSize() {
        long size;
        try {
          size = Files.size(filePath);
        } catch (IOException e) {
          size = -1;
        }
        return size;
      }

      @Override
      public Date getLastModified() {
        Date ret;
        try {
          ret = new Date(Files.getLastModifiedTime(filePath).toMillis());
        } catch (IOException e) {
          ret = null;
        }
        return ret;
      }

      @Override
      public String getFileName() {
        return filePath.getFileName().toString();
      }

      @Override
      public String getMediaType() {
        return RodaConstants.MEDIA_TYPE_APPLICATION_OCTET_STREAM;
      }
    };

    return new StreamResponse(stream);
  }

  private void validateJobPluginInformation(Job job) throws RequestNotValidException {
    Plugin<? extends IsRODAObject> plugin = RodaCoreFactory.getPluginManager().getPlugin(job.getPlugin());
    if (plugin != null) {
      // validate parameters
      try {
        plugin.setParameterValues(job.getPluginParameters());
        if (!plugin.areParameterValuesValid()) {
          throw new RequestNotValidException("Invalid plugin parameters");
        }
        job.setPluginType(plugin.getType());
      } catch (InvalidParameterException e) {
        throw new RequestNotValidException("Invalid plugin parameters");
      }

      validatePluginObjectsClasses(job, plugin);
    } else {
      throw new RequestNotValidException("No plugin was found with the id '" + job.getPlugin() + "'");
    }
  }

  private void validatePluginObjectsClasses(Job job, Plugin<? extends IsRODAObject> plugin)
    throws RequestNotValidException {
    if (!(job.getSourceObjects() instanceof SelectedItemsNone)) {
      // validate plugin class & objects class
      Set<?> pluginObjectClasses = RodaCoreFactory.getPluginManager().getPluginObjectClasses(plugin);
      String objectsClassName = job.getSourceObjects().getSelectedClass();
      Class<?> objectsClass;
      try {
        objectsClass = Class.forName(objectsClassName);
      } catch (ClassNotFoundException e) {
        throw new RequestNotValidException("Plugin class and objects class do not match (plugin classes: "
          + pluginObjectClasses + "; objects class: " + objectsClassName + ")");
      }
      if (!pluginObjectClasses.contains(objectsClass)) {
        throw new RequestNotValidException("Plugin class and objects class do not match (plugin classes: "
          + pluginObjectClasses + "; objects class: " + objectsClass + ")");
      }
    }
  }

  public List<PluginInfo> getJobPluginInfo(PluginInfoRequest pluginInfoRequest, String localeString) {
    List<PluginInfo> pluginsInfo = new ArrayList<>();
    PluginInfo originalPlugin = RodaCoreFactory.getPluginManager().getPluginInfo(pluginInfoRequest.getPlugin());

    if (originalPlugin != null) {

      translationService.translatePlugin(originalPlugin, localeString);
      translationService.translatePluginParameters(originalPlugin, localeString);

      // It's safest to assume the original object shouldn't be mutated.
      // If getPluginInfo doesn't return a deep copy, we must clone it and its
      // parameters.
      PluginInfo basePlugin = clonePluginInfo(originalPlugin);
      pluginsInfo.add(basePlugin);

      basePlugin.setName(I18nUtility.getMessage(originalPlugin.getName(), originalPlugin.getName(), localeString));
      if (StringUtils.isNotBlank(originalPlugin.getDescription())) {
        basePlugin.setDescription(
          I18nUtility.getMessage(originalPlugin.getDescription(), originalPlugin.getDescription(), localeString));
      }
      List<PluginParameter> translatedParameters = new ArrayList<>();

      for (PluginParameter originalParam : originalPlugin.getParameters()) {
        // Create a copy of the parameter so we don't mutate the cached singleton
        PluginParameter translatedParam = cloneParameter(originalParam);

        // Always use the original parameter's name/desc as the translation key
        translatedParam.setName(I18nUtility.getMessage(originalParam.getName(), originalParam.getName(), localeString));
        if (StringUtils.isNotBlank(originalParam.getDescription())) {
          translatedParam.setDescription(
            I18nUtility.getMessage(originalParam.getDescription(), originalParam.getDescription(), localeString));
        }

        if (PluginParameter.PluginParameterType.PLUGIN_SIP_TO_AIP.equals(originalParam.getType())) {
          String pluginId = pluginInfoRequest.getPluginParameters().get(originalParam.getId());
          if (pluginId == null) {
            pluginId = originalParam.getDefaultValue();
          }
          if (pluginId != null) {
            PluginInfo refPlugin = RodaCoreFactory.getPluginManager().getPluginInfo(pluginId);
            if (refPlugin != null) {
              // Clone the reference plugin as well before mutating
              PluginInfo clonedRef = clonePluginInfo(refPlugin);
              clonedRef.setName(I18nUtility.getMessage(refPlugin.getName(), refPlugin.getName(), localeString));
              clonedRef.setDescription(
                I18nUtility.getMessage(refPlugin.getDescription(), refPlugin.getDescription(), localeString));
              pluginsInfo.add(clonedRef);
            } else {
              LOGGER.warn("Could not find plugin: {}", pluginId);
            }
          }
        }
        translatedParameters.add(translatedParam);
      }

      // Override the old parameters list with your newly cloned and translated list
      basePlugin.setParameters(translatedParameters);
    }

    return pluginsInfo;
  }

  // --- Helper Methods ---

  private PluginParameter cloneParameter(PluginParameter original) {
    // If PluginParameter has a copy constructor (e.g., new
    // PluginParameter(original)), use that!
    // Otherwise, manually copy the fields based on your payload structure:
    PluginParameter copy = new PluginParameter();
    copy.setId(original.getId());
    copy.setType(original.getType());
    copy.setDefaultValue(original.getDefaultValue());
    copy.setPossibleValues(original.getPossibleValues());
    copy.setMandatory(original.isMandatory());
    copy.setReadonly(original.isReadonly());
    copy.setRenderingHints(original.getRenderingHints());
    // Name and Description will be overwritten by the i18n utility later
    return copy;
  }

  private PluginInfo clonePluginInfo(PluginInfo original) {
    // Similarly, use a copy constructor if available, or manually copy standard
    // fields.
    // If RodaCoreFactory.getPluginManager().getPluginInfo() already returns a
    // clone,
    // you might not need this helper, but you MUST still use the cloneParameter
    // helper above.
    PluginInfo copy = new PluginInfo();
    copy.setId(original.getId());
    copy.setVersion(original.getVersion());
    copy.setType(original.getType());
    copy.setCategories(original.getCategories());
    copy.setParameters(original.getParameters()); // Will be overwritten by translatedParameters
    return copy;
  }

  public Report translateReports(Report report, String localeString) {
    List<PluginType> types = new ArrayList<>(Arrays.asList(PluginType.values()));
    PluginInfoList pluginInfoList = RodaCoreFactory.getPluginManager().getPluginsInfo(types, false);

    List<Report> translatedReports = new ArrayList<>();

    for (Report innerReport : report.getReports()) {
      pluginInfoList.getPluginInfoList().stream().filter(p -> p.getId().equals(innerReport.getPlugin())).findFirst()
        .ifPresentOrElse(pluginInfo -> {
          PluginInfo infoTranslated = translationService.translatePlugin(pluginInfo, localeString);
          Report clonedReport = new Report(innerReport);
          clonedReport.setTitle(infoTranslated.getName());
          clonedReport.setPluginName(infoTranslated.getName());
          translatedReports.add(clonedReport);
        }, () -> translatedReports.add(innerReport));
    }

    report.setReports(translatedReports);
    return report;
  }
}
