/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.rsync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RsyncUtils {

  private RsyncUtils() {
  }

  public static void executeRsync(Path sourcePath, Path targetPath) throws GenericException, CommandException {
    executeRsync(sourcePath, targetPath, Collections.singletonList("-r"));
  }

  public static void executeRsync(Path sourcePath, Path targetPath, List<String> rsyncOptions)
    throws GenericException, CommandException {
    List<String> rsyncCommand = new ArrayList<>();
    rsyncCommand.add("rsync");
    rsyncCommand.addAll(rsyncOptions);

    // ensure target directory exists or can be created
    try {
      if (!FSUtils.exists(targetPath)) {
        Files.createDirectories(targetPath);
      }
    } catch (IOException e) {
      throw new GenericException("Error while creating target directory parent folder", e);
    }

    if (FSUtils.exists(sourcePath) && FSUtils.exists(targetPath)) {
      rsyncCommand.add(sourcePath + "/");
      rsyncCommand.add(targetPath + "/");

      String output = CommandUtility.execute(rsyncCommand);
    }
  }

}
