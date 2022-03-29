package org.roda.core.plugins.mutiplePlugin;

import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.StorageService;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public interface Bundle {

  /**
   * Get the {@link Plugin}.
   * 
   * @return {@link Plugin}.
   */
  Plugin<?> getPlugin();

  /**
   * Set the {@link Plugin}.
   * 
   * @param plugin
   *          a {@link Plugin}.
   */
  void setPlugin(Plugin<?> plugin);

  /**
   * Get the {@link IndexService}.
   * 
   * @return {@link IndexService}.
   */
  IndexService getIndex();

  /**
   * Set the {@link IndexService}.
   * 
   * @param service
   *          {@link IndexService}.
   */
  void setIndex(IndexService service);

  /**
   * get the {@link ModelService}.
   * 
   * @return {@link ModelService}.
   */
  ModelService getModel();

  /**
   * Set the {@link ModelService}.
   * 
   * @param model
   *          {@link ModelService}.
   */
  void setModel(ModelService model);

  /**
   * Get the {@link StorageService}.
   * 
   * @return {@link StorageService}.
   */
  StorageService getStorage();

  /**
   * Set the {@link StorageService}.
   * 
   * @param storage
   *          {@link StorageService}.
   */
  void setStorage(StorageService storage);

  /**
   * Get the {@link PluginParameter}.
   * 
   * @return {@link PluginParameter}.
   */
  PluginParameter getPluginParameter();

  /**
   * Set the {@link PluginParameter}.
   * 
   * @param pluginParameter
   *          {@link PluginParameter}.
   */
  void setPluginParameter(PluginParameter pluginParameter);

  /**
   * Get the parameter values.
   * 
   * @return {@link Map}.
   */
  Map<String, String> getParameterValues();

  /**
   * Set the parameter values.
   * 
   * @param parameterValues
   *          {@link Map}.
   */
  void setParameterValues(Map<String, String> parameterValues);

  /**
   * Set the {@link Job}.
   * 
   * @return {@link Job}.
   */
  Job getCachedJob();

  /**
   * Set the {@link Job}.
   * 
   * @param cachedJob
   *          {@link Job}.
   */
  void setCachedJob(Job cachedJob);
}
