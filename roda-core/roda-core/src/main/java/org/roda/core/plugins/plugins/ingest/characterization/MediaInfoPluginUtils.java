/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.roda.core.RodaCoreFactory;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class MediaInfoPluginUtils {
  static final private Logger logger = LoggerFactory.getLogger(MediaInfoPluginUtils.class);

  private static List<String> getBatchCommand(Path sourceDirectory) {
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    Path mediaInfoHome = Paths.get(RodaCoreFactory.getRodaConfigurationAsString("tools", "mediainfo", "path"));

    File MEDIAINFO_DIRECTORY = mediaInfoHome.toFile();

    String osName = System.getProperty("os.name");
    List<String> command;
    if (osName.startsWith("Windows")) {
      command = new ArrayList<String>(
        Arrays.asList(MEDIAINFO_DIRECTORY.getAbsolutePath() + File.separator + "mediainfo.exe", "--Output=XML",
          sourceDirectory.toFile().getAbsolutePath()));
    } else {
      command = new ArrayList<String>(
        Arrays.asList(MEDIAINFO_DIRECTORY.getAbsolutePath() + File.separator + "mediainfo", "--Output=XML",
          sourceDirectory.toFile().getAbsolutePath()));
    }
    return command;
  }

  public static String runMediaInfoOnPath(Path sourceDirectory) throws CommandException {
    List<String> command = getBatchCommand(sourceDirectory);
    String mediaInfoOutput = CommandUtility.execute(command);
    return mediaInfoOutput;
  }

}
