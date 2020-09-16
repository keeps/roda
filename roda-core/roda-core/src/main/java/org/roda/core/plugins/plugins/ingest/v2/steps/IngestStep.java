/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.v2.steps;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.exceptions.JobException;
import org.roda.core.plugins.plugins.PluginHelper;

public class IngestStep {
  private String pluginName;
  private String parameterName;
  private boolean usesCorePlugin;
  private boolean mandatory;
  private boolean needsAips;
  private boolean removesAips;
  private Map<String, String> parameters;

  public IngestStep(String pluginName, String parameterName, boolean usesCorePlugin, boolean mandatory,
    boolean needsAips, boolean removesAIPs) {
    this(pluginName, parameterName, usesCorePlugin, mandatory, needsAips, removesAIPs, new HashMap<>());
  }

  public IngestStep(String pluginName, String parameterName, boolean usesCorePlugin, boolean mandatory,
    boolean needsAips, boolean removesAIPs, Map<String, String> parameters) {
    this.pluginName = pluginName;
    this.parameterName = parameterName;
    this.usesCorePlugin = usesCorePlugin;
    this.mandatory = mandatory;
    this.needsAips = needsAips;
    this.removesAips = removesAIPs;
    this.parameters = parameters;
  }

  public String getPluginName() {
    return pluginName;
  }

  public String getParameterName() {
    return parameterName;
  }

  public boolean usesCorePlugin() {
    return usesCorePlugin;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public boolean needsAips() {
    return needsAips;
  }

  public boolean removesAips() {
    return removesAips;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  public void execute(IngestStepBundle bundle) throws JobException {
    if (PluginHelper.verifyIfStepShouldBePerformed(bundle.getIngestPlugin(), bundle.getPluginParameter(),
      !this.usesCorePlugin() ? this.getPluginName() : null)) {
      IngestStepsUtils.executePlugin(bundle, this);
      PluginHelper.updateJobInformationAsync(bundle.getIngestPlugin(),
        bundle.getJobPluginInfo().incrementStepsCompletedByOne());
    }
  }
}
