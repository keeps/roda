package org.roda.wui.api.v2.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.common.TokenManager;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;
import org.roda.core.data.v2.synchronization.central.CreateDistributedInstanceRequest;
import org.roda.core.data.v2.synchronization.central.CreateLocalInstanceRequest;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.synchronization.central.UpdateDistributedInstanceRequest;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.DistributedInstanceService;
import org.roda.wui.api.v2.services.MembersService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.client.services.DistributedInstancesRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;

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

  private static final Logger LOGGER = LoggerFactory.getLogger(DistributedInstancesController.class);

  @Override
  public DistributedInstances getDistributedInstances() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return RodaCoreFactory.getModelService().listDistributedInstances();
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | RequestNotValidException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state);
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
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, id, state,
        RodaConstants.CONTROLLER_DISTRIBUTED_INSTANCE_ID_PARAM, id);
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
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state);
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

      // delegate
      distributedInstanceService.deleteDistributedInstance(membersService, id);
      return null;
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, id, state,
        RodaConstants.CONTROLLER_DISTRIBUTED_INSTANCE_ID_PARAM, id);
    }
  }

  @Override
  public DistributedInstance createDistributedInstance(
    @RequestBody CreateDistributedInstanceRequest createDistributedInstanceRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      DistributedInstance createdInstance = distributedInstanceService
        .createDistributedInstance(createDistributedInstanceRequest, requestContext.getUser());
      createdInstance
        .setToken(RodaCoreFactory.getModelService().retrieveAccessKey(createdInstance.getAccessKeyId()).getKey());
      return createdInstance;
    } catch (AlreadyExistsException | NotFoundException | RequestNotValidException | GenericException
      | IllegalOperationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_DISTRIBUTED_INSTANCE_PARAM,
        createDistributedInstanceRequest);
    }
  }

  @Override
  public DistributedInstance updateDistributedInstance(
    @RequestBody UpdateDistributedInstanceRequest distributedInstance) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return distributedInstanceService.updateDistributionInstance(distributedInstance, requestContext.getUser());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state);
    }
  }

  @Override
  public DistributedInstance updateDistributedInstanceSyncStatus(String id, boolean activate) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return distributedInstanceService.updateDistributionInstanceStatus(id, activate, requestContext.getUser());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, id, state,
        RodaConstants.CONTROLLER_DISTRIBUTED_INSTANCE_ID_PARAM, id,
        RodaConstants.CONTROLLER_DISTRIBUTED_INSTANCE_STATUS_PARAM, activate);
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
    } catch (AuthenticationDeniedException | AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      responseList.add(e.toString());
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      responseList.add(e.toString());
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }

    return responseList;
  }

  @Override
  public LocalInstance createLocalInstance(@RequestBody CreateLocalInstanceRequest createLocalInstanceRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return distributedInstanceService.createLocalInstance(createLocalInstanceRequest);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM,
        createLocalInstanceRequest);
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

      // delegate
      return distributedInstanceService.subscribe(localInstance, requestContext.getUser());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM,
        localInstance);
    }
  }

  @Override
  public Void unsubscribeLocalInstance() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      DistributedInstance distributedInstance = SyncUtils.requestInstanceStatus(RodaCoreFactory.getLocalInstance());
      distributedInstance.setStatus(SynchronizingStatus.INACTIVE);
      SyncUtils.updateDistributedInstanceSyncStatus(RodaCoreFactory.getLocalInstance(), distributedInstance);

      // Deleting from remote
      TokenManager.getInstance().removeToken();
      RodaCoreFactory.createOrUpdateLocalInstance(null);

      return null;
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state);
    }
  }

  @Override
  public Void deleteLocalConfiguration() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      distributedInstanceService.applyInstanceIdToRodaObject(null, requestContext.getUser(), false);
      return null;
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state);
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
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_LOCAL_INSTANCE_ID_PARAM, localInstance.getId());
    }
  }

  @PostMapping(path = "/{id}/register", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Register distributed instance", description = "Registers a distributed instance", responses = {
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = DistributedInstance.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public DistributedInstance registerDistributedInstance(
    @Parameter(description = "The instance identifier") @PathVariable(name = "id") String id) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      DistributedInstance distributedInstance = RodaCoreFactory.getModelService().retrieveDistributedInstance(id);
      distributedInstance.setStatus(SynchronizingStatus.ACTIVE);
      return RodaCoreFactory.getModelService().updateDistributedInstance(distributedInstance,
        requestContext.getUser().getId());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_LOCAL_INSTANCE_ID_PARAM, id);
    }
  }

  @GetMapping(path = "/updates/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get central instance updates", description = "Get central instance updates", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Long.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  private Long getCentralInstanceUpdates(
    @Parameter(description = "The instance id") @PathVariable(name = "id") String id) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      Long total = 0L;
      ModelService model = RodaCoreFactory.getModelService();
      IndexService index = RodaCoreFactory.getIndexService();

      DistributedInstance distributedInstance = model.retrieveDistributedInstance(id);
      Date lastSynchronizationDate = distributedInstance.getLastSynchronizationDate();
      Date toDate = new Date();
      // get Jobs
      final Filter jobFilter = new Filter();
      jobFilter.add(new SimpleFilterParameter(RodaConstants.INDEX_INSTANCE_ID, id));
      jobFilter.add(new SimpleFilterParameter(RodaConstants.JOB_STATE, Job.JOB_STATE.CREATED.name()));
      jobFilter.add(new DateIntervalFilterParameter(RodaConstants.JOB_START_DATE, RodaConstants.JOB_END_DATE,
        lastSynchronizationDate, toDate));
      total += index.count(Job.class, jobFilter);

      // get Risks
      final Filter riskFilter = new Filter();
      riskFilter.add(new DateIntervalFilterParameter(RodaConstants.RISK_UPDATED_ON, RodaConstants.RISK_UPDATED_ON,
        lastSynchronizationDate, toDate));
      total += index.count(IndexedRisk.class, riskFilter);

      // get RepresentationInformation
      final Filter repFilter = new Filter();
      repFilter.add(new DateIntervalFilterParameter(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_ON,
        RodaConstants.REPRESENTATION_INFORMATION_UPDATED_ON, lastSynchronizationDate, toDate));
      total += index.count(RepresentationInformation.class, riskFilter);

      return total;
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    }
  }

  @DeleteMapping(path = "/remove/bundle/{name}/{dir}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete distributed instance", description = "Deletes a distributed instance", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  void deleteSyncBundle(@Parameter(description = "The sync bundle name") @PathVariable(name = "name") String name,
    @Parameter(description = "The sync bundle directory") @PathVariable(name = "dir") String directory) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    }

    String message = null;

    if (RodaConstants.CORE_SYNCHRONIZATION_INCOMING_FOLDER.equals(directory)) {
      Path syncBundlePath = SyncUtils.getSyncIncomingBundlePath(name);
      try {
        boolean deleteFromIncome = Files.deleteIfExists(syncBundlePath);
        if (deleteFromIncome) {
          message = "Deleted bundle from income folder with success";
        } else {
          message = "Could not find bundle in income folder";
        }
      } catch (IOException e) {
        LOGGER.error("Can not delete bundle from income folder because {}", e.getMessage());
      }
    } else {
      Path syncBundlePath = SyncUtils.getSyncOutcomeBundlePath(name);
      try {
        boolean deleteFromOutcome = Files.deleteIfExists(syncBundlePath);
        if (deleteFromOutcome) {
          message = "Deleted bundle from outcome folder with success";
        } else {
          message = "Could not find bundle in outcome folder";
        }
      } catch (IOException e) {
        LOGGER.error("Can not delete bundle from outcome folder because {}", e.getMessage());
      }
    }
    LOGGER.info(message);
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
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM,
        localInstance);
    }
  }

  @GetMapping(path = "/remote/actions/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Get instance status", description = "Gets instance status", responses = {
    @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> remoteActions(
    @Parameter(description = "The instance id") @PathVariable(name = "id") String id) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      StreamResponse response = distributedInstanceService.createCentralSyncBundle(id);

      return ApiUtils.okResponse(response);
    } catch (AlreadyExistsException | GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM,
        id);
    }
  }

  @GetMapping(path = "/sync/status/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get synchronization status", description = "Gets synchronization status", responses = {
    @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  ResponseEntity<StreamingResponseBody> synchronizationStatus(
    @Parameter(description = "The instance id") @PathVariable(name = "id") String id,
    @Parameter(description = "The entity class") @RequestParam(name = RodaConstants.API_QUERY_KEY_CLASS) String entityClass,
    @Parameter(description = "The type") @RequestParam(name = RodaConstants.API_QUERY_KEY_TYPE) String type) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return ApiUtils.okResponse(distributedInstanceService.retrieveLastSyncFileByClass(id, entityClass, type));
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM,
        id);
    }
  }

  @PostMapping(path = "/sync/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Import sync bundle", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = MultipartFile.class))), description = "Imports sync bundle", responses = {
    @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public Job importSyncBundle(
    @Parameter(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = MultipartFile.class)), description = "Multipart file") @RequestPart(value = "file") MultipartFile resource,
    @Parameter(description = "The instance id") @PathVariable(name = "id") String id) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return distributedInstanceService.importSyncBundle(requestContext.getUser(), id, resource);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_LOCAL_INSTANCE_PARAM);
    }
  }
}
