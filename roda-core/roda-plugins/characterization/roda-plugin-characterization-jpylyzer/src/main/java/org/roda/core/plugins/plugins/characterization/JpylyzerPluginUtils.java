/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.storage.Binary;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class JpylyzerPluginUtils {

  public static String inspect(File f) throws RODAException {
    try {
      List<String> command = getCommand();
      command.add(f.getAbsolutePath());
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

  public static String runJpylyzer(Binary binary, Map<String, String> parameterValues)
    throws IOException, RODAException {
    // TODO f is not deleted in runtime
    // TODO use storage method to get direct access to file
    java.io.File f = File.createTempFile("temp", ".temp");
    FileOutputStream fos = new FileOutputStream(f);
    InputStream inputStream = binary.getContent().createInputStream();
    IOUtils.copy(inputStream, fos);
    IOUtils.closeQuietly(inputStream);
    fos.close();
    return inspect(f);
  }
}
