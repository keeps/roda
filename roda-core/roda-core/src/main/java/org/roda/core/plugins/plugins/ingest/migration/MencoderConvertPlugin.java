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

public class MencoderConvertPlugin extends CommandConvertPlugin {

  @Override
  public String getName() {
    return "Mencoder video conversion";
  }

  @Override
  public String getDescription() {
    return "Generates a video format file from other video format one using Mencoder.";
  }

  @Override
  public String getVersion() {
    try {
      return MencoderConvertPluginUtils.getVersion();
    } catch (CommandException | IOException | UnsupportedOperationException e) {
      logger.debug("Error getting Mencoder version");
      return new String();
    }
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new MencoderConvertPlugin();
  }

  @Override
  public List<PluginParameter> getParameters() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "mencoderconvert", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));

    return super.getParameters();
  }

  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    fillFileFormatStructures();
  }

  @Override
  public Path executePlugin(Path uriPath, String fileFormat) throws UnsupportedOperationException, IOException,
    CommandException {

    return MencoderConvertPluginUtils.runMencoderConvert(uriPath, fileFormat, outputFormat, commandArguments);
  }

  @Override
  public void fillFileFormatStructures() {
    pronomToExtension = MencoderConvertPluginUtils.getPronomToExtension();
    mimetypeToExtension = MencoderConvertPluginUtils.getMimetypeToExtension();
    applicableTo = MencoderConvertPluginUtils.getInputExtensions();

    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "mencoderconvert", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));
  }

}
