/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.multiple;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.roda.core.data.exceptions.JobException;
import org.roda.core.plugins.PluginHelper;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class Step {

  private String pluginName;
  private String parameterName;
  private boolean usesCorePlugin;
  private boolean mandatory;
  private Optional<Integer> sourceObjectsCount;
  private Map<String, String> parameters;

  public Step(final String pluginName, final String parameterName, final boolean usesCorePlugin,
    final boolean mandatory) {
    this(pluginName, parameterName, usesCorePlugin, mandatory, new HashMap<>());
  }

  public Step(final String pluginName, final String parameterName, final boolean usesCorePlugin,
    final boolean mandatory, final Map<String, String> parameters) {
    this.pluginName = pluginName;
    this.parameterName = parameterName;
    this.usesCorePlugin = usesCorePlugin;
    this.mandatory = mandatory;
    this.parameters = parameters;
    this.sourceObjectsCount = Optional.empty();
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

  public Optional<Integer> getSourceObjectsCount() {
    return sourceObjectsCount;
  }

  public void setSourceObjectsCount(Optional<Integer> sourceObjectsCount) {
    this.sourceObjectsCount = sourceObjectsCount;
  }

  public void execute(MultipleStepBundle bundle) throws JobException {
    if (PluginHelper.verifyIfStepShouldBePerformed(bundle.getPlugin(), bundle.getPluginParameter(),
      !this.usesCorePlugin ? this.getPluginName() : null)) {
      MutipleStepUtils.executePlugin(bundle, this);
      PluginHelper.updateJobInformationAsync(bundle.getPlugin(),
        bundle.getJobPluginInfo().incrementStepsCompletedByOne());
    }
  }

}
