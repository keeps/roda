package org.roda.wui.api.v2;

import java.util.ArrayList;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobUserDetails;
import org.roda.core.data.v2.jobs.Jobs;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.BrowserHelper;
import org.roda.wui.api.controllers.JobsHelper;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.client.services.JobsService;
import org.roda.wui.common.ControllerAssistant;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Path(org.roda.wui.api.v1.JobsResource.ENDPOINT)
@Tag(name = org.roda.wui.api.v1.JobsResource.SWAGGER_ENDPOINT)
public class JobsResource implements JobsService {
  public static final String ENDPOINT = "/jobs";
  public static final String SWAGGER_ENDPOINT = "v1 jobs";

  @Context
  private HttpServletRequest request;

  @Override
  public Jobs listJobs(String start, String limit) {
    // get user
    User user = UserUtility.getApiUser(request);

    try {
      // delegate action to controller
      return JobsHelper.getJobsFromIndexResult(user, start, limit, new ArrayList<>());
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public Job createJob(Job job) {
    // get user
    User user = UserUtility.getApiUser(request);

    try {
      ControllerAssistant controllerAssistant = new ControllerAssistant() {};

      // validate input and set missing information when possible
      JobsHelper.validateAndSetJobInformation(user, job);

      // check user permissions
      controllerAssistant.checkRoles(user);

      LogEntryState state = LogEntryState.SUCCESS;
      Job updatedJob = new Job(job);

      try {
        // delegate
        updatedJob = JobsHelper.createJob(job, true);
        return updatedJob;
      } catch (RODAException e) {
        state = LogEntryState.FAILURE;
        throw e;
      } finally {
        // register action
        controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_JOB_PARAM, updatedJob);
      }
    } catch (RODAException e) {
      throw new RESTException(e);
    }

  }

  @Override
  public Job startJob(String jobId) {
    // get user
    User user = UserUtility.getApiUser(request);

    try {
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
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public Job getJob(String jobId) {
    // get user
    User user = UserUtility.getApiUser(request);

    try {
      final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

      // check user permissions
      controllerAssistant.checkRoles(user, Job.class);

      LogEntryState state = LogEntryState.SUCCESS;

      try {
        // delegate
        final Job job = BrowserHelper.retrieve(Job.class, jobId, new ArrayList<>());

        // checking object permissions
        controllerAssistant.checkObjectPermissions(user, job, Job.class);

        return job;
      } catch (RODAException e) {
        state = LogEntryState.FAILURE;
        throw e;
      } finally {
        // register action
        controllerAssistant.registerAction(user, jobId, state, RodaConstants.CONTROLLER_CLASS_PARAM,
          Job.class.getSimpleName());
      }
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public Void stopJob(String jobId) {
    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    try {
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
    } catch (RODAException e) {
      throw new RESTException(e);
    }
    return null;
  }

  @Override
  public Job approveJob(SelectedItems<Job> selectedJobs) {
    // get user
    User user = UserUtility.getApiUser(request);

    try {
      ControllerAssistant controllerAssistant = new ControllerAssistant() {};

      ModelService model = RodaCoreFactory.getModelService();

      if (selectedJobs instanceof SelectedItemsList) {
        SelectedItemsList<Job> jobsList = (SelectedItemsList<Job>) selectedJobs;
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
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public Job rejectJob(SelectedItems<Job> selectedJobs, String details) {
    // get user
    User user = UserUtility.getApiUser(request);

    try {
      ControllerAssistant controllerAssistant = new ControllerAssistant() {};

      ModelService model = RodaCoreFactory.getModelService();

      if (selectedJobs instanceof SelectedItemsList) {
        SelectedItemsList<Job> jobsList = (SelectedItemsList<Job>) selectedJobs;
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
    } catch (RODAException e) {
      throw new RESTException(e);
    }

  }

  @Override
  public Void deleteJob(String jobId) {
    // get user
    User user = UserUtility.getApiUser(request);
    try {
      ControllerAssistant controllerAssistant = new ControllerAssistant() {};

      // check user permissions
      controllerAssistant.checkRoles(user);

      LogEntryState state = LogEntryState.SUCCESS;

      try {
        // delegate
        JobsHelper.deleteJob(jobId);
      } catch (RODAException e) {
        state = LogEntryState.FAILURE;
        throw new RESTException(e);
      } finally {
        // register action
        controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId);
      }
    } catch (RODAException e) {
      throw new RESTException(e);
    }
    return null;
  }
}
