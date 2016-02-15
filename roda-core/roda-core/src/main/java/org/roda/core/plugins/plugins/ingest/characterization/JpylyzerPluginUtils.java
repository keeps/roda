/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpylyzerPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(JpylyzerPluginUtils.class);

  public static String inspect(File f) throws PluginException {
    try {
      List<String> command = getCommand();
      command.add(f.getAbsolutePath());
      return CommandUtility.execute(command);
    } catch (CommandException e) {
      throw new PluginException("Error while executing jpylyzer command");
    }
  }

  private static List<String> getCommand() {
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    Path jpylyzerHome = rodaHome.resolve(RodaCoreFactory.getRodaConfigurationAsString("tools", "jpylyzer", "path"));

    File JPYLYZER_DIRECTORY = jpylyzerHome.toFile();

    String osName = System.getProperty("os.name");
    List<String> command;
    if (osName.startsWith("Windows")) {
      command = new ArrayList<String>(
        Arrays.asList(JPYLYZER_DIRECTORY.getAbsolutePath() + File.separator + "jpylyzer.exe"));
    } else {
      command = new ArrayList<String>(
        Arrays.asList(JPYLYZER_DIRECTORY.getAbsolutePath() + File.separator + "jpylyzer"));
    }
    return command;
  }

  public static String runJpylyzer(org.roda.core.data.v2.ip.File file, Binary binary,
    Map<String, String> parameterValues) throws IOException, PluginException {
    java.io.File f = File.createTempFile("temp", ".temp");
    FileOutputStream fos = new FileOutputStream(f);
    InputStream inputStream = binary.getContent().createInputStream();
    IOUtils.copy(inputStream, fos);
    IOUtils.closeQuietly(inputStream);
    fos.close();
    return inspect(f);
  }
}
