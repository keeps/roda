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
  public String getAgentType() {
    return RodaConstants.PRESERVATION_AGENT_TYPE_SOFTWARE;
  }

  @Override
  public String getVersion() {
    try {
      return SoxConvertPluginUtils.getVersion();
    } catch (CommandException | IOException | UnsupportedOperationException e) {
      LoggerFactory.getLogger(SoxConvertPlugin.class).debug("Error getting Sox version");
      return "1.0";
    }
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new SoxConvertPlugin();
  }

  @Override
  public String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws UnsupportedOperationException, IOException, CommandException {

    String output = SoxConvertPluginUtils.executeSox(inputPath, outputPath, super.getCommandArguments());
    return output;
  }

  @Override
  public List<String> getApplicableTo() {
    return SoxConvertPluginUtils.getInputExtensions();
  }

  @Override
  public List<String> getConvertableTo() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "soxconvert", "outputFormats");
    return Arrays.asList(outputFormats.split("\\s+"));
  }

  @Override
  public Map<String, List<String>> getPronomToExtension() {
    return SoxConvertPluginUtils.getPronomToExtension();
  }

  @Override
  public Map<String, List<String>> getMimetypeToExtension() {
    return SoxConvertPluginUtils.getMimetypeToExtension();
  }

}
