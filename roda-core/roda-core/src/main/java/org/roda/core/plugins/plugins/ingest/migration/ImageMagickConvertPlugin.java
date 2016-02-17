package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.CommandException;
import org.slf4j.LoggerFactory;

public class ImageMagickConvertPlugin extends CommandConvertPlugin {

  @Override
  public String getName() {
    return "Image conversion";
  }

  @Override
  public String getDescription() {
    return "Generates an image format file from other image format one using Imagemagick.";
  }

  @Override
  public String getAgentType() {
    return RodaConstants.PRESERVATION_AGENT_TYPE_SOFTWARE;
  }

  @Override
  public String getVersion() {
    try {
      return ImageMagickConvertPluginUtils.getVersion();
    } catch (CommandException | IOException | UnsupportedOperationException e) {
      LoggerFactory.getLogger(SoxConvertPlugin.class).debug("Error getting ImageMagick version");
      return new String();
    }
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new ImageMagickConvertPlugin();
  }

  public String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws UnsupportedOperationException, IOException, CommandException {

    return ImageMagickConvertPluginUtils.executeImageMagick(inputPath, outputPath, super.getOutputFormat(),
      super.getCommandArguments());
  }

  @Override
  public List<String> getApplicableTo() {
    return ImageMagickConvertPluginUtils.getInputExtensions();
  }

  @Override
  public List<String> getConvertableTo() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "imagemagickconvert", "outputFormats");
    return Arrays.asList(outputFormats.split("\\s+"));
  }

  @Override
  public Map<String, List<String>> getPronomToExtension() {
    return ImageMagickConvertPluginUtils.getPronomToExtension();
  }

  @Override
  public Map<String, List<String>> getMimetypeToExtension() {
    return ImageMagickConvertPluginUtils.getMimetypeToExtension();
  }

}