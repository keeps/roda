package org.roda.wui.client.services;

import org.fusesource.restygwt.client.DirectRestService;
import org.glassfish.jersey.server.JSONP;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Jobs;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Reports;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ExtraMediaType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Path("../api/v2/jobs/")
@Tag(name = "v2")
public interface JobsService extends DirectRestService {

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @Operation(summary = "List jobs", description = "Gets a list of jobs", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Jobs.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  Jobs listJobs(
    @Parameter(description = "Index of the first element to return", schema = @Schema(defaultValue = "0")) @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @Parameter(description = "Maximum number of elements to return", schema = @Schema(defaultValue = "100")) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit);

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @Operation(summary = "Create job", description = "Creates a new job", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Job.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  Job createJob(
    @Parameter(name = "job", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON)) Job job);

  @POST
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @Operation(summary = "Start job", description = "Starts an already created job", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class))),
    @ApiResponse(responseCode = "409", description = "Already started", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  Job startJob(@PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId);

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @Operation(summary = "Get job", description = "Gets a job information", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  Job getJob(@PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId);

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}/stop")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @Operation(summary = "Stop job", description = "Stops a job", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  Void stopJob(@PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId);

  @POST
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}/approve")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @Operation(summary = "Approve job", description = "Approves a job.", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Job.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  Job approveJob(
    @Parameter(name = RodaConstants.API_PATH_PARAM_JOB_ID, required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON)) SelectedItems<Job> selectedJobs);

  @POST
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}/reject")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @Operation(summary = "Reject job", description = "Rejects a job.", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Job.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  Job rejectJob(
    @Parameter(name = RodaConstants.API_PATH_PARAM_JOB_ID, required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON)) SelectedItems<Job> selectedJobs,
    @QueryParam(RodaConstants.API_QUERY_JOB_DETAILS) String details);

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @Operation(summary = "Delete job", description = "Deletes a job, stopping it if still running", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  Void deleteJob(
    @PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId);

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}/reports")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @Operation(summary = "List job reports", description = "Gets a list of job reports", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Reports.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  Reports listJobReports(
    @PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId,
    @Parameter(description = "If just the failed reports should be included in the response or all the job reports", schema = @Schema(defaultValue = "false")) @QueryParam(RodaConstants.API_PATH_PARAM_JOB_JUST_FAILED) boolean justFailed,
    @Parameter(description = "Index of the first element to return", schema = @Schema(defaultValue = "0")) @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @Parameter(description = "Maximum number of elements to return", schema = @Schema(defaultValue = "100")) @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit);

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}/report/{" + RodaConstants.API_PATH_PARAM_JOB_REPORT_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @Operation(summary = "Get job reports items", description = "Gets job report items", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Reports.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  Report getJobReport(
    @PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId,
    @PathParam(RodaConstants.API_PATH_PARAM_JOB_REPORT_ID) String jobReportId);

  @GET
  @Path("/report/{" + RodaConstants.API_PATH_PARAM_JOB_REPORT_ID + "}/indexed")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.APPLICATION_JAVASCRIPT})
  @Operation(summary = "Get job reports items", description = "Gets job report items", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Reports.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  IndexedReport getIndexedJobReport(
    @PathParam(RodaConstants.API_PATH_PARAM_JOB_REPORT_ID) String jobReportId);
}
