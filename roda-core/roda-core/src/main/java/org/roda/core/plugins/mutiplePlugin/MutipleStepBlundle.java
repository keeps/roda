package org.roda.core.plugins.mutiplePlugin;

import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.MutipleJobPluginInfo;
import org.roda.core.storage.StorageService;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class MutipleStepBlundle implements Bundle {

  private Plugin<?> plugin;
  private IndexService index;
  private ModelService model;
  private StorageService storage;
  private MutipleJobPluginInfo mutipleJobPluginInfo;
  private PluginParameter pluginParameter;
  private Map<String, String> parameterValues;
  private List<IsRODAObject> objects;
  private Job cachedJob;

  public MutipleStepBlundle(final Plugin<?> plugin, final IndexService index, final ModelService model,
    final StorageService storage, final MutipleJobPluginInfo mutipleJobPluginInfo,
    final PluginParameter pluginParameter, final Map<String, String> parameterValues, final List<IsRODAObject> objects,
    final Job cachedJob) {

    this.plugin = plugin;
    this.index = index;
    this.model = model;
    this.storage = storage;
    this.mutipleJobPluginInfo = mutipleJobPluginInfo;
    this.pluginParameter = pluginParameter;
    this.parameterValues = parameterValues;
    this.objects = objects;
    this.cachedJob = cachedJob;
  }

  @Override
  public Plugin<?> getPlugin() {
    return plugin;
  }

  @Override
  public void setPlugin(final Plugin<?> plugin) {
    this.plugin = plugin;
  }

  @Override
  public IndexService getIndex() {
    return index;
  }

  @Override
  public void setIndex(final IndexService index) {
    this.index = index;
  }

  @Override
  public ModelService getModel() {
    return model;
  }

  @Override
  public void setModel(final ModelService model) {
    this.model = model;
  }

  @Override
  public StorageService getStorage() {
    return storage;
  }

  @Override
  public void setStorage(final StorageService storage) {
    this.storage = storage;
  }

  public MutipleJobPluginInfo getJobPluginInfo() {
    return mutipleJobPluginInfo;
  }

  public void setJobPluginInfo(MutipleJobPluginInfo mutipleJobPluginInfo) {
    this.mutipleJobPluginInfo = mutipleJobPluginInfo;
  }

  @Override
  public PluginParameter getPluginParameter() {
    return pluginParameter;
  }

  @Override
  public void setPluginParameter(final PluginParameter pluginParameter) {
    this.pluginParameter = pluginParameter;
  }

  @Override
  public Map<String, String> getParameterValues() {
    return parameterValues;
  }

  @Override
  public void setParameterValues(final Map<String, String> parameterValues) {
    this.parameterValues = parameterValues;
  }

  @Override
  public Job getCachedJob() {
    return cachedJob;
  }

  @Override
  public void setCachedJob(final Job cachedJob) {
    this.cachedJob = cachedJob;
  }

  public List<IsRODAObject> getObjects() {
    return objects;
  }

  public void setObjects(final List<IsRODAObject> objects) {
    this.objects = objects;
  }
}
