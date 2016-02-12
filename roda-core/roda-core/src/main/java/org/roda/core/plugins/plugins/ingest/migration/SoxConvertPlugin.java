package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.CommandException;

public class SoxConvertPlugin extends CommandConvertPlugin {

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
    } catch (CommandException | IOException | UnsupportedOperationException e) {
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
    fillFileFormatStructures();
  }

  @Override
  public String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws UnsupportedOperationException, IOException, CommandException {

    String output = SoxConvertPluginUtils.executeSox(inputPath, outputPath, commandArguments);

    return output;
  }

  @Override
  public void fillFileFormatStructures() {
    pronomToExtension = SoxConvertPluginUtils.getPronomToExtension();
    mimetypeToExtension = SoxConvertPluginUtils.getMimetypeToExtension();
    applicableTo = SoxConvertPluginUtils.getInputExtensions();

    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "soxconvert", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));
  }

}
