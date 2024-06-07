package org.roda.wui.client.services;

import org.fusesource.restygwt.client.DirectRestService;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedules;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Tag(name = "v2 disposal schedules")
@RequestMapping(path = "../api/v2/disposal/schedules")
public interface DisposalScheduleRestService extends DirectRestService {

  @RequestMapping(method = RequestMethod.GET, path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List disposal schedules", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalSchedules.class)))})
  DisposalSchedules listDisposalSchedules();

  @RequestMapping(method = RequestMethod.GET, path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Gets disposal schedule", description = "Gets disposal schedule information", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalSchedule.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalSchedule retrieveDisposalSchedule(
    @Parameter(description = "The ID of the disposal schedule to retrieve.", required = true) @PathVariable(name = "id") String id);

  @RequestMapping(method = RequestMethod.POST, path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create disposal schedule", description = "Create a new schedule schedule", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DisposalSchedule.class))), responses = {
    @ApiResponse(responseCode = "201", description = "Disposal schedule created", content = @Content(schema = @Schema(implementation = DisposalSchedule.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalSchedule createDisposalSchedule(DisposalSchedule schedule);

  @RequestMapping(method = RequestMethod.PUT, path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update disposal schedule", description = "Update existing disposal schedule", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DisposalSchedule.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Disposal schedule updated", content = @Content(schema = @Schema(implementation = DisposalSchedule.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalSchedule updateDisposalSchedule(DisposalSchedule schedule);

  @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete disposal rule", description = "Delete disposal schedule", responses = {
    @ApiResponse(responseCode = "204", description = "Resource deleted"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteDisposalSchedule(
    @Parameter(description = "The ID of the disposal schedule to delete.", required = true) @PathVariable(name = "id") String id);

  @RequestMapping(method = RequestMethod.POST, path = "/{id}/associate", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Associate a disposal schedule to selected AIPs", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItemsRequest.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class)))})
  Job associatedDisposalSchedule(
    @Parameter(description = "Selected AIPs", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItemsRequest selectedItems,
    @Parameter(description = "Disposal schedule id", required = true) @PathVariable(name = "id") String disposalScheduleId);

  @RequestMapping(method = RequestMethod.POST, path = "/disassociate", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Disassociate a disposal schedule from selected AIPs", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItemsRequest.class))), responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class)))})
  Job disassociatedDisposalSchedule(
    @Parameter(description = "Selected AIPs", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItemsRequest selectedItems);
}
