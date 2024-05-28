package org.roda.wui.api.v2.controller;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.v2.properties.SharedProperties;
import org.roda.wui.api.v2.services.ConfigurationsService;
import org.roda.wui.client.browse.Viewers;
import org.roda.wui.client.services.ConfigurationRestService;
import org.roda.wui.common.server.ServerTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/configurations")
public class ConfigurationController implements ConfigurationRestService {

  @Autowired
  ConfigurationsService configurationsService;

  @Override
  public Viewers retrieveViewersProperties() {
    return configurationsService.getViewerConfigurations();
  }

  @Override
  public SharedProperties retrieveSharedProperties(String localeString) {
    Locale locale = ServerTools.parseLocale(localeString);
    SharedProperties sharedProperties = new SharedProperties();
    sharedProperties.setProperties(RodaCoreFactory.getRodaSharedProperties(locale));
    return sharedProperties;
  }
}
