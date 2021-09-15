/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  public static <T extends IsIndexed> List<Job> createJobs(User user, SelectedItems<T> selectedItems, String jobName,
    String pluginName, Map<String, String> value, boolean async) throws AuthorizationDeniedException,
    RequestNotValidException, JobAlreadyStartedException, NotFoundException, GenericException {
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

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
