package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.plugins.Plugin;
import org.roda.core.util.CommandException;

public class GeneralCommandConvertPlugin extends CommandConvertPlugin {

  @Override
  public String getName() {
    return "General conversion";
  }

  @Override
  public String getDescription() {
    return "Generates a new format file using a generic command line.";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new GeneralCommandConvertPlugin();
  }

  @Override
  public String executePlugin(Path inputPath, Path outputPath, String fileFormat) {
    try {
      return GeneralCommandConvertPluginUtils.executeGeneralCommand(inputPath, outputPath, super.getCommandArguments());
    } catch (IOException | CommandException e) {
      return null;
    }
  }

  @Override
  public List<String> getApplicableTo() {
    return new ArrayList<String>();
  }

  @Override
  public List<String> getConvertableTo() {
    return new ArrayList<String>();
  }

  @Override
  public Map<String, List<String>> getPronomToExtension() {
    return new HashMap<String, List<String>>();
  }

  @Override
  public Map<String, List<String>> getMimetypeToExtension() {
    return new HashMap<String, List<String>>();
  }

}
