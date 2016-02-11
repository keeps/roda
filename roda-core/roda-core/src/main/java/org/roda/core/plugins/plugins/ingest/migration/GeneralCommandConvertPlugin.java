package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

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
  public Path executePlugin(Path uriPath, String fileFormat) {
    try {
      return GeneralCommandConvertPluginUtils.runGeneralCommandConvert(uriPath, fileFormat, outputFormat,
        commandArguments);
    } catch (IOException | CommandException e) {
      return null;
    }
  }

  @Override
  public void fillFileFormatStructures() {
    return;
  }

}
