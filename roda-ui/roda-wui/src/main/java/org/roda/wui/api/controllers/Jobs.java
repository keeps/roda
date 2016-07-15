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
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.common.RodaCoreService;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Jobs extends RodaCoreService {

  private static final String JOBS_COMPONENT = "Jobs";

  private Jobs() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static Job createJob(RodaUser user, Job job) throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, JobAlreadyStartedException {
    Date startDate = new Date();

    // validate input and set missing information when possible
    JobsHelper.validateAndSetCreateJobInformation(user, job);

    // check user permissions
    UserUtility.checkRoles(user, new Object(){}.getClass().getEnclosingMethod());

    // delegate
    Job updatedJob = JobsHelper.createJob(job);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, JOBS_COMPONENT, "createJob", null, duration, "job", updatedJob);

    return updatedJob;
  }

  public static Job startJob(RodaUser user, String jobId) throws RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException, JobAlreadyStartedException {
    Date startDate = new Date();

    // FIXME check user permissions
    UserUtility.checkRoles(user, new Object(){}.getClass().getEnclosingMethod());

    // delegate
    Job job = JobsHelper.startJob(jobId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, JOBS_COMPONENT, "startJob", null, duration, "jobId", jobId);

    return job;
  }

  public static void stopJob(RodaUser user, String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Date startDate = new Date();

    // FIXME check user permissions
    UserUtility.checkRoles(user, new Object(){}.getClass().getEnclosingMethod());

    // delegate
    JobsHelper.stopJob(jobId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, JOBS_COMPONENT, "stopJob", null, duration, "jobId", jobId);
  }

  public static void deleteJob(RodaUser user, String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Date startDate = new Date();

    // FIXME check user permissions
    UserUtility.checkRoles(user, new Object(){}.getClass().getEnclosingMethod());

    // delegate
    JobsHelper.deleteJob(jobId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, JOBS_COMPONENT, "deleteJob", null, duration, "jobId", jobId);
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
