/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexPlugin.class);
  private boolean clearIndexes = false;
  private String rodaObject = null;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "Clear indexes", PluginParameterType.BOOLEAN,
        "true", false, false, "Clear all indexes before reindexing them."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OBJECT_CLASS,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_OBJECT_CLASS, "RODA Object", PluginParameterType.RODA_OBJECT,
        AIP.class.getName(), true, false, "RODA object to reindex."));
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
  public String getName() {
    return "Reindex object";
  }

  @Override
  public String getDescription() {
    return "Reset RODA object index and recreate it from data existing in the storage.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_OBJECT_CLASS));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES)) {
      clearIndexes = Boolean.parseBoolean(getParameterValues().get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_OBJECT_CLASS)) {
      rodaObject = getParameterValues().get(RodaConstants.PLUGIN_PARAMS_OBJECT_CLASS);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<T> list)
    throws PluginException {
    LOGGER.debug("Reindexing a total of {} AIPs", list.size());

    if (list.isEmpty()) {
      // TODO continue..
    } else {
      if (list.get(0) instanceof AIP) {
        ReindexAIPPlugin aipReindexer = new ReindexAIPPlugin();
        aipReindexer.execute(index, model, storage, (List<AIP>) list);
      }

      // TODO continue..
    }

    return PluginHelper.initPluginReport(this);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  @Override
  public Plugin<T> cloneMe() {
    return new ReindexPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  // TODO FIX
  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  @Override
  public String getPreservationEventDescription() {
    return "XXXXXXXXXX";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(Void.class);
    list.add(AIP.class);
    // TODO add more
    return (List) list;
  }
}
