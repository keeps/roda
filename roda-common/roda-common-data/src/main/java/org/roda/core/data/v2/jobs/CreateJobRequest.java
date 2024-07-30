package org.roda.core.data.v2.jobs;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import org.roda.core.data.v2.generics.select.SelectedItemsRequest;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class CreateJobRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = 5136924368954184386L;

  private String name;
  private String plugin;
  private Map<String, String> pluginParameters;
  private SelectedItemsRequest sourceObjects;
  private String sourceObjectsClass;
  private String priority;
  private String parallelism;

  public CreateJobRequest() {
    // do nothing
  }

  public String getParallelism() {
    return parallelism;
  }

  public void setParallelism(String parallelism) {
    this.parallelism = parallelism;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public SelectedItemsRequest getSourceObjects() {
    return sourceObjects;
  }

  public void setSourceObjects(SelectedItemsRequest sourceObjects) {
    this.sourceObjects = sourceObjects;
  }

  public String getSourceObjectsClass() {
    return sourceObjectsClass;
  }

  public void setSourceObjectsClass(String sourceObjectsClass) {
    this.sourceObjectsClass = sourceObjectsClass;
  }
}
