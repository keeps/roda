/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SelectedItemsUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.JobStateNotPendingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.StringResponse;
import org.roda.core.data.v2.generics.select.SelectedItemsListRequest;
import org.roda.core.data.v2.generics.select.SelectedItemsNoneRequest;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.CreateJobRequest;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.JobUserDetails;
import org.roda.core.data.v2.jobs.Jobs;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginInfoRequest;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Reports;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.JobService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.api.v2.utils.ExtraMediaType;
import org.roda.wui.client.common.utils.JobUtils;
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
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author António Lindo <alindo@keep.pt>
 */

@RestController
@RequestMapping(path = "/api/v2/jobs")
public class JobsController implements JobsRestService, Exportable {
  @Autowired
  HttpServletRequest request;

  @Autowired
  JobService jobService;

  @Autowired
  IndexService indexService;

  @Override
  public Job getJobFromModel(String jobId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return jobService.getJobFromModel(jobId);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (NotFoundException | RequestNotValidException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId);
    }
  }

  @Override
  public StringResponse obtainJobCommand(@RequestBody Job job) {
    String path = request.getRequestURL().toString().split("/api")[0];
    return new StringResponse(jobService.buildCurlCommand(path, job));
  }

  @Override
  public Job createJob(@RequestBody CreateJobRequest jobRequest) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      Class<? extends IsIndexed> sourceObjectsClass = null;
      if (!(jobRequest.getSourceObjects() instanceof SelectedItemsNoneRequest)) {
        sourceObjectsClass = SelectedItemsUtils.parseClass(jobRequest.getSourceObjectsClass());
      }
      SelectedItems<?> sourceObjects = CommonServicesUtils.convertSelectedItems(jobRequest.getSourceObjects(),
        sourceObjectsClass);
      Job job = JobUtils.createJob(jobRequest.getName(), JobPriority.valueOf(jobRequest.getPriority()),
        JobParallelism.valueOf(jobRequest.getParallelism()), sourceObjects, jobRequest.getPlugin(),
        jobRequest.getPluginParameters());
      // validate input and set missing information when possible
      jobService.validateAndSetJobInformation(requestContext.getUser(), job);
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return jobService.createJob(job, true);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | JobAlreadyStartedException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_JOB_CREATE_REQUEST,
        jobRequest);
    }
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
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId);
    }
    return null;
  }

  @Override
  public Jobs approveJob(SelectedItemsRequest selectedJobs) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    ModelService model = RodaCoreFactory.getModelService();

    Jobs jobs = new Jobs();

    try {
      if (selectedJobs instanceof SelectedItemsListRequest jobsList) {
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
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | JobAlreadyStartedException | JobStateNotPendingException | NotFoundException
      | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_ITEMS_PARAM,
        selectedJobs);
    }

    return jobs;
  }

  @Override
  public Jobs rejectJob(SelectedItemsRequest selectedJobs, String details) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    ModelService model = RodaCoreFactory.getModelService();

    Jobs jobs = new Jobs();
    try {
      if (selectedJobs instanceof SelectedItemsListRequest jobsList) {
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
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | JobStateNotPendingException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_ITEMS_PARAM,
        selectedJobs);
    }
    return jobs;
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
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId,
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
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_JOB_ID_PARAM, jobId,
        RodaConstants.CONTROLLER_JOB_REPORT_ID_PARAM, jobReportId);
    }
  }

  @GetMapping(path = "/{id}/attachment/{attachment-id}", produces = ExtraMediaType.APPLICATION_ZIP)
  @Operation(summary = "Gets the Job attachment", description = "Gets the attachments of a job", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> retrieveJobAttachment(
    @Parameter(description = "The ID of the existing job", required = true) @PathVariable(name = "id") String id,
    @Parameter(description = "The ID of the existing attachment", required = true) @PathVariable(name = "attachment-id") String attachmentId,
    WebRequest webRequest) {

    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      StreamResponse streamResponse = jobService.retrieveJobAttachment(id, attachmentId);
      return ApiUtils.okResponse(streamResponse, webRequest);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_JOB_ID_PARAM, id,
        RodaConstants.CONTROLLER_JOB_ATTACHMENT_ID_PARAM, attachmentId);
    }
  }

  @Override
  public List<PluginInfo> getJobPluginInfo(@RequestBody PluginInfoRequest pluginInfoRequest) {
    List<PluginInfo> pluginsInfo = new ArrayList<>();

    return jobService.getJobPluginInfo(pluginInfoRequest, pluginsInfo);
  }

  @Override
  public IndexedJob findByUuid(String uuid, String localeString) {
    return indexService.retrieve(IndexedJob.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<IndexedJob> find(@RequestBody FindRequest findRequest, String localeString) {
    return indexService.find(IndexedJob.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_JOB)) {
      return new LongResponse(indexService.count(IndexedJob.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(SuggestRequest suggestRequest) {
    return indexService.suggest(suggestRequest, IndexedJob.class);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    // delegate
    return ApiUtils.okResponse(indexService.exportToCSV(findRequestString, IndexedJob.class));
  }
}
