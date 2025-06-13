package org.roda.wui.api.v2.controller;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Metrics;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.MetricsService;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
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
  public static final String SWAGGER_ENDPOINT = "Metrics";

  @Autowired
  MetricsService metricsService;

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private RequestHandler requestHandler;

  @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get RODA metrics", description = "Gets a list of RODA metrics", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Metrics.class)))})
  public Metrics getMetrics(@RequestParam(RodaConstants.API_METRICS_TO_OBTAIN) final List<String> metricsToObtain) {

    return requestHandler.processRequest(new RequestHandler.RequestProcessor<Metrics>() {
      @Override
      public Metrics process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_REQUEST_METRICS_PARAM, metricsToObtain);
        controllerAssistant.checkRoles(requestContext.getUser());

        return metricsService.getMetrics(metricsToObtain);
      }
    });
  }
}
