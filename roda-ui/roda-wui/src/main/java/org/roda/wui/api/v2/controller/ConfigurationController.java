package org.roda.wui.api.v2.controller;

import java.util.List;
import java.util.Locale;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.jobs.PluginInfoList;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.properties.ConversionProfileOutcomeType;
import org.roda.core.data.v2.properties.ConversionProfiles;
import org.roda.core.data.v2.properties.DropdownPluginParameterItems;
import org.roda.core.data.v2.properties.ObjectClassFields;
import org.roda.core.data.v2.properties.ReindexPluginObject;
import org.roda.core.data.v2.properties.ReindexPluginObjects;
import org.roda.core.data.v2.properties.SharedProperties;
import org.roda.core.plugins.PluginHelper;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.ConfigurationsService;
import org.roda.wui.client.browse.Viewers;
import org.roda.wui.client.services.ConfigurationRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.server.ServerTools;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/configurations")
public class ConfigurationController implements ConfigurationRestService {

  @Autowired
  HttpServletRequest request;

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

  @Override
  public ObjectClassFields retrieveObjectClassFields(String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    try {
      return configurationsService.retrieveObjectClassFields(localeString);

    } finally {
      controllerAssistant.registerAction(requestContext, LogEntryState.SUCCESS);
    }
  }

  @Override
  public PluginInfoList retrievePluginsInfo(List<PluginType> types) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    try {
      controllerAssistant.checkRoles(requestContext.getUser());

      return RodaCoreFactory.getPluginManager().getPluginsInfo(types);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state);
    }
  }

  @Override
  public ReindexPluginObjects retrieveReindexPluginObjectClasses() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());

      ReindexPluginObjects pluginObjects = new ReindexPluginObjects();

      List<Class<? extends IsRODAObject>> classes = PluginHelper.getReindexObjectClasses();
      classes.remove(Void.class);

      for (Class<? extends IsRODAObject> c : classes) {
        ReindexPluginObject object = new ReindexPluginObject(c.getSimpleName(), c.getName());
        pluginObjects.addObject(object);
      }

      return pluginObjects;
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state);
    }
  }

  @Override
  public Boolean retrieveShowEmbeddedDIP() {
    return RodaCoreFactory.getRodaConfiguration().getBoolean("ui.dip.externalURL.showEmbedded", false);
  }

  @Override
  public DropdownPluginParameterItems retrieveDropdownPluginItems(String parameterId, String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());

      return configurationsService.retrieveDropDownPluginParameterItems(parameterId, localeString);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state);
    }
  }

  @Override
  public ConversionProfiles retrieveConversionProfiles(String pluginId, ConversionProfileOutcomeType outcomeType,
    String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      return configurationsService.retrieveConversionProfilePluginItems(pluginId, outcomeType, localeString);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state);
    }
  }
}
