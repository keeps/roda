/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.util.ArrayList;
import java.util.List;

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
import org.roda.core.data.v2.TransferredResource;
import org.roda.core.plugins.Plugin;
import org.roda.wui.api.controllers.BrowserHelper;
import org.roda.wui.api.v1.entities.Job;
import org.roda.wui.api.v1.entities.Jobs;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.common.client.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static Logger LOGGER = LoggerFactory.getLogger(JobsResource.class);

  @Context
  private HttpServletRequest request;

  @GET
  public Response listJobs(@QueryParam("acceptFormat") String acceptFormat) {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request.getHeader("Accept"));
    Jobs jobs = new Jobs();
    jobs.getJobs().add(new Job("xtpo"));
    jobs.getJobs().add(new Job("zjas"));
    return Response.ok(jobs, mediaType).build();
  }

  @GET
  @Path("/{jobId}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response getJob(@PathParam("jobId") String jobId, @QueryParam("acceptFormat") String acceptFormat) {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request.getHeader("Accept"));

    Job job = new Job();
    job.setId(jobId);

    return Response.ok(job, mediaType).build();
  }

  /**
   * {"id":"20151119_ingest_PT-KEEPS-CC-3-29","plugin":
   * "org.roda.core.plugins.plugins.ingest.BagitToAIPPlugin","resourceType":
   * "bagit","orchestratorMethod":"runPluginOnTransferredResources","objectType"
   * :"org.roda.core.data.v2.TransferredResource","pluginParameters":null,
   * "objectIds":["PT-KEEPS-CC-3-29.zip"]} curl -H
   * "Content-Type: application/json" -X POST -d 'JSON' -u admin:roda
   * http://192.168.2.56:8888/api/v1/jobs?acceptFormat=json
   */
  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response createJob(Job job, @QueryParam("acceptFormat") String acceptFormat) {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request.getHeader("Accept"));

    if (isJobvalid(job)) {
      if ("runPluginOnTransferredResources".equalsIgnoreCase(job.getOrchestratorMethod())) {
        RodaCoreFactory.getPluginOrchestrator().runPluginOnTransferredResources(
          (Plugin<TransferredResource>) RodaCoreFactory.getPluginManager().getPlugin(job.getPlugin()),
          getTransferredResourcesFromObjectIds(job.getObjectIds()));
      } else {

      }
    } else {
      // TODO return error response
    }

    return Response.ok(job, mediaType).build();
  }

  private List<TransferredResource> getTransferredResourcesFromObjectIds(List<String> objectIds) {
    List<TransferredResource> res = new ArrayList<TransferredResource>();
    for (String objectId : objectIds) {
      try {
        res.add(BrowserHelper.retrieveTransferredResource(objectId));
      } catch (GenericException e) {
        LOGGER.error("Error retrieving transferred resource {}", objectId, e);
      }
    }
    LOGGER.info(">>" + res);
    return res;
  }

  private boolean isJobvalid(Job job) {
    // FIXME
    return true;
  }
}
