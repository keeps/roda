package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.CommandException;

public class AvconvConvertPlugin extends CommandConvertPlugin {

  private String outputArguments;

  public AvconvConvertPlugin() {
    super();
    outputArguments = "";
  }

  @Override
  public String getName() {
    return "Video conversion";
  }

  @Override
  public String getDescription() {
    return "Generates a video format file from other video format one using Avconv.";
  }

  @Override
  public String getVersion() {
    try {
      return AvconvConvertPluginUtils.getVersion();
    } catch (CommandException | IOException | UnsupportedOperationException e) {
      logger.debug("Error getting Avconv version");
      return new String();
    }
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new AvconvConvertPlugin();
  }

  @Override
  public List<PluginParameter> getParameters() {

    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "avconvconvert", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));
    return super.getParameters();
  }

  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    // avconv output command arguments
    if (parameters.containsKey("outputArguments")) {
      outputArguments = parameters.get("outputArguments");
    }

    fillFileFormatStructures();
  }

  @Override
  public Path executePlugin(Path uriPath, String fileFormat) throws UnsupportedOperationException, IOException,
    CommandException {

    return AvconvConvertPluginUtils.runAvconvVideoConvert(uriPath, fileFormat, outputFormat, commandArguments,
      outputArguments);
  }

  @Override
  public void fillFileFormatStructures() {
    pronomToExtension = AvconvConvertPluginUtils.getPronomToExtension();
    mimetypeToExtension = AvconvConvertPluginUtils.getMimetypeToExtension();
    applicableTo = AvconvConvertPluginUtils.getInputExtensions();

    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "avconvconvert", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));
  }
}