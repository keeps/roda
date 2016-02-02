package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;

public class SoxConvertPlugin extends CommandConvertPlugin {

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Sound conversion";
  }

  @Override
  public String getDescription() {
    return "Generates a sound format file from other sound format one using SOX.";
  }

  @Override
  public String getVersion() {
    try {
      return SoxConvertPluginUtils.getVersion();
    } catch (CommandException e) {
      logger.debug("Error getting Sox version");
      return new String();
    }
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new SoxConvertPlugin();
  }

  @Override
  public List<PluginParameter> getParameters() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "soxconvert", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));

    return super.getParameters();
  }

  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    String inputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "soxconvert", "inputFormats");
    applicableTo.addAll(Arrays.asList(inputFormats.split("\\s+")));

    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "soxconvert", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));
  }

  @Override
  public Path executePlugin(Binary binary, String fileFormat) throws UnsupportedOperationException, IOException,
    CommandException {
    Path uriPath = Paths.get(binary.getContent().getURI());
    Path pluginResult;

    if (Files.exists(uriPath)) {
      pluginResult = SoxConvertPluginUtils.runSoxSoundConvert(uriPath, fileFormat, outputFormat, commandArguments);
    } else {
      pluginResult = SoxConvertPluginUtils.runSoxSoundConvert(binary.getContent().createInputStream(), fileFormat,
        outputFormat, commandArguments);
    }

    return pluginResult;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

}
