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
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.Binary;
import org.roda.core.util.CommandException;

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
  public String getVersion() {
    try {
      return ImageMagickConvertPluginUtils.getVersion();
    } catch (CommandException | IOException | UnsupportedOperationException e) {
      logger.debug("Error getting ImageMagick version");
      return new String();
    }
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new ImageMagickConvertPlugin();
  }

  @Override
  public List<PluginParameter> getParameters() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "imagemagickconvert", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));

    return super.getParameters();
  }

  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    fillFileFormatStructures();
  }

  public Path executePlugin(Binary binary, String fileFormat) throws UnsupportedOperationException, IOException,
    CommandException {
    Path uriPath = Paths.get(binary.getContent().getURI());
    Path pluginResult;

    if (Files.exists(uriPath)) {
      pluginResult = ImageMagickConvertPluginUtils.runImageMagickConvert(uriPath, fileFormat, outputFormat,
        commandArguments);
    } else {
      pluginResult = ImageMagickConvertPluginUtils.runImageMagickConvert(binary.getContent().createInputStream(),
        fileFormat, outputFormat, commandArguments);
    }

    return pluginResult;
  }

  @Override
  public void fillFileFormatStructures() {
    pronomToExtension = ImageMagickConvertPluginUtils.getPronomToExtension();
    mimetypeToExtension = ImageMagickConvertPluginUtils.getMimetypeToExtension();
    applicableTo = ImageMagickConvertPluginUtils.getInputExtensions();

    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "imagemagickconvert", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));
  }

}