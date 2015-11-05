/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.action.ingest.characterization.MediaInfo.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.roda.common.RodaCoreFactory;
import org.roda.util.CommandException;
import org.roda.util.CommandUtility;

public class MediaInfoUtils {
  static final private Logger logger = Logger.getLogger(MediaInfoUtils.class);

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
