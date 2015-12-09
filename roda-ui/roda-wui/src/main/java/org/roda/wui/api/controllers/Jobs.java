/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.Date;

import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.AuthorizationDeniedException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.Job;
import org.roda.core.data.v2.JobReport;
import org.roda.core.data.v2.RodaUser;
import org.roda.wui.common.RodaCoreService;
import org.roda.wui.common.client.GenericException;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Jobs extends RodaCoreService {

  private static final String JOBS_COMPONENT = "Jobs";
  private static final String INGEST_SUBMIT_ROLE = "ingest.submit";

  private Jobs() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static Job createJob(RodaUser user, Job job)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    Date startDate = new Date();

    // validate input and set missing information when possible
    JobsHelper.validateAndSetCreateJobInformation(user, job);

    // check user permissions
    UserUtility.checkRoles(user, INGEST_SUBMIT_ROLE);

    // delegate
    Job updatedJob = JobsHelper.createJob(job);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, JOBS_COMPONENT, "createJob", null, duration, "job", updatedJob);

    return updatedJob;
  }

  public static Job getJob(RodaUser user, String jobId) throws NotFoundException, GenericException {
    Date startDate = new Date();

    // check user permissions
    // TODO ???

    // delegate
    Job job = JobsHelper.retrieveJob(jobId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, JOBS_COMPONENT, "getJob", null, duration, "job_id", job.getId());

    return job;
  }

  public static IndexResult<Job> findJobs(RodaUser user, Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException {
    Date startDate = new Date();

    // check user permissions
    // TODO ???

    // delegate
    IndexResult<Job> findJobs = JobsHelper.findJobs(filter, sorter, sublist, facets);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, JOBS_COMPONENT, "findJobs", null, duration, RodaConstants.CONTROLLER_FILTER_PARAM, filter,
      RodaConstants.CONTROLLER_SORTER_PARAM, sorter, RodaConstants.CONTROLLER_SUBLIST_PARAM, sublist);

    return findJobs;
  }

  public static IndexResult<JobReport> findJobReports(RodaUser user, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets) throws GenericException {
    Date startDate = new Date();

    // check user permissions
    // TODO ???

    // delegate
    IndexResult<JobReport> findJobReports = JobsHelper.findJobReports(filter, sorter, sublist, facets);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, JOBS_COMPONENT, "findJobReports", null, duration, RodaConstants.CONTROLLER_FILTER_PARAM,
      filter, RodaConstants.CONTROLLER_SORTER_PARAM, sorter, RodaConstants.CONTROLLER_SUBLIST_PARAM, sublist);

    return findJobReports;
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
