package org.roda.wui.api.v2.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.common.TokenManager;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.EntityResponse;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.generics.CreateDistributedInstanceRequest;
import org.roda.core.data.v2.generics.CreateLocalInstanceRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.wui.api.controllers.RODAInstance;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.DistributedInstanceService;
import org.roda.wui.api.v2.services.MembersService;
import org.roda.wui.client.services.DistributedInstancesRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/distributed-instances")
public class DistributedInstancesController implements DistributedInstancesRestService {

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private MembersService membersService;

  @Autowired
  private DistributedInstanceService distributedInstanceService;

  @Override
  public DistributedInstances getDistributedInstances() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      return RodaCoreFactory.getModelService().listDistributedInstances();
    } catch (RODAException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state);
    }
  }

  @Override
  public DistributedInstance getDistributedInstance(String id) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      return RodaCoreFactory.getModelService().retrieveDistributedInstance(id);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state);
    }
  }

  @Override
  public LocalInstance getLocalInstance() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
      return Objects.requireNonNullElseGet(localInstance, LocalInstance::new);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }

  @Override
  public Void deleteDistributedInstance(String id) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      final DistributedInstance distributedInstance = RodaCoreFactory.getModelService().retrieveDistributedInstance(id);
      final String username = RodaConstants.DISTRIBUTED_INSTANCE_USER_PREFIX + distributedInstance.getName();
      RodaCoreFactory.getModelService().deleteDistributedInstance(id);
      membersService.deleteUser(username);
      return null;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state);
    }
  }

  @Override
  public DistributedInstance createDistributedInstance(
    @RequestBody CreateDistributedInstanceRequest createDistributedInstanceRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    DistributedInstance distributedInstance = new DistributedInstance();
    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      distributedInstance.setName(createDistributedInstanceRequest.getName());
      distributedInstance.setDescription(createDistributedInstanceRequest.getDescription());
      DistributedInstance createdInstance = distributedInstanceService.createDistributedInstance(distributedInstance,
        requestContext.getUser());
      createdInstance
        .setToken(RodaCoreFactory.getModelService().retrieveAccessKey(createdInstance.getAccessKeyId()).getKey());
      return createdInstance;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_DISTRIBUTED_INSTANCE_PARAM, distributedInstance);
    }
  }

  @Override
  public DistributedInstance updateDistributedInstance(@RequestBody DistributedInstance distributedInstance) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      return RodaCoreFactory.getModelService().updateDistributedInstance(distributedInstance,
        requestContext.getUser().getId());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state);
    }
  }

  @Override
  public List<String> testLocalInstanceConfiguration(@RequestBody LocalInstance localInstance) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    List<String> responseList = new ArrayList<>();
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      TokenManager.getInstance().getAccessToken(localInstance);
      TokenManager.getInstance().removeToken();
    } catch (RODAException | IllegalArgumentException e) {
      state = LogEntryState.FAILURE;
      responseList.add(e.toString());
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }

    return responseList;
  }

  @Override
  public LocalInstance createLocalInstance(@RequestBody CreateLocalInstanceRequest createLocalInstanceRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;
    LocalInstance localInstance = new LocalInstance();
    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      localInstance.setCentralInstanceURL(createLocalInstanceRequest.getCentralInstanceURL());
      localInstance.setAccessKey(createLocalInstanceRequest.getAccessKey());
      localInstance.setId(createLocalInstanceRequest.getId());
      RodaCoreFactory.createOrUpdateLocalInstance(localInstance);
      localInstance.setAccessKey(null);
      return localInstance;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM,
        localInstance);
    }
  }

  @Override
  public LocalInstance subscribeLocalInstance(@RequestBody LocalInstance localInstance) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // Apply Identifiers
      localInstance.setStatus(SynchronizingStatus.APPLYINGIDENTIFIER);
      RodaCoreFactory.createOrUpdateLocalInstance(localInstance);
      distributedInstanceService.applyInstanceIdToRodaObject(localInstance, requestContext.getUser(), true);
      RODAInstanceUtils.createDistributedGroup(requestContext.getUser());
      localInstance.setAccessKey(null);
      return localInstance;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }

  @Override
  public Void deleteLocalInstanceConfiguration() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      try {
        // Deleting from distributed
        DistributedInstance distributedInstance = SyncUtils.requestInstanceStatus(RodaCoreFactory.getLocalInstance());
        distributedInstance.setStatus(SynchronizingStatus.INACTIVE);

        SyncUtils.updateDistributedInstance(RodaCoreFactory.getLocalInstance(), distributedInstance);
      } catch (GenericException e) {
        // Do nothing distributed instance was removed
      }
      // Deleting from remote
      TokenManager.getInstance().removeToken();
      RodaCoreFactory.createOrUpdateLocalInstance(null);
      return null;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }

  @Override
  public Void deleteLocalConfiguration(@RequestBody LocalInstance localInstance) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    List<String> responseList = new ArrayList();
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      distributedInstanceService.applyInstanceIdToRodaObject(localInstance, requestContext.getUser(), false);
      return null;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }

  @Override
  public Job synchronize(@RequestBody LocalInstance localInstance) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      if (!SynchronizingStatus.ACTIVE.equals(localInstance.getStatus())) {
        state = LogEntryState.FAILURE;
        throw new GenericException("The instance isn't in Active state.");
      } else {
        return distributedInstanceService.synchronize(requestContext.getUser(), localInstance);
      }
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }

  @Override
  public DistributedInstance status(String id) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      // check permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      return RodaCoreFactory.getModelService().retrieveDistributedInstance(id);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM,
        id);
    }

  }

  @Override
  public LocalInstance updateLocalInstanceConfiguration(@RequestBody LocalInstance localInstance) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      RodaCoreFactory.createOrUpdateLocalInstance(localInstance);
      return RodaCoreFactory.getLocalInstance();
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }

  @RequestMapping(path = "/remote_actions/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get instance status", description = "Gets instance status", responses = {
    @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = Response.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Response remoteActions(@Parameter(description = "The instance id") @PathVariable(name = "id") String id) {
    // get user
    User user = UserUtility.getApiUser(request);
    try {
      EntityResponse response = RODAInstance.retrieveRemoteActions(user, id);
      if (response instanceof StreamResponse) {
        return ApiUtils.okResponse((StreamResponse) response);
      } else {
        return Response.noContent().build();
      }
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }
}
