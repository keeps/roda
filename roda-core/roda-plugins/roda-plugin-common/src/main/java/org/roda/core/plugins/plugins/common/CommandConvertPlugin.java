/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;

public abstract class CommandConvertPlugin<T extends IsRODAObject> extends AbstractConvertPlugin<T> {

  private String commandArguments;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_COMMAND_ARGUMENTS,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_COMMAND_ARGUMENTS, "Command arguments",
        PluginParameterType.STRING, "", true, true,
        "Command arguments that will be passed to the command of the tool as configured (advanced users only!)"));
  }

  protected CommandConvertPlugin() {
    super();
    commandArguments = "";
  }

  protected Map<String, PluginParameter> getDefaultParameters() {
    Map<String, PluginParameter> defaultParameters = super.getDefaultParameters();
    defaultParameters.putAll(pluginParameters.entrySet().stream()
      .collect(Collectors.toMap(e -> e.getKey(), e -> new PluginParameter(e.getValue()))));
    return defaultParameters;
  }

  protected List<PluginParameter> orderParameters(Map<String, PluginParameter> params) {
    List<PluginParameter> orderedList = super.orderParameters(params);
    orderedList.add(params.get(RodaConstants.PLUGIN_PARAMS_COMMAND_ARGUMENTS));
    return orderedList;
  }

  public String getCommandArguments() {
    return commandArguments;
  }

  public void setCommandArguments(String args) {
    commandArguments = args;
  }

  @Override
  public List<PluginParameter> getParameters() {
    Map<String, PluginParameter> parameters = getDefaultParameters();
    return orderParameters(parameters);
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    // add command arguments
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_COMMAND_ARGUMENTS)) {
      setCommandArguments(parameters.get(RodaConstants.PLUGIN_PARAMS_COMMAND_ARGUMENTS).trim());
    }
  }

}
