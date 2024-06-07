package org.roda.wui.api.v2.services;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobMixIn;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.JobUserDetails;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Reports;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JobService {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

  public String buildCurlCommand(String path, Job job) {
    String command = RodaCoreFactory.getRodaConfiguration().getString("ui.createJob.curl");
    if (command != null) {
      command = command.replace("{{jsonObject}}",
          StringEscapeUtils.escapeJava(JsonUtils.getJsonFromObject(job, JobMixIn.class)));

      command = command.replace("{{RODA_CONTEXT_PATH}}",
          StringEscapeUtils.escapeJava(path));
      return command;
    } else {
      return "";
    }
  }

  public Job createJob(Job job, boolean async) throws NotFoundException, GenericException, JobAlreadyStartedException,
    RequestNotValidException, AuthorizationDeniedException {
    Job updatedJob = new Job(job);

    RodaCoreFactory.getPluginOrchestrator().createAndExecuteJobs(updatedJob, async);

    // force commit
    RodaCoreFactory.getIndexService().commit(Job.class);

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
    List<String> fieldsToReturn) throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    Reports reports = new Reports();

    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    boolean justActive = false;
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, jobId));

    if (justFailed) {
      filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_PLUGIN_STATE, PluginState.FAILURE.toString()));
    }
    IndexResult<IndexedReport> listJobReportsIndexResult = org.roda.wui.api.controllers.Browser.find(
      IndexedReport.class, filter, Sorter.NONE,
      new Sublist(new Sublist(pagingParams.getFirst(), pagingParams.getSecond())), Facets.NONE, user, justActive,
      fieldsToReturn);

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

  public StreamResponse retrieveJobAttachment(String jobId, String attachmentId) throws NotFoundException {
    Path filePath = RodaCoreFactory.getJobAttachmentsDirectoryPath().resolve(jobId).resolve(attachmentId);
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

  public List<PluginInfo> getJobPluginInfo(Job job, List<PluginInfo> pluginsInfo) {
    PluginInfo basePlugin = RodaCoreFactory.getPluginManager().getPluginInfo(job.getPlugin());

    if (basePlugin != null) {
      pluginsInfo.add(basePlugin);

      for (PluginParameter parameter : basePlugin.getParameters()) {
        if (PluginParameter.PluginParameterType.PLUGIN_SIP_TO_AIP.equals(parameter.getType())) {
          String pluginId = job.getPluginParameters().get(parameter.getId());
          if (pluginId == null) {
            pluginId = parameter.getDefaultValue();
          }
          if (pluginId != null) {
            PluginInfo refPlugin = RodaCoreFactory.getPluginManager().getPluginInfo(pluginId);
            if (refPlugin != null) {
              pluginsInfo.add(refPlugin);
            } else {
              LOGGER.warn("Could not find plugin: {}", pluginId);
            }
          }
        }
      }
    }

    // FIXME nvieira 20170208 it could possibly, in the future, be necessary to
    // add more plugin types adding all AIP to AIP plugins for job report list
    List<PluginInfo> aipToAipPlugins = RodaCoreFactory.getPluginManager().getPluginsInfo(PluginType.AIP_TO_AIP);
    if (aipToAipPlugins != null) {
      pluginsInfo.addAll(aipToAipPlugins);
    }
    return pluginsInfo;
  }

  private JobParallelism getJobParallelismFromConfiguration() {
    // Fetch priority
    String parallelism = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.CORE_ORCHESTRATOR_PREFIX,
      RodaConstants.CORE_ORCHESTRATOR_PROP_INTERNAL_JOBS_PARALLELISM);

    if (parallelism == null) {
      return JobParallelism.NORMAL;
    }

    try {
      return JobParallelism.valueOf(parallelism);
    } catch (IllegalArgumentException e) {
      return JobParallelism.NORMAL;
    }
  }

  private JobPriority getJobPriorityFromConfiguration() {
    // Fetch priority
    String priority = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.CORE_ORCHESTRATOR_PREFIX,
      RodaConstants.CORE_ORCHESTRATOR_PROP_INTERNAL_JOBS_PRIORITY);

    if (priority == null) {
      return JobPriority.MEDIUM;
    }

    try {
      return JobPriority.valueOf(priority);
    } catch (IllegalArgumentException e) {
      return JobPriority.MEDIUM;
    }
  }
}
