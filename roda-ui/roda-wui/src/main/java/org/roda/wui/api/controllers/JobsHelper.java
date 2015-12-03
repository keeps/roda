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
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.Job;
import org.roda.core.data.v2.Job.JOB_TYPE;
import org.roda.core.data.v2.Job.RESOURCE_TYPE;
import org.roda.core.data.v2.RodaUser;
import org.roda.core.index.IndexServiceException;
import org.roda.wui.common.client.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.pattern.Patterns;

public class JobsHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobsHelper.class);

  private static final List<String> ORCHESTRATOR_METHODS = Arrays.asList("runPluginOnTransferredResources");
  private static final List<RESOURCE_TYPE> RESOURCE_TYPES = Arrays.asList(RESOURCE_TYPE.BAGIT);
  private static final List<JOB_TYPE> JOB_TYPES = Arrays.asList(JOB_TYPE.INGEST);

  protected static void validateCreateJob(Job job) throws RequestNotValidException {
    if (!ORCHESTRATOR_METHODS.contains(job.getOrchestratorMethod())) {
      throw new RequestNotValidException("Invalid orchestrator method '" + job.getOrchestratorMethod() + "'");
    }
    if (!RESOURCE_TYPES.contains(job.getResourceType())) {
      throw new RequestNotValidException("Invalid resource type '" + job.getResourceType() + "'");
    }
    if (!JOB_TYPES.contains(job.getType())) {
      throw new RequestNotValidException("Invalid plugin type '" + job.getType() + "'");
    }

    // the following checks are not impeditive for job creation
    if (org.apache.commons.lang3.StringUtils.isBlank(job.getId())) {
      job.setId(UUID.randomUUID().toString());
    }
    if (org.apache.commons.lang3.StringUtils.isBlank(job.getName())) {
      job.setName(job.getId());
    }
  }

  protected static Job createJob(RodaUser user, Job job) throws NotFoundException {
    Job updatedJob = new Job(job);
    updatedJob.setUsername(user.getName());

    // serialize job to file & index it
    RodaCoreFactory.getModelService().createJob(updatedJob, null);

    Patterns.ask(RodaCoreFactory.getPluginOrchestrator().getCoordinator(), updatedJob, 5);

    return updatedJob;
  }

  public static Job retrieveJob(String jobId) throws NotFoundException, GenericException {
    try {
      return RodaCoreFactory.getIndexService().retrieve(Job.class, jobId);
    } catch (IndexServiceException e) {
      if (e.getCode() == IndexServiceException.NOT_FOUND) {
        throw new NotFoundException("Job with id '" + jobId + "' was not found.");
      } else {
        throw new GenericException("Error getting Job with id '" + jobId + "'.");
      }
    }
  }

  public static IndexResult<Job> findJobs(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException {
    try {
      return RodaCoreFactory.getIndexService().find(Job.class, filter, sorter, sublist);
    } catch (IndexServiceException e) {
      LOGGER.error("Error getting jobs", e);
      throw new GenericException("Error getting jobs: " + e.getMessage());
    }
  }

  public static org.roda.core.data.v2.Jobs getJobsFromIndexResult(IndexResult<Job> jobsFromIndexResult) {
    org.roda.core.data.v2.Jobs jobs = new org.roda.core.data.v2.Jobs();

    for (Job job : jobsFromIndexResult.getResults()) {
      jobs.addJob(job);
    }

    return jobs;
  }

}
