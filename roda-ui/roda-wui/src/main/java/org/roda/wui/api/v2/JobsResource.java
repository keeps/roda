package org.roda.wui.api.v2;

import java.util.ArrayList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.server.JSONP;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.EntityResponse;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobUserDetails;
import org.roda.core.data.v2.jobs.Jobs;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Reports;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.BrowserHelper;
import org.roda.wui.api.controllers.JobsHelper;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;
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

  @Override
  public Reports listJobReports(String jobId, boolean justFailed, String start, String limit) {
    // get user
    User user = UserUtility.getApiUser(request);

    try {
      // delegate action to controller
      return JobsHelper.getJobReportsFromIndexResult(user, jobId, justFailed, start, limit, new ArrayList<>());
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public Report getJobReport(String jobId, String jobReportId) {
    // get user
    User user = UserUtility.getApiUser(request);
    try {
      final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
      // check permissions
      controllerAssistant.checkRoles(user);
      LogEntryState state = LogEntryState.SUCCESS;
      ModelService model = RodaCoreFactory.getModelService();

      try {
        return model.retrieveJobReport(jobId, jobReportId);
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
  public IndexedReport getIndexedJobReport(String jobReportId) {
    User user = UserUtility.getApiUser(request);
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    try {
      // check user permissions
      controllerAssistant.checkRoles(user, IndexedReport.class);

      LogEntryState state = LogEntryState.SUCCESS;

      try {
        // delegate
        final IndexedReport ret = BrowserHelper.retrieve(IndexedReport.class, jobReportId, new ArrayList<>());

        // checking object permissions
        controllerAssistant.checkObjectPermissions(user, ret, IndexedReport.class);

        return ret;
      } catch (RODAException e) {
        state = LogEntryState.FAILURE;
        throw e;
      } finally {
        // register action
        controllerAssistant.registerAction(user, jobReportId, state, RodaConstants.CONTROLLER_CLASS_PARAM,
          IndexedReport.class.getSimpleName());
      }
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}/attachment/{" + RodaConstants.API_PATH_PARAM_JOB_ATTACHMENT_ID
    + "}")
  @Produces({ExtraMediaType.APPLICATION_ZIP})
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @Operation(summary = "Get attachment", description = "Gets the attachments of a job", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ApiResponseMessage.class)))})
  public Response retrieveJobAttachment(
    @Parameter(description = "The ID of the existing job", required = true) @PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId,
    @Parameter(description = "The ID of the existing attachment", required = true) @PathParam(RodaConstants.API_PATH_PARAM_JOB_ATTACHMENT_ID) String attachmentId,
    @Parameter(description = "Choose format in which to get the attachments", schema = @Schema(allowableValues = {
      ExtraMediaType.APPLICATION_ZIP}, defaultValue = ExtraMediaType.APPLICATION_ZIP)) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {

    // get user
    User user = UserUtility.getApiUser(request);

    EntityResponse response = org.roda.wui.api.controllers.Jobs.retrieveJobAttachment(user, jobId, attachmentId);
    return ApiUtils.okResponse((StreamResponse) response);
  }
}
