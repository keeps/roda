/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.CommandException;
import org.slf4j.LoggerFactory;

public class AvconvConvertPlugin<T extends Serializable> extends CommandConvertPlugin<T> {

  private String outputArguments;
  private static final String TOOLNAME = "avconvconvert";

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
  public Map<String, String> getParameterValues() {
    Map<String, String> params = super.getParameterValues();
    params.put("outputArguments", outputArguments);
    return params;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    // avconv output command arguments
    if (parameters.containsKey("outputArguments")) {
      setOutputArguments(parameters.get("outputArguments"));
    }
  }

  @Override
  public String executePlugin(Path inputPath, Path outputPath, String fileFormat) throws UnsupportedOperationException,
    IOException, CommandException {

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
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", TOOLNAME, "outputFormats");
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
}