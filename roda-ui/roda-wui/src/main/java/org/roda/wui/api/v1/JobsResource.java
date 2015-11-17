/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
/*
JOB OBJECT (Serializable)
###########################
- ID (auto-generated or provided during creation)
- START_DATETIME (auto-generated)
- END_DATETIME (auto-generated)
- STATUS (auto-generated)

- PLUGIN (provided during creation, qualified name string)
- PLUGIN_PARAMS (provided during creation, map<string,string>)

- RESOURCES_TYPE (transferred resources type, i.e., bagit, etc.)

- LIST<T> ???
- T (provided during creation, qualified name string)
- ORCHESTRATOR_METHOD

WORKFLOW
###########################

1) REST API POST to create a new job
2) The newly created job is sent to orchestrator (embedded or distributed)
3) If the orchestrator can execute the job, then return Ok to REST API, NotOk otherwise
4)

 * */

@Path(JobsResource.ENDPOINT)
@Api(value = JobsResource.SWAGGER_ENDPOINT)
public class JobsResource {
  public static final String ENDPOINT = "/v1/jobs";
  public static final String SWAGGER_ENDPOINT = "v1 jobs";

  @Context
  private HttpServletRequest request;

  @GET
  public Response listJobs() {
    return null;
  }

  @GET
  @Path("/{jobId}")
  public Response getJob(@PathParam("jobId") String jobId) {
    return null;
  }

  @POST
  public Response createJobWithoutId() {
    return null;
  }

  @POST
  @Path("/{jobId}")
  public Response createJob(@PathParam("jobId") String jobId) {
    return null;
  }
}
