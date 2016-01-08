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
    command = new ArrayList<String>(Arrays.asList(siegfriedPath.toString(), "-json=true", "-z=true", "-nr=true",
      sourceDirectory.toFile().getAbsolutePath()));
    return command;
  }

  public static String runSiegfriedOnPath(Path sourceDirectory) throws PluginException {
    try {
      List<String> command = getBatchCommand(sourceDirectory);
      String siegfriedOutput = CommandUtility.execute(command);
      return siegfriedOutput;
    } catch (CommandException e) {
      throw new PluginException("Error while executing Siegfried command");
    }
  }

}
