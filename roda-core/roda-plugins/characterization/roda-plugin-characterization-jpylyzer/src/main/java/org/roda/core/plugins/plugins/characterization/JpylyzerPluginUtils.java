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

public class JpylyzerPluginUtils {

  public static String inspect(Path path) throws RODAException {
    try {
      List<String> command = getCommand();
      command.add(path.toString());
      return CommandUtility.execute(command);
    } catch (CommandException e) {
      throw new RODAException("Error while executing jpylyzer command");
    }
  }

  private static List<String> getCommand() {
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    Path jpylyzerHome = rodaHome
      .resolve(RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "jpylyzer", "path"));

    File jpylyzerDirectory = jpylyzerHome.toFile();

    String osName = System.getProperty("os.name");
    List<String> command;
    if (osName.startsWith("Windows")) {
      command = new ArrayList<String>(
        Arrays.asList(jpylyzerDirectory.getAbsolutePath() + File.separator + "jpylyzer.exe"));
    } else {
      command = new ArrayList<String>(Arrays.asList(jpylyzerDirectory.getAbsolutePath() + File.separator + "jpylyzer"));
    }
    return command;
  }

  public static String runJpylyzer(StorageService storage, Binary binary, Map<String, String> parameterValues)
    throws IOException, RODAException {
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
