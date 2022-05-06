/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.EntityResponse;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.JobStateNotPendingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.JobUserDetails;
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

  public static Job approveJob(User user, Job job, boolean async) throws AuthorizationDeniedException,
          RequestNotValidException, NotFoundException, GenericException, JobAlreadyStartedException, JobStateNotPendingException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

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
      return updatedJob;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_JOB_PARAM, updatedJob);
    }
  }

  public static Job rejectJob(User user, Job job, String details) throws AuthorizationDeniedException,
          RequestNotValidException, NotFoundException, GenericException, JobAlreadyStartedException, JobStateNotPendingException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // validate input and set missing information when possible
    JobsHelper.validateJobInformation(user, job);

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      job = JobsHelper.rejectJob(job, details);
      return job;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_JOB_PARAM, job);
    }
  }

  public static <T extends IsIndexed> List<Job> createJobs(User user, SelectedItems<T> selectedItems, String jobName,
    String pluginName, Map<String, String> value, JobPriority priority, JobParallelism parallelism, boolean async)
    throws AuthorizationDeniedException, RequestNotValidException, JobAlreadyStartedException, NotFoundException,
    GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    List<Job> jobs = new ArrayList<>();

    // check user permissions
    controllerAssistant.checkRoles(user);

    // split instances
    HashMap<String, SelectedItems<T>> instancesItems = JobsHelper.splitInstancesItems(selectedItems);

    for (String instance : instancesItems.keySet()) {
      Job job = new Job();
      job.setName(jobName);
      job.setSourceObjects(instancesItems.get(instance));
      job.setPlugin(pluginName);
      job.setPluginParameters(value);
      job.setInstanceId(instance);
      job.setPriority(priority);
      job.setParallelism(parallelism);

      JobUserDetails jobUserDetails = new JobUserDetails();
      jobUserDetails.setUsername(user.getName());
      jobUserDetails.setEmail(user.getEmail());
      jobUserDetails.setFullname(user.getFullName());
      jobUserDetails.setRole(RodaConstants.PreservationAgentRole.EXECUTING_PROGRAM.toString());
      job.getJobUsersDetails().add(jobUserDetails);
      // When it is RODA CENTRAL
      if (instance == null) {
        Job updatedJob = createJob(user, job, async);
        jobs.add(updatedJob);
      } else {

        // validate input and set missing information when possible
        JobsHelper.validateAndSetJobInformation(user, job);

        Job updatedJob = new Job(job);

        LogEntryState state = LogEntryState.SUCCESS;

        try {
          // delegate
          updatedJob = JobsHelper.createJob(job, async);
          jobs.add(updatedJob);
        } catch (RODAException e) {
          state = LogEntryState.FAILURE;
          throw e;
        } finally {
          // register action
          controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_JOB_PARAM, updatedJob);
        }
      }
    }
    return jobs;
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
