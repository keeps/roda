package org.roda.wui.client.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fusesource.restygwt.client.DirectRestService;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.generics.CreateDistributedInstanceRequest;
import org.roda.core.data.v2.generics.CreateLocalInstanceRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Tag(name = "v2 distributed instances")
@RequestMapping(path = "../api/v2/distributed-instances")
public interface DistributedInstancesRestService extends DirectRestService {

  @RequestMapping(path = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get distributed instances", description = "Gets distributed instances list", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DistributedInstances.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DistributedInstances getDistributedInstances();

  @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get distributed instance", description = "Gets a particular distributed instance", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DistributedInstance.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DistributedInstance getDistributedInstance(
    @Parameter(description = "The distributed instance id") @PathVariable(name = "id") String id);

  @RequestMapping(path = "/local", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get local instance", description = "Gets a particular local instance", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LocalInstance.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  LocalInstance getLocalInstance();

  @RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete distributed instance", description = "Deletes a distributed instance", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteDistributedInstance(
    @Parameter(description = "The distributed instance id") @PathVariable(name = "id") String id);

  @RequestMapping(path = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create distributed instance", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateDistributedInstanceRequest.class))), description = "Creates a new distributed instance", responses = {
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = DistributedInstance.class))),
    @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DistributedInstance createDistributedInstance(
    @Parameter(name = "distributed-instance", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) CreateDistributedInstanceRequest distributedInstance);

  @RequestMapping(path = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update distributed instance", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DistributedInstance.class))), description = "Updates a new distributed instance", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DistributedInstance.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DistributedInstance updateDistributedInstance(
    @Parameter(name = "distributed-instance", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) DistributedInstance distributedInstance);

  @RequestMapping(path = "/test-configuration", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Test local instance configuration", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LocalInstance.class))), description = "Tests local instance configuration", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = List.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  List<String> testLocalInstanceConfiguration(
    @Parameter(name = "local-instance", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) LocalInstance localInstance);

  @RequestMapping(path = "/local", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create local instance", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateLocalInstanceRequest.class))), description = "Creates a new local instance", responses = {
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = LocalInstance.class))),
    @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  LocalInstance createLocalInstance(
    @Parameter(name = "create-local-instance-request", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) CreateLocalInstanceRequest createLocalInstanceRequest);

  @RequestMapping(path = "/local/subscribe", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Subscribe local instance", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LocalInstance.class))), description = "Subscribes local instance", responses = {
    @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = LocalInstance.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  LocalInstance subscribeLocalInstance(
    @Parameter(name = "local-instance", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) LocalInstance localInstance);

  @RequestMapping(path = "/local/instance-configuration", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete local instance configuration", description = "Deletes local instance configuration", responses = {
    @ApiResponse(responseCode = "204", description = "No content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteLocalInstanceConfiguration();

  @RequestMapping(path = "/local/configuration", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete local configuration", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LocalInstance.class))) ,description = "Deletes local configuration", responses = {
    @ApiResponse(responseCode = "204", description = "No content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteLocalConfiguration(
    @Parameter(name = "local-instance", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) LocalInstance localInstance);

  @RequestMapping(path = "/local/synchronize", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Synchronize instances", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LocalInstance.class))) ,description = "Synchronizes instances", responses = {
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job synchronize(
    @Parameter(name = "local-instance", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) LocalInstance localInstance);

  @RequestMapping(path = "/status/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get instance status", description = "Gets instance status", responses = {
    @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = DistributedInstance.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DistributedInstance status(
    @Parameter(description = "The instance id") @PathVariable(name = "id") String id);

  @RequestMapping(path = "/local", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update local instance configuration", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LocalInstance.class))) ,description = "Updates local instance configuration", responses = {
    @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = LocalInstance.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  LocalInstance updateLocalInstanceConfiguration(
    @Parameter(name = "local-instance", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) LocalInstance localInstance);
}
