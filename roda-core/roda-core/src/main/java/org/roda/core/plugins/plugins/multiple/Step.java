package org.roda.core.plugins.plugins.multiple;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.exceptions.JobException;
import org.roda.core.plugins.plugins.PluginHelper;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class Step {

  private String pluginName;
  private Class<?> pluginClass;
  private String parameterName;
  private boolean usesCorePlugin;
  private boolean mandatory;
  private Map<String, String> parameters;

  public Step(final String pluginName, final Class<?> pluginClass, final String parameterName,
    final boolean usesCorePlugin, final boolean mandatory) {
    this(pluginName, pluginClass, parameterName, usesCorePlugin, mandatory, new HashMap<>());
  }

  public Step(final String pluginName, final Class<?> pluginClass, final String parameterName,
    final boolean usesCorePlugin, final boolean mandatory, final Map<String, String> parameters) {
    this.pluginName = pluginName;
    this.pluginClass = pluginClass;
    this.parameterName = parameterName;
    this.usesCorePlugin = usesCorePlugin;
    this.mandatory = mandatory;
    this.parameters = parameters;
  }

  public String getPluginName() {
    return pluginName;
  }

  public void setPluginName(final String pluginName) {
    this.pluginName = pluginName;
  }

  public Class<?> getPluginClass() {
    return pluginClass;
  }

  public void setPluginClass(Class<?> pluginClass) {
    this.pluginClass = pluginClass;
  }

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName(final String parameterName) {
    this.parameterName = parameterName;
  }

  public boolean isUsesCorePlugin() {
    return usesCorePlugin;
  }

  public void setUsesCorePlugin(final boolean usesCorePlugin) {
    this.usesCorePlugin = usesCorePlugin;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public void setMandatory(final boolean mandatory) {
    this.mandatory = mandatory;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  public void execute(MultipleStepBundle bundle) throws JobException {
    MutipleStepUtils.executePlugin(bundle, this);
    PluginHelper.updateJobInformationAsync(bundle.getPlugin(),
      bundle.getJobPluginInfo().incrementStepsCompletedByOne());
  }
}
