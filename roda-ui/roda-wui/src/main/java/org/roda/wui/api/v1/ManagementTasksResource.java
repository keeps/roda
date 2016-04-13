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
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.ORCHESTRATOR_METHOD;
import org.roda.core.data.v2.messages.Message;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.plugins.plugins.base.LogCleanerPlugin;
import org.roda.core.plugins.plugins.base.ReindexAIPPlugin;
import org.roda.core.plugins.plugins.base.ReindexJobPlugin;
import org.roda.core.plugins.plugins.base.ReindexRodaEntityPlugin;
import org.roda.wui.api.controllers.Jobs;
import org.roda.wui.api.v1.entities.TaskList;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.common.RodaCoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

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
  public Response executeTask(
    @ApiParam(value = "", allowableValues = "index", defaultValue = "index") final @PathParam("sub_resource") String sub_resource,
    @ApiParam(value = "", allowableValues = "reindex,logclean", defaultValue = "reindex") final @PathParam("task_id") String task_id,
    @ApiParam(value = "", allowableValues = "aip,job,risk,agent,format,message", defaultValue = "aip") @QueryParam("entity") String entity,
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
          if ("aip".equals(entity)) {
            if (params.isEmpty()) {
              response = createJobToReindexAllAIPs(user, startDate);
            } else {
              response = createJobToReindexAIPs(user, params, startDate);
            }
          } else if ("job".equals(entity)) {
            response = createJobToReindexAllJobs(user, startDate);
          } else if ("risk".equals(entity)) {
            response = createJobToReindexAllRisks(user, startDate);
          } else if ("agent".equals(entity)) {
            response = createJobToReindexAllAgents(user, startDate);
          } else if ("format".equals(entity)) {
            response = createJobToReindexAllFormats(user, startDate);
          } else if ("message".equals(entity)) {
            response = createJobToReindexAllMessages(user, startDate);
          }
        } else if ("logclean".equals(task_id)) {
          response = createJobForRunningLogCleaner(user, params);
        }
      }
      return Response.ok().entity(response).build();
    }
  }

  private ApiResponseMessage createJobToReindexAllJobs(RodaUser user, Date startDate) {
    ApiResponseMessage response;
    response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'Jobs' job")
      .setOrchestratorMethod(ORCHESTRATOR_METHOD.RUN_PLUGIN).setPlugin(ReindexJobPlugin.class.getCanonicalName());
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Reindex Jobs job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "reindex jobs", null, duration);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      LOGGER.error("Error creating reindex Jobs job", e);
    }
    return response;
  }

  private ApiResponseMessage createJobToReindexAllRisks(RodaUser user, Date startDate) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'Risks' job")
      .setOrchestratorMethod(ORCHESTRATOR_METHOD.RUN_PLUGIN)
      .setPlugin(ReindexRodaEntityPlugin.class.getCanonicalName());
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME, Risk.class.getCanonicalName());
    job.setPluginParameters(pluginParameters);
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Reindex job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "reindex risks", null, duration);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      LOGGER.error("Error creating reindex Risks job", e);
    }
    return response;
  }

  private ApiResponseMessage createJobToReindexAllAgents(RodaUser user, Date startDate) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'Agents' job")
      .setOrchestratorMethod(ORCHESTRATOR_METHOD.RUN_PLUGIN)
      .setPlugin(ReindexRodaEntityPlugin.class.getCanonicalName());
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME, Agent.class.getCanonicalName());
    job.setPluginParameters(pluginParameters);
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Reindex job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "reindex agents", null, duration);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      LOGGER.error("Error creating reindex Agents job", e);
    }
    return response;
  }

  private ApiResponseMessage createJobToReindexAllFormats(RodaUser user, Date startDate) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'Formats' job")
      .setOrchestratorMethod(ORCHESTRATOR_METHOD.RUN_PLUGIN)
      .setPlugin(ReindexRodaEntityPlugin.class.getCanonicalName());
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME, Format.class.getCanonicalName());
    job.setPluginParameters(pluginParameters);
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Reindex job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "reindex formats", null, duration);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      LOGGER.error("Error creating reindex Formats job", e);
    }
    return response;
  }

  private ApiResponseMessage createJobToReindexAllMessages(RodaUser user, Date startDate) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'Messages' job")
      .setOrchestratorMethod(ORCHESTRATOR_METHOD.RUN_PLUGIN)
      .setPlugin(ReindexRodaEntityPlugin.class.getCanonicalName());
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME, Message.class.getCanonicalName());
    job.setPluginParameters(pluginParameters);
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Reindex job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "reindex messages", null, duration);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      LOGGER.error("Error creating reindex Messages job", e);
    }
    return response;
  }

  private ApiResponseMessage createJobToReindexAllAIPs(RodaUser user, Date startDate) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job();
    job.setName("Management Task | Reindex 'all AIPs' job").setOrchestratorMethod(ORCHESTRATOR_METHOD.ON_ALL_AIPS)
      .setPlugin(ReindexAIPPlugin.class.getCanonicalName());
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    job.setPluginParameters(pluginParameters);
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Reindex job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "reindex all aips", null, duration);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      LOGGER.error("Error creating reindex job", e);
    }
    return response;
  }

  private ApiResponseMessage createJobToReindexAIPs(RodaUser user, List<String> params, Date startDate) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job();
    job.setName("Management Task | Reindex 'AIPs' job").setOrchestratorMethod(ORCHESTRATOR_METHOD.ON_AIPS)
      .setPlugin(ReindexAIPPlugin.class.getCanonicalName()).setObjects(new SelectedItemsList(params));
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Reindex job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "reindex aips", null, duration, "params", params);
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
