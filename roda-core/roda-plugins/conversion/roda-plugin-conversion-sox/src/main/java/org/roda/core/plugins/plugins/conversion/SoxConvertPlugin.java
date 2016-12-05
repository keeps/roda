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
import org.roda.core.data.v2.jobs.PluginParameter;
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

public class SoxConvertPlugin<T extends IsRODAObject> extends CommandConvertPlugin<T> {
  private static final String TOOLNAME = "soxconvert";

  public SoxConvertPlugin() {
    super();
  }

  @Override
  public String getName() {
    return "Audio conversion (SoX)";
  }

  @Override
  public String getDescription() {
    return "SoX is a cross-platform utility that can convert various formats of computer audio files in to other formats. It can also apply "
      + "various effects to these sound files (see the “Command arguments” setting).\nThe results of conversion will be placed on a new "
      + "representation under the same Archival Information Package (AIP) where the files were originally found. A PREMIS event is also recorded "
      + "after the task is run.\nFor a full list of supported audio formats and effects, please visit http://sox.sourceforge.net/Docs/Features";
  }

  @Override
  public String getVersionImpl() {
    try {
      return SoxConvertPluginUtils.getVersion();
    } catch (CommandException | IOException | UnsupportedOperationException e) {
      LoggerFactory.getLogger(SoxConvertPlugin.class).debug("Error getting Sox version");
      return "1.0";
    }
  }

  @Override
  public List<PluginParameter> getParameters() {
    Map<String, PluginParameter> parameters = super.getDefaultParameters();
    parameters.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT).setDefaultValue("mp3");
    parameters.get(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_TITLE).setDefaultValue("MP3 audio");
    parameters.get(RodaConstants.PLUGIN_PARAMS_DISSEMINATION_DESCRIPTION)
      .setDefaultValue("MP3 audio format for Web reproduction.");
    return super.orderParameters(parameters);
  }

  @Override
  public Plugin<T> cloneMe() {
    return new SoxConvertPlugin<T>();
  }

  @Override
  public String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws UnsupportedOperationException, IOException, CommandException {
    return SoxConvertPluginUtils.executeSox(inputPath, outputPath, super.getCommandArguments());
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

}
