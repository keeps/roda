package org.roda.wui.client.services;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.fusesource.restygwt.client.DirectRestService;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.rule.DisposalRules;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Tag(name = "v2 disposal rules")
@RequestMapping(path = "../api/v2/disposal/rules")
public interface DisposalRuleRestService extends DirectRestService {
  @RequestMapping(method = RequestMethod.GET, path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List disposal rules", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalRules.class)))})
  DisposalRules listDisposalRules();

  @RequestMapping(method = RequestMethod.PUT, path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update disposal rule", description = "Update existing disposal rule", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DisposalRule.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalRule.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalRule updateDisposalRule(DisposalRule disposalRule);

  @RequestMapping(method = RequestMethod.POST, path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create disposal rule", description = "Create a new disposal rule", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DisposalRule.class))), responses = {
    @ApiResponse(responseCode = "201", description = "Disposal rul created", content = @Content(schema = @Schema(implementation = DisposalRule.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalRule createDisposalRule(DisposalRule disposalRule);

  @RequestMapping(method = RequestMethod.GET, path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get disposal rule", description = "Get disposal rule information", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DisposalRule.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  DisposalRule retrieveDisposalRule(
    @Parameter(description = "The ID of the disposal rule to retrieve.", required = true) @PathVariable(name = "id") String id);

  @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete disposal rule", description = "Delete disposal rule", responses = {
    @ApiResponse(responseCode = "204", description = "Resource deleted"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteDisposalRule(
    @Parameter(description = "The ID of the disposal rule to delete.", required = true) @PathVariable(name = "id") String id);

  @RequestMapping(method = RequestMethod.POST, path = "/apply", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Apply disposal rule to repository", description = "Applies the set of rules defined to the all repository. An AIP's manually associated disposal schedules may be overwritten by specifying the appropriate query parameter.", responses = {
    @ApiResponse(responseCode = "200", description = "Job created", content = @Content(schema = @Schema(implementation = Job.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Job applyDisposalRules(
    @Parameter(description = "If true, overrides manually associated disposal schedules.", content = @Content(schema = @Schema(implementation = boolean.class, defaultValue = "false"))) @RequestParam(name = "overrideManualAssociations", required = false, defaultValue = "false") boolean overrideManualAssociations);
}
