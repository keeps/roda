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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.roda.core.RodaCoreFactory;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class ExifToolPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExifToolPluginUtils.class);

  public static Path inspect(File f) throws PluginException {
    try {
      List<String> command = getCommand();
      command.add(f.getAbsolutePath());
      String exifToolOutput = CommandUtility.execute(command);
      Path p = Files.createTempFile("exiftool", ".xml");
      Files.write(p, exifToolOutput.getBytes());
      return p;
    } catch (CommandException e) {
      throw new PluginException("Error while executing ExifTool");
    } catch (IOException e) {
      throw new PluginException("Error while executing ExifTool");
    }
  }

  private static List<String> getCommand() {
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    Path exifToolHome = rodaHome
      .resolve(RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "exiftool", "home"));

    File EXIFTOOL_DIRECTORY = exifToolHome.toFile();

    String osName = System.getProperty("os.name");
    List<String> command;
    if (osName.startsWith("Windows")) {
      command = new ArrayList<String>(
        Arrays.asList(EXIFTOOL_DIRECTORY.getAbsolutePath() + File.separator + "exiftool.exe", "-X"));
    } else {
      command = new ArrayList<String>(
        Arrays.asList(EXIFTOOL_DIRECTORY.getAbsolutePath() + File.separator + "exiftool", "-X"));
    }
    return command;
  }

  private static List<String> getBatchCommand(Path sourceDirectory, Path exifToolOutputDirectory) {
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    Path exifToolHome = rodaHome
      .resolve(RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "exiftool", "home"));

    File EXIFTOOL_DIRECTORY = exifToolHome.toFile();

    String osName = System.getProperty("os.name");
    List<String> command;
    if (osName.startsWith("Windows")) {
      command = new ArrayList<String>(Arrays.asList(
        EXIFTOOL_DIRECTORY.getAbsolutePath() + File.separator + "exiftool.exe", "-X", "-w",
        exifToolOutputDirectory.toFile().getAbsolutePath() + "/%f.%e.xml", sourceDirectory.toFile().getAbsolutePath()));
    } else {
      command = new ArrayList<String>(Arrays.asList(EXIFTOOL_DIRECTORY.getAbsolutePath() + File.separator + "exiftool",
        "-X", "-w", exifToolOutputDirectory.toFile().getAbsolutePath() + "/%f.%e.xml",
        sourceDirectory.toFile().getAbsolutePath()));
    }
    return command;
  }

  public static Path runExifTool(org.roda.core.data.v2.ip.File file, Binary binary, Map<String, String> parameterValues)
    throws IOException, PluginException {
    java.io.File f = File.createTempFile("temp", ".temp");
    FileOutputStream fos = new FileOutputStream(f);
    InputStream inputStream = binary.getContent().createInputStream();
    IOUtils.copy(inputStream, fos);
    IOUtils.closeQuietly(inputStream);
    fos.close();
    return inspect(f);
  }

  public static String runExifToolOnPath(Path sourceDirectory, Path exifToolOutputDirectory) throws CommandException {
    List<String> command = getBatchCommand(sourceDirectory, exifToolOutputDirectory);
    String exifToolOutput = CommandUtility.execute(command);
    return exifToolOutput;
  }

}
