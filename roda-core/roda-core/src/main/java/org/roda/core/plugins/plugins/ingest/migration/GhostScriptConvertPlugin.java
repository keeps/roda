package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ghost4j.GhostscriptException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.CommandException;

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
      logger.debug("Error getting GhostScript version");
      return new String();
    }
  }

  @Override
  public Plugin<Serializable> cloneMe() {
    return new GhostScriptConvertPlugin();
  }

  @Override
  public List<PluginParameter> getParameters() {
    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "ghostscriptconvert", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));

    return super.getParameters();
  }

  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    fillFileFormatStructures();
  }

  @Override
  public Path executePlugin(Path uriPath, String fileFormat) throws UnsupportedOperationException, IOException,
    CommandException {

    try {
      return GhostScriptConvertPluginUtils.runGhostScriptConvert(uriPath, fileFormat, outputFormat, commandArguments);
    } catch (GhostscriptException e) {
      return null;
    }
  }

  @Override
  public void fillFileFormatStructures() {
    pronomToExtension = GhostScriptConvertPluginUtils.getPronomToExtension();
    mimetypeToExtension = GhostScriptConvertPluginUtils.getMimetypeToExtension();
    applicableTo = GhostScriptConvertPluginUtils.getInputExtensions();

    String outputFormats = RodaCoreFactory.getRodaConfigurationAsString("tools", "ghostscriptconvert", "outputFormats");
    convertableTo.addAll(Arrays.asList(outputFormats.split("\\s+")));
  }

}
