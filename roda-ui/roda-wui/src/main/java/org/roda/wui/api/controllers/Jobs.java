/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

public class Jobs extends RodaWuiController {

  private Jobs() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static Job createJob(User user, Job job) throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, JobAlreadyStartedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input and set missing information when possible
    JobsHelper.validateAndSetJobInformation(user, job);

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    Job updatedJob = JobsHelper.createJob(job);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "job", updatedJob);

    return updatedJob;
  }

  public static Job startJob(User user, String jobId) throws RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException, JobAlreadyStartedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    Job job = JobsHelper.startJob(jobId);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "jobId", jobId);

    return job;
  }

  public static void stopJob(User user, String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    JobsHelper.stopJob(jobId);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "jobId", jobId);
  }

  public static void deleteJob(User user, String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    JobsHelper.deleteJob(jobId);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, "jobId", jobId);
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
