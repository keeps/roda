/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class FITSPluginUtils {

  private FITSPluginUtils() {
    // do nothing
  }

  private static List<String> getBatchCommand(Path sourceDirectory, Path outputDirectory) {
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    Path fitsHome = rodaHome.resolve(RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "fits", "home"));

    File fitsDirectory = fitsHome.toFile();

    String osName = System.getProperty("os.name");
    List<String> command;
    if (osName.startsWith("Windows")) {
      command = new ArrayList<>(Arrays.asList(fitsDirectory.getAbsolutePath() + File.separator + "fits.bat", "-o",
        outputDirectory.toFile().getAbsolutePath(), "-i"));
    } else {
      command = new ArrayList<>(Arrays.asList(fitsDirectory.getAbsolutePath() + File.separator + "fits.sh", "-o",
        outputDirectory.toFile().getAbsolutePath(), "-i"));
    }
    command.add(sourceDirectory.toFile().getAbsolutePath());
    return command;
  }

  public static String runFITSOnPath(Path sourceDirectory, Path outputDirectory) throws RODAException {
    try {
      List<String> command = getBatchCommand(sourceDirectory, outputDirectory);
      return CommandUtility.execute(command);
    } catch (CommandException e) {
      throw new RODAException("Error while executing FITS command");
    }
  }
}
