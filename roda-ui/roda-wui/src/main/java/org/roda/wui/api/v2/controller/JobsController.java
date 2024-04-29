package org.roda.wui.api.v2.controller;

import java.util.ArrayList;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
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
import org.roda.core.model.ModelService;
import org.roda.wui.api.v1.utils.ExtraMediaType;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.JobService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.client.services.JobsRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author António Lindo <alindo@keep.pt>
 */

@RestController
@RequestMapping(path = "/api/v2/jobs")
@Tag(name = JobsController.SWAGGER_ENDPOINT)
public class JobsController implements JobsRestService {
  public static final String SWAGGER_ENDPOINT = "v2 jobs";

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private JobService jobService;

  @Autowired
  private IndexService indexService;

  @Override
  public Job createJob(@RequestBody Job job) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // validate input and set missing information when possible
      jobService.validateAndSetJobInformation(requestContext.getUser(), job);
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return jobService.createJob(job, true);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_JOB_PARAM, job);
    }
  }

  @Override
  public Job startJob(String jobId) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      return jobService.startJob(jobId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId);
    }
  }

  @Override
  public Job getJob(String jobId) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.retrieve(requestContext.getUser(), Job.class, jobId, new ArrayList<>());
  }

  @Override
  public Void stopJob(String jobId) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      jobService.stopJob(jobId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId);
    }
    return null;
  }

  @Override
  public Jobs approveJob(SelectedItems<Job> selectedJobs) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    ModelService model = RodaCoreFactory.getModelService();

    Jobs jobs = new Jobs();

    try {
      if (selectedJobs instanceof SelectedItemsList<Job> jobsList) {
        for (String id : jobsList.getIds()) {
          Job job = model.retrieveJob(id);
          if (job.getState().equals(Job.JOB_STATE.PENDING_APPROVAL)) {
            // validate input and set missing information when possible
            jobService.validateJobInformation(job);

            // check user permissions
            controllerAssistant.checkRoles(requestContext.getUser());

            Job retrievedJob = model.retrieveJob(job.getId());

            JobUserDetails jobUserDetails = jobService.buildJobUserDetails(requestContext.getUser());
            retrievedJob.getJobUsersDetails().add(jobUserDetails);
            model.createOrUpdateJob(retrievedJob);

            // delegate
            jobs.addJob(jobService.startJob(job.getId()));
          }
        }
      }
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_ITEMS_PARAM,
        selectedJobs);
    }

    return jobs;
  }

  @Override
  public Jobs rejectJob(SelectedItems<Job> selectedJobs, String details) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    ModelService model = RodaCoreFactory.getModelService();

    Jobs jobs = new Jobs();
    try {
      if (selectedJobs instanceof SelectedItemsList<Job> jobsList) {
        for (String id : jobsList.getIds()) {
          Job job = model.retrieveJob(id);
          if (job.getState().equals(Job.JOB_STATE.PENDING_APPROVAL)) {
            // validate input and set missing information when possible
            jobService.validateJobInformation(job);

            // check user permissions
            controllerAssistant.checkRoles(requestContext.getUser());

            // delegate
            jobs.addJob(jobService.rejectJob(job, details));
          }
        }
      }
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_ITEMS_PARAM,
        selectedJobs);
    }
    return jobs;
  }

  @Override
  public Void deleteJob(String jobId) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      jobService.deleteJob(jobId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId);
    }
    return null;
  }

  @Override
  public Reports listJobReports(String jobId, boolean justFailed, String start, String limit) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate action to controller
      return jobService.getJobReportsFromIndexResult(requestContext.getUser(), jobId, justFailed, start, limit,
        new ArrayList<>());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId,
        RodaConstants.CONTROLLER_JOB_JUST_FAILED_PARAM, justFailed, RodaConstants.CONTROLLER_START_PARAM, start,
        RodaConstants.CONTROLLER_LIMIT_PARAM, limit);
    }
  }

  @Override
  public Report getJobReport(String jobId, String jobReportId) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      ModelService model = RodaCoreFactory.getModelService();
      return model.retrieveJobReport(jobId, jobReportId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId,
        RodaConstants.CONTROLLER_JOB_REPORT_ID_PARAM, jobReportId);
    }
  }

  @Override
  public IndexedReport getIndexedJobReport(String jobReportId) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser(), IndexedReport.class);

      // delegate
      final IndexedReport ret = indexService.retrieve(requestContext.getUser(), IndexedReport.class, jobReportId,
        new ArrayList<>());

      // checking object permissions
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), ret, IndexedReport.class);

      return ret;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), jobReportId, state,
        RodaConstants.CONTROLLER_JOB_REPORT_ID_PARAM, jobReportId);
    }
  }

  @GetMapping(path = "/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}/attachment/{"
    + RodaConstants.API_PATH_PARAM_JOB_ATTACHMENT_ID + "}", produces = ExtraMediaType.APPLICATION_ZIP)
  @Operation(summary = "Get attachment", description = "Gets the attachments of a job", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> retrieveJobAttachment(
    @Parameter(description = "The ID of the existing job", required = true) @PathVariable(name = RodaConstants.API_PATH_PARAM_JOB_ID) String jobId,
    @Parameter(description = "The ID of the existing attachment", required = true) @PathVariable(name = RodaConstants.API_PATH_PARAM_JOB_ATTACHMENT_ID) String attachmentId,
    WebRequest webRequest) {

    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      StreamResponse streamResponse = jobService.retrieveJobAttachment(jobId, attachmentId);
      return ApiUtils.okResponse(streamResponse, webRequest);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId,
        RodaConstants.CONTROLLER_JOB_ATTACHMENT_ID_PARAM, attachmentId);
    }
  }
}
