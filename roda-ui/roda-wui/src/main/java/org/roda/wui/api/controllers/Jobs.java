/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.v2.EntityResponse;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.JobStateNotPendingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobUserDetails;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
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
  public static Job createJob(User user, Job job, boolean async) throws AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, GenericException, JobAlreadyStartedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input and set missing information when possible
    JobsHelper.validateAndSetJobInformation(user, job);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;
    Job updatedJob = new Job(job);

    try {
      // delegate
      updatedJob = JobsHelper.createJob(job, async);
      return updatedJob;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_JOB_PARAM, updatedJob);
    }
  }

  public static Job approveJob(User user, SelectedItems<Job> jobs, boolean async)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    JobAlreadyStartedException, JobStateNotPendingException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    ModelService model = RodaCoreFactory.getModelService();

    if (jobs instanceof SelectedItemsList) {
      SelectedItemsList<Job> jobsList = (SelectedItemsList<Job>) jobs;
      for (String id : jobsList.getIds()) {
        Job job = model.retrieveJob(id);
        if (job.getState().equals(Job.JOB_STATE.PENDING_APPROVAL)) {
          // validate input and set missing information when possible
          JobsHelper.validateJobInformation(user, job);

          // check user permissions
          controllerAssistant.checkRoles(user);

          LogEntryState state = LogEntryState.SUCCESS;
          ModelService modelService = RodaCoreFactory.getModelService();
          Job retrievedJob = modelService.retrieveJob(job.getId());
          JobUserDetails jobUserDetails = new JobUserDetails();
          jobUserDetails.setUsername(user.getName());
          jobUserDetails.setFullname(user.getFullName());
          jobUserDetails.setRole(RodaConstants.PreservationAgentRole.AUTHORIZER.toString());
          jobUserDetails.setEmail(user.getEmail());
          retrievedJob.getJobUsersDetails().add(jobUserDetails);
          modelService.createOrUpdateJob(retrievedJob);

          Job updatedJob = null;

          try {
            // delegate
            updatedJob = JobsHelper.startJob(job.getId());
            // return updatedJob;
          } catch (RODAException e) {
            state = LogEntryState.FAILURE;
            throw e;
          } finally {
            // register action
            controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_JOB_PARAM, updatedJob);
          }
        }
      }
    }
    return null;
  }

  public static Job rejectJob(User user, SelectedItems<Job> jobs, String details)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    JobAlreadyStartedException, JobStateNotPendingException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    ModelService model = RodaCoreFactory.getModelService();

    if (jobs instanceof SelectedItemsList) {
      SelectedItemsList<Job> jobsList = (SelectedItemsList<Job>) jobs;
      for (String id : jobsList.getIds()) {
        Job job = model.retrieveJob(id);
        if (job.getState().equals(Job.JOB_STATE.PENDING_APPROVAL)) {
          // validate input and set missing information when possible
          JobsHelper.validateJobInformation(user, job);

          // check user permissions
          controllerAssistant.checkRoles(user);

          LogEntryState state = LogEntryState.SUCCESS;

          try {
            // delegate
            job = JobsHelper.rejectJob(job, details);
          } catch (RODAException e) {
            state = LogEntryState.FAILURE;
            throw e;
          } finally {
            // register action
            controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_JOB_PARAM, job);
          }
        }
      }
    }
    return null;
  }

  public static Job startJob(User user, String jobId) throws RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException, JobAlreadyStartedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return JobsHelper.startJob(jobId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId);
    }
  }

  public static void stopJob(User user, String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      JobsHelper.stopJob(jobId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId);
    }
  }

  public static void deleteJob(User user, String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      JobsHelper.deleteJob(jobId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId);
    }
  }

  public static EntityResponse retrieveJobAttachment(User user, String jobId, String attachmentId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    // check permissions
    controllerAssistant.checkRoles(user);
    LogEntryState state = LogEntryState.SUCCESS;
    try {
      return JobsHelper.retrieveJobAttachment(jobId, attachmentId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId);
    }
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
