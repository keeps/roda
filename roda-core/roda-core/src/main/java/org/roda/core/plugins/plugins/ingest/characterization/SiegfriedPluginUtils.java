/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.plugins.PluginException;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiegfriedPluginUtils {

  static final private Logger logger = LoggerFactory.getLogger(SiegfriedPluginUtils.class);

  private static List<String> getBatchCommand(Path sourceDirectory) {
    List<String> command;
    String siegfriedPath = RodaCoreFactory.getRodaConfigurationAsString("tools", "siegfried", "binary");
    command = new ArrayList<String>(Arrays.asList(siegfriedPath.toString(), "-json=true", "-z=false", "-nr=true",
      sourceDirectory.toFile().getAbsolutePath()));
    return command;
  }

  public static String runSiegfriedOnPath(Path sourceDirectory) throws PluginException {
    try {
      List<String> command = getBatchCommand(sourceDirectory);
      String siegfriedOutput = CommandUtility.execute(command, false);
      return siegfriedOutput;
    } catch (CommandException e) {
      throw new PluginException("Error while executing Siegfried command");
    }
  }

  public static String getVersion() {
    String version = null;
    try {
      List<String> command;
      String siegfriedPath = RodaCoreFactory.getRodaConfigurationAsString("tools", "siegfried", "binary");
      command = new ArrayList<String>(Arrays.asList(siegfriedPath.toString(), "--version"));
      String siegfriedOutput = CommandUtility.execute(command);
      if (siegfriedOutput.contains("\n")) {
        return siegfriedOutput.split("\\n")[0].split(" ")[1];
      }
    } catch (CommandException ce) {
      logger.error("Error getting siegfried version: " + ce.getMessage(), ce);
    }
    return version;
  }

}
