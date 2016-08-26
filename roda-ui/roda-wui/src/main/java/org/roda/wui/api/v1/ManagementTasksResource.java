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
import org.roda.core.data.exceptions.*;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsAll;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.index.SelectedItemsNone;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RodaSimpleUser;
import org.roda.core.data.v2.user.User;
import org.roda.core.plugins.plugins.base.ActionLogCleanerPlugin;
import org.roda.core.plugins.plugins.base.ReindexRodaEntityPlugin;
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
    @ApiParam(value = "", allowableValues = "ALL,aip,job,risk,riskincidence,agent,format,notification,actionlogs,transferred_resources,users_and_groups", defaultValue = "aip") @QueryParam("entity") String entity,
    @QueryParam("params") List<String> params) throws AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // get user & check permissions
    RodaSimpleUser user = UserUtility.getApiUser(request);

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
    RodaSimpleUser user = UserUtility.getApiUser(request);

    controllerAssistant.checkGroup(user, "administrators");

    return Response.ok().entity(createJobForRunningActionlogCleaner(user, daysToKeep, controllerAssistant)).build();
  }

  private Response executeReindex(RodaSimpleUser user, ControllerAssistant controllerAssistant, String entity,
    List<String> params) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    if ("aip".equals(entity)) {
      response = createJobToReindex(user, controllerAssistant, params, AIP.class);
    } else if ("job".equals(entity)) {
      response = createJobToReindex(user, controllerAssistant, params, Job.class);
    } else if ("risk".equals(entity)) {
      response = createJobToReindex(user, controllerAssistant, params, Risk.class);
    } else if ("riskincidence".equals(entity)) {
      response = createJobToReindex(user, controllerAssistant, params, RiskIncidence.class);
    } else if ("agent".equals(entity)) {
      response = createJobToReindex(user, controllerAssistant, params, Agent.class);
    } else if ("format".equals(entity)) {
      response = createJobToReindex(user, controllerAssistant, params, Format.class);
    } else if ("notification".equals(entity)) {
      response = createJobToReindex(user, controllerAssistant, params, Notification.class);
    } else if ("transferred_resources".equals(entity)) {
      response = createJobToReindex(user, controllerAssistant, params, TransferredResource.class);
    } else if ("actionlogs".equals(entity)) {
      response = createJobToReindex(user, controllerAssistant, params, LogEntry.class);
    } else if ("users_and_groups".equals(entity)) {
      response = reindexUsersAndGroups(user, controllerAssistant, params);
    } else if ("ALL".equals(entity)) {
      response = createJobToReindexAllRODAObjects(user, controllerAssistant);
    }
    return Response.ok().entity(response).build();
  }

  private <T extends IsRODAObject> ApiResponseMessage createJobToReindex(RodaSimpleUser user,
    ControllerAssistant controllerAssistant, List<String> params, Class<T> classToCreate) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");

    SelectedItems<?> create;
    if (params.isEmpty()) {
      create = SelectedItemsAll.create(classToCreate);
    } else {
      create = SelectedItemsList.create(classToCreate, params);
    }

    Job job = new Job().setName("Management Task | Reindex '" + classToCreate.getSimpleName() + "' job")
      .setSourceObjects(create).setPlugin(ReindexRodaEntityPlugin.class.getName());

    createJobAndRegisterAction(user, controllerAssistant, response, job);

    return response;
  }

  private ApiResponseMessage createJobToReindexAllRODAObjects(RodaSimpleUser user, ControllerAssistant controllerAssistant) {
    ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
    Job job = new Job().setName("Management Task | Reindex 'All RODA Objects' job")
      .setSourceObjects(SelectedItemsNone.create()).setPlugin(ReindexRodaEntityPlugin.class.getName());
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_REINDEX_ALL, "true");
    job.setPluginParameters(pluginParameters);

    createJobAndRegisterAction(user, controllerAssistant, response, job, "params", pluginParameters);

    return response;
  }

  private ApiResponseMessage reindexUsersAndGroups(RodaSimpleUser user, ControllerAssistant controllerAssistant,
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

  private void createJobAndRegisterAction(RodaSimpleUser user, ControllerAssistant controllerAssistant,
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

  private ApiResponseMessage createJobForRunningActionlogCleaner(RodaSimpleUser user, String daysToKeep,
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
