/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.LdapUtilityException;
import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.SelectedItemsAll;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.index.SelectedItemsNone;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.user.User;
import org.roda.core.plugins.plugins.base.ActionLogCleanerPlugin;
import org.roda.core.plugins.plugins.base.ReindexAIPPlugin;
import org.roda.core.plugins.plugins.base.ReindexActionLogPlugin;
import org.roda.core.plugins.plugins.base.ReindexJobPlugin;
import org.roda.core.plugins.plugins.base.ReindexRodaEntityPlugin;
import org.roda.core.plugins.plugins.base.ReindexTransferredResourcePlugin;
import org.roda.wui.api.controllers.Jobs;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.common.ControllerAssistant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Api(value = ManagementTasksResource.SWAGGER_ENDPOINT)
@Path(ManagementTasksResource.ENDPOINT)
public class ManagementTasksResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(ManagementTasksResource.class);

  public static final String ENDPOINT = "/v1/management_tasks";
  public static final String SWAGGER_ENDPOINT = "v1 management tasks";

  @Context
  private HttpServletRequest request;

  @POST
  @Path("/index/reindex")
  public Response executeIndexReindexTask(
    @ApiParam(value = "", allowableValues = "aip,job,risk,riskincidence,agent,format,notification,actionlogs,transferred_resources,users_and_groups", defaultValue = "aip") @QueryParam("entity") String entity,
    @QueryParam("params") List<String> params) throws AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // get user & check permissions
    RodaUser user = UserUtility.getApiUser(request);

    controllerAssistant.checkGroup(user, "administrators");

    return executeReindex(user, controllerAssistant, entity, params);

  }

  @POST
  @Path("/index/actionlogclean")
  public Response executeIndexActionLogCleanTask(
    @ApiParam(value = "Amount of days to keep action information in the index", defaultValue = "30") @QueryParam("daysToKeep") String daysToKeep)
    throws AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // get user & check permissions
    RodaUser user = UserUtility.getApiUser(request);

    controllerAssistant.checkGroup(user, "administrators");

    return Response.ok().entity(createJobForRunningActionlogCleaner(user, daysToKeep, controllerAssistant)).build();
  }

  private Response executeReindex(RodaUser user, ControllerAssistant controllerAssistant, String entity,
    List<String> params) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    if ("aip".equals(entity)) {
      if (params.isEmpty()) {
        response = createJobToReindexAllAIPs(user, controllerAssistant);
      } else {
        response = createJobToReindexAIPs(user, params, controllerAssistant);
      }
    } else if ("job".equals(entity)) {
      response = createJobToReindexAllJobs(user, controllerAssistant);
    } else if ("risk".equals(entity)) {
      response = createJobToReindexAllRisks(user, controllerAssistant);
    } else if ("riskincidence".equals(entity)) {
      response = createJobToReindexAllRiskIncidences(user, controllerAssistant);
    } else if ("agent".equals(entity)) {
      response = createJobToReindexAllAgents(user, controllerAssistant);
    } else if ("format".equals(entity)) {
      response = createJobToReindexAllFormats(user, controllerAssistant);
    } else if ("notification".equals(entity)) {
      response = createJobToReindexAllNotifications(user, controllerAssistant);
    } else if ("transferred_resources".equals(entity)) {
      response = createJobToReindexTransferredResources(user, controllerAssistant, params);
    } else if ("actionlogs".equals(entity)) {
      response = createJobToReindexActionlogs(user, controllerAssistant, params);
    } else if ("users_and_groups".equals(entity)) {
      response = reindexUsersAndGroups(user, controllerAssistant, params);
    }
    return Response.ok().entity(response).build();
  }

  private ApiResponseMessage createJobToReindexAllJobs(RodaUser user, ControllerAssistant controllerAssistant) {
    ApiResponseMessage response;
    response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'Jobs' job").setSourceObjects(SelectedItemsNone.create())
      .setPlugin(ReindexJobPlugin.class.getName());

    createJobAndRegisterAction(user, controllerAssistant, response, job);

    return response;
  }

  private ApiResponseMessage createJobToReindexAllRisks(RodaUser user, ControllerAssistant controllerAssistant) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'Risks' job").setSourceObjects(SelectedItemsNone.create())
      .setPlugin(ReindexRodaEntityPlugin.class.getName());
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME, Risk.class.getName());
    job.setPluginParameters(pluginParameters);

    createJobAndRegisterAction(user, controllerAssistant, response, job, "params", pluginParameters);

    return response;
  }

  private ApiResponseMessage createJobToReindexAllRiskIncidences(RodaUser user,
    ControllerAssistant controllerAssistant) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'Risk Incidences' job")
      .setSourceObjects(SelectedItemsNone.create()).setPlugin(ReindexRodaEntityPlugin.class.getName());
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME, RiskIncidence.class.getName());
    job.setPluginParameters(pluginParameters);

    createJobAndRegisterAction(user, controllerAssistant, response, job, "params", pluginParameters);

    return response;
  }

  private ApiResponseMessage createJobToReindexAllAgents(RodaUser user, ControllerAssistant controllerAssistant) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'Agents' job").setSourceObjects(SelectedItemsNone.create())
      .setPlugin(ReindexRodaEntityPlugin.class.getName());
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME, Agent.class.getName());
    job.setPluginParameters(pluginParameters);

    createJobAndRegisterAction(user, controllerAssistant, response, job, "params", pluginParameters);

    return response;
  }

  private ApiResponseMessage createJobToReindexAllFormats(RodaUser user, ControllerAssistant controllerAssistant) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'Formats' job").setSourceObjects(SelectedItemsNone.create())
      .setPlugin(ReindexRodaEntityPlugin.class.getName());
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME, Format.class.getName());
    job.setPluginParameters(pluginParameters);

    createJobAndRegisterAction(user, controllerAssistant, response, job, "params", pluginParameters);

    return response;
  }

  private ApiResponseMessage createJobToReindexAllNotifications(RodaUser user,
    ControllerAssistant controllerAssistant) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'Notifications' job")
      .setSourceObjects(SelectedItemsNone.create()).setPlugin(ReindexRodaEntityPlugin.class.getName());
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME, Notification.class.getName());
    job.setPluginParameters(pluginParameters);

    createJobAndRegisterAction(user, controllerAssistant, response, job, "params", pluginParameters);

    return response;
  }

  private ApiResponseMessage createJobToReindexTransferredResources(RodaUser user,
    ControllerAssistant controllerAssistant, List<String> params) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'TransferredResources' job")
      .setSourceObjects(SelectedItemsNone.create()).setPlugin(ReindexTransferredResourcePlugin.class.getName());
    if (!params.isEmpty()) {
      Map<String, String> pluginParameters = new HashMap<>();
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_STRING_VALUE, params.get(0));
      job.setPluginParameters(pluginParameters);
    }

    createJobAndRegisterAction(user, controllerAssistant, response, job, "params", params);

    return response;
  }

  private ApiResponseMessage createJobToReindexActionlogs(RodaUser user, ControllerAssistant controllerAssistant,
    List<String> params) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'ActionLogs' job")
      .setSourceObjects(SelectedItemsNone.create()).setPlugin(ReindexActionLogPlugin.class.getName());
    if (!params.isEmpty()) {
      Map<String, String> pluginParameters = new HashMap<>();
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INT_VALUE, params.get(0));
      job.setPluginParameters(pluginParameters);
    }

    createJobAndRegisterAction(user, controllerAssistant, response, job, "params", job.getPluginParameters());

    return response;
  }

  private ApiResponseMessage reindexUsersAndGroups(RodaUser user, ControllerAssistant controllerAssistant,
    List<String> params) {
    boolean success = true;
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    try {
      for (User ldapUser : UserUtility.getLdapUtility().getUsers(new Filter())) {
        LOGGER.debug("User to be indexed: {}", ldapUser);
        RodaCoreFactory.getModelService().addUser(ldapUser, false, true);
      }
      for (Group ldapGroup : UserUtility.getLdapUtility().getGroups(new Filter())) {
        LOGGER.debug("Group to be indexed: {}", ldapGroup);
        RodaCoreFactory.getModelService().addGroup(ldapGroup, false, true);
      }
      response.setMessage("Ended users and groups reindex");

    } catch (NotFoundException | GenericException | AlreadyExistsException | EmailAlreadyExistsException
      | UserAlreadyExistsException | IllegalOperationException | LdapUtilityException e) {
      LOGGER.error("Error reindexing users and groups", e);
      response.setMessage("Error reindexing users and groups: " + e.getMessage());
      success = false;
    }

    controllerAssistant.registerAction(user, success ? LOG_ENTRY_STATE.SUCCESS : LOG_ENTRY_STATE.FAILURE);

    return response;
  }

  private ApiResponseMessage createJobToReindexAllAIPs(RodaUser user, ControllerAssistant controllerAssistant) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job();
    job.setName("Management Task | Reindex 'all AIPs' job")
      .setSourceObjects(new SelectedItemsAll<>(AIP.class.getName())).setPlugin(ReindexAIPPlugin.class.getName());
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    job.setPluginParameters(pluginParameters);

    createJobAndRegisterAction(user, controllerAssistant, response, job);

    return response;
  }

  private void createJobAndRegisterAction(RodaUser user, ControllerAssistant controllerAssistant,
    ApiResponseMessage response, Job job, Object... params) {
    boolean success = true;
    try {
      Job jobCreated = Jobs.createJob(user, job);
      response.setMessage(job.getName() + " created (" + jobCreated + ")");
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | JobAlreadyStartedException e) {
      LOGGER.error("Error creating {}", job.getName(), e);
      success = false;
    }

    // register action
    controllerAssistant.registerAction(user, success ? LOG_ENTRY_STATE.SUCCESS : LOG_ENTRY_STATE.FAILURE, params);
  }

  private ApiResponseMessage createJobToReindexAIPs(RodaUser user, List<String> params,
    ControllerAssistant controllerAssistant) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job();
    job.setName("Management Task | Reindex 'AIPs' job").setPlugin(ReindexAIPPlugin.class.getName())
      .setSourceObjects(SelectedItemsList.create(AIP.class, params));

    createJobAndRegisterAction(user, controllerAssistant, response, job, "params", params);

    return response;
  }

  private ApiResponseMessage createJobForRunningActionlogCleaner(RodaUser user, String daysToKeep,
    ControllerAssistant controllerAssistant) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job();
    job.setName("Management Task | Log cleaner job").setSourceObjects(SelectedItemsNone.create())
      .setPlugin(ActionLogCleanerPlugin.class.getName());
    if (!daysToKeep.isEmpty()) {
      Map<String, String> pluginParameters = new HashMap<String, String>();
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INT_VALUE, daysToKeep);
      job.setPluginParameters(pluginParameters);
    }

    createJobAndRegisterAction(user, controllerAssistant, response, job, "params", job.getPluginParameters());

    return response;
  }
}
