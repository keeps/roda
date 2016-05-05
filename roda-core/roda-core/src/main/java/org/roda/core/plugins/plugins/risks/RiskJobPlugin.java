/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.risks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.RiskJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * https://docs.google.com/spreadsheets/d/
 * 1Ncu0My6tf19umSClIA6iXeYlJ4_FP6MygRwFCe0EzyM
 * 
 * FIXME 20160323 hsilva: after each task (i.e. plugin), the AIP should be
 * obtained again from model (as it might have changed)
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class RiskJobPlugin extends AbstractPlugin<Serializable> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RiskJobPlugin.class);

  private RiskJobPluginInfo info = new RiskJobPluginInfo();

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_RISK_ID, "Risks", PluginParameterType.RISK_ID, "", false, false,
        "Add the risks that will be associated with the elements above."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION, "Job finished notification",
        PluginParameterType.STRING, "", false, false,
        "Send a notification, after finishing the process, to one or more e-mail addresses (comma separated)"));
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RISK_ID));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION));
    return parameters;
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<Serializable> resources)
    throws PluginException {
    Report report = executePlugin(index, model, storage, resources, RiskIncidencePlugin.class.getName());
    return report;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    try {

      String emails = PluginHelper.getStringFromParameters(this,
        pluginParameters.get(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION));
      if (!"".equals(emails)) {
        List<String> emailList = new ArrayList<String>(Arrays.asList(emails.split("\\s*,\\s*")));
        Notification notification = new Notification();
        notification.setSubject("RODA risk process finished - XXX");
        notification.setFromUser("Job Process");
        notification.setRecipientUsers(emailList);
        Map<String, Object> scopes = new HashMap<String, Object>();
        model.createNotification(notification, RodaConstants.RISK_EMAIL_TEMPLATE, scopes);
      }

    } catch (GenericException e) {
      LOGGER.error("Could not send ingest notification");
    }

    return null;
  }

  private Report executePlugin(IndexService index, ModelService model, StorageService storage,
    List<Serializable> objects, String pluginClassName) {
    return executePlugin(index, model, storage, objects, pluginClassName, null);
  }

  private Report executePlugin(IndexService index, ModelService model, StorageService storage,
    List<Serializable> objects, String pluginClassName, Map<String, String> params) {
    Report report = null;
    Plugin<Serializable> plugin = (Plugin<Serializable>) RodaCoreFactory.getPluginManager().getPlugin(pluginClassName);
    Map<String, String> mergedParams = new HashMap<String, String>(getParameterValues());
    if (params != null) {
      mergedParams.putAll(params);
    }

    try {
      plugin.setParameterValues(mergedParams);
      report = plugin.execute(index, model, storage, objects);
    } catch (Throwable e) {
      LOGGER.error("Error executing plug-in", e);
    }

    return report;
  }

  @Override
  public PluginType getType() {
    return PluginType.RISK;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public String getName() {
    return "Risk Job Plugin Name";
  }

  @Override
  public String getDescription() {
    return "Risk Job Plugin Description";
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new RiskJobPlugin();
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.VALIDATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "";
  }
}
