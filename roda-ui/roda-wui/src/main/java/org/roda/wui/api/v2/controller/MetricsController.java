package org.roda.wui.api.v2.controller;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.common.Metrics;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.MetricsService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/api/v2/metrics")
@Tag(name = MetricsController.SWAGGER_ENDPOINT)
public class MetricsController {
  public static final String SWAGGER_ENDPOINT = "v2 metrics";
  @Autowired
  MetricsService metricsService;
  @Autowired
  private HttpServletRequest request;

  @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get RODA metrics", description = "Gets a list of RODA metrics", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Metrics.class)))})
  public Metrics getMetrics(@RequestParam(RodaConstants.API_METRICS_TO_OBTAIN) final List<String> metricsToObtain) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());

      return metricsService.getMetrics(metricsToObtain);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_REQUEST_METRICS_PARAM, metricsToObtain);
    }
  }
}
