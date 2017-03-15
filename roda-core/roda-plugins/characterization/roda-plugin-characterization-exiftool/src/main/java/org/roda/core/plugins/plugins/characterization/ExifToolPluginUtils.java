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
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class ExifToolPluginUtils {

  private ExifToolPluginUtils() {
    // do nothing
  }

  private static List<String> getBatchCommand(Path sourceDirectory, Path exifToolOutputDirectory) {
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    Path exifToolHome = rodaHome
      .resolve(RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "exiftool", "home"));

    File exiftoolDirectory = exifToolHome.toFile();

    String osName = System.getProperty("os.name");
    List<String> command;
    if (osName.startsWith("Windows")) {
      command = new ArrayList<>(Arrays.asList(exiftoolDirectory.getAbsolutePath() + File.separator + "exiftool.exe",
        "-X", "-w", exifToolOutputDirectory.toFile().getAbsolutePath() + "/%f.%e.xml",
        sourceDirectory.toFile().getAbsolutePath()));
    } else {
      command = new ArrayList<>(Arrays.asList(exiftoolDirectory.getAbsolutePath() + File.separator + "exiftool", "-X",
        "-w", exifToolOutputDirectory.toFile().getAbsolutePath() + "/%f.%e.xml",
        sourceDirectory.toFile().getAbsolutePath()));
    }
    return command;
  }

  public static String runExifToolOnPath(Path sourceDirectory, Path exifToolOutputDirectory) throws CommandException {
    List<String> command = getBatchCommand(sourceDirectory, exifToolOutputDirectory);
    return CommandUtility.execute(command);
  }

}
