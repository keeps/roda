package org.roda.wui.client.services;

import org.fusesource.restygwt.client.DirectRestService;
import org.roda.core.data.v2.generics.StringResponse;
import org.roda.core.data.v2.log.ClientLogCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Tag(name = "Client logger")
@RequestMapping(path = "../api/v2/logger")
public interface ClientLoggerRestService extends DirectRestService {

  @RequestMapping(method = RequestMethod.POST, path = "/log", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Logs client messages", description = "Logs client messages", responses = {
    @ApiResponse(responseCode = "201", description = "Log entry created", content = @Content(schema = @Schema(implementation = StringResponse.class)))})
  StringResponse log(ClientLogCreateRequest createRequest);
}
