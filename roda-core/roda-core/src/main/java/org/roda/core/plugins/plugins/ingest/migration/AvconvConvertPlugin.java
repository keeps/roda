package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.CommandException;
import org.slf4j.LoggerFactory;

public class AvconvConvertPlugin extends CommandConvertPlugin {

  private String outputArguments;

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
  public String getAgentType(){
    return RodaConstants.PRESERVATION_AGENT_TYPE_SOFTWARE;
  }
  
  @Override
  public String getVersion() {
    try {
      return AvconvConvertPluginUtils.getVersion();
    } catch (CommandException | IOException | UnsupportedOperationException e) {
      LoggerFactory.getLogger(SoxConvertPlugin.class).debug("Error getting Avconv version");
      return new String();
    }
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new AvconvConvertPlugin();
  }

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
    return AvconvConvertPluginUtils.getInputExtensions();
  }

  @Override
  public List<String> getConvertableTo() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "avconvconvert", "outputFormats");
    return Arrays.asList(outputFormats.split("\\s+"));
  }

  @Override
  public Map<String, List<String>> getPronomToExtension() {
    return AvconvConvertPluginUtils.getPronomToExtension();
  }

  @Override
  public Map<String, List<String>> getMimetypeToExtension() {
    return AvconvConvertPluginUtils.getMimetypeToExtension();
  }
}