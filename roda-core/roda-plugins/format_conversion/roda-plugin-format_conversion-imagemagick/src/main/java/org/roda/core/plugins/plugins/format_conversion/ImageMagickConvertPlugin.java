/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.format_conversion;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;
import org.slf4j.LoggerFactory;

public class ImageMagickConvertPlugin<T extends Serializable> extends CommandConvertPlugin<T> {

  private static final String TOOLNAME = "imagemagickconvert";

  @Override
  public String getName() {
    return "Image conversion";
  }

  @Override
  public String getDescription() {
    return "Generates an image format file from other image format one using Imagemagick.";
  }

  @Override
  public String getVersionImpl() {
    try {
      return ImageMagickConvertPluginUtils.getVersion();
    } catch (CommandException | IOException | UnsupportedOperationException e) {
      LoggerFactory.getLogger(ImageMagickConvertPlugin.class).debug("Error getting ImageMagick version");
      return "1.0";
    }
  }

  @Override
  public Plugin<T> cloneMe() {
    return new ImageMagickConvertPlugin<T>();
  }

  public String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws UnsupportedOperationException, IOException, CommandException {

    return ImageMagickConvertPluginUtils.executeImageMagick(inputPath, outputPath, super.getOutputFormat(),
      super.getCommandArguments());
  }

  @Override
  public List<String> getApplicableTo() {
    // TODO add missing extensions
    return FileFormatUtils.getInputExtensions(TOOLNAME);
  }

  @Override
  public List<String> getConvertableTo() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", TOOLNAME, "outputFormats");
    return Arrays.asList(outputFormats.split("\\s+"));
  }

  @Override
  public Map<String, List<String>> getPronomToExtension() {
    // TODO add missing pronoms
    return FileFormatUtils.getPronomToExtension(TOOLNAME);
  }

  @Override
  public Map<String, List<String>> getMimetypeToExtension() {
    // TODO add missing mimetypes
    return FileFormatUtils.getMimetypeToExtension(TOOLNAME);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_FORMAT_CONVERSION);
  }

}