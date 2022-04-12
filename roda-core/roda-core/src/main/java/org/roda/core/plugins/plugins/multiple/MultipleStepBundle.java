package org.roda.core.plugins.plugins.multiple;

import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.MultipleJobPluginInfo;
import org.roda.core.storage.StorageService;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class MultipleStepBundle {

  private Plugin<?> plugin;
  private IndexService index;
  private ModelService model;
  private StorageService storage;
  private MultipleJobPluginInfo mutipleJobPluginInfo;
  private PluginParameter pluginParameter;
  private Map<String, String> parameterValues;
  private List<? extends IsRODAObject> objects;
  private Job cachedJob;

  public MultipleStepBundle(final Plugin<?> plugin, final IndexService index, final ModelService model,
    final StorageService storage, final MultipleJobPluginInfo mutipleJobPluginInfo,
    final PluginParameter pluginParameter, final Map<String, String> parameterValues, final List<? extends IsRODAObject> objects,
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

  public Plugin<?> getPlugin() {
    return plugin;
  }

  public void setPlugin(final Plugin<?> plugin) {
    this.plugin = plugin;
  }

  public IndexService getIndex() {
    return index;
  }

  public void setIndex(final IndexService index) {
    this.index = index;
  }

  public ModelService getModel() {
    return model;
  }

  public void setModel(final ModelService model) {
    this.model = model;
  }

  public StorageService getStorage() {
    return storage;
  }

  public void setStorage(final StorageService storage) {
    this.storage = storage;
  }

  public MultipleJobPluginInfo getJobPluginInfo() {
    return mutipleJobPluginInfo;
  }

  public void setJobPluginInfo(MultipleJobPluginInfo multipleJobPluginInfo) {
    this.mutipleJobPluginInfo = multipleJobPluginInfo;
  }

  public PluginParameter getPluginParameter() {
    return pluginParameter;
  }

  public void setPluginParameter(final PluginParameter pluginParameter) {
    this.pluginParameter = pluginParameter;
  }

  public Map<String, String> getParameterValues() {
    return parameterValues;
  }

  public void setParameterValues(final Map<String, String> parameterValues) {
    this.parameterValues = parameterValues;
  }

  public Job getCachedJob() {
    return cachedJob;
  }

  public void setCachedJob(final Job cachedJob) {
    this.cachedJob = cachedJob;
  }

  public List<? extends IsRODAObject> getObjects() {
    return objects;
  }

  public void setObjects(final List<? extends IsRODAObject> objects) {
    this.objects = objects;
  }
}
