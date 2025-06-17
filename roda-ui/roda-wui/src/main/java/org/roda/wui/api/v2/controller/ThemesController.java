package org.roda.wui.api.v2.controller;

import java.io.IOException;

import org.roda.core.common.ProvidesInputStream;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.ThemeService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(path = "/api/v2/themes")
@Tag(name = "Themes")
public class ThemesController {

  @Autowired
  ThemeService themeService;

  @Autowired
  RequestHandler requestHandler;

  @GetMapping(path = "")
  @Operation(summary = "Get theme resources", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelectedItemsRequest.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> getResource(
    @Parameter(description = "The resource id", required = true) @RequestParam("resource-id") String resourceId,
    @Parameter(description = "The default resource id") @RequestParam(value = "default-resource-id", required = false) String fallbackResourceId,
    @Parameter(description = "If the resource is served inline") @RequestParam(value = RodaConstants.API_QUERY_PARAM_INLINE, required = false, defaultValue = "false") boolean inline,
    @Parameter(description = "The resource type, can be internal or plugin", schema = @Schema(implementation = RodaConstants.ResourcesTypes.class, defaultValue = RodaConstants.API_QUERY_PARAM_DEFAULT_RESOURCE_TYPE)) @RequestParam(defaultValue = RodaConstants.API_QUERY_PARAM_DEFAULT_RESOURCE_TYPE, name = "resource-type", required = false) String type,
    WebRequest request) {

    return requestHandler
      .processRequestWithoutCheckRoles(new RequestHandler.RequestProcessor<ResponseEntity<StreamingResponseBody>>() {
        @Override
        public ResponseEntity<StreamingResponseBody> process(RequestContext requestContext,
          RequestControllerAssistant controllerAssistant) throws RODAException, RESTException, IOException {
          Pair<String, ProvidesInputStream> themeResource = themeService.getThemeResource(resourceId,
            fallbackResourceId, type);

          if (themeResource.getSecond() != null) {
            return ApiUtils.okResponse(themeService.getThemeResourceStreamResponse(themeResource, type), request);
          } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found: " + resourceId);
          }
        }
      });
  }
}
