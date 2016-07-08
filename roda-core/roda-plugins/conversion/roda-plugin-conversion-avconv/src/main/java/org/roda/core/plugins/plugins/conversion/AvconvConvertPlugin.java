/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
R * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.conversion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
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

public class AvconvConvertPlugin<T extends IsRODAObject> extends CommandConvertPlugin<T> {

  private String outputArguments;
  private static final String TOOLNAME = "avconvconvert";

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_ARGUMENTS,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_OUTPUT_ARGUMENTS, "Output command arguments",
        PluginParameterType.STRING, "", true, true, "Output command arguments to modify the output type"));
  }

  public AvconvConvertPlugin() {
    super();
    outputArguments = "";
  }

  public String getOutputArguments() {
    return outputArguments;
  }

  public void setOutputArguments(String args) {
    outputArguments = args;
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
  public String getVersionImpl() {
    try {
      return AvconvConvertPluginUtils.getVersion();
    } catch (CommandException | IOException | UnsupportedOperationException e) {
      LoggerFactory.getLogger(AvconvConvertPlugin.class).debug("Error getting Avconv version");
      return "1.0";
    }
  }

  @Override
  public Plugin<T> cloneMe() {
    return new AvconvConvertPlugin<T>();
  }

  @Override
  public List<PluginParameter> getParameters() {
    List<PluginParameter> params = new ArrayList<PluginParameter>();
    params.addAll(super.getParameters());
    params.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_ARGUMENTS));
    return params;
  }

  @Override
  public Map<String, String> getParameterValues() {
    Map<String, String> params = super.getParameterValues();
    params.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_ARGUMENTS, outputArguments);
    return params;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    // avconv output command arguments
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_OUTPUT_ARGUMENTS)) {
      setOutputArguments(parameters.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_ARGUMENTS));
    }
  }

  @Override
  public String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws UnsupportedOperationException, IOException, CommandException {

    return AvconvConvertPluginUtils.executeAvconv(inputPath, outputPath, super.getCommandArguments(),
      getOutputArguments());
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
}