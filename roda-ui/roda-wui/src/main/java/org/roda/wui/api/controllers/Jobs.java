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
import org.roda.core.data.common.AuthorizationDeniedException;
import org.roda.core.data.common.NotFoundException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.Job;
import org.roda.core.data.v2.RodaUser;
import org.roda.wui.api.exceptions.RequestNotValidException;
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

    // validate input
    JobsHelper.validateCreateJob(job);

    // check user permissions
    UserUtility.checkRoles(user, INGEST_SUBMIT_ROLE);

    // delegate
    Job updatedJob = JobsHelper.createJob(user, job);

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
    Job job = JobsHelper.getJob(jobId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, JOBS_COMPONENT, "getJob", null, duration, "job_id", job.getId());

    return job;
  }

  public static org.roda.core.data.v2.Jobs listJobs(RodaUser user, String start, String limit) {
    Date startDate = new Date();

    // check user permissions
    // TODO ???

    // delegate
    org.roda.core.data.v2.Jobs jobs = JobsHelper.listJobs(start, limit);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, JOBS_COMPONENT, "listJobs", null, duration, RodaConstants.API_QUERY_KEY_START, start,
      RodaConstants.API_QUERY_KEY_LIMIT, limit);

    return jobs;
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
