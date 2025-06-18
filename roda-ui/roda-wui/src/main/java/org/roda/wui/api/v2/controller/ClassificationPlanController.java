package org.roda.wui.api.v2.controller;

import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.StreamResponse;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.ClassificationPlanService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(path = "/api/v2/classification-plans")
@Tag(name = ClassificationPlanController.SWAGGER_ENDPOINT)
public class ClassificationPlanController {
  public static final String SWAGGER_ENDPOINT = "Classification plan";

  @Autowired
  ClassificationPlanService classificationPlanService;

  @Autowired
  RequestHandler requestHandler;

  @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the classification plan from Catalogue", responses = {
    @ApiResponse(responseCode = "200", description = "OK")})
  public ResponseEntity<StreamingResponseBody> getClassificationPlan(
    @Parameter(description = "Choose file name", schema = @Schema(defaultValue = "plan.json")) @RequestParam(value = "filename", defaultValue = "plan.json", required = false) String filename) {

    return requestHandler.processRequest(new RequestHandler.RequestProcessor<ResponseEntity<StreamingResponseBody>>() {
      @Override
      public ResponseEntity<StreamingResponseBody> process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        // delegate
        ConsumesOutputStream consumesOutputStream = classificationPlanService
          .retrieveClassificationPlan(requestContext.getUser(), filename);
        return ApiUtils.okResponse(new StreamResponse(consumesOutputStream), null);
      }
    });
  }
}
