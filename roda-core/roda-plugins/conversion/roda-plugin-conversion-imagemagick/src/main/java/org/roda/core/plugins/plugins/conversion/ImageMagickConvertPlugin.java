/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.conversion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.common.CommandConvertPlugin;
import org.roda.core.plugins.plugins.common.FileFormatUtils;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;
import org.slf4j.LoggerFactory;

public class ImageMagickConvertPlugin<T extends IsRODAObject> extends CommandConvertPlugin<T> {
  private static final String TOOLNAME = "imagemagickconvert";

  public ImageMagickConvertPlugin() {
    super();
  }

  @Override
  public String getName() {
    return "Image conversion (imagemagick)";
  }

  @Override
  public String getDescription() {
    return "ImageMagick is a tool that can read and write images in a variety of formats (over 200) including PNG, JPEG, JPEG-2000, GIF, TIFF, DPX, EXR, WebP, Postscript, "
      + "PDF, and SVG.\nImageMagick can also be used to resize, flip, mirror, rotate, distort, shear and transform images, adjust image colours, apply various special effects, "
      + "or draw text, lines, polygons, ellipses and Bézier curves (e.g. set Command arguments to “ -resample 90” to resize the image to 90 dpi)."
      + "\nThe results of conversion will be placed on a new representation under the same Archival Information Package (AIP) where the files were originally found."
      + " A PREMIS event is also recorded after the task is run."
      + "\nFor a full list of supported formats, please visit http://www.imagemagick.org/script/formats.php ";
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_CONVERSION);
  }

  @Override
  public String getDIPTitle() {
    return "Imagemagick DIP title";
  }

  @Override
  public String getDIPDescription() {
    return "Imagemagick DIP description";
  }

}
