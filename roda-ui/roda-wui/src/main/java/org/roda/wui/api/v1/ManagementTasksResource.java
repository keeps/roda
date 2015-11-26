/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
import org.roda.core.data.common.AuthorizationDeniedException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.RodaUser;
import org.roda.core.plugins.orchestrate.akka.Master.Work;
import org.roda.wui.api.v1.entities.TaskList;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.common.RodaCoreService;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

@Path(ManagementTasksResource.ENDPOINT)
public class ManagementTasksResource extends RodaCoreService {
  public static final String ENDPOINT = "/v1/management_tasks";
  private static final TaskList TASKS = new TaskList("index/reindex", "index/orphans", "orchestrator/execute");

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response listTasks(@QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat) {
    return Response.ok(TASKS, ApiUtils.getMediaType(acceptFormat, request.getHeader("Accept"))).build();
  }

  @POST
  @Path("/{sub_resource}/{task_id}")
  public Response executeTask(final @PathParam("sub_resource") String sub_resource,
    final @PathParam("task_id") String task_id, @QueryParam("params") List<String> params)
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

    return execute(user, startDate, sub_resource, task_id, params);

  }

  private Response execute(RodaUser user, Date startDate, final String sub_resource, final String task_id,
    List<String> params) {
    if (!TASKS.getTasks().contains(sub_resource + "/" + task_id)) {
      return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
        "No task was found in the sub-resource \"" + sub_resource + "\" with the id \"" + task_id + "\"")).build();
    } else {
      ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, "Action done!");
      if ("index".equals(sub_resource)) {
        if ("reindex".equals(task_id)) {
          if (params.isEmpty()) {
            RodaCoreFactory.runReindexAipsPlugin();
            // register action
            long duration = new Date().getTime() - startDate.getTime();
            registerAction(user, "ManagementTasks", "reindex", null, duration);
          } else {
            RodaCoreFactory.runReindexAipsPlugin(params);
            // register action
            long duration = new Date().getTime() - startDate.getTime();
            registerAction(user, "ManagementTasks", "reindex", null, duration, "params", params);
          }
        } else if ("orphans".equals(task_id)) {
          if (!params.isEmpty()) {
            RodaCoreFactory.runRemoveOrphansPlugin(params.get(0));
            // register action
            long duration = new Date().getTime() - startDate.getTime();
            registerAction(user, "ManagementTasks", "orphans", null, duration, "params", params);
          }
        }
      } else if ("orchestrator".equals(sub_resource)) {
        // TODO shouldn't this have its own REST endpoint???
        if ("execute".equals(task_id)) {
          // invoke plugins

          ActorRef frontend = RodaCoreFactory.getAkkaDistributedPluginOrchestrator().getFrontend();

          Timeout t = new Timeout(10, TimeUnit.SECONDS);
          Future<Object> fut = Patterns.ask(frontend, new Work(params.get(0) + "-" + UUID.randomUUID().toString(), 1),
            t);
          Object futResponse;
          try {
            futResponse = (Object) Await.result(fut, t.duration());
            System.out.println(futResponse);
          } catch (Exception e) {
            e.printStackTrace();
          }

          // register action
          long duration = new Date().getTime() - startDate.getTime();
          registerAction(user, "ManagementTasks", "execute", null, duration);
        }
      }
      return Response.ok().entity(response).build();
    }
  }

}
