package org.roda.core.data.v2.jobs;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class PluginInfoRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = 6949874214306617597L;

  private String plugin;
  private Map<String, String> pluginParameters;

  public PluginInfoRequest() {
    // empty constructor
  }

  public PluginInfoRequest(String plugin, Map<String, String> pluginParameters) {
    this.plugin = plugin;
    this.pluginParameters = pluginParameters;
  }

  public PluginInfoRequest(Job job) {
    this.plugin = job.getPlugin();
    this.pluginParameters = job.getPluginParameters();
  }

  public PluginInfoRequest(IndexedJob indexedJob) {
    this.plugin = indexedJob.getPlugin();
    this.pluginParameters = indexedJob.getPluginParameters();
  }

  public String getPlugin() {
    return plugin;
  }

  public void setPlugin(String plugin) {
    this.plugin = plugin;
  }

  public Map<String, String> getPluginParameters() {
    return pluginParameters;
  }

  public void setPluginParameters(Map<String, String> pluginParameters) {
    this.pluginParameters = pluginParameters;
  }
}
