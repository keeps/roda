package org.roda.wui.api.v2.controller;

import org.roda.core.data.v2.generics.StringResponse;
import org.roda.core.data.v2.log.ClientLogCreateRequest;
import org.roda.wui.api.v2.services.ClientLoggerService;
import org.roda.wui.client.services.ClientLoggerRestService;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ClientLoggerController implements ClientLoggerRestService {

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
