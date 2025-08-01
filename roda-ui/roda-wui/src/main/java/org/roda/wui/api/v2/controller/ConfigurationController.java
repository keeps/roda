/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.controller;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.StringResponse;
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

import it.burning.cron.CronExpressionDescriptor;
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
  public StringResponse retrieveCronValue(String localeString) {
    String syncSchedule = RodaCoreFactory.getRodaConfigurationAsString("core.synchronization.scheduleInfo");
    String description = null;
    if (StringUtils.isNotBlank(syncSchedule)) {
      CronExpressionDescriptor.setDefaultLocale(localeString.split("_")[0]);
      description = CronExpressionDescriptor.getDescription(syncSchedule);
    }
    return new StringResponse(description);
  }

  @Override
  public LongResponse retrieveExportLimit() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    try {
      long value = RodaCoreFactory.getRodaConfiguration().getInt("ui.list.export_limit",
        RodaConstants.DEFAULT_LIST_EXPORT_LIMIT);
      return new LongResponse(value);
    } finally {
      controllerAssistant.registerAction(requestContext, LogEntryState.SUCCESS);
    }
  }

  @Override
  public Viewers retrieveViewersProperties() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    try {
      return configurationsService.getViewerConfigurations();
    } finally {
      controllerAssistant.registerAction(requestContext, LogEntryState.SUCCESS);
    }
  }

  /**
   * This method retrieves shared properties based on the provided locale string.
   *
   * @param localeString
   *          The locale string to determine the language of the shared
   *          properties.
   * @return SharedProperties The shared properties object containing properties
   *         based on the provided locale.
   */
  @Override
  public SharedProperties retrieveSharedProperties(String localeString) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    try {
      Locale locale = ServerTools.parseLocale(localeString);
      SharedProperties sharedProperties = new SharedProperties();
      sharedProperties.setProperties(RodaCoreFactory.getRodaSharedProperties(locale));
      return sharedProperties;
    } finally {
      controllerAssistant.registerAction(requestContext, LogEntryState.SUCCESS);
    }
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
  public PluginInfoList retrievePluginsInfo(List<PluginType> types, boolean removeNotListable) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    try {
      controllerAssistant.checkRoles(requestContext.getUser());

      return RodaCoreFactory.getPluginManager().getPluginsInfo(types, removeNotListable);
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
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    try {
      return RodaCoreFactory.getRodaConfiguration().getBoolean("ui.dip.externalURL.showEmbedded", false);
    } finally {
      controllerAssistant.registerAction(requestContext, LogEntryState.SUCCESS);
    }
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
