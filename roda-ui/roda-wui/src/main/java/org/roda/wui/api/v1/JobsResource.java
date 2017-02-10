/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Jobs;
import org.roda.core.data.v2.jobs.Reports;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.JobsHelper;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path(JobsResource.ENDPOINT)
@Api(value = JobsResource.SWAGGER_ENDPOINT)
public class JobsResource {
  public static final String ENDPOINT = "/v1/jobs";
  public static final String SWAGGER_ENDPOINT = "v1 jobs";

  @Context
  private HttpServletRequest request;

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "List jobs", notes = "Get a list of jobs.", response = Jobs.class, responseContainer = "List")
  public Response listJobs(@QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Jobs jobs = JobsHelper.getJobsFromIndexResult(user, start, limit, new ArrayList<>());
    return Response.ok(jobs, mediaType).build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Create a new job", notes = "Create a new job.", response = Job.class)
  public Response createJob(Job job, @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Job updatedJob = org.roda.wui.api.controllers.Jobs.createJob(user, job, true);
    return Response.created(ApiUtils.getUriFromRequest(request)).entity(updatedJob).type(mediaType).build();
  }

  @POST
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Start an already created job", notes = "Start job.", response = Job.class)
  public Response startJob(@PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Job job = org.roda.wui.api.controllers.Jobs.startJob(user, jobId);
    return Response.created(ApiUtils.getUriFromRequest(request)).entity(job).type(mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get job", notes = "Gets a particular job.", response = Job.class)
  public Response getJob(@PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Job job = org.roda.wui.api.controllers.Browser.retrieve(user, Job.class, jobId, new ArrayList<>());
    return Response.ok(job, mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}/stop")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Stop job", notes = "Stop a particular job.", response = Job.class)
  public Response stopJob(@PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    org.roda.wui.api.controllers.Jobs.stopJob(user, jobId);
    return Response.ok("Stopped", mediaType).build();
  }

  @DELETE
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Delete job", notes = "Delete a particular job, stoping it if still running.", response = ApiResponseMessage.class)
  public Response deleteJob(@PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    org.roda.wui.api.controllers.Jobs.deleteJob(user, jobId);
    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, "Job deleted"), mediaType).build();
  }

  @GET
  @Path("/{" + RodaConstants.API_PATH_PARAM_JOB_ID + "}/reports")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "List job reports", notes = "Get a list of job reports.", response = Reports.class, responseContainer = "List")
  public Response listJobReports(@PathParam(RodaConstants.API_PATH_PARAM_JOB_ID) String jobId,
    @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "If just the failed reports should be included in the response or all the job reports", defaultValue = "false") @QueryParam(RodaConstants.API_PATH_PARAM_JOB_JUST_FAILED) boolean justFailed,
    @ApiParam(value = "Index of the first element to return", defaultValue = "0") @QueryParam(RodaConstants.API_QUERY_KEY_START) String start,
    @ApiParam(value = "Maximum number of elements to return", defaultValue = "100") @QueryParam(RodaConstants.API_QUERY_KEY_LIMIT) String limit)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);

    // get user
    User user = UserUtility.getApiUser(request);

    // delegate action to controller
    Reports reports = JobsHelper.getJobReportsFromIndexResult(user, jobId, justFailed, start, limit, new ArrayList<>());
    return Response.ok(reports, mediaType).build();
  }

}
