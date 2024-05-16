package org.roda.wui.client.services;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Jobs;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Reports;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * @author António Lindo <alindo@keep.pt>
 */

@Tag(name = "v2 jobs")
@RequestMapping(path = "../api/v2/jobs")
public interface JobsRestService extends RODAEntityRestService<Job> {

  @RequestMapping(method = RequestMethod.POST, path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create job", description = "Creates a new job", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Job.class))), responses = {
    @ApiResponse(responseCode = "201", description = "Job created successfully", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job createJob(
    @Parameter(name = "job", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) Job job);

  @RequestMapping(method = RequestMethod.GET, path = "/{id}/stop", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  @Operation(summary = "Stop job", description = "Stops a job", responses = {
    @ApiResponse(responseCode = "202", description = "Accepted"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void stopJob(@PathVariable(name = "id") String id);

  @RequestMapping(method = RequestMethod.POST, path = "/approve", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Approve job", description = "Approves a job.", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Jobs approveJob(
    @Parameter(name = "jobId", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<Job> selectedJobs);

  @RequestMapping(method = RequestMethod.POST, path = "/reject", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Reject job", description = "Rejects a job.", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Jobs rejectJob(
    @Parameter(name = "jobId", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<Job> selectedJobs,
    @Parameter(description = "Justification for why the job was rejected") @RequestParam(name = RodaConstants.API_QUERY_JOB_DETAILS) String details);

  @RequestMapping(method = RequestMethod.GET, path = "/{id}/reports", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List job reports", description = "Gets a list of job reports", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Reports.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Reports listJobReports(@PathVariable(name = "id") String id,
    @Parameter(description = "If just the failed reports should be included in the response or all the job reports", schema = @Schema(defaultValue = "false")) @RequestParam(name = RodaConstants.API_PATH_PARAM_JOB_JUST_FAILED, defaultValue = "false", required = false) boolean justFailed,
    @Parameter(description = "Index of the first element to return", schema = @Schema(defaultValue = "0")) @RequestParam(value = RodaConstants.API_QUERY_KEY_START, defaultValue = "0", required = false) String start,
    @Parameter(description = "Maximum number of elements to return", schema = @Schema(defaultValue = "100")) @RequestParam(value = RodaConstants.API_QUERY_KEY_LIMIT, defaultValue = "0", required = false) String limit);

  @RequestMapping(method = RequestMethod.GET, path = "/{id}/report/{" + RodaConstants.API_PATH_PARAM_JOB_REPORT_ID
    + "}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get job reports items", description = "Gets job report items", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Report.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Report getJobReport(@PathVariable(name = "id") String id,
                      @PathVariable(name = RodaConstants.API_PATH_PARAM_JOB_REPORT_ID) String reportId);

  @RequestMapping(method = RequestMethod.GET, path = "/report/{jobReportId}/indexed", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get job reports items", description = "Gets job report items", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = IndexedReport.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  IndexedReport getIndexedJobReport(
    @PathVariable(name = RodaConstants.API_PATH_PARAM_JOB_REPORT_ID) String jobReportId);

  @RequestMapping(method = RequestMethod.POST, path = "/plugin-info", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get job plugin info", description = "Gets the information about the plugins executed on the job", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Job.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PluginInfo.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  List<PluginInfo> getJobPluginInfo(
    @Parameter(name = "job", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) Job job);
}
