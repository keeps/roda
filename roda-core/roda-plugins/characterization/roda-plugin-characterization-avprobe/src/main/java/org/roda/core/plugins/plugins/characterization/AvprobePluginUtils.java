/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class AvprobePluginUtils {

  public static String AVPROBE_METADATA_FORMAT = "json";

  public static String inspect(Path path) throws RODAException {
    try {
      List<String> command = getCommand();
      command.add(path.toString());
      return CommandUtility.execute(command);
    } catch (CommandException e) {
      throw new RODAException("Error while executing Avprobe command");
    }
  }

  private static List<String> getCommand() {
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    Path avprobeHome = rodaHome
      .resolve(RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "avprobe", "path"));

    File AVPROBE_DIRECTORY = avprobeHome.toFile();
    List<String> command = new ArrayList<String>(
      Arrays.asList(AVPROBE_DIRECTORY.getAbsolutePath() + File.separator + "avprobe", "-show_format", "-show_streams",
        "-show_packets", "-of", AvprobePluginUtils.AVPROBE_METADATA_FORMAT, "-v", "quiet"));

    return command;
  }

  public static String runAvprobe(StorageService storage, Binary binary, String fileFormat,
    Map<String, String> parameterValues) throws IOException, RODAException {
    DirectResourceAccess directAccess = storage.getDirectAccess(binary.getStoragePath());
    InputStream inputStream = Files.newInputStream(directAccess.getPath());

    Path newPath = Files.createTempFile("temp", ".temp");
    OutputStream fos = Files.newOutputStream(newPath);
    IOUtils.copy(inputStream, fos);

    IOUtils.closeQuietly(inputStream);
    fos.close();

    String inspectString = inspect(newPath);
    FSUtils.deletePathQuietly(newPath);
    return inspectString;
  }
}
