package org.roda.wui.api.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.roda.core.data.v2.generics.StringResponse;
import org.roda.core.data.v2.log.ClientLogCreateRequest;
import org.roda.wui.api.v2.services.ClientLoggerService;
import org.roda.wui.client.services.ClientLoggerRestService;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/logger")
@Tag(name = ClientLoggerController.SWAGGER_ENDPOINT)
public class ClientLoggerController implements ClientLoggerRestService {
  public static final String SWAGGER_ENDPOINT = "Client logger";

  @Autowired
  HttpServletRequest request;

  @Autowired
  ClientLoggerService clientLoggerService;

  @Override
  public StringResponse log(@RequestBody ClientLogCreateRequest clientLogCreateRequest) {
    if (ConfigurationManager.isInitialized()
      && !ConfigurationManager.getBoolean(false, "ui.sharedProperties.clientLogger.disabled")) {
      clientLoggerService.log(clientLogCreateRequest, request);
    }

    return new StringResponse("Created");
  }
}
