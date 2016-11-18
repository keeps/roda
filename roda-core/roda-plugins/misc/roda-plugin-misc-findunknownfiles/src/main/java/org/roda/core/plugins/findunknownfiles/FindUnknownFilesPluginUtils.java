/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.findunknownfiles;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindUnknownFilesPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(FindUnknownFilesPluginUtils.class);

  private static List<String> getBatchCommand(Path sourceDirectory) {
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    Path droidHome = rodaHome.resolve(RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "droid", "home"));
    Path signature = rodaHome
      .resolve(RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "droid", "signatureFile"));
    Path containerSignature = rodaHome
      .resolve(RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "droid", "containerSignatureFile"));

    File DROID_DIRECTORY = droidHome.toFile();

    String osName = System.getProperty("os.name");
    List<String> command;
    if (osName.startsWith("Windows")) {
      command = new ArrayList<String>(Arrays.asList(DROID_DIRECTORY.getAbsolutePath() + File.separator + "droid.bat",
        "-Ns", signature.toFile().getAbsolutePath(), "-Nc", containerSignature.toFile().getAbsolutePath(), "-q", "-Nr",
        sourceDirectory.toFile().getAbsolutePath()));
    } else {
      command = new ArrayList<String>(Arrays.asList(DROID_DIRECTORY.getAbsolutePath() + File.separator + "droid.sh",
        "-Ns", signature.toFile().getAbsolutePath(), "-Nc", containerSignature.toFile().getAbsolutePath(), "-q", "-Nr",
        sourceDirectory.toFile().getAbsolutePath()));
    }
    return command;
  }

  public static String runDROIDOnPath(Path sourceDirectory) throws RODAException {
    try {
      List<String> command = getBatchCommand(sourceDirectory);
      String droidOutput = CommandUtility.execute(command);
      return droidOutput;
    } catch (CommandException e) {
      throw new RODAException("Error while executing DROID command", e.getCause());
    }
  }
}
