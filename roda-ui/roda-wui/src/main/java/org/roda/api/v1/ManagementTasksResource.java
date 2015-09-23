package org.roda.api.v1;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.roda.api.v1.entities.TaskList;
import org.roda.api.v1.utils.ApiResponseMessage;
import org.roda.api.v1.utils.ApiUtils;
import org.roda.common.UserUtility;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.common.RodaCoreService;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;

@Path(ManagementTasksResource.ENDPOINT)
public class ManagementTasksResource extends RodaCoreService {
  public static final String ENDPOINT = "/v1/management_tasks";
  private static final TaskList TASKS = new TaskList("index/reindex", "index/orphans");

  @Context
  private HttpServletRequest request;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response listTasks(@QueryParam("acceptFormat") String acceptFormat) {
    return Response.ok(TASKS, ApiUtils.getMediaType(acceptFormat, request.getHeader("Accept"))).build();
  }

  @POST
  @Path("/{sub_resource}/{task_id}")
  public Response executeTask(final @PathParam("sub_resource") String sub_resource,
    final @PathParam("task_id") String task_id, @QueryParam("params") List<String> params) {
    String authorization = request.getHeader("Authorization");
    Date startDate = new Date();
    try {

      // get user & check permissions
      RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
      // FIXME see if this is the proper way to ensure that the user can execute
      // this task
      if (!user.getAllGroups().contains("administrators")) {
        return Response.status(Status.UNAUTHORIZED).entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
          "User \"" + user.getId() + "\" doesn't have permission the execute the requested task!")).build();
      }

      return execute(user, startDate, sub_resource, task_id, params);

    } catch (AuthorizationDeniedException e) {
      if (authorization == null) {
        return Response.status(Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RODA REST API\"")
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      } else {
        return Response.status(Status.UNAUTHORIZED)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
      }
    }
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
            RodaCoreFactory.runReindexAipsAction();
            // register action
            long duration = new Date().getTime() - startDate.getTime();
            registerAction(user, "ManagementTasks", "reindex", null, duration);
          } else {
            RodaCoreFactory.runReindexAipsAction(params);
            // register action
            long duration = new Date().getTime() - startDate.getTime();
            registerAction(user, "ManagementTasks", "reindex", null, duration, "params", params);
          }
        } else if ("orphans".equals(task_id)) {
          if (!params.isEmpty()) {
            RodaCoreFactory.runRemoveOrphansAction(params.get(0));
            // register action
            long duration = new Date().getTime() - startDate.getTime();
            registerAction(user, "ManagementTasks", "orphans", null, duration, "params", params);
          }
        }
      }
      return Response.ok().entity(response).build();
    }
  }

}
