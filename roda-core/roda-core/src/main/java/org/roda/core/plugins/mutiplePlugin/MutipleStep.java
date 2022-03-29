package org.roda.core.plugins.mutiplePlugin;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.exceptions.JobException;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class MutipleStep {

  private String pluginName;
  private String parameterName;
  private boolean usesCorePlugin;
  private boolean mandatory;
  private Map<String, String> parameters;

  public MutipleStep(final String pluginName, final String parameterName, final boolean usesCorePlugin,
    final boolean mandatory, final boolean needsAips, final boolean removesAIPs) {
    this(pluginName, parameterName, usesCorePlugin, mandatory, new HashMap<>());
  }

  public MutipleStep(final String pluginName, final String parameterName, final boolean usesCorePlugin,
    final boolean mandatory, final Map<String, String> parameters) {
    this.pluginName = pluginName;
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

  public void execute(final MutipleStepBlundle bundle, final Class classOfPlugin) throws JobException {
    MutipleStepUtils.executePlugin(bundle, this, classOfPlugin);
  }
}
