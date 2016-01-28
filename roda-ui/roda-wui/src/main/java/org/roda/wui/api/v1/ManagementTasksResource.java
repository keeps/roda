/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
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
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.ORCHESTRATOR_METHOD;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.plugins.plugins.base.LogCleanerPlugin;
import org.roda.core.plugins.plugins.base.ReindexPlugin;
import org.roda.wui.api.controllers.Jobs;
import org.roda.wui.api.v1.entities.TaskList;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.common.RodaCoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;

@Api(value = ManagementTasksResource.SWAGGER_ENDPOINT)
@Path(ManagementTasksResource.ENDPOINT)
public class ManagementTasksResource extends RodaCoreService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ManagementTasksResource.class);

  public static final String ENDPOINT = "/v1/management_tasks";
  public static final String SWAGGER_ENDPOINT = "v1 management tasks";
  private static final TaskList TASKS = new TaskList("index/reindex", "index/logclean");

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response listTasks(@QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) {
    return Response.ok(TASKS, ApiUtils.getMediaType(acceptFormat, request)).build();
  }

  @POST
  @Path("/{sub_resource}/{task_id}")
  public Response executeTask(final @PathParam("sub_resource") String sub_resource,
    final @PathParam("task_id") String task_id, @QueryParam("entity") String entity,
    @QueryParam("params") List<String> params) throws AuthorizationDeniedException {
    Date startDate = new Date();

    // get user & check permissions
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // FIXME see if this is the proper way to ensure that the user can execute
    // this task
    if (!user.getAllGroups().contains("administrators")) {
      throw new AuthorizationDeniedException(
        "User \"" + user.getId() + "\" doesn't have permission the execute the requested task!");
    }

    return execute(user, startDate, sub_resource, task_id, entity, params);

  }

  private Response execute(RodaUser user, Date startDate, final String sub_resource, final String task_id,
    String entity, List<String> params) {
    if (!TASKS.getTasks().contains(sub_resource + "/" + task_id)) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
        "No task was found in the sub-resource \"" + sub_resource + "\" with the id \"" + task_id + "\"")).build();
    } else {
      ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
      if ("index".equals(sub_resource)) {
        if ("reindex".equals(task_id)) {
          if (params.isEmpty()) {
            response = createJobToReindexAllAIPs(user, startDate);
          } else {
            response = createJobToReindexAIPs(user, params, startDate);
          }
          // } else if ("orphans".equals(task_id)) {
          // if (!params.isEmpty()) {
          // RodaCoreFactory.runRemoveOrphansPlugin(params.get(0));
          // // register action
          // long duration = new Date().getTime() - startDate.getTime();
          // registerAction(user, "ManagementTasks", "orphans", null, duration,
          // "params", params);
          // }
        } else if ("logclean".equals(task_id)) {
          response = createJobForRunningLogCleaner(user, params);
        }
      }
      return Response.ok().entity(response).build();
    }
  }

  private ApiResponseMessage createJobToReindexAllAIPs(RodaUser user, Date startDate) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job();
    job.setName("Management Task | Reindex job").setOrchestratorMethod(ORCHESTRATOR_METHOD.ON_ALL_AIPS)
      .setPlugin(ReindexPlugin.class.getCanonicalName());
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Reindex job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "reindex", null, duration);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      LOGGER.error("Error creating reindex job", e);
    }
    return response;
  }

  private ApiResponseMessage createJobToReindexAIPs(RodaUser user, List<String> params, Date startDate) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job();
    job.setName("Management Task | Reindex job").setOrchestratorMethod(ORCHESTRATOR_METHOD.ON_AIPS)
      .setPlugin(ReindexPlugin.class.getCanonicalName()).setObjectIds(params);
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Reindex job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "reindex", null, duration, "params", params);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      LOGGER.error("Error creating reindex job", e);
    }
    return response;
  }

  private ApiResponseMessage createJobForRunningLogCleaner(RodaUser user, List<String> params) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job();
    job.setName("Management Task | Log cleaner job").setOrchestratorMethod(ORCHESTRATOR_METHOD.RUN_PLUGIN)
      .setPlugin(LogCleanerPlugin.class.getCanonicalName());
    if (!params.isEmpty()) {
      Map<String, String> pluginParameters = new HashMap<String, String>();
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INT_VALUE, params.get(0));
      job.setPluginParameters(pluginParameters);
    }
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Log cleaner created (" + jobCreated + ")");
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      LOGGER.error("Error creating log cleaner job", e);
    }

    return response;
  }

}
