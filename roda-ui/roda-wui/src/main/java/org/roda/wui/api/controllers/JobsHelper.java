/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.ORCHESTRATOR_METHOD;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobsHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobsHelper.class);

  private static final List<ORCHESTRATOR_METHOD> ORCHESTRATOR_METHODS = Arrays.asList(
    ORCHESTRATOR_METHOD.ON_TRANSFERRED_RESOURCES, ORCHESTRATOR_METHOD.ON_AIPS, ORCHESTRATOR_METHOD.ON_ALL_AIPS,
    ORCHESTRATOR_METHOD.RUN_PLUGIN);

  protected static void validateAndSetCreateJobInformation(RodaUser user, Job job) throws RequestNotValidException {
    if (!ORCHESTRATOR_METHODS.contains(job.getOrchestratorMethod())) {
      throw new RequestNotValidException("Invalid orchestrator method '" + job.getOrchestratorMethod() + "'");
    }

    validateJobPluginInformation(job);

    // set "missing" information whenever it is not impeditive for job creation
    if (org.apache.commons.lang3.StringUtils.isBlank(job.getId())) {
      job.setId(UUID.randomUUID().toString());
    }
    if (org.apache.commons.lang3.StringUtils.isBlank(job.getName())) {
      job.setName(job.getId());
    }
    job.setUsername(user.getName());
  }

  private static void validateJobPluginInformation(Job job) throws RequestNotValidException {
    Plugin<?> plugin = RodaCoreFactory.getPluginManager().getPlugin(job.getPlugin());
    if (plugin != null) {
      try {
        plugin.setParameterValues(job.getPluginParameters());
        if (!plugin.areParameterValuesValid()) {
          throw new RequestNotValidException("Invalid plugin parameters");
        }
        job.setPluginType(plugin.getType());
      } catch (InvalidParameterException e) {
        throw new RequestNotValidException("Invalid plugin parameters");
      }
    } else {
      throw new RequestNotValidException("No plugin was found with the id '" + job.getPlugin() + "'");
    }
  }

  protected static Job createJob(Job job) throws NotFoundException, GenericException {
    Job updatedJob = new Job(job);

    // serialize job to file & index it
    RodaCoreFactory.getModelService().createOrUpdateJob(updatedJob);

    // ask plugin orchestrator to execute the job
    RodaCoreFactory.getPluginOrchestrator().executeJob(updatedJob);

    // force commit
    RodaCoreFactory.getIndexService().commit(Job.class);

    return updatedJob;
  }

  public static org.roda.core.data.v2.jobs.Jobs getJobsFromIndexResult(IndexResult<Job> jobsFromIndexResult) {
    org.roda.core.data.v2.jobs.Jobs jobs = new org.roda.core.data.v2.jobs.Jobs();

    for (Job job : jobsFromIndexResult.getResults()) {
      jobs.addJob(job);
    }

    return jobs;
  }

  public static Job stopJob(String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    // retrieve job
    Job job = RodaCoreFactory.getModelService().retrieveJob(jobId);

    // stop it
    RodaCoreFactory.getPluginOrchestrator().stopJob(job);

    return job;
  }

  public static void deleteJob(String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    // stop it (if it is running)
    Job job = stopJob(jobId);

    // delete all job reports associated with this job
    deleteJobReports(job);

    // delete it
    RodaCoreFactory.getModelService().deleteJob(jobId);

  }

  private static void deleteJobReports(Job job) throws GenericException, RequestNotValidException {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, job.getId()));
    Sublist sublist = new Sublist(0, 100);
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    Long jobReportsCount = index.count(Report.class, filter);

    for (int i = 0; i < jobReportsCount; i += 100) {
      sublist.setFirstElementIndex(i);
      IndexResult<Report> jobReports = index.find(Report.class, filter, null, sublist);
      for (Report report : jobReports.getResults()) {
        try {
          model.deleteJobReport(report.getId());
        } catch (NotFoundException | AuthorizationDeniedException e) {
          LOGGER.error("Error while deleting Job Report", e);
        }
      }

    }
  }

}
