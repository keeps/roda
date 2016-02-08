package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.Binary;
import org.roda.core.util.CommandException;

public class GeneralCommandConvertPlugin extends AbstractConvertPlugin {

  public String commandArguments;

  @Override
  public String getName() {
    return "name";
  }

  @Override
  public String getDescription() {
    return "description";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new GeneralCommandConvertPlugin();
  }

  @Override
  public List<PluginParameter> getParameters() {
    List<PluginParameter> params = new ArrayList<PluginParameter>();

    PluginParameter outputParam = new PluginParameter("outputParams", "Output parameters", PluginParameterType.STRING,
      "", true, true, "Output format");

    PluginParameter commandArgs = new PluginParameter("command", "Command arguments", PluginParameterType.STRING, "",
      true, true, "Command to execute");

    params.add(outputParam);
    params.add(commandArgs);
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

  @Override
  public Path executePlugin(Binary binary, String fileFormat) throws UnsupportedOperationException, IOException,
    CommandException {
    Path uriPath = Paths.get(binary.getContent().getURI());
    Path pluginResult;

    if (Files.exists(uriPath)) {
      pluginResult = GeneralCommandConvertPluginUtils.runGeneralCommandConvert(uriPath, fileFormat, outputFormat,
        commandArguments);
    } else {
      pluginResult = GeneralCommandConvertPluginUtils.runGeneralCommandConvert(binary.getContent().createInputStream(),
        fileFormat, outputFormat, commandArguments);
    }

    return pluginResult;
  }

  @Override
  public void fillFileFormatStructures() {
    return;
  }

}
