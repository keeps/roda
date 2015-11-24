/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RODAException;
import org.roda.core.data.v2.Job;
import org.roda.core.data.v2.Jobs;
import org.roda.core.data.v2.RodaUser;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path(JobsResource.ENDPOINT)
@Api(value = JobsResource.SWAGGER_ENDPOINT)
public class JobsResource {
  public static final String ENDPOINT = "/v1/jobs";
  public static final String SWAGGER_ENDPOINT = "v1 jobs";

  private static Logger LOGGER = LoggerFactory.getLogger(JobsResource.class);

  @Context
  private HttpServletRequest request;

  // TODO this is WIP
  @GET
  @ApiOperation(value = "List Jobs", notes = "Gets a list of Jobs.", response = Job.class, responseContainer = "List")
  public Response listJobs(@QueryParam("acceptFormat") String acceptFormat) {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request.getHeader("Accept"));
    Jobs jobs = new Jobs();
    jobs.getJobs().add(new Job("xtpo"));
    jobs.getJobs().add(new Job("zjas"));
    return Response.ok(jobs, mediaType).build();
  }

  // TODO this is WIP
  @GET
  @Path("/{jobId}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Get Job", notes = "Gets a particular Job.", response = Job.class)
  public Response getJob(@PathParam("jobId") String jobId, @QueryParam("acceptFormat") String acceptFormat) {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request.getHeader("Accept"));

    Job job = new Job();
    job.setId(jobId);
    job.setObjectIds(Arrays.asList("objectId1", "objectId2"));

    return Response.ok(job, mediaType).build();
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Creates a new Job", notes = "Creates a new Job.", response = Job.class)
  public Response createJob(Job job, @QueryParam("acceptFormat") String acceptFormat) throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request.getHeader("Accept"));

    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    // TODO run in a separate thread to be able to return right away
    Job updatedJob = org.roda.wui.api.controllers.Jobs.createJob(user, job);

    return Response.created(ApiUtils.getUriFromRequest(request)).entity(updatedJob).type(mediaType).build();
  }

}
