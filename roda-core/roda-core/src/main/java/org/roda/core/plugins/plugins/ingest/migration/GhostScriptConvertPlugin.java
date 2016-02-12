package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ghost4j.GhostscriptException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.CommandException;
import org.slf4j.LoggerFactory;

public class GhostScriptConvertPlugin extends CommandConvertPlugin {

  @Override
  public String getName() {
    return "GhostScript conversion";
  }

  @Override
  public String getDescription() {
    return "Converts files using GhostScript.";
  }

  @Override
  public String getVersion() {
    try {
      return GhostScriptConvertPluginUtils.getVersion();
    } catch (CommandException | IOException | UnsupportedOperationException e) {
      LoggerFactory.getLogger(SoxConvertPlugin.class).debug("Error getting GhostScript version");
      return new String();
    }
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new GhostScriptConvertPlugin();
  }

  @Override
  public String executePlugin(Path inputPath, Path outputPath, String fileFormat) throws UnsupportedOperationException,
    IOException, CommandException {

    try {
      return GhostScriptConvertPluginUtils.executeGS(inputPath, outputPath, commandArguments);
    } catch (GhostscriptException e) {
      return null;
    }
  }

  @Override
  public List<String> getApplicableTo() {
    return GhostScriptConvertPluginUtils.getInputExtensions();
  }

  @Override
  public List<String> getConvertableTo() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "ghostscriptconvert", "outputFormats");
    return Arrays.asList(outputFormats.split("\\s+"));
  }

  @Override
  public Map<String, List<String>> getPronomToExtension() {
    return GhostScriptConvertPluginUtils.getPronomToExtension();
  }

  @Override
  public Map<String, List<String>> getMimetypeToExtension() {
    return GhostScriptConvertPluginUtils.getMimetypeToExtension();
  }

}
