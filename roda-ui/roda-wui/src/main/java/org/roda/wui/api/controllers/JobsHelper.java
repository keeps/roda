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
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.Job;
import org.roda.core.data.v2.Job.ORCHESTRATOR_METHOD;
import org.roda.core.data.v2.JobReport;
import org.roda.core.data.v2.RodaUser;
import org.roda.core.plugins.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.pattern.Patterns;

public class JobsHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobsHelper.class);

  private static final List<ORCHESTRATOR_METHOD> ORCHESTRATOR_METHODS = Arrays
    .asList(ORCHESTRATOR_METHOD.ON_TRANSFERRED_RESOURCES);

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

  protected static Job createJob(Job job) throws NotFoundException {
    Job updatedJob = new Job(job);

    // serialize job to file & index it
    RodaCoreFactory.getModelService().createJob(updatedJob, null);

    // FIXME should we verify if the job was created with success???
    // FIXME correctly handle future returned by Patterns.ask
    Patterns.ask(RodaCoreFactory.getPluginOrchestrator().getCoordinator(), updatedJob, 5);

    return updatedJob;
  }

  public static Job retrieveJob(String jobId) throws NotFoundException, GenericException {
    return RodaCoreFactory.getIndexService().retrieve(Job.class, jobId);
  }

  public static IndexResult<Job> findJobs(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().find(Job.class, filter, sorter, sublist, facets);
  }

  public static org.roda.core.data.v2.Jobs getJobsFromIndexResult(IndexResult<Job> jobsFromIndexResult) {
    org.roda.core.data.v2.Jobs jobs = new org.roda.core.data.v2.Jobs();

    for (Job job : jobsFromIndexResult.getResults()) {
      jobs.addJob(job);
    }

    return jobs;
  }

  public static IndexResult<JobReport> findJobReports(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().find(JobReport.class, filter, sorter, sublist, facets);
  }

  public static JobReport retrieveJobReport(String jobReportId) throws NotFoundException, GenericException {
    return RodaCoreFactory.getIndexService().retrieve(JobReport.class, jobReportId);
  }

}
