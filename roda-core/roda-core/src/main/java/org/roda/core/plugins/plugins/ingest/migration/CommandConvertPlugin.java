package org.roda.core.plugins.plugins.ingest.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;

public abstract class CommandConvertPlugin extends AbstractConvertPlugin {

  public String commandArguments;

  public CommandConvertPlugin() {
    super();
    commandArguments = "";
  }

  @Override
  public List<PluginParameter> getParameters() {
    List<PluginParameter> params = new ArrayList<PluginParameter>();

    PluginParameter commandArgs = new PluginParameter("commandArgs", "Command arguments", PluginParameterType.STRING,
      "", true, true, "Command arguments to modify the command to execute");

    params.add(commandArgs);
    params.addAll(super.getParameters());
    return params;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    // add command arguments
    if (parameters.containsKey("commandArguments")) {
      commandArguments = parameters.get("commandArguments").trim();
    }
  }

}
