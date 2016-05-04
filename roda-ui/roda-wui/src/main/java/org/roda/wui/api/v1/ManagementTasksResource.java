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
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.ExportType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.ORCHESTRATOR_METHOD;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.model.utils.JsonUtils;
import org.roda.core.plugins.plugins.base.ActionLogCleanerPlugin;
import org.roda.core.plugins.plugins.base.ExportAIPPlugin;
import org.roda.core.plugins.plugins.base.ReindexAIPPlugin;
import org.roda.core.plugins.plugins.base.ReindexActionLogPlugin;
import org.roda.core.plugins.plugins.base.ReindexJobPlugin;
import org.roda.core.plugins.plugins.base.ReindexRodaEntityPlugin;
import org.roda.core.plugins.plugins.base.ReindexTransferredResourcePlugin;
import org.roda.wui.api.controllers.Jobs;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
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

  @Context
  private HttpServletRequest request;

  @POST
  @Path("/index/reindex")
  public Response executeIndexReindexTask(
    @ApiParam(value = "", allowableValues = "aip,job,risk,riskincidence,agent,format,notification,actionlogs,transferred_resources", defaultValue = "aip") @QueryParam("entity") String entity,
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

    return executeReindex(user, startDate, entity, params);

  }

  @POST
  @Path("/index/actionlogclean")
  public Response executeIndexActionLogCleanTask(
    @ApiParam(value = "Amount of days to keep action information in the index", defaultValue = "30") @QueryParam("daysToKeep") String daysToKeep)
    throws AuthorizationDeniedException {
    Date startDate = new Date();

    // get user & check permissions
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // FIXME see if this is the proper way to ensure that the user can execute
    // this task
    if (!user.getAllGroups().contains("administrators")) {
      throw new AuthorizationDeniedException(
        "User \"" + user.getId() + "\" doesn't have permission the execute the requested task!");
    }

    return Response.ok().entity(createJobForRunningActionlogCleaner(user, daysToKeep, startDate)).build();
  }

  @POST
  @Path("/model/exportAIPs")
  public Response exportAIPs(
    @ApiParam(value = "Folder where to put the export", defaultValue = "") @QueryParam("outputFolder") @DefaultValue("") String outputFolder,
    @ApiParam(value = "Selected items (serialized as json using 'JsonUtils.getJsonFromObject()')", defaultValue = "") @QueryParam("selected") @DefaultValue("") String selected,
    @ApiParam(value = "Export type", allowableValues = "SINGLE_ZIP,MULTI_ZIP,FOLDER", defaultValue = "SINGLE_ZIP", required = true) @DefaultValue("SINGLE_ZIP") @QueryParam("type") ExportType type)
    throws AuthorizationDeniedException {
    Date startDate = new Date();

    // get user & check permissions
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // FIXME see if this is the proper way to ensure that the user can execute
    // this task
    if (!user.getAllGroups().contains("administrators")) {
      throw new AuthorizationDeniedException(
        "User \"" + user.getId() + "\" doesn't have permission the execute the requested task!");
    }

    ApiResponseMessage response = createJobToExportAIPs(user, startDate, outputFolder, selected, type);

    return Response.ok().entity(response).build();
  }

  private Response executeReindex(RodaUser user, Date startDate, String entity, List<String> params) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
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
    } else if ("riskincidence".equals(entity)) {
      response = createJobToReindexAllRiskIncidences(user, startDate);
    } else if ("agent".equals(entity)) {
      response = createJobToReindexAllAgents(user, startDate);
    } else if ("format".equals(entity)) {
      response = createJobToReindexAllFormats(user, startDate);
    } else if ("notification".equals(entity)) {
      response = createJobToReindexAllNotifications(user, startDate);
    } else if ("transferred_resources".equals(entity)) {
      response = createJobToReindexTransferredResources(user, startDate, params);
    } else if ("actionlogs".equals(entity)) {
      response = createJobToReindexActionlogs(user, startDate, params);
    }
    return Response.ok().entity(response).build();
  }

  private ApiResponseMessage createJobToExportAIPs(RodaUser user, Date startDate, String outputFolder, String selected,
    ExportType type) {
    ApiResponseMessage response;
    response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    try {
      Job job = new Job();
      SelectedItems selectedItems = JsonUtils.getObjectFromJson(selected, SelectedItems.class);
      job.setName("Management Task | Export 'AIPs' job").setOrchestratorMethod(ORCHESTRATOR_METHOD.ON_AIPS)
        .setPlugin(ExportAIPPlugin.class.getCanonicalName()).setObjects(selectedItems);
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put(ExportAIPPlugin.EXPORT_FOLDER_PARAMETER, outputFolder);
      parameters.put(ExportAIPPlugin.EXPORT_TYPE, type.toString());
      job.setPluginParameters(parameters);
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Export AIPs job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "export aips", null, duration);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | JobAlreadyStartedException e) {
      LOGGER.error("Error creating export AIPs job", e);
    }
    return response;
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
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | JobAlreadyStartedException e) {
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
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | JobAlreadyStartedException e) {
      LOGGER.error("Error creating reindex Risks job", e);
    }
    return response;
  }

  private ApiResponseMessage createJobToReindexAllRiskIncidences(RodaUser user, Date startDate) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'Risk RiskIncidences' job")
      .setOrchestratorMethod(ORCHESTRATOR_METHOD.RUN_PLUGIN)
      .setPlugin(ReindexRodaEntityPlugin.class.getCanonicalName());
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME, RiskIncidence.class.getCanonicalName());
    job.setPluginParameters(pluginParameters);
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Reindex job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "reindex risk incidences", null, duration);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | JobAlreadyStartedException e) {
      LOGGER.error("Error creating reindex Risk Incidences job", e);
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
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | JobAlreadyStartedException e) {
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
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | JobAlreadyStartedException e) {
      LOGGER.error("Error creating reindex Formats job", e);
    }
    return response;
  }

  private ApiResponseMessage createJobToReindexAllNotifications(RodaUser user, Date startDate) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'Notifications' job")
      .setOrchestratorMethod(ORCHESTRATOR_METHOD.RUN_PLUGIN)
      .setPlugin(ReindexRodaEntityPlugin.class.getCanonicalName());
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME, Notification.class.getCanonicalName());
    job.setPluginParameters(pluginParameters);
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Reindex job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "reindex notifications", null, duration);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | JobAlreadyStartedException e) {
      LOGGER.error("Error creating reindex Notifications job", e);
    }
    return response;
  }

  private ApiResponseMessage createJobToReindexTransferredResources(RodaUser user, Date startDate,
    List<String> params) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'TransferredResources' job")
      .setOrchestratorMethod(ORCHESTRATOR_METHOD.RUN_PLUGIN)
      .setPlugin(ReindexTransferredResourcePlugin.class.getCanonicalName());
    if (!params.isEmpty()) {
      Map<String, String> pluginParameters = new HashMap<>();
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_STRING_VALUE, params.get(0));
      job.setPluginParameters(pluginParameters);
    }
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Reindex job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "reindex transferred resources", null, duration);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | JobAlreadyStartedException e) {
      LOGGER.error("Error creating reindex TransferredResources job", e);
    }
    return response;
  }

  private ApiResponseMessage createJobToReindexActionlogs(RodaUser user, Date startDate, List<String> params) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'ActionLogs' job")
      .setOrchestratorMethod(ORCHESTRATOR_METHOD.RUN_PLUGIN).setPlugin(ReindexActionLogPlugin.class.getCanonicalName());
    if (!params.isEmpty()) {
      Map<String, String> pluginParameters = new HashMap<>();
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INT_VALUE, params.get(0));
      job.setPluginParameters(pluginParameters);
    }
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Reindex job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "reindex action logs", null, duration);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | JobAlreadyStartedException e) {
      LOGGER.error("Error creating reindex Action logs job", e);
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
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | JobAlreadyStartedException e) {
      LOGGER.error("Error creating reindex job", e);
    }
    return response;
  }

  private ApiResponseMessage createJobToReindexAIPs(RodaUser user, List<String> params, Date startDate) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job();
    job.setName("Management Task | Reindex 'AIPs' job").setOrchestratorMethod(ORCHESTRATOR_METHOD.ON_AIPS)
      .setPlugin(ReindexAIPPlugin.class.getCanonicalName())
      .setObjects(new SelectedItemsList(params, IndexedAIP.class.getName()));
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Reindex job created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "reindex aips", null, duration, "params", params);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | JobAlreadyStartedException e) {
      LOGGER.error("Error creating reindex job", e);
    }
    return response;
  }

  private ApiResponseMessage createJobForRunningActionlogCleaner(RodaUser user, String daysToKeep, Date startDate) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job();
    job.setName("Management Task | Log cleaner job").setOrchestratorMethod(ORCHESTRATOR_METHOD.RUN_PLUGIN)
      .setPlugin(ActionLogCleanerPlugin.class.getCanonicalName());
    if (!daysToKeep.isEmpty()) {
      Map<String, String> pluginParameters = new HashMap<String, String>();
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INT_VALUE, daysToKeep);
      job.setPluginParameters(pluginParameters);
    }
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage("Log cleaner created (" + jobCreated + ")");
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "ManagementTasks", "action log clean", null, duration, "daysToKeep", daysToKeep);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | JobAlreadyStartedException e) {
      LOGGER.error("Error creating log cleaner job", e);
    }

    return response;
  }

}
